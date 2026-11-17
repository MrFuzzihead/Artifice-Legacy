package com.mrfuzzihead.artifice.block.frame;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

public class ItemBlockFrame extends ItemBlockArtifice {

    public ItemBlockFrame(Block block) {
        super(block);
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() >= ArtificeConfig.tiers.length ? Block.getBlockFromItem(stack.getItem())
            .getUnlocalizedName() + "."
            + ArtificeConfig.tiers[0].toLowerCase(Locale.ENGLISH)
            : Block.getBlockFromItem(stack.getItem())
                .getUnlocalizedName() + "."
                + ArtificeConfig.tiers[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
    }
}
