package com.mrfuzzihead.artifice.recipe;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.util.BoxContents;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

public class BoxCraftingHandler {

    @SubscribeEvent
    public void onCrafting(ItemCraftedEvent event) {
        IInventory craft = event.craftMatrix;
        EntityPlayer player = event.player;

        for (int i = 0; i < craft.getSizeInventory(); i++) {
            ItemStack slot = craft.getStackInSlot(i);
            if (slot == null || !slot.getItem()
                .equals(ArtificeItems.itemBox)) {
                continue;
            }

            if (event.crafting.getItem()
                .equals(ArtificeItems.itemBox)) {
                continue;
            }

            // An empty box has no NBT at all (crafted, or emptied by right-click), and a box from an older or broken
            // save can name an item that no longer exists. Both used to NPE here, inside a crafting event that fires
            // for every craft while the box sits in the grid (CODE_REVIEW 5.17).
            ItemStack thing = BoxContents.read(slot.getTagCompound());
            if (thing == null) {
                continue;
            }

            // The box's damage is how many items it holds, so crafting with one in the grid has to hand all of them
            // over: this used to give nothing at all unless the contents overflowed a stack, and consumed the box
            // either way (found while fixing 5.17 - the contents were being destroyed).
            // The server knows the player's sneak state (it is synced every tick), so ask it directly instead of
            // consulting the id list the sneak packet maintains - that list is never cleaned on logout, so a stale
            // id could enable shift behaviour for a different player (CODE_REVIEW 5.36).
            boolean shift = player.isSneaking();
            int boxes = shift ? slot.stackSize : 1;
            int perBox = slot.getItemDamage();
            int items = boxes * perBox;

            giveItems(player, thing, items);
            slot.stackSize -= boxes;
        }
    }

    /**
     * Hands {@code count} copies of {@code thing} to the player, splitting into max-stack-sized chunks and dropping
     * any that do not fit - the same shape the right-click path in {@code ItemBox} uses.
     */
    private static void giveItems(EntityPlayer player, ItemStack thing, int count) {
        int given = 0;

        while (given < count) {
            ItemStack out = thing.copy();
            out.stackSize = Math.min(count - given, thing.getMaxStackSize());
            given += out.stackSize;

            if (!player.inventory.addItemStackToInventory(out) && !player.worldObj.isRemote) {
                player.worldObj.spawnEntityInWorld(
                    new EntityItem(player.worldObj, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, out));
            }
        }
    }
}
