package com.mrfuzzihead.artifice.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Fallbacks for ore drops that are looked up in the ore dictionary (CODE_REVIEW 5.9).
 *
 * <p>
 * {@code BlockOre} takes its drop, its drop metadata and its silk-touch stack from {@code ItemHelper.getOre(name)}.
 * That is null whenever the pack does not register the ore - a mod that is absent, or one that registers after
 * Artifice in the load order - and every one of those paths used to dereference it unconditionally, so breaking an
 * ore (or silk-touching it, or calling {@code createStackedBlock}) threw {@code NullPointerException}.
 * </p>
 *
 * <p>
 * The fallback is deliberately the block itself: an ore that cannot name its drop still drops as an ore, which is
 * recoverable, instead of vanishing or crashing.
 * </p>
 */
public final class OreDictFallback {

    private OreDictFallback() {}

    /**
     * @param entry    the ore dictionary stack, possibly {@code null}
     * @param fallback the block to fall back to
     * @return the dropped item
     */
    public static Item item(ItemStack entry, Block fallback) {
        return entry != null ? entry.getItem() : Item.getItemFromBlock(fallback);
    }

    /**
     * @param entry    the ore dictionary stack, possibly {@code null}
     * @param fallback the block to fall back to
     * @return the silk-touch stack: the block the entry resolves to, or the fallback block
     */
    public static ItemStack blockStack(ItemStack entry, Block fallback) {
        if (entry == null) {
            return new ItemStack(fallback);
        }

        Block resolved = Block.getBlockFromItem(entry.getItem());
        return new ItemStack(resolved != null ? resolved : fallback);
    }

    /**
     * @return the entry's metadata, or 0 (the base variant) when there is no entry
     */
    public static int meta(ItemStack entry) {
        return entry != null ? entry.getItemDamage() : 0;
    }
}
