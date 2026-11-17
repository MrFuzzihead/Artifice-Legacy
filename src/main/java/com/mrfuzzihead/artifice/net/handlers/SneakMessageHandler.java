package shukaro.artifice.net.handlers;

import cpw.mods.fml.common.network.NetworkRegistry;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import shukaro.artifice.event.Tracking;
import shukaro.artifice.net.packets.ArtificePacketSneak;

@Sharable
public class SneakMessageHandler extends SimpleChannelInboundHandler<ArtificePacketSneak> {
   protected void channelRead0(ChannelHandlerContext ctx, ArtificePacketSneak msg) throws Exception {
      INetHandler handler = (INetHandler)ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
      if (handler instanceof NetHandlerPlayServer) {
         if (Tracking.sneaks.contains(msg.playerID)) {
            Tracking.sneaks.remove(Integer.valueOf(msg.playerID));
         } else {
            Tracking.sneaks.add(msg.playerID);
         }
      }
   }
}
