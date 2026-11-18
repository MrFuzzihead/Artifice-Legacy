package com.mrfuzzihead.artifice.event;

import java.util.ArrayDeque;

import net.minecraft.world.World;

import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.world.IDelayedGen;
import com.mrfuzzihead.artifice.world.ISuspendableGen;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

/**
 * Runs the worldgen generators that were suspended rather than completed inside the cofh feature callback.
 *
 * <p>
 * One chunk of the head generator per five world ticks, one delayed generator per ten. The "is it finished?"
 * answer comes from {@link ISuspendableGen#doGeneration}, which the generators derive from their pending-block map
 * via {@link com.mrfuzzihead.artifice.util.PendingChunks#hasWork} - it used to be the size of a key set that never
 * shrank, so nothing ever left the queue (CODE_REVIEW 5.6).
 * </p>
 */
public class ArtificeTickHandler {

    public static TMap<Integer, ArrayDeque<ISuspendableGen>> suspendedGenerators = new THashMap<>();
    public static TMap<Integer, ArrayDeque<IDelayedGen>> delayedGenerators = new THashMap<>();

    public static void queueGeneration(int dimID, ISuspendableGen gen) {
        if (gen.getChunksToGen()
            .size() != 0) {
            if (!suspendedGenerators.containsKey(dimID)) {
                suspendedGenerators.put(dimID, new ArrayDeque<ISuspendableGen>());
            }

            suspendedGenerators.get(dimID)
                .add(gen);
        }
    }

    public static void queueGeneration(int dimID, IDelayedGen gen) {
        if (!delayedGenerators.containsKey(dimID)) {
            delayedGenerators.put(dimID, new ArrayDeque<IDelayedGen>());
        }

        delayedGenerators.get(dimID)
            .add(gen);
    }

    @SubscribeEvent
    public void worldTickEnd(WorldTickEvent event) {
        if (event.phase != Phase.END || event.side != Side.SERVER) {
            return;
        }

        World world = event.world;
        int dim = world.provider.dimensionId;
        long time = world.getTotalWorldTime();

        if (time % 5L == 0L) {
            tickSuspendableGenerators(suspendedGenerators.get(dim));
        }

        if (time % 10L == 0L) {
            tickDelayedGenerators(delayedGenerators.get(dim));
        }
    }

    /**
     * Advances one chunk of the generator at the head of the queue, and drops it once it reports that it has no
     * work left.
     *
     * <p>
     * The head is served until it finishes, which is the behaviour the queue was written for: generators are
     * queued as blobs are placed and each is expected to be short-lived. Nothing is removed from inside an
     * iterator over the generator's own chunk set any more - the generator says whether it is done.
     * </p>
     *
     * @return whether a chunk was generated
     */
    static boolean tickSuspendableGenerators(ArrayDeque<ISuspendableGen> queue) {
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        ISuspendableGen gen = queue.peek();
        if (gen == null) {
            queue.poll();
            return false;
        }

        ChunkCoord next = null;
        for (ChunkCoord chunk : gen.getChunksToGen()) {
            next = chunk;
            break;
        }

        if (next == null) {
            // Nothing queued to hand it: the generator is finished, whether it knows it or not.
            queue.remove(gen);
            return false;
        }

        if (!gen.doGeneration(next.chunkX, next.chunkZ)) {
            queue.remove(gen);
        }

        return true;
    }

    /**
     * @return whether a delayed generator ran
     */
    static boolean tickDelayedGenerators(ArrayDeque<IDelayedGen> queue) {
        if (queue == null || queue.isEmpty()) {
            return false;
        }

        IDelayedGen gen = queue.poll();
        if (gen == null) {
            return false;
        }

        gen.doGeneration();
        return true;
    }
}
