package com.mrfuzzihead.artifice.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class NameMetaPair {

    private String name;
    private int meta;

    public NameMetaPair(Item item, int meta) {
        this(Item.itemRegistry.getNameForObject(item), meta);
    }

    public NameMetaPair(Block block, int meta) {
        this(Block.blockRegistry.getNameForObject(block), meta);
    }

    public NameMetaPair(String name, int meta) {
        this.name = name;
        this.meta = meta;
    }

    public NameMetaPair(Item item) {
        this(item, -1);
    }

    public NameMetaPair(Block block) {
        this(block, -1);
    }

    public NameMetaPair(String name) {
        this(name, -1);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Block getBlock() {
        return (Block) Block.blockRegistry.getObject(this.name);
    }

    public Item getItem() {
        return (Item) Item.itemRegistry.getObject(this.name);
    }

    public int getMetadata() {
        return this.meta;
    }

    public void setMeta(int meta) {
        this.meta = meta;
    }

    public ItemStack getStack() {
        if (this.isValidBlock()) {
            return new ItemStack(this.getBlock(), 1, this.getMetadata());
        } else {
            return this.isValidItem() ? new ItemStack(this.getItem(), 1, this.getMetadata()) : null;
        }
    }

    public boolean isValidBlock() {
        return this.getBlock() != null && this.meta >= 0 && this.meta < 65536;
    }

    public boolean isValidItem() {
        return this.getItem() != null && this.meta >= 0 && this.meta < 65536;
    }

    /**
     * {@code equals} treats metadata {@code -1} as "any metadata", so metadata must not take part in the hash: a
     * wildcard pair has to land in the same bucket as every concrete pair it matches, or a {@code HashSet} /
     * {@code HashMap} lookup with the wildcard form silently misses. The previous {@code name.hashCode() | meta}
     * set every bit for {@code meta == -1}, which is exactly the case that mattered.
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof NameMetaPair) {
            NameMetaPair imp = (NameMetaPair) o;
            return this.name.equals(imp.name) && (this.meta == imp.meta || this.meta == -1 || imp.meta == -1);
        } else {
            return false;
        }
    }

    public boolean equals(NameMetaPair o) {
        return this.name.equals(o.name) && (this.meta == o.meta || this.meta == -1 || o.meta == -1);
    }

    @Override
    public String toString() {
        return this.name + "@" + this.meta;
    }
}
