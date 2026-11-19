package com.mrfuzzihead.artifice.net;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.gui.GuiHandlerArtifice;

import cpw.mods.fml.common.network.NetworkRegistry;

public class CommonProxy {

    public void init() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ArtificeCore.instance, new GuiHandlerArtifice());
    }
}
