package shukaro.artifice.net.packets;

import io.netty.buffer.ByteBuf;

public interface ArtificePacket {
   void readBytes(ByteBuf var1);

   void writeBytes(ByteBuf var1);
}
