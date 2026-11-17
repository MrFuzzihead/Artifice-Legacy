package com.mrfuzzihead.artifice.util;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link ChunkCoord}, the chunk key used by the suspendable worldgen queues.
 *
 * <p>
 * The negative-coordinate behaviour is the interesting part: the class uses {@code >> 4} rather than division,
 * which is what keeps chunk {@code -1} distinct from chunk {@code 0}. Several worldgen bugs live in this area
 * (see CODE_REVIEW.md section 6).
 * </p>
 */
public class ChunkCoordTest {

    @Test
    public void containsUsesChunkGranularity() {
        ChunkCoord chunk = new ChunkCoord(0, 0);

        Assert.assertTrue(chunk.contains(0, 0));
        Assert.assertTrue(chunk.contains(15, 15));
        Assert.assertFalse(chunk.contains(16, 0));
        Assert.assertFalse(chunk.contains(0, 16));
        Assert.assertFalse(chunk.contains(-1, -1));
    }

    @Test
    public void containsHandlesNegativeCoordinates() {
        ChunkCoord chunk = new ChunkCoord(-1, -1);

        Assert.assertTrue(chunk.contains(-1, -1));
        Assert.assertTrue(chunk.contains(-16, -16));
        Assert.assertFalse(chunk.contains(0, 0));
        Assert.assertFalse(chunk.contains(-17, -16));
    }

    @Test
    public void blockCoordConstructorDerivesTheChunk() {
        Assert.assertEquals(new ChunkCoord(1, -2), new ChunkCoord(new BlockCoord(23, 64, -17)));
    }

    @Test
    public void centerIsTheMiddleOfTheChunk() {
        Assert.assertEquals(8, new ChunkCoord(0, 0).getCenterX());
        Assert.assertEquals(8, new ChunkCoord(0, 0).getCenterZ());
        Assert.assertEquals(-8, new ChunkCoord(-1, -1).getCenterX());
        Assert.assertEquals(-8, new ChunkCoord(-1, -1).getCenterZ());
    }

    @Test
    public void equalityAndHashingAgree() {
        Set<ChunkCoord> set = new HashSet<>();
        set.add(new ChunkCoord(3, 4));

        Assert.assertTrue(set.contains(new ChunkCoord(3, 4)));
        Assert.assertFalse(set.contains(new ChunkCoord(4, 3)));
        Assert.assertEquals(new ChunkCoord(3, 4), new ChunkCoord(3, 4));
        Assert.assertNotEquals(new ChunkCoord(3, 4), new ChunkCoord(3, 5));
        Assert.assertNotEquals(new ChunkCoord(3, 4), null);
        Assert.assertNotEquals(new ChunkCoord(3, 4), "3,4");
    }

    @Test
    public void compareToOrdersXThenZ() {
        Assert.assertTrue(new ChunkCoord(0, 99).compareTo(new ChunkCoord(1, 0)) < 0);
        Assert.assertTrue(new ChunkCoord(1, 0).compareTo(new ChunkCoord(1, 1)) < 0);
        Assert.assertEquals(0, new ChunkCoord(1, 1).compareTo(new ChunkCoord(1, 1)));
    }

    @Test
    public void toStringIsTheCoordinatePair() {
        Assert.assertEquals("[3, 4]", new ChunkCoord(3, 4).toString());
    }
}
