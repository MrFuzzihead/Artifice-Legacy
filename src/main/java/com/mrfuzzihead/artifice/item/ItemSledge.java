package com.mrfuzzihead.artifice.item;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.ArtificeTooltips;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSledge extends ItemTool {

    private IIcon icon;
    private int lossChance;

    public ItemSledge(ToolMaterial mat) {
        super(2.0F, mat, null);
        this.setCreativeTab(ArtificeCore.mainTab);
        if (mat == ToolMaterial.EMERALD) {
            this.setMaxDamage(mat.getMaxUses() / 32);
        } else {
            this.setMaxDamage(mat.getMaxUses() / 16);
        }

        this.setUnlocalizedName(
            "artifice.sledge." + this.toolMaterial.toString()
                .toLowerCase(Locale.ENGLISH));
        this.lossChance = this.getLossChance(mat);
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    public int getLossChance(ToolMaterial mat) {
        switch (mat) {
            case EMERALD:
                return 20;
            case GOLD:
                return 25;
            case IRON:
                return 30;
            case STONE:
                return 40;
            case WOOD:
                return 50;
            default:
                return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
        if (ArtificeConfig.tooltips) {
            NameMetaPair pair = new NameMetaPair(stack.getItem(), 0);
            if (ArtificeRegistry.getTooltipMap()
                .get(pair) != null) {
                for (String s : ArtificeRegistry.getTooltipMap()
                    .get(pair)) {
                    if (ArtificeConfig.flavorText || !s.startsWith(ArtificeTooltips.commentCode)) {
                        infoList.add(s);
                    }
                }
            }
        }
    }

    public boolean func_150897_b(Block block) {
        return true;
    }

    public float func_150893_a(ItemStack stack, Block block) {
        return this.toolMaterial.getEfficiencyOnProperMaterial();
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(
            reg,
            "sledge_" + this.toolMaterial.toString()
                .toLowerCase(Locale.ENGLISH),
            "sledge");
    }

    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        World world = player.worldObj;
        if (!world.isRemote && !player.capabilities.isCreativeMode) {
            try {
                ArrayList<ItemStack> dropped = getIngredientsForBlock(world, x, y, z, player.getHeldItem());
                if (dropped != null) {
                    for (ItemStack is : dropped) {
                        if (world.rand.nextInt(100) > this.lossChance) {
                            world.spawnEntityInWorld(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, is.copy()));
                        }
                    }

                    world.setBlockToAir(x, y, z);
                }
            } catch (Exception var10) {
                var10.printStackTrace();
            }

            return super.onBlockStartBreak(stack, x, y, z, player);
        } else {
            return super.onBlockStartBreak(stack, x, y, z, player);
        }
    }

    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z,
        EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            if (player.capabilities.isCreativeMode) {
                return true;
            }
        }

        stack.damageItem(1, entity);
        return true;
    }

    public static ArrayList<ItemStack> getIngredientsForBlock(World world, int x, int y, int z, ItemStack held) {
        if (world.isAirBlock(x, y, z)) {
            return null;
        } else {
            Block b = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);
            ArrayList<ItemStack> drops = b.getDrops(
                world,
                x,
                y,
                z,
                meta,
                EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, held));
            ArrayList<ItemStack> smashedDrops = new ArrayList<>();

            for (ItemStack normalDrop : drops) {
                ArrayList<ItemStack> ingredients = getIngredientsForItemStack(normalDrop, world.rand);
                if (ingredients != null) {
                    smashedDrops.addAll(ingredients);
                } else {
                    smashedDrops.add(normalDrop);
                }
            }

            return smashedDrops;
        }
    }

    private static ArrayList<ItemStack> getIngredientsForItemStack(ItemStack input, Random rand) {
        if (input != null) {
            for (IRecipe recipe : CraftingManager.getInstance()
                .getRecipeList()) {
                if ((recipe instanceof ShapedRecipes || recipe instanceof ShapedOreRecipe)
                    && recipe.getRecipeOutput() != null
                    && recipe.getRecipeOutput()
                        .getUnlocalizedName()
                        .equals(input.getUnlocalizedName())
                    && (!recipe.getRecipeOutput()
                        .getHasSubtypes()
                        || recipe.getRecipeOutput()
                            .getItemDamage() == input.getItemDamage())) {
                    if (recipe.getRecipeOutput().stackSize > 1) {
                        return null;
                    }

                    return getIngredientsForRecipe(recipe);
                }
            }
        }

        return null;
    }

    private static ArrayList<ItemStack> getIngredientsForRecipe(IRecipe recipe) {
        if (recipe instanceof ShapedRecipes) {
            return getIngredientsForShapedRecipe((ShapedRecipes) recipe);
        } else {
            return recipe instanceof ShapedOreRecipe ? getIngredientsForShapedOreRecipe((ShapedOreRecipe) recipe)
                : null;
        }
    }

    private static ArrayList<ItemStack> getIngredientsForShapedRecipe(ShapedRecipes recipe) {
        ArrayList<ItemStack> ingredients = new ArrayList<>();

        for (ItemStack input : recipe.recipeItems) {
            ingredients.add(input);
        }

        return ingredients;
    }

    private static ArrayList<ItemStack> getIngredientsForShapedOreRecipe(ShapedOreRecipe recipe) {
        try {
            ArrayList<ItemStack> ingredients = new ArrayList<>();
            Object[] objects = (Object[]) ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, recipe, 3);
            ItemStack[] items = new ItemStack[objects.length];

            for (int i = 0; i < objects.length; i++) {
                if (objects[i] instanceof ItemStack) {
                    items[i] = (ItemStack) objects[i];
                }

                if (objects[i] instanceof ArrayList && ((ArrayList) objects[i]).size() > 0) {
                    items[i] = (ItemStack) ((ArrayList) objects[i]).get(0);
                }
            }

            for (ItemStack input : items) {
                if (input != null) {
                    ingredients.add(input);
                }
            }

            return ingredients;
        } catch (IllegalArgumentException var8) {
            var8.printStackTrace();
        } catch (SecurityException var9) {
            var9.printStackTrace();
        }

        return null;
    }
}
