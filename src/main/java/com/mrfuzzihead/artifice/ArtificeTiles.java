package com.mrfuzzihead.artifice;

import com.mrfuzzihead.artifice.tile.TileEntityAttuned;
import com.mrfuzzihead.artifice.tile.TileEntityHeatingCoil;
import com.mrfuzzihead.artifice.tile.TileEntityLogic;
import com.mrfuzzihead.artifice.tile.TileEntityNuclearBattery;

import cpw.mods.fml.common.registry.GameRegistry;

public class ArtificeTiles {

    public static void initTiles() {
        GameRegistry.registerTileEntity(TileEntityAttuned.class, "tileArtificeAttuned");
        GameRegistry.registerTileEntity(TileEntityLogic.class, "tileArtificeLogic");
        GameRegistry.registerTileEntity(TileEntityNuclearBattery.class, "tileArtificeRadioBattery");
        GameRegistry.registerTileEntity(TileEntityHeatingCoil.class, "tileArtificeHeatingCoil");
    }
}
