package com.mrfuzzihead.artifice.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.ArtificeTooltips;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class ItemArtifice extends Item {

    public ItemArtifice() {
        this.setHasSubtypes(true);
        this.setCreativeTab(ArtificeCore.mainTab);
    }

    @SideOnly(Side.CLIENT)
    public abstract IIcon getIconFromDamage(int var1);

    @SideOnly(Side.CLIENT)
    public abstract void registerIcons(IIconRegister var1);

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
        if (ArtificeConfig.tooltips) {
            NameMetaPair pair = new NameMetaPair(stack.getItem(), stack.getItemDamage());
            if (ArtificeRegistry.getTooltipMap()
                .get(pair) != null) {
                for (String s : ArtificeRegistry.getTooltipMap()
                    .get(pair)) {
                    if (ArtificeConfig.flavorText || !s.startsWith(ArtificeTooltips.commentCode)) {
                        infoList.add(s);
                    }
                }
            }
        }
    }
}
