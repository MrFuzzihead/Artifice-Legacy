package com.mrfuzzihead.artifice.block.frame;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameDetector extends BlockFrame {

    private static final int tickRate = 2;
    private IIcon icon;

    public BlockFrameDetector() {
        this.setBlockName("artifice.detector");

        for (int i = 1; i <= 3; i++) {
            this.validTiers.remove(ArtificeConfig.tiers[i]);
        }

        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "detector", "misc");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        return this.icon;
    }

    public boolean canProvidePower() {
        return true;
    }

    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return blockAccess.getBlockMetadata(x, y, z) == 1 ? 15 : 0;
    }

    public boolean canConnectRedstone(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!neighbor.equals(this) && world.getBlockMetadata(x, y, z) == 0) {
            world.scheduleBlockUpdate(x, y, z, this, 2);
        }
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (world.getBlockMetadata(x, y, z) == 0) {
            world.setBlockMetadataWithNotify(x, y, z, 1, 3);
            world.scheduleBlockUpdate(x, y, z, this, 2);
        } else if (world.getBlockMetadata(x, y, z) == 1) {
            world.setBlockMetadataWithNotify(x, y, z, 2, 3);
            world.scheduleBlockUpdate(x, y, z, this, 2);
        } else {
            world.setBlockMetadataWithNotify(x, y, z, 0, 3);
        }

        world.notifyBlocksOfNeighborChange(x, y, z, this);
    }
}
