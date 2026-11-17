package com.mrfuzzihead.artifice.block.decorative;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockSteel extends Block {

    private IIcon icon;

    public BlockSteel() {
        super(Material.iron);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setHardness(3.0F);
        this.setResistance(10.0F);
        this.setBlockName("artifice.steel");
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "steel", "misc");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }
}
