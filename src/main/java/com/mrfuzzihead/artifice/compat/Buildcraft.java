package com.mrfuzzihead.artifice.compat;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeFluids;

import cpw.mods.fml.common.event.FMLInterModComms;

public class Buildcraft implements ICompat {

    @Override
    public String getModID() {
        return "BuildCraft|Core";
    }

    @Override
    public void load() {
        try {
            for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
                FMLInterModComms.sendMessage(
                    this.getModID(),
                    "add-facade",
                    Block.blockRegistry.getNameForObject(ArtificeBlocks.blockReinforced) + "@" + i);
            }

            for (int i = 0; i < ArtificeConfig.rocks.length; i++) {
                FMLInterModComms.sendMessage(
                    this.getModID(),
                    "add-facade",
                    Block.blockRegistry.getNameForObject(ArtificeBlocks.blockBasalt) + "@" + i);
                FMLInterModComms.sendMessage(
                    this.getModID(),
                    "add-facade",
                    Block.blockRegistry.getNameForObject(ArtificeBlocks.blockMarble) + "@" + i);
            }

            for (int i = 0; i < ArtificeBlocks.blockLimestones.length; i++) {
                for (int j = 0; j < ArtificeConfig.rocks.length; j++) {
                    FMLInterModComms.sendMessage(
                        this.getModID(),
                        "add-facade",
                        Block.blockRegistry.getNameForObject(ArtificeBlocks.blockLimestones[i]) + "@" + j);
                }
            }

            for (int i = 0; i < ArtificeBlocks.blockLamps.length; i++) {
                FMLInterModComms.sendMessage(
                    this.getModID(),
                    "add-facade",
                    Block.blockRegistry.getNameForObject(ArtificeBlocks.blockLamps[i]) + "@" + 0);
                FMLInterModComms.sendMessage(
                    this.getModID(),
                    "add-facade",
                    Block.blockRegistry.getNameForObject(ArtificeBlocks.blockLampsInverted[i]) + "@" + 0);
            }

            FMLInterModComms.sendMessage(
                this.getModID(),
                "add-facade",
                Block.blockRegistry.getNameForObject(ArtificeBlocks.blockSteel) + "@0");
            FMLInterModComms.sendMessage(
                this.getModID(),
                "add-facade",
                Block.blockRegistry.getNameForObject(ArtificeBlocks.blockUranium) + "@1");
            FluidStack oil = new FluidStack(ArtificeFluids.fluidOil, 1);
            FluidStack fuel = new FluidStack(ArtificeFluids.fluidFuel, 1);
            NBTTagCompound recipe = new NBTTagCompound();
            NBTTagCompound input = new NBTTagCompound();
            NBTTagCompound output = new NBTTagCompound();
            oil.writeToNBT(input);
            fuel.writeToNBT(output);
            recipe.setTag("input", input);
            recipe.setTag("output", output);
            recipe.setInteger("energy", 12);
            recipe.setInteger("delay", 1);
            FMLInterModComms.sendMessage(this.getModID(), "add-refinery-recipe", recipe);
            ArtificeCore.logger.info("BuildCraft Compat Initialized");
        } catch (Throwable var6) {
            ArtificeCore.logger.warn("Couldn't initialize BuildCraft compat");
            var6.printStackTrace();
        }
    }
}
