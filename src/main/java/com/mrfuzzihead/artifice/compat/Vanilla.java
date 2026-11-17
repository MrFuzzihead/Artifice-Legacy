package com.mrfuzzihead.artifice.compat;

import net.minecraft.init.Blocks;

import com.mrfuzzihead.artifice.ArtificeRegistry;

public class Vanilla implements ICompat {

    @Override
    public String getModID() {
        return null;
    }

    @Override
    public void load() {
        ArtificeRegistry.registerDimensionBlacklist(1);
        ArtificeRegistry.registerDimensionBlacklist(-1);
        ArtificeRegistry.registerStoneType(Blocks.stone, 0);
        ArtificeRegistry.registerWorldTypeBlacklist("flat");
    }
}
