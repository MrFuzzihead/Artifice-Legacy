package com.mrfuzzihead.artifice.net;

import net.minecraftforge.common.MinecraftForge;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.event.ArtificeClientEventHandler;
import com.mrfuzzihead.artifice.render.FrameRenderer;
import com.mrfuzzihead.artifice.render.LotusRenderer;
import com.mrfuzzihead.artifice.render.OreRenderer;
import com.mrfuzzihead.artifice.render.connectedtexture.CTMRenderer;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy {

    public static int renderPass;

    @Override
    public void init() {
        super.init();
        MinecraftForge.EVENT_BUS.register(new ArtificeClientEventHandler());
        ArtificeConfig.lotusRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ArtificeConfig.lotusRenderID, new LotusRenderer());
        ArtificeConfig.frameRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ArtificeConfig.frameRenderID, new FrameRenderer());
        ArtificeConfig.oreRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ArtificeConfig.oreRenderID, new OreRenderer());
        ArtificeConfig.ctmRenderID = RenderingRegistry.getNextAvailableRenderId();
        RenderingRegistry.registerBlockHandler(ArtificeConfig.ctmRenderID, new CTMRenderer());
    }
}
