package com.mrfuzzihead.artifice.util;

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

import com.mrfuzzihead.artifice.tile.IHeatReceiver;
import com.mrfuzzihead.artifice.tile.TileFurnaceWrapper;

import cofh.api.energy.IEnergyReceiver;

public class MiscUtils {

    public static <T> void shuffleArray(T[] array) {
        for (int i = array.length; i > 1; i--) {
            T temp = array[i - 1];
            int randIx = (int) (Math.random() * i);
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
            EntityItem entityitem = new EntityItem(
                world,
                x + xOffset,
                y + yOffset,
                z + zOffset,
                itemstack.splitStack(amountToDrop));
            float motionMultiplier = 0.05F;
            entityitem.motionX = (float) world.rand.nextGaussian() * motionMultiplier;
            entityitem.motionY = (float) world.rand.nextGaussian() * motionMultiplier + 0.2F;
            entityitem.motionZ = (float) world.rand.nextGaussian() * motionMultiplier;
            world.spawnEntityInWorld(entityitem);
        }
    }

    public static void addChatMessage(EntityPlayer player, String message) {
        player.addChatMessage(new ChatComponentText(message));
    }

    public static IHeatReceiver getHeatReceiverNeighbor(World world, int x, int y, int z) {
        Integer[] sides = new Integer[] { 0, 1, 2, 3, 4, 5 };
        shuffleArray(sides);

        // One index, not two. The old form - for (int i : sides) { VALID_DIRECTIONS[sides[i]] } - composed the
        // shuffled array with itself, which is still a permutation, so it visited every side exactly once and no
        // behaviour changed (CODE_REVIEW 5.4 retraction). This is readability, not a fix.
        for (int side : sides) {
            ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
            TileEntity te = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            if (te instanceof TileEntityFurnace) {
                return new TileFurnaceWrapper((TileEntityFurnace) te);
            }

            if (te instanceof IHeatReceiver) {
                return (IHeatReceiver) te;
            }
        }

        return null;
    }

    public static int provideEnergyToNeighbors(int amount, World world, int x, int y, int z) {
        Integer[] sides = new Integer[] { 0, 1, 2, 3, 4, 5 };
        shuffleArray(sides);

        for (int side : sides) {
            ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
            TileEntity te = world.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
            if (te instanceof IEnergyReceiver) {
                IEnergyReceiver rec = (IEnergyReceiver) te;
                if (rec.canConnectEnergy(dir.getOpposite())) {
                    amount -= rec.receiveEnergy(dir.getOpposite(), amount, false);
                }
            }
        }

        return amount;
    }

    public static <T extends TileEntity> List<T> getAdjoinedTileEntities(Class<T> teClass, World world, int x, int y,
        int z) {
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
                    adjoined.add((T) te);

                    // Expand from the tile we just popped, not from the origin (CODE_REVIEW 5.5).
                    // Using x/y/z here meant the walk was one block deep: every neighbour of the origin was already
                    // in done, so the frontier never grew past the first ring.
                    final int cx = te.xCoord;
                    final int cy = te.yCoord;
                    final int cz = te.zCoord;
                    for (int i = 0; i < 6; i++) {
                        ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
                        int nx = cx + dir.offsetX;
                        int ny = cy + dir.offsetY;
                        int nz = cz + dir.offsetZ;
                        if (world.blockExists(nx, ny, nz)) {
                            TileEntity neighbour = world.getTileEntity(nx, ny, nz);
                            if (neighbour != null
                                && !done.contains(new BlockCoord(neighbour.xCoord, neighbour.yCoord, neighbour.zCoord))
                                && !teList.contains(neighbour)) {
                                teList.add(neighbour);
                            }
                        }
                    }
                }
            }

            return adjoined;
        }
    }
}
