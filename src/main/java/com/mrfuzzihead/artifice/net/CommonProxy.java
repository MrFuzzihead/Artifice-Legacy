package shukaro.artifice.net;

import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import io.netty.channel.ChannelHandler;
import java.util.EnumMap;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.gui.GuiHandlerArtifice;
import shukaro.artifice.net.handlers.SneakMessageHandler;

public class CommonProxy {
   public static EnumMap<Side, FMLEmbeddedChannel> artificeChannel;

   public void init() {
      artificeChannel = NetworkRegistry.INSTANCE.newChannel("Artifice", new ChannelHandler[]{new ArtificeMessageToMessageCodec(), new SneakMessageHandler()});
      NetworkRegistry.INSTANCE.registerGuiHandler(ArtificeCore.instance, new GuiHandlerArtifice());
   }
}
