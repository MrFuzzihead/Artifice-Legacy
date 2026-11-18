package com.mrfuzzihead.artifice.world;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link GenFrequency} (CODE_REVIEW 7.2), which replaced eight copies of the same broken expression.
 *
 * <p>
 * The contract: {@code frequency} is a percentage, 100 = one feature per chunk, 0 = off. The old expression gave
 * zero the *maximum* rate, inverted everything below 100 except exactly 50, and saturated above 1000.
 * </p>
 */
public class GenFrequencyTest {

    private static final int SAMPLES = 20000;

    /** Mean rolls per chunk over many draws, with a fixed seed. */
    private static double mean(int frequency) {
        Random rand = new Random(0xC0FFEEL);
        long total = 0;

        for (int i = 0; i < SAMPLES; i++) {
            total += GenFrequency.rollsThisChunk(rand, frequency);
        }

        return (double) total / SAMPLES;
    }

    private static void assertMean(int frequency, double expected) {
        double actual = mean(frequency);
        Assert.assertEquals(
            "frequency " + frequency + " should average " + expected + " per chunk",
            expected,
            actual,
            0.02 * Math.max(1.0, expected));
    }

    @Test
    public void zeroAndNegativeMeanOff() {
        Random rand = new Random(1L);

        for (int frequency : new int[] { 0, -1, -100, Integer.MIN_VALUE }) {
            for (int i = 0; i < 200; i++) {
                Assert.assertEquals(
                    "frequency " + frequency + " must place nothing",
                    0,
                    GenFrequency.rollsThisChunk(rand, frequency));
            }
        }
    }

    @Test
    public void oneHundredIsExactlyOnePerChunk() {
        Random rand = new Random(2L);
        for (int i = 0; i < 500; i++) {
            Assert.assertEquals(1, GenFrequency.rollsThisChunk(rand, 100));
        }
    }

    @Test
    public void percentagesBelowOneHundredAreTheChanceOfOne() {
        assertMean(50, 0.5);
        assertMean(25, 0.25);
        assertMean(99, 0.99);
        assertMean(1, 0.01);
    }

    @Test
    public void valuesAboveOneHundredScaleLinearly() {
        assertMean(150, 1.5);
        assertMean(199, 1.99);
        assertMean(200, 2.0);
        assertMean(250, 2.5);

        Random rand = new Random(3L);
        for (int i = 0; i < 200; i++) {
            Assert.assertEquals(2, GenFrequency.rollsThisChunk(rand, 200));
        }
    }

    @Test
    public void largeValuesDoNotSaturate() {
        // The old % 10^floor(log10(f)) divisor saturated: 1500 % 1000 == 500, so the "extra roll" branch was always
        // taken and 1500 produced 16 per chunk instead of 15. 1900 produced 20 instead of 19.
        Random rand = new Random(4L);
        for (int i = 0; i < 200; i++) {
            Assert.assertEquals("1500 is exactly fifteen, not sixteen", 15, GenFrequency.rollsThisChunk(rand, 1500));
            Assert.assertEquals("1900 is exactly nineteen", 19, GenFrequency.rollsThisChunk(rand, 1900));
        }

        assertMean(1550, 15.5);

        int seenSixteen = 0;
        for (int i = 0; i < 4000; i++) {
            if (GenFrequency.rollsThisChunk(rand, 1550) == 16) {
                seenSixteen++;
            }
        }
        Assert.assertTrue("1550 must sometimes take the extra roll", seenSixteen > 0);
        Assert.assertTrue("1550 must not always take the extra roll", seenSixteen < 4000);
    }

    @Test
    public void theWholePartIsAlwaysPresent() {
        Random rand = new Random(5L);

        for (int frequency = 100; frequency < 5000; frequency += 37) {
            int whole = frequency / 100;
            for (int i = 0; i < 50; i++) {
                int rolls = GenFrequency.rollsThisChunk(rand, frequency);
                Assert.assertTrue(
                    "frequency " + frequency + " produced " + rolls + " rolls, below its whole part",
                    rolls >= whole);
                Assert.assertTrue("frequency " + frequency + " over-rolled", rolls <= whole + 1);
            }
        }
    }

    @Test
    public void isDeterministicForASeed() {
        Random a = new Random(7L);
        Random b = new Random(7L);

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(GenFrequency.rollsThisChunk(a, 137), GenFrequency.rollsThisChunk(b, 137));
        }
    }

    @Test
    public void neverReturnsANegative() {
        Random rand = new Random(8L);

        for (int frequency = -500; frequency <= 500; frequency += 7) {
            Assert.assertTrue(GenFrequency.rollsThisChunk(rand, frequency) >= 0);
        }
    }
}
