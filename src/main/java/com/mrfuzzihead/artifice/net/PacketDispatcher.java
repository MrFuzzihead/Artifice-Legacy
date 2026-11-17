package com.mrfuzzihead.artifice.net;

import com.mrfuzzihead.artifice.net.packets.ArtificePacketSneak;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketDispatcher {

    @SideOnly(Side.CLIENT)
    public static void sendSneakEvent(int playerID) {
        try {
            ClientProxy.artificeChannel.get(Side.CLIENT)
                .writeOutbound(new Object[] { new ArtificePacketSneak(playerID) });
        } catch (Exception var2) {
            var2.printStackTrace();
        }
    }
}
