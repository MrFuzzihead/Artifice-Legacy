package com.mrfuzzihead.artifice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.mrfuzzihead.artifice.tile.IHeatReceiver;

import cofh.api.energy.IEnergyReceiver;

/**
 * Covers the neighbour helpers in {@link MiscUtils} that the heating coil and the nuclear battery lean on.
 */
public class MiscUtilsTest {

    @Test
    public void shuffleArrayKeepsExactlyTheOriginalElements() {
        Integer[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        for (int run = 0; run < 200; run++) {
            Integer[] shuffled = original.clone();
            MiscUtils.shuffleArray(shuffled);

            Assert.assertEquals("shuffle changed the element count", original.length, shuffled.length);
            Integer[] sortedOriginal = original.clone();
            Integer[] sortedShuffled = shuffled.clone();
            Arrays.sort(sortedOriginal);
            Arrays.sort(sortedShuffled);
            Assert.assertArrayEquals("shuffle lost or duplicated an element", sortedOriginal, sortedShuffled);
        }
    }

    @Test
    public void shuffleArrayToleratesEmptyAndSingleElementArrays() {
        Integer[] empty = {};
        MiscUtils.shuffleArray(empty);
        Assert.assertEquals(0, empty.length);

        Integer[] single = { 7 };
        MiscUtils.shuffleArray(single);
        Assert.assertArrayEquals(new Integer[] { 7 }, single);
    }

    @Test
    public void shuffleArrayActuallyMovesElements() {
        // Probability of the identity permutation in 200 draws is (1/10!)^200; this cannot flake.
        boolean moved = false;
        Integer[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        for (int run = 0; run < 200 && !moved; run++) {
            Integer[] shuffled = original.clone();
            MiscUtils.shuffleArray(shuffled);
            moved = !Arrays.equals(original, shuffled);
        }

        Assert.assertTrue("shuffleArray never changed the order in 200 runs", moved);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Neighbour iteration
    //
    // These tests do NOT pin a defect. The double index in CODE_REVIEW 5.4 turned out to be a no-op (composing a
    // permutation with itself is still a permutation, so every side was visited exactly once), and these properties
    // hold on the old code too. They stay because they describe the contract the heating coil and the nuclear
    // battery need, and they will catch a real neighbour-lookup regression later.
    // ---------------------------------------------------------------------------------------------------------

    /** Records which side of the machine each transfer arrived on. */
    private static final class RecordingReceiver extends TileEntity implements IEnergyReceiver {

        private final List<ForgeDirection> sides = new ArrayList<>();
        private int stored;

        @Override
        public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
            int accepted = Math.min(maxReceive, 60 - this.stored);
            if (!simulate) {
                this.sides.add(from);
                this.stored += accepted;
            }
            return accepted;
        }

        @Override
        public int getEnergyStored(ForgeDirection from) {
            return this.stored;
        }

        @Override
        public int getMaxEnergyStored(ForgeDirection from) {
            return 60;
        }

        @Override
        public boolean canConnectEnergy(ForgeDirection from) {
            return true;
        }
    }

    @Test
    public void provideEnergyToNeighborsVisitsEverySideExactlyOnce() {
        for (int run = 0; run < 50; run++) {
            RecordingReceiver receiver = new RecordingReceiver();
            World world = Mockito.mock(World.class);
            Mockito.when(world.getTileEntity(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
                .thenReturn(receiver);

            int left = MiscUtils.provideEnergyToNeighbors(1000, world, 0, 0, 0);

            Set<ForgeDirection> distinct = new HashSet<>(receiver.sides);
            Assert.assertEquals(
                "run " + run + " visited " + receiver.sides + " - expected all six sides",
                6,
                distinct.size());
            Assert.assertEquals("run " + run + " visited a side twice", 6, receiver.sides.size());
            Assert.assertEquals("the receiver accepts 60, so 1000 should leave 940", 940, left);
        }
    }

    @Test
    public void provideEnergyToNeighborsSkipsNonReceiversButStillVisitsThem() {
        World world = Mockito.mock(World.class);
        Mockito.when(world.getTileEntity(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(null);

        Assert.assertEquals(500, MiscUtils.provideEnergyToNeighbors(500, world, 0, 0, 0));
    }

    private static final class StubHeatReceiver extends TileEntity implements IHeatReceiver {

        @Override
        public int receiveHeat(int amount) {
            return amount;
        }

        @Override
        public int getHeat() {
            return 0;
        }

        @Override
        public int getMaxHeat() {
            return 1000;
        }
    }

    @Test
    public void getHeatReceiverNeighborFindsAReceiverOnAnySide() {
        // One receiver on one specific side, 200 draws: this fails if any side ever drops out of the lookup.
        for (int run = 0; run < 200; run++) {
            StubHeatReceiver receiver = new StubHeatReceiver();
            World world = Mockito.mock(World.class);
            Mockito.when(world.getTileEntity(0, 1, 0))
                .thenReturn(receiver);

            Assert.assertNotNull(
                "run " + run + " missed the receiver above the origin",
                MiscUtils.getHeatReceiverNeighbor(world, 0, 0, 0));
        }
    }

    @Test
    public void getHeatReceiverNeighborIsFairBetweenTwoAdjacentReceivers() {
        // The heating coil hands its whole tick's heat to whichever receiver this returns, so "which side wins" has
        // to be uniform. A rotation of a fixed side order would not be: with candidates on sides 0 and 1, a rotation
        // from a uniform start visits side 0 first in 5 of 6 cases, and a coil between two furnaces would feed one of
        // them five times out of six (CODE_REVIEW §6).
        StubHeatReceiver onX = new StubHeatReceiver();
        StubHeatReceiver onY = new StubHeatReceiver();

        World world = Mockito.mock(World.class);
        Mockito.when(world.getTileEntity(1, 0, 0))
            .thenReturn(onX);
        Mockito.when(world.getTileEntity(0, 1, 0))
            .thenReturn(onY);

        int runs = 6000;
        int first = 0;
        for (int run = 0; run < runs; run++) {
            if (MiscUtils.getHeatReceiverNeighbor(world, 0, 0, 0) == onX) {
                first++;
            }
        }

        // 6000 draws at p=1/2 has a standard deviation of 39, so anything outside 2700..3300 is a broken order, not
        // bad luck.
        Assert.assertTrue("side 0 was picked " + first + " of " + runs + " times", first > 2700 && first < 3300);
    }

    @Test
    public void getHeatReceiverNeighborReturnsNullWhenNothingIsAdjacent() {
        World world = Mockito.mock(World.class);
        Assert.assertNull(MiscUtils.getHeatReceiverNeighbor(world, 0, 0, 0));
    }
}
