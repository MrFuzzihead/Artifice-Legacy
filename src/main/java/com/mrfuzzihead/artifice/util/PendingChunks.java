package com.mrfuzzihead.artifice.util;

import java.util.ArrayList;
import java.util.Iterator;

import gnu.trove.map.TMap;

/**
 * Bookkeeping for the suspendable worldgen generators' per-chunk work lists.
 *
 * <p>
 * Each generator keeps a {@code TMap<ChunkCoord, ArrayList<BlockCoord>>}: the blocks still waiting to be replaced,
 * grouped by the chunk they are in. Entries are added as the blob grows and the lists are drained as blocks are
 * placed, but the map keys were never removed - so {@code getChunksToGen()} only ever grew, {@link
 * com.mrfuzzihead.artifice.event.ArtificeTickHandler} could never tell a finished generator from a suspended one,
 * and every generator ever queued stayed in the queue for the life of the save, costing a scan every five ticks
 * (CODE_REVIEW 5.6).
 * </p>
 *
 * <p>
 * {@link #hasWork} is the one place that decides "is this generator finished": drop the chunks whose list is empty,
 * then report whether anything is left. It is called from {@code doGeneration}'s return statements, so the value the
 * interface always promised now actually means something.
 * </p>
 */
public final class PendingChunks {

    private PendingChunks() {}

    /**
     * Drops the chunk entries whose pending list is empty.
     *
     * @return whether any work remains, i.e. whether the generator should stay queued
     */
    public static boolean hasWork(TMap<ChunkCoord, ArrayList<BlockCoord>> pending) {
        for (Iterator<ChunkCoord> it = pending.keySet()
            .iterator(); it.hasNext();) {
            ArrayList<BlockCoord> blocks = pending.get(it.next());
            if (blocks == null || blocks.isEmpty()) {
                it.remove();
            }
        }

        return !pending.isEmpty();
    }
}
