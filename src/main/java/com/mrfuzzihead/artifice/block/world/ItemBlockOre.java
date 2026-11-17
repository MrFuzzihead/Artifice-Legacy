package com.mrfuzzihead.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

public class ItemBlockOre extends ItemBlockArtifice {

    public ItemBlockOre(Block block) {
        super(block);
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() >= ArtificeBlocks.rockBlockNames.length ? Block.getBlockFromItem(stack.getItem())
            .getUnlocalizedName()
            : Block.getBlockFromItem(stack.getItem())
                .getUnlocalizedName() + "."
                + ArtificeBlocks.rockBlockNames[stack.getItemDamage()];
    }
}
