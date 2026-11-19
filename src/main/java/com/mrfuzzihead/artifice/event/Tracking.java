package com.mrfuzzihead.artifice.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.tile.TileEntityAttuned;

/**
 * Frequency bookkeeping for the attuned redstone pair.
 *
 * <p>
 * The attuned transmitter asks "what is the strongest receiver on my frequency?" once per tick, from
 * {@link TileEntityAttuned#getPower()}. That query used to answer by walking every loaded tile entity of every world -
 * in a large pack, tens of thousands of them - so N transmitters cost N full scans per tick, and
 * {@code BlockAttunedRedstone.onNeighborBlockChange} paid the same price on every redstone update (CODE_REVIEW §6).
 * </p>
 *
 * <p>
 * The tile entities now index themselves by frequency as they load and unload, so a query only sees the handful of
 * blocks that share its frequency. The index is not a cache of power values: it holds the tile entities themselves and
 * still reads their live {@code power} field, which is what the old scan did. That matters because receiver power is a
 * redstone input that can change at any time, and a cached maximum would go stale the moment a lever moved.
 * </p>
 *
 * <p>
 * The index is the whole point, so its cost is observable: {@link #visitsOfLastQuery()} reports how many tiles the
 * last query walked, which is how the game test asserts that a query costs O(tiles on that frequency) rather than
 * O(loaded tiles). It is a counter, not a cache - it never influences the answer.
 * </p>
 *
 * <p>
 * Entries are removed by three mechanisms, in order of how early they can fire: {@code invalidate()} on the tile
 * entity, {@link #forget(World)} on a world unload (a world that never ticks again would not reach
 * {@code invalidate()}), and a sweep of invalid entries during a query. Only the third is load-bearing for correctness
 * - the other two keep the lists small.
 * </p>
 */
public class Tracking {

    /** Every attuned tile entity, by frequency. Insertion-ordered so a query sees a stable order. */
    private static final Map<String, Set<TileEntityAttuned>> BY_FREQUENCY = new HashMap<>();

    /** Tiles walked by the last {@link #getHighestFrequencyPower(String)}; diagnostics for the game test. */
    private static int lastQueryVisits;

    /** The signal a transmitter publishes for {@code frequency}: the highest power of any receiver on it. */
    public static int getHighestFrequencyPower(String frequency) {
        lastQueryVisits = 0;
        if (frequency == null) {
            return 0;
        }

        Set<TileEntityAttuned> teas = BY_FREQUENCY.get(frequency);
        if (teas == null) {
            return 0;
        }

        int max = 0;
        for (Iterator<TileEntityAttuned> iterator = teas.iterator(); iterator.hasNext();) {
            TileEntityAttuned tea = iterator.next();
            lastQueryVisits++;
            if (tea.isInvalid()) {
                iterator.remove();
                continue;
            }

            // A receiver that has been replaced by something else keeps its tile entity only until the chunk is
            // saved, so this check is not redundant with the one above.
            World world = tea.getWorldObj();
            if (world != null
                && world.getBlock(tea.xCoord, tea.yCoord, tea.zCoord) != ArtificeBlocks.blockAttunedRedstoneReceiver) {
                continue;
            }

            if (tea.power > max) {
                max = tea.power;
            }
        }

        if (teas.isEmpty()) {
            BY_FREQUENCY.remove(frequency);
        }

        return max;
    }

    /** Re-runs the frequency's tiles, for a block that has just been placed, removed or wired differently. */
    public static void updateFrequency(String frequency) {
        if (frequency == null) {
            return;
        }

        Set<TileEntityAttuned> teas = BY_FREQUENCY.get(frequency);
        if (teas == null) {
            return;
        }

        // Snapshot: updateEntity() may itself reindex (it does not today, but a transmitter recomputing its power
        // calls back into this class, and an iterator over the live set would be a ConcurrentModificationException
        // waiting for the first such change).
        for (TileEntityAttuned tea : teas.toArray(new TileEntityAttuned[0])) {
            if (!tea.isInvalid()) {
                tea.updateEntity();
            }
        }
    }

    public static void track(TileEntityAttuned tea) {
        String frequency = tea.getFrequency();
        if (frequency == null) {
            return;
        }

        Set<TileEntityAttuned> teas = BY_FREQUENCY.get(frequency);
        if (teas == null) {
            teas = new LinkedHashSet<>();
            BY_FREQUENCY.put(frequency, teas);
        }
        teas.add(tea);
    }

    public static void untrack(TileEntityAttuned tea) {
        String frequency = tea.getFrequency();
        if (frequency == null) {
            return;
        }

        Set<TileEntityAttuned> teas = BY_FREQUENCY.get(frequency);
        if (teas == null) {
            return;
        }

        teas.remove(tea);
        if (teas.isEmpty()) {
            BY_FREQUENCY.remove(frequency);
        }
    }

    /** Moves an entry when its frequency changes; {@code previous} is the frequency it was filed under. */
    public static void reindex(TileEntityAttuned tea, String previous) {
        if (previous != null) {
            Set<TileEntityAttuned> old = BY_FREQUENCY.get(previous);
            if (old != null) {
                old.remove(tea);
                if (old.isEmpty()) {
                    BY_FREQUENCY.remove(previous);
                }
            }
        }
        track(tea);
    }

    /** Drops every entry belonging to a world that is going away. */
    public static void forget(World world) {
        for (Iterator<Set<TileEntityAttuned>> sets = BY_FREQUENCY.values()
            .iterator(); sets.hasNext();) {
            Set<TileEntityAttuned> teas = sets.next();
            for (Iterator<TileEntityAttuned> iterator = teas.iterator(); iterator.hasNext();) {
                TileEntityAttuned tea = iterator.next();
                if (tea.getWorldObj() == world || tea.isInvalid()) {
                    iterator.remove();
                }
            }
            if (teas.isEmpty()) {
                sets.remove();
            }
        }
    }

    /** How many tiles the last query walked, regardless of how many are loaded. For the cost assertion. */
    public static int visitsOfLastQuery() {
        return lastQueryVisits;
    }

    /** How many tiles are filed under a frequency. Package-private: this is for the tests and for debugging. */
    static int tracked(String frequency) {
        Set<TileEntityAttuned> teas = BY_FREQUENCY.get(frequency);
        return teas == null ? 0 : teas.size();
    }
}
