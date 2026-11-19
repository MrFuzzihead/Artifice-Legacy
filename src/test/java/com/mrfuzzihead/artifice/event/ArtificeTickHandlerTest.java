package com.mrfuzzihead.artifice.event;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.world.ISuspendableGen;

/**
 * Covers the queue sweep in {@link ArtificeTickHandler} (CODE_REVIEW 5.6).
 *
 * <p>
 * The sweep used to remove a generator only when {@code getChunksToGen().size() == 0}, and the generators never
 * removed a key from that map - so the condition was never true and every generator stayed queued forever. The
 * queue is now drained by the generator's own answer, so the tests use fakes that decide when they are done.
 * </p>
 */
public class ArtificeTickHandlerTest {

    /** A generator with a fixed number of chunks that reports done after {@code chunksBeforeDone} calls. */
    private static final class FakeGen implements ISuspendableGen {

        private final Set<ChunkCoord> chunks = new LinkedHashSet<>();
        private final List<ChunkCoord> generated = new ArrayList<>();
        private final int chunksBeforeDone;
        private boolean alwaysSuspended;

        private FakeGen(int chunkCount, int chunksBeforeDone) {
            for (int i = 0; i < chunkCount; i++) {
                this.chunks.add(new ChunkCoord(i, 0));
            }
            this.chunksBeforeDone = chunksBeforeDone;
        }

        @Override
        public Set<ChunkCoord> getChunksToGen() {
            return this.chunks;
        }

        @Override
        public boolean doGeneration(int chunkX, int chunkZ) {
            if (this.alwaysSuspended) {
                return true;
            }

            ChunkCoord chunk = new ChunkCoord(chunkX, chunkZ);
            this.generated.add(chunk);
            this.chunks.remove(chunk);
            return this.generated.size() < this.chunksBeforeDone;
        }
    }

    @Test
    public void aFinishedGeneratorLeavesTheQueue() {
        ArrayDeque<ISuspendableGen> queue = new ArrayDeque<>();
        FakeGen gen = new FakeGen(1, 1);
        queue.add(gen);

        Assert.assertTrue(ArtificeTickHandler.tickSuspendableGenerators(queue));
        Assert.assertTrue("a generator that reported done must be dequeued", queue.isEmpty());
        Assert.assertEquals(1, gen.generated.size());
    }

    @Test
    public void aSuspendedGeneratorStaysQueued() {
        ArrayDeque<ISuspendableGen> queue = new ArrayDeque<>();
        FakeGen gen = new FakeGen(3, 3);
        gen.alwaysSuspended = true;
        queue.add(gen);

        for (int tick = 0; tick < 5; tick++) {
            Assert.assertTrue(ArtificeTickHandler.tickSuspendableGenerators(queue));
            Assert.assertEquals("the generator still has work", 1, queue.size());
        }
    }

    @Test
    public void oneChunkIsGeneratedPerTick() {
        ArrayDeque<ISuspendableGen> queue = new ArrayDeque<>();
        FakeGen gen = new FakeGen(3, 3);
        queue.add(gen);

        ArtificeTickHandler.tickSuspendableGenerators(queue);
        Assert.assertEquals("one chunk per sweep, not the whole blob", 1, gen.generated.size());

        ArtificeTickHandler.tickSuspendableGenerators(queue);
        ArtificeTickHandler.tickSuspendableGenerators(queue);
        Assert.assertEquals(3, gen.generated.size());
        Assert.assertTrue(queue.isEmpty());
    }

    @Test
    public void aGeneratorWithNothingQueuedIsDroppedWithoutBeingRun() {
        ArrayDeque<ISuspendableGen> queue = new ArrayDeque<>();
        FakeGen empty = new FakeGen(0, 1);
        queue.add(empty);

        Assert.assertFalse(ArtificeTickHandler.tickSuspendableGenerators(queue));
        Assert.assertTrue("an empty generator must not linger", queue.isEmpty());
        Assert.assertTrue(empty.generated.isEmpty());
    }

    @Test
    public void theQueueDrainsInOrder() {
        ArrayDeque<ISuspendableGen> queue = new ArrayDeque<>();
        FakeGen first = new FakeGen(2, 2);
        FakeGen second = new FakeGen(1, 1);
        queue.add(first);
        queue.add(second);

        // The head is served to completion before the next one, which is how the queue was written.
        ArtificeTickHandler.tickSuspendableGenerators(queue);
        Assert.assertEquals(2, queue.size());
        ArtificeTickHandler.tickSuspendableGenerators(queue);
        Assert.assertEquals(1, queue.size());
        Assert.assertSame(second, queue.peek());
        ArtificeTickHandler.tickSuspendableGenerators(queue);
        Assert.assertTrue(queue.isEmpty());
    }

    @Test
    public void anEmptyQueueIsANoOp() {
        Assert.assertFalse(ArtificeTickHandler.tickSuspendableGenerators(new ArrayDeque<ISuspendableGen>()));
        Assert.assertFalse(ArtificeTickHandler.tickSuspendableGenerators(null));
    }
}
