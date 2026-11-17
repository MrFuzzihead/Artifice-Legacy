package com.mrfuzzihead.artifice.util;

import java.util.HashSet;
import java.util.Set;

import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link BlockCoord}, the mutable coordinate used by all of the mod's blob worldgen, the scaffold support
 * check and the sledge/sickle area-of-effect code.
 *
 * <p>
 * The mutating-API semantics matter: {@code offset}/{@code add}/{@code sub} modify {@code this} and return it, and
 * {@code getAdjacent()}/{@code getNearby()}/{@code getRadiusBlocks()} allocate fresh coordinates. Worldgen relies
 * on that distinction; a "helpful" refactor to immutable coordinates would silently break every caller.
 * </p>
 */
public class BlockCoordTest {

    @Test
    public void constructorFormsAreEquivalent() {
        Assert.assertEquals(new BlockCoord(1, 2, 3), new BlockCoord(new int[] { 1, 2, 3 }));
        Assert.assertEquals(new BlockCoord(0, 0, 0), new BlockCoord());
    }

    @Test
    public void offsetUsesForgeDirectionOrdinals() {
        Assert.assertEquals(new BlockCoord(0, 1, 0), new BlockCoord().offset(ForgeDirection.UP));
        Assert.assertEquals(new BlockCoord(0, -1, 0), new BlockCoord().offset(ForgeDirection.DOWN));
        Assert.assertEquals(new BlockCoord(0, 0, -1), new BlockCoord().offset(ForgeDirection.NORTH));
        Assert.assertEquals(new BlockCoord(0, 0, 1), new BlockCoord().offset(ForgeDirection.SOUTH));
        Assert.assertEquals(new BlockCoord(-1, 0, 0), new BlockCoord().offset(ForgeDirection.WEST));
        Assert.assertEquals(new BlockCoord(1, 0, 0), new BlockCoord().offset(ForgeDirection.EAST));
    }

    @Test
    public void offsetScalesByAmountInBothDirections() {
        Assert.assertEquals(new BlockCoord(0, 9, 0), new BlockCoord().offset(ForgeDirection.UP, 9));
        Assert.assertEquals(new BlockCoord(0, -9, 0), new BlockCoord().offset(ForgeDirection.DOWN, 9));
        Assert.assertEquals(
            new BlockCoord(0, 0, 0),
            new BlockCoord().offset(ForgeDirection.UP, 7)
                .offset(ForgeDirection.DOWN, 7));
    }

    @Test
    public void arithmeticMutatesInPlace() {
        BlockCoord coord = new BlockCoord(1, 2, 3);

        BlockCoord returned = coord.add(1, 1, 1);
        Assert.assertSame("add() is a mutating builder", coord, returned);
        Assert.assertEquals(new BlockCoord(2, 3, 4), coord);

        coord.sub(new BlockCoord(1, 1, 1));
        Assert.assertEquals(new BlockCoord(1, 2, 3), coord);
    }

    @Test
    public void copyIsIndependent() {
        BlockCoord original = new BlockCoord(1, 2, 3);
        BlockCoord copy = original.copy();
        copy.add(10, 10, 10);

        Assert.assertEquals(new BlockCoord(1, 2, 3), original);
        Assert.assertEquals(new BlockCoord(11, 12, 13), copy);
    }

    @Test
    public void adjacentReturnsSixDistinctNeighbours() {
        BlockCoord[] adjacent = new BlockCoord(5, 64, 5).getAdjacent();
        Set<BlockCoord> unique = new HashSet<>();

        Assert.assertEquals(6, adjacent.length);
        for (BlockCoord coord : adjacent) {
            Assert.assertEquals(1.0F, new BlockCoord(5, 64, 5).getDistance(coord), 0.0F);
            Assert.assertTrue("duplicate neighbour " + coord, unique.add(coord));
        }
    }

    @Test
    public void nearbyReturnsTheFullTwentySixBlockCube() {
        Assert.assertEquals(
            26,
            new BlockCoord().getNearby()
                .size());
    }

    @Test
    public void radiusBlocksCoversTheFullCube() {
        // The pre-allocation inside getRadiusBlocks is (2r)^3, which is wrong, but the list it returns is the
        // (2r+1)^3 cube. This pins the contract that callers actually rely on.
        Assert.assertEquals(
            27,
            new BlockCoord().getRadiusBlocks(1)
                .size());
        Assert.assertEquals(
            125,
            new BlockCoord().getRadiusBlocks(2)
                .size());
    }

    @Test
    public void distanceIsEuclidean() {
        Assert.assertEquals(0.0F, new BlockCoord(1, 1, 1).getDistance(new BlockCoord(1, 1, 1)), 0.0F);
        Assert.assertEquals(1.0F, new BlockCoord(0, 0, 0).getDistance(new BlockCoord(1, 0, 0)), 0.0F);
        Assert.assertEquals(5.0F, new BlockCoord(0, 0, 0).getDistance(new BlockCoord(3, 4, 0)), 0.0001F);
        Assert.assertEquals(1.4142135F, new BlockCoord(0, 0, 0).get2dDistance(1, 1), 0.0001F);
    }

    @Test
    public void chunkCoordinatesUseArithmeticShift() {
        Assert.assertEquals(0, new BlockCoord(0, 0, 0).getChunkX());
        Assert.assertEquals(0, new BlockCoord(15, 0, 15).getChunkZ());
        Assert.assertEquals(1, new BlockCoord(16, 0, 16).getChunkX());
        // Arithmetic shift, so -1 stays in chunk -1 (an integer division would return 0).
        Assert.assertEquals(-1, new BlockCoord(-1, 0, -1).getChunkX());
        Assert.assertEquals(-1, new BlockCoord(-16, 0, -16).getChunkZ());
    }

    @Test
    public void boundHeightClampsToTheWorld() {
        BlockCoord low = new BlockCoord(0, -40, 0);
        low.boundHeight();
        Assert.assertEquals(0, low.y);

        BlockCoord high = new BlockCoord(0, 400, 0);
        high.boundHeight();
        Assert.assertEquals(255, high.y);
    }

    @Test
    public void compareToOrdersByXThenYThenZ() {
        Assert.assertTrue(new BlockCoord(0, 99, 99).compareTo(new BlockCoord(1, 0, 0)) < 0);
        Assert.assertTrue(new BlockCoord(1, 0, 0).compareTo(new BlockCoord(1, 1, 0)) < 0);
        Assert.assertTrue(new BlockCoord(1, 1, 0).compareTo(new BlockCoord(1, 1, 1)) < 0);
        Assert.assertEquals(0, new BlockCoord(1, 1, 1).compareTo(new BlockCoord(1, 1, 1)));
    }

    @Test
    public void hashAndEqualsAgree() {
        Set<BlockCoord> set = new HashSet<>();
        set.add(new BlockCoord(1, 2, 3));

        Assert.assertTrue(set.contains(new BlockCoord(1, 2, 3)));
        Assert.assertFalse(set.contains(new BlockCoord(3, 2, 1)));
        Assert.assertEquals(new BlockCoord(0, 0, 0).hashCode(), new BlockCoord(0, 0, 0).hashCode());
    }

    @Test
    public void intArrayRoundTrips() {
        Assert.assertArrayEquals(new int[] { 7, 8, 9 }, new BlockCoord(7, 8, 9).intArray());
    }

    @Test
    public void isZeroOnlyForTheOrigin() {
        Assert.assertTrue(new BlockCoord().isZero());
        Assert.assertFalse(new BlockCoord(1, 0, 0).isZero());
    }
}
