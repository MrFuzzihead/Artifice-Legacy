package com.mrfuzzihead.artifice.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.ArtificeTooltips;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.BoxContents;
import com.mrfuzzihead.artifice.util.FormatCodes;
import com.mrfuzzihead.artifice.util.NameMetaPair;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBox extends ItemArtifice {

    private IIcon icon;

    public ItemBox() {
        this.setUnlocalizedName(Names.BOX);
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName();
    }

    public void getSubItems(Item id, CreativeTabs tab, List list) {
        list.add(new ItemStack(id, 1, 0));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "box", "box");
    }

    @SideOnly(Side.CLIENT)
    public EnumRarity getRarity(ItemStack stack) {
        return stack.hasTagCompound() ? EnumRarity.uncommon : EnumRarity.common;
    }

    @SideOnly(Side.CLIENT)
    public boolean hasEffect(ItemStack stack, int pass) {
        return stack.hasTagCompound();
    }

    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float xOffset, float yOffset, float zOffset) {
        NBTTagCompound tag = stack.getTagCompound();
        int num = 1;
        if (player.isSneaking()) {
            num = stack.stackSize;
        }

        if (tag != null) {
            this.onItemRightClick(stack, world, player);
            return true;
        } else {
            return false;
        }
    }

    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            ItemStack thing = BoxContents.read(tag);
            if (thing == null) {
                return stack;
            }
            thing.stackSize = stack.getItemDamage();

            if (player.isSneaking()) {
                int numCrafted = stack.stackSize * stack.getItemDamage();
                if (numCrafted > thing.getMaxStackSize()) {
                    int given = 0;

                    while (given < numCrafted) {
                        ItemStack out = thing.copy();
                        out.stackSize = numCrafted - given > thing.getMaxStackSize() ? thing.getMaxStackSize()
                            : numCrafted - given;
                        given += out.stackSize;
                        if (out != null && out.stackSize > 0
                            && !player.inventory.addItemStackToInventory(out)
                            && !player.worldObj.isRemote) {
                            player.worldObj.spawnEntityInWorld(
                                new EntityItem(
                                    player.worldObj,
                                    player.posX + 0.5,
                                    player.posY + 0.5,
                                    player.posZ + 0.5,
                                    out));
                        }
                    }
                } else {
                    ItemStack out = thing.copy();
                    out.stackSize = numCrafted;
                    if (out != null && out.stackSize > 0
                        && !player.inventory.addItemStackToInventory(out)
                        && !player.worldObj.isRemote) {
                        player.worldObj.spawnEntityInWorld(
                            new EntityItem(
                                player.worldObj,
                                player.posX + 0.5,
                                player.posY + 0.5,
                                player.posZ + 0.5,
                                out));
                    }
                }

                stack.stackSize = 0;
                return stack;
            }

            ItemStack extra = null;
            if (thing.stackSize > thing.getMaxStackSize() && stack.stackSize > 1) {
                extra = stack.copy();
                extra.stackSize = stack.stackSize - 1;
                stack.stackSize = 1;
                thing.stackSize = stack.getItemDamage();
            }

            ItemStack drop = null;
            if (thing.stackSize > thing.getMaxStackSize()) {
                drop = thing.copy();
                drop.stackSize = thing.getMaxStackSize();
                stack.setItemDamage(stack.getItemDamage() - drop.stackSize);
                if (stack.getItemDamage() < 1) {
                    stack.stackSize--;
                }

                if (drop != null && !player.inventory.addItemStackToInventory(drop) && !world.isRemote) {
                    world.spawnEntityInWorld(
                        new EntityItem(world, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, drop));
                }
            } else {
                drop = thing.copy();
                stack.stackSize--;
                if (drop != null && !player.inventory.addItemStackToInventory(drop) && !world.isRemote) {
                    world.spawnEntityInWorld(
                        new EntityItem(world, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, drop));
                }
            }

            if (extra != null && !player.inventory.addItemStackToInventory(extra) && !world.isRemote) {
                world.spawnEntityInWorld(
                    new EntityItem(world, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, extra));
            }
        }

        return stack;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
        if (ArtificeConfig.tooltips) {
            // The box has never shown flavour text, whatever the config says.
            ArtificeRegistry.appendTooltips(stack, infoList, false);
        }

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            ItemStack contained = BoxContents.read(tag);
            if (contained == null) {
                return;
            }

            infoList.add(
                FormatCodes.Italic.code + StatCollector.translateToLocal("tooltip.artifice.box.contains")
                    + " "
                    + FormatCodes.Reset.code
                    + FormatCodes.Aqua.code
                    + contained.getDisplayName());
            infoList.add(
                FormatCodes.Italic.code + StatCollector.translateToLocal("tooltip.artifice.box.amount")
                    + " "
                    + FormatCodes.Reset.code
                    + FormatCodes.Aqua.code
                    + stack.getItemDamage());
            NBTTagList enchants = tag.getCompoundTag("nbt")
                .getTag("ench") != null
                    ? (NBTTagList) tag.getCompoundTag("nbt")
                        .getTag("ench")
                    : (NBTTagList) tag.getCompoundTag("nbt")
                        .getTag("StoredEnchantments");
            if (enchants != null) {
                for (int i = 0; i < enchants.tagCount(); i++) {
                    short id = enchants.getCompoundTagAt(i)
                        .getShort("id");
                    short lvl = enchants.getCompoundTagAt(i)
                        .getShort("lvl");
                    if (Enchantment.enchantmentsList[id] != null) {
                        infoList.add(FormatCodes.Yellow.code + Enchantment.enchantmentsList[id].getTranslatedName(lvl));
                    }
                }
            }
        }

        if (ArtificeConfig.tooltips) {
            NameMetaPair pair = new NameMetaPair(stack.getItem(), 0);
            if (ArtificeRegistry.getTooltipMap()
                .get(pair) != null) {
                for (String sx : ArtificeRegistry.getTooltipMap()
                    .get(pair)) {
                    if (ArtificeConfig.flavorText && sx.startsWith(ArtificeTooltips.commentCode)) {
                        infoList.add(sx);
                    }
                }
            }
        }
    }
}
