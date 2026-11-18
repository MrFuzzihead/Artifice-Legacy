package com.mrfuzzihead.artifice.block.world;

import java.util.Random;

import net.minecraft.block.BlockLog;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockCharredLog extends BlockLog {

    private IIcon bark;
    private IIcon core;

    public BlockCharredLog() {
        this.setBlockName(Names.CHARRED_LOG);
        this.setCreativeTab(ArtificeCore.worldTab);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.bark = TextureHandler.registerIcon(reg, "charredlogbark", "misc");
        this.core = TextureHandler.registerIcon(reg, "charredlogcore", "misc");
    }

    @SideOnly(Side.CLIENT)
    protected IIcon getSideIcon(int meta) {
        return this.bark;
    }

    @SideOnly(Side.CLIENT)
    protected IIcon getTopIcon(int meta) {
        return this.core;
    }

    public boolean canSustainLeaves(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public int getExpDrop(IBlockAccess world, int meta, int fortune) {
        return Blocks.coal_ore.getExpDrop(world, meta, fortune);
    }

    public boolean isWood(IBlockAccess world, int x, int y, int z) {
        return false;
    }

    public int damageDropped(int meta) {
        return 1;
    }

    public Item getItemDropped(int meta, Random rand, int fortune) {
        return Items.coal;
    }

    public int quantityDropped(int meta, int fortune, Random rand) {
        return Blocks.coal_ore.quantityDropped(meta, fortune, rand);
    }
}
