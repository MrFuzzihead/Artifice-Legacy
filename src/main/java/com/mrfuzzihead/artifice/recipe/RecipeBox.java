package com.mrfuzzihead.artifice.recipe;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.util.ComparableItemStackNBT;

public class RecipeBox implements IRecipe {

    private ItemStack output;

    public boolean matches(InventoryCrafting craft, World world) {
        this.output = null;
        ItemStack box = null;
        ItemStack thing = null;
        boolean hasBox = false;
        boolean isEmpty = true;

        for (int i = 0; i < craft.getSizeInventory(); i++) {
            ItemStack stack = craft.getStackInSlot(i);
            if (stack != null && stack.getItem()
                .equals(ArtificeItems.itemBox)) {
                hasBox = true;
                if (stack.getTagCompound() != null) {
                    isEmpty = false;
                }
                break;
            }
        }

        if (!hasBox) {
            return false;
        } else {
            int c = 0;

            for (int ix = 0; ix < craft.getSizeInventory(); ix++) {
                ItemStack stack = craft.getStackInSlot(ix);
                if (stack != null && stack.getItem()
                    .equals(ArtificeItems.itemBox)) {
                    c++;
                }
            }

            if (c != 1) {
                return false;
            } else if (!isEmpty) {
                for (int ixx = 0; ixx < craft.getSizeInventory(); ixx++) {
                    ItemStack stack = craft.getStackInSlot(ixx);
                    if (stack != null && !stack.getItem()
                        .equals(ArtificeItems.itemBox)) {
                        return false;
                    }
                }

                for (int ixxx = 0; ixxx < craft.getSizeInventory(); ixxx++) {
                    ItemStack stack = craft.getStackInSlot(ixxx);
                    if (stack != null && stack.getItem()
                        .equals(ArtificeItems.itemBox)) {
                        box = stack;
                    }
                }

                NBTTagCompound tag = box.getTagCompound();
                int id = tag.getInteger("id");
                int meta = tag.getInteger("meta");
                int size = box.getItemDamage();
                NBTTagCompound nbt = tag.getCompoundTag("nbt");
                thing = new ItemStack(Item.getItemById(id), 1, meta);
                if (nbt != null) {
                    thing.setTagCompound(nbt);
                }

                int outAmount = size;
                if (size > thing.getMaxStackSize()) {
                    outAmount = thing.getMaxStackSize();
                }

                this.output = new ItemStack(Item.getItemById(id), outAmount, meta);
                if (nbt != null && !nbt.hasNoTags()) {
                    this.output.setTagCompound(nbt);
                }

                return true;
            } else {
                for (int ixxxx = 0; ixxxx < craft.getSizeInventory(); ixxxx++) {
                    ItemStack stack = craft.getStackInSlot(ixxxx);
                    if (stack != null && !stack.getItem()
                        .equals(ArtificeItems.itemBox)) {
                        thing = stack;
                        break;
                    }
                }

                if (thing == null) {
                    return false;
                } else {
                    ComparableItemStackNBT contents = new ComparableItemStackNBT(thing);
                    int num = 0;

                    for (int ixxxxx = 0; ixxxxx < craft.getSizeInventory(); ixxxxx++) {
                        ItemStack stack = craft.getStackInSlot(ixxxxx);
                        if (stack != null) {
                            ComparableItemStackNBT t = new ComparableItemStackNBT(stack);
                            if (!stack.getItem()
                                .equals(ArtificeItems.itemBox)) {
                                if (!t.isStackEqual(contents)) {
                                    return false;
                                }

                                num++;
                            }
                        }
                    }

                    NBTTagCompound tagx = new NBTTagCompound();
                    tagx.setInteger("meta", thing.getItemDamage());
                    tagx.setInteger("id", Item.getIdFromItem(thing.getItem()));
                    if (thing.getTagCompound() != null && !thing.getTagCompound()
                        .hasNoTags()) {
                        tagx.setTag("nbt", thing.getTagCompound());
                    }

                    this.output = new ItemStack(ArtificeItems.itemBox, 1, num);
                    this.output.setTagCompound(tagx);
                    return true;
                }
            }
        }
    }

    public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
        return this.output.copy();
    }

    public int getRecipeSize() {
        return 10;
    }

    public ItemStack getRecipeOutput() {
        return this.output;
    }
}
