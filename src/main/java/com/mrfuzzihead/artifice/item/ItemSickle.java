package com.mrfuzzihead.artifice.item;

import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSickle extends ItemTool {

    private IIcon icon;
    private int radius;

    public ItemSickle(ToolMaterial mat) {
        super(3.0F, mat, null);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setMaxDamage(mat.getMaxUses() / 2);
        this.setUnlocalizedName(Names.tool("sickle", this.toolMaterial));
        this.radius = this.getRadius(mat);
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    public int getRadius(ToolMaterial mat) {
        switch (mat) {
            case EMERALD:
                return 5;
            case GOLD:
                return 4;
            case IRON:
                return 3;
            case STONE:
                return 2;
            case WOOD:
                return 1;
            default:
                return 0;
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
        if (ArtificeConfig.tooltips) {
            // Tools carry one metadata value, so the lookup matches whatever damage the stack has rather than
            // assuming 0 (CODE_REVIEW §7.5).
            ArtificeRegistry.appendTooltips(stack.getItem(), infoList);
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(
            reg,
            "sickle_" + this.toolMaterial.toString()
                .toLowerCase(Locale.ENGLISH),
            "sickle");
    }

    public float func_150893_a(ItemStack stack, Block block) {
        return 1.0F;
    }

    public boolean onBlockStartBreak(ItemStack stack, int x, int y, int z, EntityPlayer player) {
        BlockCoord coord = new BlockCoord(x, y, z);
        World world = player.worldObj;
        Block block = coord.getBlock(world);
        int radius = this.getRadius(this.toolMaterial);
        if (!(block instanceof IPlantable)) {
            if (block != null && block.isLeaves(world, x, y, z)) {
                int count = 0;

                for (BlockCoord c : coord.getRadiusBlocks(radius)) {
                    Block b = world.getBlock(c.x, c.y, c.z);
                    int meta = world.getBlockMetadata(c.x, c.y, c.z);
                    if (b != null && coord.getDistance(c) <= radius && b.isLeaves(world, c.x, c.y, c.z)) {
                        if (b.canHarvestBlock(player, meta)) {
                            b.harvestBlock(world, player, c.x, c.y, c.z, meta);
                        }

                        world.setBlockToAir(c.x, c.y, c.z);
                        count++;
                    }
                }

                if (!world.isRemote) {
                    world.playSoundEffect(
                        x + 0.5,
                        y + 0.5,
                        z + 0.5,
                        block.stepSound.getBreakSound(),
                        1.0F,
                        world.rand.nextFloat() * 0.1F + 0.9F);
                }

                // Integer division gave zero durability for 1..3 leaves; a harvest always costs something.
                stack.damageItem(Math.max(1, count / 4), player);
                if (stack.stackSize <= 0) {
                    player.destroyCurrentEquippedItem();
                }

                return true;
            } else {
                return super.onBlockStartBreak(stack, x, y, z, player);
            }
        } else {
            int count = 0;

            // Inclusive on both sides: `i < radius` harvested an off-centre square that never reached +radius
            // (CODE_REVIEW 5.14).
            for (int i = -radius; i <= radius; i++) {
                for (int k = -radius; k <= radius; k++) {
                    Block b = world.getBlock(x + i, y, z + k);
                    int meta = world.getBlockMetadata(x + i, y, z + k);
                    if (b instanceof IPlantable && coord.getDistance(x + i, y, z + k) <= radius
                        && b.canHarvestBlock(player, meta)) {
                        // onBlockStartBreak runs on both sides; the block change belongs to the server only, or the
                        // client sends a redundant update per block harvested.
                        if (!world.isRemote) {
                            b.harvestBlock(world, player, x + i, y, z + k, meta);
                            world.setBlockToAir(x + i, y, z + k);
                        }
                        count++;
                    }
                }
            }

            if (!world.isRemote) {
                world.playSoundEffect(
                    x + 0.5,
                    y + 0.5,
                    z + 0.5,
                    block.stepSound.getBreakSound(),
                    1.0F,
                    world.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (stack.attemptDamageItem(Math.max(1, count / 2), itemRand)) {
                player.destroyCurrentEquippedItem();
            }

            return true;
        }
    }
}
