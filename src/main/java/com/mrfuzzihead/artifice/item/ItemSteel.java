package com.mrfuzzihead.artifice.item;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemSteel extends ItemArtifice {

    private IIcon icon;
    private IIcon dustIcon;
    private String[] names = new String[] { "ingot", "dust" };

    public ItemSteel() {
        this.setUnlocalizedName("artifice.steel");
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta) {
        return meta == 0 ? this.icon : this.dustIcon;
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() > 1 ? "item.artifice.steel." + this.names[0].toLowerCase(Locale.ENGLISH)
            : "item.artifice.steel." + this.names[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item id, CreativeTabs tab, List list) {
        list.add(new ItemStack(id, 1, 0));
        list.add(new ItemStack(id, 1, 1));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "ingot_steel", "steel");
        this.dustIcon = TextureHandler.registerIcon(reg, "dust_steel", "steel");
    }
}
