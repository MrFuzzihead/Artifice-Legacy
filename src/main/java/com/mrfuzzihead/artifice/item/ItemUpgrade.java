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

public class ItemUpgrade extends ItemArtifice {

    private IIcon[] icons = new IIcon[13];
    private String[] upgrades = new String[] { "Sharpening Kit", "Reinforcement", "Reinforced Limbs", "Plaited String",
        "Counterweight", "Armor Spikes", "Laminated Padding", "Quilted Cover", "Elastic Soles", "Firedamp",
        "Elastic Layering", "Scuba Tank", "Dive Kit" };

    public ItemUpgrade() {
        this.setUnlocalizedName("artifice.upgrade");
        GameRegistry.registerItem(this, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIconFromDamage(int meta) {
        return this.icons[meta];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister reg) {
        for (int i = 0; i < this.upgrades.length; i++) {
            this.icons[i] = TextureHandler.registerIcon(
                reg,
                this.upgrades[i].toLowerCase(Locale.ENGLISH)
                    .replaceAll("\\s", ""),
                "upgrade");
        }
    }

    @SideOnly(Side.CLIENT)
    public void getSubItems(Item id, CreativeTabs tab, List list) {
        for (int i = 0; i < this.upgrades.length; i++) {
            list.add(new ItemStack(id, 1, i));
        }
    }

    public String getUnlocalizedName(ItemStack stack) {
        return "item.artifice.upgrade." + this.upgrades[stack.getItemDamage()].toLowerCase(Locale.ENGLISH)
            .replaceAll("\\s", "");
    }
}
