package shukaro.artifice.recipe;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import shukaro.artifice.ArtificeItems;
import shukaro.artifice.event.Tracking;

public class BoxCraftingHandler {
   @SubscribeEvent
   public void onCrafting(ItemCraftedEvent event) {
      IInventory craft = event.craftMatrix;
      EntityPlayer player = event.player;

      for (int i = 0; i < craft.getSizeInventory(); i++) {
         ItemStack slot = craft.getStackInSlot(i);
         if (slot != null && slot.getItem().equals(ArtificeItems.itemBox) && !event.crafting.getItem().equals(ArtificeItems.itemBox)) {
            boolean shift = false;
            if (Tracking.sneaks.contains(player.getEntityId())) {
               shift = true;
            }

            int num;
            if (shift) {
               num = slot.stackSize;
            } else {
               num = 1;
            }

            NBTTagCompound tag = slot.getTagCompound();
            ItemStack thing = new ItemStack(Item.getItemById(tag.getInteger("id")), 1, tag.getInteger("meta"));
            if (!tag.getCompoundTag("nbt").hasNoTags()) {
               thing.setTagCompound(tag.getCompoundTag("nbt"));
            }

            int size = slot.getItemDamage();
            int numPer = thing.getMaxStackSize() < size ? thing.getMaxStackSize() : size;
            ItemStack damaged = null;
            if (!shift) {
               if (thing.getMaxStackSize() < size) {
                  damaged = slot.copy();
                  damaged.stackSize = 1;
                  damaged.setItemDamage(damaged.getItemDamage() - thing.getMaxStackSize());
               }

               slot.stackSize--;
            } else {
               slot.stackSize = 0;
            }

            if (damaged != null && damaged.stackSize > 0 && !player.inventory.addItemStackToInventory(damaged) && !player.worldObj.isRemote) {
               player.worldObj
                  .spawnEntityInWorld(
                     new EntityItem(player.worldObj, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, damaged)
                  );
            }

            if (shift) {
               int numCrafted = num * size - numPer;
               if (numCrafted > thing.getMaxStackSize()) {
                  int given = 0;

                  while (given < numCrafted) {
                     ItemStack out = thing.copy();
                     out.stackSize = numCrafted - given > thing.getMaxStackSize() ? thing.getMaxStackSize() : numCrafted - given;
                     given += out.stackSize;
                     if (out != null && out.stackSize > 0 && !player.inventory.addItemStackToInventory(out) && !player.worldObj.isRemote) {
                        player.worldObj
                           .spawnEntityInWorld(
                              new EntityItem(player.worldObj, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, out)
                           );
                     }
                  }
               } else {
                  ItemStack out = thing.copy();
                  out.stackSize = numCrafted;
                  if (out != null && out.stackSize > 0 && !player.inventory.addItemStackToInventory(out) && !player.worldObj.isRemote) {
                     player.worldObj
                        .spawnEntityInWorld(
                           new EntityItem(player.worldObj, player.posX + 0.5, player.posY + 0.5, player.posZ + 0.5, out)
                        );
                  }
               }
            }
         }
      }
   }
}
