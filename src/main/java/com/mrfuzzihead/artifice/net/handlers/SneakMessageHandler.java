package com.mrfuzzihead.artifice.net.handlers;

import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;

import com.mrfuzzihead.artifice.event.Tracking;
import com.mrfuzzihead.artifice.net.packets.ArtificePacketSneak;

import cpw.mods.fml.common.network.NetworkRegistry;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@Sharable
public class SneakMessageHandler extends SimpleChannelInboundHandler<ArtificePacketSneak> {

    protected void channelRead0(ChannelHandlerContext ctx, ArtificePacketSneak msg) throws Exception {
        INetHandler handler = (INetHandler) ctx.channel()
            .attr(NetworkRegistry.NET_HANDLER)
            .get();
        if (handler instanceof NetHandlerPlayServer) {
            if (Tracking.sneaks.contains(msg.playerID)) {
                Tracking.sneaks.remove(Integer.valueOf(msg.playerID));
            } else {
                Tracking.sneaks.add(msg.playerID);
            }
        }
    }
}
