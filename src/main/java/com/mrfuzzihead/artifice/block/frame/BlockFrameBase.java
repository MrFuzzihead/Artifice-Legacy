package com.mrfuzzihead.artifice.block.frame;

import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.ArrayUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameBase extends BlockFrame {

    private IIcon[] icons = new IIcon[ArtificeConfig.tiers.length];

    public BlockFrameBase() {
        this.setBlockName(Names.FRAME);
        this.setBlockBounds(0.01F, 0.01F, 0.01F, 0.99F, 0.99F, 0.99F);
        GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
            this.icons[i] = TextureHandler
                .registerIcon(reg, ArtificeConfig.tiers[i].toLowerCase(Locale.ENGLISH), "frame");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        // Metadata 4..15 is reachable: a world edit, a corrupt save or another mod's tool can write it, and the frame
        // table only has one entry per tier. This was the one icon table in the mod that indexed unchecked.
        return this.icons[ArrayUtils.clampIndex(meta, this.icons.length)];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        return this.icons[ArrayUtils.clampIndex(block.getBlockMetadata(x, y, z), this.icons.length)];
    }

    public int getRenderType() {
        return ArtificeConfig.frameRenderID;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return false;
    }
}
