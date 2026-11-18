package com.mrfuzzihead.artifice.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * Reads and writes the item a box holds.
 *
 * <p>
 * The key was a numeric item id, which is <b>not</b> stable across mod changes in 1.7.10 - it depends on
 * registration order - so a box in a chest could silently turn into a different item after a pack update
 * (CODE_REVIEW 9.6). Boxes now store the registry name as well and prefer it on read; the numeric id stays as a
 * fallback so boxes written by older versions keep working.
 * </p>
 */
public final class BoxContents {

    private BoxContents() {}

    /**
     * Adds the stored item to {@code tag}: the registry name (preferred on read) and the legacy numeric id. The
     * *count* is the box's item damage, not part of this tag.
     *
     * @return {@code tag}, for chaining
     */
    public static NBTTagCompound write(NBTTagCompound tag, ItemStack stored) {
        tag.setInteger("meta", stored.getItemDamage());
        tag.setInteger("id", Item.getIdFromItem(stored.getItem()));

        String name = Item.itemRegistry.getNameForObject(stored.getItem());
        if (name != null) {
            tag.setString("name", name);
        }

        return tag;
    }

    /**
     * @return the stored item, or {@code null} when the tag names nothing this pack has - the caller drops the slot
     *         rather than NPEing on a box from a pack that removed the item
     */
    public static Item item(NBTTagCompound tag) {
        if (tag == null) {
            return null;
        }

        if (tag.hasKey("name")) {
            Object named = Item.itemRegistry.getObject(tag.getString("name"));
            if (named instanceof Item) {
                return (Item) named;
            }
        }

        // Legacy box, or a name that no longer resolves but whose numeric id still does.
        return Item.getItemById(tag.getInteger("id"));
    }

    /**
     * @return a single-item stack of the box's contents, or {@code null} when it cannot be resolved
     */
    public static ItemStack read(NBTTagCompound tag) {
        Item item = item(tag);
        if (item == null) {
            return null;
        }

        ItemStack stack = new ItemStack(item, 1, tag.getInteger("meta"));
        if (tag.getCompoundTag("nbt")
            .hasNoTags() == false) {
            stack.setTagCompound(tag.getCompoundTag("nbt"));
        }

        return stack;
    }
}
