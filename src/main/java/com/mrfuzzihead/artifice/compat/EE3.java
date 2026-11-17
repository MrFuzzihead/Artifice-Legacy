package com.mrfuzzihead.artifice.compat;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;

import cpw.mods.fml.common.registry.GameRegistry;

public class EE3 implements ICompat {

    @Override
    public String getModID() {
        return "EE3";
    }

    @Override
    public void load() {
        try {
            Class<?> itemClass = Class.forName("com.pahimar.ee3.init.ModItems");
            List<ItemStack> stoneList = new ArrayList<>();
            stoneList.add(
                (ItemStack) itemClass.getField("stoneMinium")
                    .get(null));
            stoneList.add(
                (ItemStack) itemClass.getField("stonePhilosophers")
                    .get(null));
            ArtificeCore.logger.info("Adding transmutation recipes");

            for (ItemStack stone : stoneList) {
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockBasalt, 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeBlack" }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockMarble, 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeWhite" }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[0], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeGray" }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[1], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0) }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[2], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), Blocks.dirt }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[3], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), Blocks.sand }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[4], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeRed" }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[5], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeBlue" }));
                GameRegistry.addRecipe(
                    new ShapelessOreRecipe(
                        new ItemStack(ArtificeBlocks.blockLimestones[6], 1, 1),
                        new Object[] { stone, new ItemStack(Blocks.cobblestone, 1, 0), "dyeGreen" }));
            }

            ArtificeCore.logger.info("EE3 Compat Initialized");
        } catch (Throwable var5) {
            ArtificeCore.logger.info("Couldn't initialize EE3 compat");
            var5.printStackTrace();
        }
    }
}
