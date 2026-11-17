package com.mrfuzzihead.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.block.ItemBlockArtifice;

public class ItemBlockNiter extends ItemBlockArtifice {

    public ItemBlockNiter(Block block) {
        super(block);
    }

    public String getUnlocalizedName(ItemStack stack) {
        return stack.getItemDamage() == 0 ? Block.getBlockFromItem(stack.getItem())
            .getUnlocalizedName() + ".ore"
            : Block.getBlockFromItem(stack.getItem())
                .getUnlocalizedName() + ".block";
    }
}
