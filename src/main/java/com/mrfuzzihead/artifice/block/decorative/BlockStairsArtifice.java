package com.mrfuzzihead.artifice.block.decorative;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.world.IBlockAccess;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockStairsArtifice extends BlockStairs {

    private int color = 16777215;

    public BlockStairsArtifice(Block block, int meta) {
        super(block, meta);
        this.setLightOpacity(0);
        this.setCreativeTab(ArtificeCore.worldTab);
        String name = block.getUnlocalizedName() + ".stairs." + ArtificeConfig.rocks[meta].toLowerCase(Locale.ENGLISH);
        name = name.replace("tile.", "");
        this.setBlockName(name);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public BlockStairsArtifice(Block block, int meta, int color) {
        this(block, meta);
        this.color = color;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return this.color;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess block, int x, int y, int z) {
        return this.color;
    }
}
