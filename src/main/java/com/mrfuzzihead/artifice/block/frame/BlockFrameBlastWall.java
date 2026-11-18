package com.mrfuzzihead.artifice.block.frame;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameBlastWall extends BlockFrame {

    private IIcon[] icons = new IIcon[4];

    public BlockFrameBlastWall() {
        this.setBlockName(Names.REINFORCED);
        GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        TextureHandler.registerConnectedTexture(reg, this, 0, "basic", "blastwall");
        this.icons[0] = TextureHandler.getConnectedTexture(this, 0, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 1, "reinforced", "blastwall");
        this.icons[1] = TextureHandler.getConnectedTexture(this, 1, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 2, "industrial", "blastwall");
        this.icons[2] = TextureHandler.getConnectedTexture(this, 2, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 3, "advanced", "blastwall");
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

    public int getRenderType() {
        return ArtificeConfig.ctmRenderID;
    }

    public float getExplosionResistance(Entity par1Entity, World world, int x, int y, int z, double explosionX,
        double explosionY, double explosionZ) {
        int meta = world.getBlockMetadata(x, y, z);
        return this.getResistance(meta);
    }

    public float getResistance(int meta) {
        switch (meta) {
            case 0:
                return 20.0F;
            case 1:
                return 30.0F;
            case 2:
                return 50.0F;
            case 3:
                return 80.0F;
            default:
                return 10.0F;
        }
    }

    /**
     * A blast wall is five harder than the frame of the same tier - the "double dip" CODE_REVIEW §9.4 noticed is
     * deliberate, and now it says so instead of looking like arithmetic that forgot its superclass.
     */
    public static final float BLAST_BONUS = 5.0F;

    @Override
    public float getBlockHardness(World world, int x, int y, int z) {
        return super.getBlockHardness(world, x, y, z) + BLAST_BONUS;
    }
}
