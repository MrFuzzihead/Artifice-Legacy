package com.mrfuzzihead.artifice.net;

import com.mrfuzzihead.artifice.net.packets.ArtificePacket;
import com.mrfuzzihead.artifice.net.packets.ArtificePacketSneak;

import cpw.mods.fml.common.network.FMLIndexedMessageToMessageCodec;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class ArtificeMessageToMessageCodec extends FMLIndexedMessageToMessageCodec<ArtificePacket> {

    public static final int SNEAKEVENT = 1;

    public ArtificeMessageToMessageCodec() {
        this.addDiscriminator(1, ArtificePacketSneak.class);
    }

    public void encodeInto(ChannelHandlerContext ctx, ArtificePacket msg, ByteBuf target) throws Exception {
        msg.writeBytes(target);
    }

    public void decodeInto(ChannelHandlerContext ctx, ByteBuf source, ArtificePacket msg) {
        msg.readBytes(source);
    }
}
