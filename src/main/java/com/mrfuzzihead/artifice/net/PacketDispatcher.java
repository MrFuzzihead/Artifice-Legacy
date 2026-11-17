package shukaro.artifice.net;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import shukaro.artifice.net.packets.ArtificePacketSneak;

public class PacketDispatcher {
   @SideOnly(Side.CLIENT)
   public static void sendSneakEvent(int playerID) {
      try {
         ClientProxy.artificeChannel.get(Side.CLIENT).writeOutbound(new Object[]{new ArtificePacketSneak(playerID)});
      } catch (Exception var2) {
         var2.printStackTrace();
      }
   }
}
