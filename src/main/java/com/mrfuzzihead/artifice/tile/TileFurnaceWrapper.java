package shukaro.artifice.tile;

import net.minecraft.block.BlockFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import shukaro.artifice.ArtificeConfig;

public class TileFurnaceWrapper implements IHeatReceiver {
   private TileEntityFurnace furnace;

   public TileFurnaceWrapper(TileEntityFurnace furnace) {
      this.furnace = furnace;
   }

   public boolean canSmelt() {
      if (this.furnace.getStackInSlot(0) == null) {
         return false;
      } else {
         ItemStack output = FurnaceRecipes.smelting().getSmeltingResult(this.furnace.getStackInSlot(0));
         if (output == null) {
            return false;
         } else if (this.furnace.getStackInSlot(2) == null) {
            return true;
         } else if (!this.furnace.getStackInSlot(2).isItemEqual(output)) {
            return false;
         } else {
            int result = this.furnace.getStackInSlot(2).stackSize + output.stackSize;
            return result <= this.furnace.getInventoryStackLimit() && result <= output.getMaxStackSize();
         }
      }
   }

   @Override
   public int receiveHeat(int amount) {
      if (!this.canSmelt()) {
         return 0;
      } else {
         int received = Math.min(this.getMaxHeat() - this.getHeat(), amount);
         this.furnace.furnaceBurnTime = this.furnace.furnaceBurnTime + received / ArtificeConfig.RFperFurnaceHeat;
         this.furnace.furnaceCookTime = Math.min(this.furnace.furnaceCookTime, 199);
         BlockFurnace.updateFurnaceBlockState(
            this.furnace.furnaceCookTime > 0,
            this.furnace.getWorldObj(),
            this.furnace.xCoord,
            this.furnace.yCoord,
            this.furnace.zCoord
         );
         return received;
      }
   }

   @Override
   public int getHeat() {
      return this.furnace.furnaceBurnTime * ArtificeConfig.RFperFurnaceHeat;
   }

   @Override
   public int getMaxHeat() {
      return 1600;
   }
}
