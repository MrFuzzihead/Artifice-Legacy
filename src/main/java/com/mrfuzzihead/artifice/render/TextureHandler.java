package com.mrfuzzihead.artifice.render;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.render.connectedtexture.ConnectedTexture;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

public class TextureHandler {

    private static TMap<NameMetaPair, ConnectedTexture[]> connectedTextures = new THashMap();

    public static IIcon registerIcon(IIconRegister reg, String name, String folder) {
        return reg.registerIcon("Artifice".toLowerCase(Locale.ENGLISH) + ":" + folder + "/" + name);
    }

    public static void registerConnectedTexture(IIconRegister reg, Block block, int meta, String name, String folder) {
        ConnectedTexture[] textures = new ConnectedTexture[6];

        for (int i = 0; i < 6; i++) {
            textures[i] = new ConnectedTexture(reg, name, folder);
        }

        connectedTextures.put(new NameMetaPair(block, meta), textures);
    }

    public static void registerConnectedTexture(IIconRegister reg, Block block, int meta, int side, String name,
        String folder) {
        NameMetaPair pair = new NameMetaPair(block, meta);
        ConnectedTexture[] textures;
        if (connectedTextures.get(pair) == null) {
            textures = new ConnectedTexture[6];
        } else {
            textures = (ConnectedTexture[]) connectedTextures.get(pair);
        }

        textures[side] = new ConnectedTexture(reg, name, folder);
        connectedTextures.put(pair, textures);
    }

    public static ConnectedTexture getConnectedTexture(Block block, int meta, int side) {
        NameMetaPair pair = new NameMetaPair(block, meta);
        return connectedTextures.get(pair) != null && ((ConnectedTexture[]) connectedTextures.get(pair))[side] != null
            ? ((ConnectedTexture[]) connectedTextures.get(pair))[side]
            : null;
    }
}
