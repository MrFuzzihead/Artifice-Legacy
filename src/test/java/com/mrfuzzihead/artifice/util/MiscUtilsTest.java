package com.mrfuzzihead.artifice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.mrfuzzihead.artifice.tile.IHeatReceiver;

import cofh.api.energy.IEnergyReceiver;

/**
 * Covers the two pure helpers in {@link MiscUtils} that the heating coil and the volcano generator lean on.
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

    @Test
    public void randWithin50NeverReturnsNonPositive() {
        Random rand = new Random(1234L);

        for (int input = -5; input < 200; input++) {
            for (int i = 0; i < 20; i++) {
                int value = MiscUtils.randWithin50(rand, input);
                Assert.assertTrue("randWithin50(" + input + ") returned " + value, value > 0);
            }
        }
    }

    @Test
    public void randWithin50IsAtLeastOneForDegenerateInput() {
        Random rand = new Random(1234L);
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, 0));
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, 1));
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, -100));
    }

    @Test
    public void randWithin50StaysWithinTheDocumentedBand() {
        Random rand = new Random(99L);

        for (int input = 2; input < 500; input++) {
            int half = input / 2;
            int min = input - (half - 1);
            int max = input + (half - 1);

            for (int i = 0; i < 10; i++) {
                int value = MiscUtils.randWithin50(rand, input);
                Assert.assertTrue(
                    "randWithin50(" + input + ")=" + value + " outside [" + min + ", " + max + "]",
                    value >= min && value <= max);
            }
        }
    }

    @Test
    public void randWithin50IsDeterministicForASeed() {
        Assert.assertEquals(MiscUtils.randWithin50(new Random(42L), 100), MiscUtils.randWithin50(new Random(42L), 100));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Neighbour iteration and the flood fill
    //
    // The flood-fill tests pin a real defect (CODE_REVIEW 5.5): the walk expanded from the origin instead of the
    // tile it had just popped, so it was one block deep - "expected 4, got 2" against the old code.
    //
    // The neighbour-iteration tests do NOT pin a defect. The double index in CODE_REVIEW 5.4 turned out to be a
    // no-op (composing a permutation with itself is still a permutation, so every side was visited exactly once),
    // and these properties hold on the old code too. They stay because they describe the contract the heating coil
    // and the nuclear battery need, and they will catch a real neighbour-lookup regression later.
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
    public void getHeatReceiverNeighborReturnsNullWhenNothingIsAdjacent() {
        World world = Mockito.mock(World.class);
        Assert.assertNull(MiscUtils.getHeatReceiverNeighbor(world, 0, 0, 0));
    }

    @Test
    public void getAdjoinedTileEntitiesWalksTheWholeConnectedRun() {
        // A 1x1x4 line of furnaces. Depth 1 (the old behaviour) returns two of them: the origin and one neighbour.
        World world = Mockito.mock(World.class);
        Mockito.when(world.blockExists(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(true);

        List<TileEntityFurnace> furnaces = new ArrayList<>();
        for (int y = 0; y < 4; y++) {
            TileEntityFurnace furnace = new TileEntityFurnace();
            furnace.xCoord = 0;
            furnace.yCoord = y;
            furnace.zCoord = 0;
            furnaces.add(furnace);
            Mockito.when(world.getTileEntity(0, y, 0))
                .thenReturn(furnace);
        }

        List<TileEntityFurnace> adjoined = MiscUtils.getAdjoinedTileEntities(TileEntityFurnace.class, world, 0, 0, 0);

        Assert.assertNotNull(adjoined);
        Assert.assertEquals("expected the whole four-tile run", 4, adjoined.size());
        Assert.assertTrue(adjoined.containsAll(furnaces));
    }

    @Test
    public void getAdjoinedTileEntitiesDoesNotLeaveTheClass() {
        World world = Mockito.mock(World.class);
        Mockito.when(world.blockExists(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt()))
            .thenReturn(true);

        TileEntityFurnace furnace = new TileEntityFurnace();
        furnace.yCoord = 1;
        TileEntity chest = new TileEntity();
        chest.yCoord = 2;

        Mockito.when(world.getTileEntity(0, 0, 0))
            .thenReturn(furnace);
        Mockito.when(world.getTileEntity(0, 1, 0))
            .thenReturn(chest);

        List<TileEntityFurnace> adjoined = MiscUtils.getAdjoinedTileEntities(TileEntityFurnace.class, world, 0, 0, 0);

        Assert.assertNotNull(adjoined);
        Assert.assertEquals("a non-furnace must not be collected", 1, adjoined.size());
        Assert.assertSame(furnace, adjoined.get(0));
    }

    @Test
    public void getAdjoinedTileEntitiesReturnsNullOffAnyTileEntity() {
        World world = Mockito.mock(World.class);
        Assert.assertNull(MiscUtils.getAdjoinedTileEntities(TileEntityFurnace.class, world, 10, 10, 10));
    }
}
