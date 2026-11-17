package com.mrfuzzihead.artifice.compat;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;

public class CarpentersBlocks implements ICompat {

    @Override
    public String getModID() {
        return "CarpentersBlocks";
    }

    @Override
    public void load() {
        try {
            Class<?> featureClass = Class.forName("com.carpentersblocks.util.registry.FeatureRegistry");
            ArrayList<String> coverExceptions = (ArrayList<String>) featureClass.getField("coverExceptions")
                .get(null);

            for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockReinforced, 1, i).getDisplayName());
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockGlassWall, 1, i).getDisplayName());
            }

            for (int i = 0; i < ArtificeConfig.rocks.length; i++) {
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockBasalt, 1, i).getDisplayName());
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockMarble, 1, i).getDisplayName());

                for (int j = 0; j < ArtificeBlocks.blockLimestones.length; j++) {
                    coverExceptions.add(new ItemStack(ArtificeBlocks.blockLimestones[j], 1, i).getDisplayName());
                }
            }

            for (int i = 0; i < ArtificeBlocks.blockLamps.length; i++) {
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockLamps[i], 1, 0).getDisplayName());
                coverExceptions.add(new ItemStack(ArtificeBlocks.blockLampsInverted[i], 1, 0).getDisplayName());
            }

            coverExceptions.add(new ItemStack(ArtificeBlocks.blockSteel, 1, 0).getDisplayName());
            coverExceptions.add(new ItemStack(ArtificeBlocks.blockUranium, 1, 1).getDisplayName());
            ArtificeCore.logger.info("Carpenter's Blocks compat initialized");
        } catch (Throwable var5) {
            ArtificeCore.logger.warn("Couldn't load Carpenter's Blocks compat");
            var5.printStackTrace();
        }
    }
}
