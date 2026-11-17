package shukaro.artifice.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ComparableItemStackNBT extends ComparableItemStack {
   public NBTTagCompound tag;

   public ComparableItemStackNBT(ItemStack stack) {
      super(stack);
      if (stack != null && stack.stackTagCompound != null) {
         this.tag = (NBTTagCompound)stack.stackTagCompound.copy();
      }
   }

   @Override
   public boolean isStackEqual(ComparableItemStack other) {
      return super.isStackEqual(other) && this.isStackTagEqual((ComparableItemStackNBT)other);
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
         ret.stackTagCompound = (NBTTagCompound)this.tag.copy();
         return ret;
      }
   }
}
