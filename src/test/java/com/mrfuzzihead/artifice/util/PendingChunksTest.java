package com.mrfuzzihead.artifice.util;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

/**
 * Covers {@link PendingChunks}, the "is this generator finished?" rule the suspendable worldgen generators report
 * through (CODE_REVIEW 5.6).
 *
 * <p>
 * The generators used to expose their chunk map's key set directly, and keys were added but never removed, so the
 * size never reached zero: every blob ever generated stayed in {@code ArtificeTickHandler}'s queue for the life of
 * the save and cost a scan every five ticks.
 * </p>
 */
public class PendingChunksTest {

    private static BlockCoord block(int x, int y, int z) {
        BlockCoord coord = new BlockCoord();
        coord.set(x, y, z);
        return coord;
    }

    private static ChunkCoord chunk(int x, int z) {
        return new ChunkCoord(x, z);
    }

    private static TMap<ChunkCoord, ArrayList<BlockCoord>> map() {
        return new THashMap<>();
    }

    private static ArrayList<BlockCoord> list(BlockCoord... blocks) {
        ArrayList<BlockCoord> list = new ArrayList<>();
        for (BlockCoord block : blocks) {
            list.add(block);
        }
        return list;
    }

    @Test
    public void anEmptyMapHasNoWork() {
        Assert.assertFalse(PendingChunks.hasWork(map()));
    }

    @Test
    public void aChunkWithPendingBlocksIsWork() {
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        map.put(chunk(0, 0), list(block(1, 2, 3)));

        Assert.assertTrue(PendingChunks.hasWork(map));
        Assert.assertEquals("a chunk with work must not be pruned", 1, map.size());
    }

    @Test
    public void aDrainedChunkStopsCountingAsWork() {
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        ArrayList<BlockCoord> blocks = list(block(1, 2, 3));
        map.put(chunk(0, 0), blocks);

        Assert.assertTrue(PendingChunks.hasWork(map));
        blocks.clear();

        Assert.assertFalse("a drained chunk must not keep the generator queued", PendingChunks.hasWork(map));
        Assert.assertTrue("the drained entry must be dropped, not just ignored", map.isEmpty());
    }

    @Test
    public void onlyTheDrainedChunksAreDropped() {
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        map.put(chunk(0, 0), list(block(1, 2, 3)));
        map.put(chunk(1, 0), list());
        map.put(chunk(2, 0), list(block(4, 5, 6)));

        Assert.assertTrue(PendingChunks.hasWork(map));
        Assert.assertEquals(2, map.size());
        Assert.assertTrue(map.containsKey(chunk(0, 0)));
        Assert.assertFalse(map.containsKey(chunk(1, 0)));
        Assert.assertTrue(map.containsKey(chunk(2, 0)));
    }

    @Test
    public void aNullValueIsTreatedAsDrained() {
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        map.put(chunk(0, 0), null);

        Assert.assertFalse(PendingChunks.hasWork(map));
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void repeatedCallsAreIdempotent() {
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        map.put(chunk(0, 0), list(block(1, 2, 3)));

        for (int i = 0; i < 5; i++) {
            Assert.assertTrue(PendingChunks.hasWork(map));
        }
        Assert.assertEquals(1, map.size());
    }

    @Test
    public void theKeySetShrinksAsChunksDrain() {
        // The exact contract the tick handler relies on: getChunksToGen() must get smaller, not just stop growing.
        TMap<ChunkCoord, ArrayList<BlockCoord>> map = map();
        Set<ChunkCoord> keys = new LinkedHashSet<>();
        for (int i = 0; i < 3; i++) {
            ChunkCoord chunk = chunk(i, 0);
            keys.add(chunk);
            map.put(chunk, list(block(i, 1, 0)));
        }

        Assert.assertEquals(
            3,
            map.keySet()
                .size());
        for (ChunkCoord chunk : keys) {
            map.get(chunk)
                .clear();
        }

        Assert.assertFalse(PendingChunks.hasWork(map));
        Assert.assertEquals(
            0,
            map.keySet()
                .size());
    }
}
