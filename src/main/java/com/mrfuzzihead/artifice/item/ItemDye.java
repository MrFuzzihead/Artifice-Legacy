package com.mrfuzzihead.artifice.item;

import java.util.List;
import java.util.Locale;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.ArrayUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemDye extends ItemArtifice {

    private IIcon[] icons = new IIcon[4];
    private String[] names = new String[] { "Blue", "Black", "Brown", "White" };

    public ItemDye() {
        this.setUnlocalizedName(Names.DYE);
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta) {
        return ArrayUtils.getOrFirst(this.icons, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        for (int i = 0; i < this.names.length; i++) {
            this.icons[i] = TextureHandler.registerIcon(reg, this.names[i].toLowerCase(Locale.ENGLISH), "dye");
        }
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item id, CreativeTabs tab, List list) {
        for (int i = 0; i < this.names.length; i++) {
            list.add(new ItemStack(id, 1, i));
        }
    }

    public String getUnlocalizedName(ItemStack stack) {
        return "item.artifice.dye." + this.names[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
    }
}
