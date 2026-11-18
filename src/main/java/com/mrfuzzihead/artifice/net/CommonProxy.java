package com.mrfuzzihead.artifice.net;

import java.util.EnumMap;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.gui.GuiHandlerArtifice;
import com.mrfuzzihead.artifice.net.handlers.SneakMessageHandler;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelHandler;

public class CommonProxy {

    public static EnumMap<Side, FMLEmbeddedChannel> artificeChannel;

    public void init() {
        // Literal, not ArtificeCore.modID: this is the wire protocol's name and both sides must agree on it. Tying it
        // to the mod id would silently break every client the moment the id changed.
        artificeChannel = NetworkRegistry.INSTANCE.newChannel(
            "Artifice",
            new ChannelHandler[] { new ArtificeMessageToMessageCodec(), new SneakMessageHandler() });
        NetworkRegistry.INSTANCE.registerGuiHandler(ArtificeCore.instance, new GuiHandlerArtifice());
    }
}
