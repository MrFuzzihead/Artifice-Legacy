package com.mrfuzzihead.artifice.net;

import org.junit.Assert;
import org.junit.Test;

import com.mrfuzzihead.artifice.net.packets.ArtificePacketSneak;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * Round-trips the mod's packets through the real codec implementation.
 *
 * <p>
 * Packet codecs are pure data, so they need no game - and they are where the least visible bugs live, because a
 * mis-ordered read/write pair only shows up as a wrong value in gameplay. {@link ArtificeMessageToMessageCodec}
 * currently carries a single packet, but the discriminator table and the read/write symmetry are worth pinning
 * before more packets are added.
 * </p>
 */
public class PacketCodecTest {

    @Test
    public void sneakPacketRoundTripsThroughTheCodec() throws Exception {
        ArtificeMessageToMessageCodec codec = new ArtificeMessageToMessageCodec();

        ArtificePacketSneak out = new ArtificePacketSneak(4242);
        ByteBuf buf = Unpooled.buffer();
        codec.encodeInto(null, out, buf);

        Assert.assertEquals("the packet payload is exactly one int", 4, buf.readableBytes());

        ArtificePacketSneak in = new ArtificePacketSneak();
        codec.decodeInto(null, buf, in);

        Assert.assertEquals(4242, in.playerID);
        Assert.assertEquals("the codec must consume the whole payload", 0, buf.readableBytes());
    }

    @Test
    public void sneakPacketRoundTripsEdgeValues() throws Exception {
        ArtificeMessageToMessageCodec codec = new ArtificeMessageToMessageCodec();
        int[] values = { 0, 1, -1, Integer.MAX_VALUE, Integer.MIN_VALUE };

        for (int value : values) {
            ByteBuf buf = Unpooled.buffer();
            codec.encodeInto(null, new ArtificePacketSneak(value), buf);

            ArtificePacketSneak in = new ArtificePacketSneak();
            codec.decodeInto(null, buf, in);

            Assert.assertEquals("player id " + value + " did not survive the codec", value, in.playerID);
        }
    }

    @Test
    public void packetWriteAndReadAreSymmetricWithoutTheCodec() {
        ArtificePacketSneak out = new ArtificePacketSneak(77);
        ByteBuf buf = Unpooled.buffer();
        out.writeBytes(buf);

        ArtificePacketSneak in = new ArtificePacketSneak();
        in.readBytes(buf);

        Assert.assertEquals(77, in.playerID);
        Assert.assertEquals(0, buf.readableBytes());
    }
}
