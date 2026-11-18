package com.mrfuzzihead.artifice.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ComparableItemStackNBT extends ComparableItemStack {

    public NBTTagCompound tag;

    public ComparableItemStackNBT(ItemStack stack) {
        super(stack);
        if (stack != null && stack.stackTagCompound != null) {
            this.tag = (NBTTagCompound) stack.stackTagCompound.copy();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * The tag comparison only applies between two tagged stacks. A plain {@link ComparableItemStack} in the other
     * slot - which is what {@link #isStackEqual(ComparableItemStack)}'s signature allows - used to be cast without a
     * check and threw {@code ClassCastException} from inside a recipe match (CODE_REVIEW §8).
     * </p>
     */
    @Override
    public boolean isStackEqual(ComparableItemStack other) {
        return super.isStackEqual(other) && other instanceof ComparableItemStackNBT
            && this.isStackTagEqual((ComparableItemStackNBT) other);
    }

    private boolean isStackTagEqual(ComparableItemStackNBT other) {
        return this.tag == null ? other.tag == null : other.tag != null && this.tag.equals(other.tag);
    }

    @Override
    public ItemStack toItemStack() {
        if (this.tag == null) {
            return super.toItemStack();
        } else {
            ItemStack ret = new ItemStack(this.item, this.stackSize, this.metadata);
            ret.stackTagCompound = (NBTTagCompound) this.tag.copy();
            return ret;
        }
    }
}
