package shukaro.artifice.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class ComparableItemStack {
   public Item item;
   public int metadata = -1;
   public int stackSize = -1;
   public int oreID = -1;

   public ComparableItemStack(ItemStack stack) {
      if (stack != null) {
         this.item = stack.getItem();
         this.metadata = stack.getItemDamage();
         this.stackSize = stack.stackSize;
         this.oreID = OreDictionary.getOreID(stack);
      }
   }

   public ComparableItemStack(Item item, int damage, int stackSize) {
      this.item = item;
      this.metadata = damage;
      this.stackSize = stackSize;
      this.oreID = OreDictionary.getOreID(this.toItemStack());
   }

   public ComparableItemStack(ComparableItemStack stack) {
      this.item = stack.getItem();
      this.metadata = stack.metadata;
      this.stackSize = stack.stackSize;
      this.oreID = stack.oreID;
   }

   public ComparableItemStack(String oreName) {
      if (!OreDictionary.getOres(oreName).isEmpty()) {
         ItemStack ore = (ItemStack)OreDictionary.getOres(oreName).get(0);
         this.item = ore.getItem();
         this.metadata = ore.getItemDamage();
         this.stackSize = 1;
         this.oreID = OreDictionary.getOreID(oreName);
      }
   }

   public boolean isItemEqual(ComparableItemStack other) {
      return other != null && (this.oreID != -1 && this.oreID == other.oreID || this.item.equals(other.getItem()) && this.metadata == other.metadata);
   }

   public boolean isStackEqual(ComparableItemStack other) {
      return this.isItemEqual(other) && this.stackSize == other.stackSize;
   }

   public boolean isStackValid() {
      return this.item != null;
   }

   @Deprecated
   public Item getItem() {
      return this.item;
   }

   public ItemStack toItemStack() {
      return this.isStackValid() ? new ItemStack(this.item, this.stackSize, this.metadata) : null;
   }

   public ComparableItemStack clone() {
      return new ComparableItemStack(this);
   }

   @Override
   public int hashCode() {
      return this.oreID != -1 ? this.oreID : this.metadata | Item.getIdFromItem(this.item) << 16;
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof ComparableItemStack && this.isItemEqual((ComparableItemStack)o);
   }
}
