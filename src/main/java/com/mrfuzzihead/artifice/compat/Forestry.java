package com.mrfuzzihead.artifice.compat;

import java.util.List;

import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;

import forestry.api.apiculture.FlowerManager;

public class Forestry implements ICompat {

    @Override
    public String getModID() {
        return "Forestry";
    }

    @Override
    public void load() {
        try {
            // compileOnly reference (Forestry is optional, so this class is only loaded when isModLoaded said so).
            // Throwable rather than Exception: a Forestry build without the field throws NoSuchFieldError.
            List<ItemStack> flowerList = FlowerManager.plainFlowers;
            ArtificeCore.logger.info("Adding flowers to the Flower Manager");

            for (int i = 0; i < 4; i++) {
                flowerList.add(new ItemStack(ArtificeBlocks.blockFlora, 1, i));
            }

            ArtificeCore.logger.info("Forestry Compat Initialized");
        } catch (Throwable var4) {
            ArtificeCore.logger.warn("Couldn't initialize Forestry compat");
            var4.printStackTrace();
        }
    }
}
