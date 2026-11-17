package com.mrfuzzihead.artifice.block.world;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockFlora extends ItemBlockArtifice {

    public ItemBlockFlora(Block block) {
        super(block);
    }

    @Override
    public int getMetadata(int meta) {
        return meta >= ArtificeConfig.flora.length ? 0 : meta;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return meta >= ArtificeConfig.flora.length ? BlockFlora.icons[0] : BlockFlora.icons[meta];
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() >= ArtificeConfig.flora.length ? Block.getBlockFromItem(stack.getItem())
            .getUnlocalizedName() + "."
            + ArtificeConfig.flora[0].toLowerCase(Locale.ENGLISH)
            : Block.getBlockFromItem(stack.getItem())
                .getUnlocalizedName() + "."
                + ArtificeConfig.flora[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
    }
}
