package com.mrfuzzihead.artifice.item;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.ArrayUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;

public class ItemResource extends ItemArtifice {

    private String[] names = new String[] { "sulfur", "niter", "enderdust", "uranium" };
    private IIcon[] icons = new IIcon[this.names.length];

    public ItemResource() {
        this.setCreativeTab(ArtificeCore.worldTab);
        this.setUnlocalizedName(Names.RESOURCE);
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() >= this.names.length
            ? "item.artifice.resource." + this.names[0].toLowerCase(Locale.ENGLISH)
            : "item.artifice.resource." + this.names[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
    }

    public void getSubItems(Item id, CreativeTabs tab, List list) {
        for (int i = 0; i < this.names.length; i++) {
            list.add(new ItemStack(id, 1, i));
        }
    }

    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icons[ArrayUtils.clampIndex(meta, this.names.length)];
    }

    @Override
    public void registerIcons(IIconRegister reg) {
        for (int i = 0; i < this.names.length; i++) {
            this.icons[i] = TextureHandler.registerIcon(reg, this.names[i], "resources");
        }
    }
}
