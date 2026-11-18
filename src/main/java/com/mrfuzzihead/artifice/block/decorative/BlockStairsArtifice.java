package com.mrfuzzihead.artifice.block.decorative;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.world.IBlockAccess;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockStairsArtifice extends BlockStairs {

    private int color = 16777215;

    public BlockStairsArtifice(Block block, int meta) {
        super(block, meta);
        this.setLightOpacity(0);
        this.setCreativeTab(ArtificeCore.worldTab);
        this.setBlockName(Names.stairs(block, ArtificeConfig.rocks[meta]));
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
