package com.mrfuzzihead.artifice.block.frame;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameGlassWall extends BlockFrame {

    boolean isDark;
    private IIcon[] icons = new IIcon[4];

    public BlockFrameGlassWall(boolean isDark) {
        this.isDark = isDark;
        if (isDark) {
            this.setBlockName("artifice.glasswalldark");
            this.setLightOpacity(15);
        } else {
            this.setBlockName("artifice.glasswall");
        }

        GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        TextureHandler.registerConnectedTexture(reg, this, 0, "basic", "glasswall");
        this.icons[0] = TextureHandler.getConnectedTexture(this, 0, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 1, "reinforced", "glasswall");
        this.icons[1] = TextureHandler.getConnectedTexture(this, 1, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 2, "industrial", "glasswall");
        this.icons[2] = TextureHandler.getConnectedTexture(this, 2, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 3, "advanced", "glasswall");
        this.icons[3] = TextureHandler.getConnectedTexture(this, 3, 0).icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta >= ArtificeConfig.tiers.length) {
            meta = 0;
        }

        return this.icons[meta];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        return this.getIcon(side, access.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    public int getRenderBlockPass() {
        return 1;
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        Block i1 = par1IBlockAccess.getBlock(par2, par3, par4);
        return i1 != this && super.shouldSideBeRendered(par1IBlockAccess, par2, par3, par4, par5);
    }

    public int getRenderType() {
        return ArtificeConfig.ctmRenderID;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX,
        double explosionY, double explosionZ) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getResistance(meta);
    }

    public float getResistance(int meta) {
        switch (meta) {
            case 0:
                return 15.0F;
            case 1:
                return 25.0F;
            case 2:
                return 45.0F;
            case 3:
                return 75.0F;
            default:
                return 10.0F;
        }
    }
}
