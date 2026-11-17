package shukaro.artifice.util;

import cofh.api.energy.IEnergyReceiver;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import shukaro.artifice.tile.IHeatReceiver;
import shukaro.artifice.tile.TileFurnaceWrapper;

public class MiscUtils {
   public static <T> void shuffleArray(T[] array) {
      for (int i = array.length; i > 1; i--) {
         T temp = array[i - 1];
         int randIx = (int)(Math.random() * i);
         array[i - 1] = array[randIx];
         array[randIx] = temp;
      }
   }

   public static int randWithin50(Random rand, int input) {
      return input > 1 ? input + rand.nextInt(input / 2) - rand.nextInt(input / 2) : 1;
   }

   public static void dropStack(World world, int x, int y, int z, ItemStack itemstack) {
      while (itemstack.stackSize > 0) {
         float xOffset = world.rand.nextFloat() * 0.8F + 0.1F;
         float yOffset = world.rand.nextFloat() * 0.8F + 0.1F;
         float zOffset = world.rand.nextFloat() * 0.8F + 0.1F;
         int amountToDrop = Math.min(world.rand.nextInt(21) + 10, itemstack.stackSize);
         EntityItem entityitem = new EntityItem(world, x + xOffset, y + yOffset, z + zOffset, itemstack.splitStack(amountToDrop));
         float motionMultiplier = 0.05F;
         entityitem.motionX = (float)world.rand.nextGaussian() * motionMultiplier;
         entityitem.motionY = (float)world.rand.nextGaussian() * motionMultiplier + 0.2F;
         entityitem.motionZ = (float)world.rand.nextGaussian() * motionMultiplier;
         world.spawnEntityInWorld(entityitem);
      }
   }

   public static void addChatMessage(EntityPlayer player, String message) {
      player.addChatMessage(new ChatComponentText(message));
   }

   public static IHeatReceiver getHeatReceiverNeighbor(World world, int x, int y, int z) {
      Integer[] sides = new Integer[]{0, 1, 2, 3, 4, 5};
      shuffleArray(sides);
      Integer[] arr$ = sides;
      int len$ = sides.length;

      for (int i$ = 0; i$ < len$; i$++) {
         int i = arr$[i$];
         ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[sides[i]];
         TileEntity te = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
         if (te instanceof TileEntityFurnace) {
            return new TileFurnaceWrapper((TileEntityFurnace)te);
         }

         if (te instanceof IHeatReceiver) {
            return (IHeatReceiver)te;
         }
      }

      return null;
   }

   public static int provideEnergyToNeighbors(int amount, World world, int x, int y, int z) {
      Integer[] sides = new Integer[]{0, 1, 2, 3, 4, 5};
      shuffleArray(sides);
      Integer[] arr$ = sides;
      int len$ = sides.length;

      for (int i$ = 0; i$ < len$; i$++) {
         int i = arr$[i$];
         ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[sides[i]];
         TileEntity te = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
         if (te instanceof IEnergyReceiver) {
            IEnergyReceiver rec = (IEnergyReceiver)te;
            if (rec.canConnectEnergy(dir.getOpposite())) {
               amount -= rec.receiveEnergy(dir.getOpposite(), amount, false);
            }
         }
      }

      return amount;
   }

   public static <T extends TileEntity> List<T> getAdjoinedTileEntities(Class<T> teClass, World world, int x, int y, int z) {
      TileEntity te = world.getTileEntity(x, y, z);
      if (te == null) {
         return null;
      } else {
         List<TileEntity> teList = new ArrayList<>();
         List<BlockCoord> done = new ArrayList<>();
         List<T> adjoined = new ArrayList<>();
         teList.add(te);

         while (!teList.isEmpty()) {
            te = teList.remove(0);
            done.add(new BlockCoord(te.xCoord, te.yCoord, te.zCoord));
            if (teClass.isInstance(te)) {
               adjoined.add((T)te);

               for (int i = 0; i < 6; i++) {
                  ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
                  if (world.blockExists(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {
                     te = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
                     if (te != null && !done.contains(new BlockCoord(te.xCoord, te.yCoord, te.zCoord))) {
                        teList.add(te);
                     }
                  }
               }
            }
         }

         return adjoined;
      }
   }
}
