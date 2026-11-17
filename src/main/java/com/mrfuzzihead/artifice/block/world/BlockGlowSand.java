package com.mrfuzzihead.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSand;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

import cpw.mods.fml.common.registry.GameRegistry;

public class BlockGlowSand extends BlockSand {

    public BlockGlowSand() {
        this.setBlockName("artifice.glowsand");
        this.setCreativeTab(ArtificeCore.worldTab);
        this.setLightLevel(0.5F);
        this.setStepSound(Block.soundTypeSand);
        this.setHardness(0.5F);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }
}
