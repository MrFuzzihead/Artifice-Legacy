package com.mrfuzzihead.artifice.block.decorative;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.BlockArtifice;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.MinecraftColors;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLamp extends BlockArtifice {

    private static final int tickRate = 2;

    MinecraftColors color;
    private boolean inverted;
    private IIcon icon;

    public BlockLamp(MinecraftColors color, boolean inverted) {
        super(Material.glass);
        this.color = color;
        this.inverted = inverted;
        this.setHardness(0.3F);
        this.setStepSound(soundTypeGlass);
        this.setBlockName(Names.lamp(color.name(), inverted));

        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
        float par8, float par9) {
        if (!world.isRemote) {
            ItemStack held = player.getHeldItem();
            if (held == null && player.isSneaking()) {
                if (this.inverted) {
                    return world.setBlock(x, y, z, ArtificeBlocks.blockLamps[this.getLampID(world, x, y, z)], 0, 3);
                }

                return world.setBlock(x, y, z, ArtificeBlocks.blockLampsInverted[this.getLampID(world, x, y, z)], 0, 3);
            }
        }

        return false;
    }

    private int getLampID(World world, int x, int y, int z) {
        if (!world.isAirBlock(x, y, z) && world.getBlock(x, y, z) instanceof BlockLamp) {
            MinecraftColors c = ((BlockLamp) world.getBlock(x, y, z)).color;

            for (int i = 0; i < 16; i++) {
                if (c.equals(MinecraftColors.values()[i])) {
                    return i;
                }
            }
        }

        return -1;
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < 16; i++) {
            if (this.inverted) {
                TextureHandler.registerConnectedTexture(reg, this, i, "lampinverted", "lamp");
            } else {
                TextureHandler.registerConnectedTexture(reg, this, i, "lamp", "lamp");
            }
        }

        this.icon = TextureHandler.getConnectedTexture(this, 0, 0).icon;
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }

    @Override
    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        int meta = block.getBlockMetadata(x, y, z);
        if (meta > 15) {
            meta = 0;
        }

        return this.getIcon(side, meta);
    }

    /**
     * The metadata a lamp wants: the strongest of the indirect power around it and whatever is fed directly into it.
     * The old code spelled this out as two branches around the same pair of queries, which is why a lamp re-ran its
     * own tick on every neighbour change - there was no way to ask "would this change anything?".
     */
    static int targetMetadata(int indirect, int direct) {
        return indirect > direct ? indirect : direct;
    }

    private static int targetMetadata(World world, int x, int y, int z) {
        return targetMetadata(world.getStrongestIndirectPower(x, y, z), world.getBlockPowerInput(x, y, z));
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        int target = targetMetadata(world, x, y, z);
        if (world.getBlockMetadata(x, y, z) != target) {
            world.setBlockMetadataWithNotify(x, y, z, target, 3);
        }
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;
        // Our own metadata write notifies the neighbours, which notify us back. Sorting that out is what
        // updateTick is for, so only a change that would actually alter the light is worth a tick.
        if (neighbor == this) return;
        if (targetMetadata(world, x, y, z) == world.getBlockMetadata(x, y, z)) return;

        world.scheduleBlockUpdate(x, y, z, this, tickRate);
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
        list.add(new ItemStack(item, 1, 0));
    }

    @Override
    public int damageDropped(int meta) {
        return 0;
    }

    @Override
    public int getDamageValue(World world, int x, int y, int z) {
        return 0;
    }

    @SideOnly(Side.CLIENT)
    /**
     * The tint offsets. A normal lamp darkens the colour and lets the metadata (the light level) bring it back up;
     * an inverted lamp starts slightly brightened and dims as its metadata rises.
     */
    private static final int TINT_DARKENED = -40;
    private static final int TINT_INVERTED = 5;

    public int getRenderColor(int meta) {
        return this.inverted ? this.color.toAdjusted(TINT_INVERTED) : this.color.toAdjusted(TINT_DARKENED);
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess block, int x, int y, int z) {
        return this.inverted ? this.color.toAdjusted(TINT_INVERTED - block.getBlockMetadata(x, y, z))
            : this.color.toAdjusted(block.getBlockMetadata(x, y, z) + TINT_DARKENED);
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        return this.inverted ? 15 - world.getBlockMetadata(x, y, z) : world.getBlockMetadata(x, y, z);
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ArtificeConfig.ctmRenderID;
    }
}
