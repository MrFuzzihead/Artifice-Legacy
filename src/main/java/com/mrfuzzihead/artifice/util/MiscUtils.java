package com.mrfuzzihead.artifice.util;

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

    /**
     * Own generator rather than {@code Math.random()}: that is one shared {@link Random} behind a lock, and the two
     * neighbour helpers below run on the tile-entity tick path (CODE_REVIEW §6).
     */
    private static final Random SIDE_RANDOM = new Random();

    public static <T> void shuffleArray(T[] array) {
        for (int i = array.length; i > 1; i--) {
            T temp = array[i - 1];
            int randIx = SIDE_RANDOM.nextInt(i);
            array[i - 1] = array[randIx];
            array[randIx] = temp;
        }
    }

    /**
     * A fair permutation of the six sides, in a primitive array.
     *
     * <p>
     * A rotation of a static {@code int[]} would avoid even this one allocation, and that was the suggestion in
     * CODE_REVIEW §6 - but a rotation is not a fair order for "which side wins". With a candidate on sides 0 and 1,
     * a rotation from a uniform start visits side 0 first in 5 of 6 cases. Both callers below hand something valuable
     * to the first side they find (the heating coil's heat, the battery's energy), so a coil between two furnaces
     * would feed one of them five times out of six and let the other starve.
     * </p>
     *
     * <p>
     * What is left of the old cost is one six-element {@code int[]}: no {@code Integer[]}, no boxing, no
     * {@code Math.random()} lock. Against six {@code world.getTileEntity} calls per invocation, it is noise.
     * </p>
     */
    private static int[] shuffledSides() {
        int[] sides = { 0, 1, 2, 3, 4, 5 };
        for (int i = sides.length - 1; i > 0; i--) {
            int j = SIDE_RANDOM.nextInt(i + 1);
            int swap = sides[i];
            sides[i] = sides[j];
            sides[j] = swap;
        }
        return sides;
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
        // One index, not two. The old form - for (int i : sides) { VALID_DIRECTIONS[sides[i]] } - composed the
        // shuffled array with itself, which is still a permutation, so it visited every side exactly once and no
        // behaviour changed (CODE_REVIEW 5.4 retraction). This is readability, not a fix.
        for (int side : shuffledSides()) {
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
        for (int side : shuffledSides()) {
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
}
