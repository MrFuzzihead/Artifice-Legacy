package com.mrfuzzihead.artifice.item;

import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityDispenser;

public class DispenserBehaviorBox extends BehaviorDefaultDispenseItem {

    private final BehaviorDefaultDispenseItem defaultDispense = new BehaviorDefaultDispenseItem();

    public ItemStack dispenseStack(IBlockSource dispenser, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            ItemStack thing = new ItemStack(
                Item.getItemById(tag.getInteger("id")),
                stack.getItemDamage(),
                tag.getInteger("meta"));
            if (!tag.getCompoundTag("nbt")
                .hasNoTags()) {
                thing.setTagCompound(tag.getCompoundTag("nbt"));
            }

            ItemStack extra = null;
            if (thing.stackSize > thing.getMaxStackSize() && stack.stackSize > 1) {
                extra = stack.copy();
                extra.stackSize = stack.stackSize - 1;
                stack.stackSize = 1;
                thing.stackSize = stack.getItemDamage();
            }

            ItemStack drop = null;
            if (thing.stackSize > thing.getMaxStackSize()) {
                drop = thing.copy();
                drop.stackSize = thing.getMaxStackSize();
                stack.setItemDamage(stack.getItemDamage() - drop.stackSize);
                if (stack.getItemDamage() < 1) {
                    stack.stackSize--;
                }

                if (drop != null) {
                    this.defaultDispense.dispense(dispenser, drop);
                }
            } else {
                drop = thing.copy();
                stack.stackSize--;
                if (drop != null) {
                    this.defaultDispense.dispense(dispenser, drop);
                }
            }

            TileEntityDispenser d = (TileEntityDispenser) dispenser.getBlockTileEntity();
            if (extra != null && d.func_146019_a(extra) == -1) {
                this.defaultDispense.dispense(dispenser, extra);
            }
        }

        return stack;
    }

    protected void playDispenseSound(IBlockSource dispenser) {
        dispenser.getWorld()
            .playAuxSFX(1002, dispenser.getXInt(), dispenser.getYInt(), dispenser.getZInt(), 0);
    }
}
