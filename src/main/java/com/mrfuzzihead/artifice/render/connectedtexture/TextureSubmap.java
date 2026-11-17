package com.mrfuzzihead.artifice.render.connectedtexture;

import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class TextureSubmap {

    public int width;
    public int height;
    public IIcon icon;
    public IIcon[] icons;

    public TextureSubmap(IIcon i, int w, int h) {
        this.icon = i;
        this.width = w;
        this.height = h;
        this.icons = new IIcon[this.width * this.height];
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void TexturesStitched(Post event) {
        for (int x = 0; x < this.width; x++) {
            for (int y = 0; y < this.height; y++) {
                this.icons[y * this.width + x] = new TextureVirtual(this.icon, this.width, this.height, x, y);
            }
        }
    }
}
