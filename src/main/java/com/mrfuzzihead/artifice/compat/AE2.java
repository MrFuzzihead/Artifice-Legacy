package com.mrfuzzihead.artifice.compat;

import com.mrfuzzihead.artifice.ArtificeCore;

import cpw.mods.fml.common.event.FMLInterModComms;

public class AE2 implements ICompat {

    @Override
    public String getModID() {
        return "appliedenergistics2";
    }

    @Override
    public void load() {
        try {
            FMLInterModComms
                .sendMessage(this.getModID(), "whitelist-spatial", "com.mrfuzzihead.artifice.tile.TileEntityAttuned");
            FMLInterModComms.sendMessage(
                this.getModID(),
                "whitelist-spatial",
                "com.mrfuzzihead.artifice.tile.TileEntityHeatingCoil");
            FMLInterModComms
                .sendMessage(this.getModID(), "whitelist-spatial", "com.mrfuzzihead.artifice.tile.TileEntityLogic");
            FMLInterModComms.sendMessage(
                this.getModID(),
                "whitelist-spatial",
                "com.mrfuzzihead.artifice.tile.TileEntityNuclearBattery");
            ArtificeCore.logger.info("AE2 Compat Initialized");
        } catch (Throwable var2) {
            ArtificeCore.logger.warn("Couldn't initialize AE2 compat");
            var2.printStackTrace();
        }
    }
}
