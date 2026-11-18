package com.mrfuzzihead.artifice.block.frame;

import org.junit.Assert;
import org.junit.Test;

/**
 * The detector's pulse is a three-state table, and the whole §5.19 fix rests on which of those states emits power.
 *
 * <p>
 * Kept headless on purpose: the classes here are static and take no world, so the table can be pinned without a
 * Minecraft registry. The in-game test ({@code detector pulse} in {@code ServerGameTests}) covers the half that needs
 * a real world - that the pulse actually stops next to a neighbour that echoes the notification back.
 * </p>
 */
public class BlockFrameDetectorTest {

    @Test
    public void pulseWalksIdleActiveTailAndBack() {
        int meta = BlockFrameDetector.META_IDLE;
        Assert.assertEquals(BlockFrameDetector.META_ACTIVE, BlockFrameDetector.nextMetadata(meta));

        meta = BlockFrameDetector.META_ACTIVE;
        Assert.assertEquals(BlockFrameDetector.META_TAIL, BlockFrameDetector.nextMetadata(meta));

        meta = BlockFrameDetector.META_TAIL;
        Assert.assertEquals(BlockFrameDetector.META_IDLE, BlockFrameDetector.nextMetadata(meta));

        // Three steps, one circuit: the tail is the only reason the cycle terminates instead of repeating, which is
        // what aFullPulseTakesThreeStepsAndEmitsPowerOnce pins down.
    }

    @Test
    public void onlyTheActiveStateEmitsPower() {
        Assert.assertTrue(BlockFrameDetector.emitsPower(BlockFrameDetector.META_ACTIVE));
        Assert.assertFalse(BlockFrameDetector.emitsPower(BlockFrameDetector.META_IDLE));
        Assert.assertFalse(BlockFrameDetector.emitsPower(BlockFrameDetector.META_TAIL));
    }

    @Test
    public void exactlyTwoOfTheThreeStepsArePowerEdges() {
        // A neighbour is notified on the rising edge (idle -> active) and the falling edge (active -> tail). The tail
        // step changes nothing a consumer cares about, and notifying it is what used to restart the pulse.
        int idle = BlockFrameDetector.META_IDLE;
        int active = BlockFrameDetector.META_ACTIVE;
        int tail = BlockFrameDetector.META_TAIL;

        Assert.assertTrue(BlockFrameDetector.isPowerEdge(idle, active));
        Assert.assertTrue(BlockFrameDetector.isPowerEdge(active, tail));
        Assert.assertFalse(BlockFrameDetector.isPowerEdge(tail, idle));
    }

    @Test
    public void corruptMetadataHealsToIdleInsteadOfCrashing() {
        // Metadata is a nibble from the chunk, so an older save or a bad setBlock can produce anything from 3 to 15.
        // Every one of those must behave as "off", not walk off the end of the table.
        for (int meta = 3; meta <= 15; meta++) {
            Assert.assertEquals(
                "metadata " + meta + " should heal to idle",
                BlockFrameDetector.META_IDLE,
                BlockFrameDetector.nextMetadata(meta));
            Assert.assertFalse("metadata " + meta + " must not emit power", BlockFrameDetector.emitsPower(meta));
        }
    }

    @Test
    public void aFullPulseTakesThreeStepsAndEmitsPowerOnce() {
        int meta = BlockFrameDetector.META_IDLE;
        int steps = 0;
        int powered = 0;

        do {
            meta = BlockFrameDetector.nextMetadata(meta);
            steps++;
            if (BlockFrameDetector.emitsPower(meta)) powered++;
        } while (meta != BlockFrameDetector.META_IDLE || steps < 3);

        Assert.assertEquals(3, steps);
        Assert.assertEquals(1, powered);
    }
}
