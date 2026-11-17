package com.mrfuzzihead.artifice.util;

import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link XSRandom}, the xorshift {@link Random} used by the mod's worldgen helpers.
 *
 * <p>
 * Nothing in the current source instantiates it (see the dead-code list in CODE_REVIEW.md section 7), but it is a
 * public API class and a drop-in {@code Random}, so its contract is worth pinning before it is either wired up or
 * deleted: same seed must mean same sequence, and {@code nextInt(bound)} must respect the bound.
 * </p>
 */
public class XSRandomTest {

    @Test
    public void sameSeedProducesTheSameSequence() {
        XSRandom a = new XSRandom(12345L);
        XSRandom b = new XSRandom(12345L);

        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(a.nextInt(), b.nextInt());
        }
    }

    @Test
    public void differentSeedsDiverge() {
        XSRandom a = new XSRandom(1L);
        XSRandom b = new XSRandom(2L);

        boolean differs = false;
        for (int i = 0; i < 20 && !differs; i++) {
            differs = a.nextInt() != b.nextInt();
        }

        Assert.assertTrue("two seeds produced an identical 20-int sequence", differs);
    }

    @Test
    public void nextIntRespectsItsBound() {
        Random random = new XSRandom(7L);

        for (int bound = 1; bound < 200; bound++) {
            for (int i = 0; i < 50; i++) {
                int value = random.nextInt(bound);
                Assert.assertTrue("nextInt(" + bound + ") returned " + value, value >= 0 && value < bound);
            }
        }
    }

    @Test
    public void nextIntIsReasonablySpread() {
        Random random = new XSRandom(99L);
        int[] buckets = new int[10];

        for (int i = 0; i < 10000; i++) {
            buckets[random.nextInt(10)]++;
        }

        for (int i = 0; i < buckets.length; i++) {
            Assert.assertTrue("bucket " + i + " only saw " + buckets[i] + " of 10000 draws", buckets[i] > 500);
        }
    }

    @Test
    public void nextDoubleAndNextFloatStayInRange() {
        Random random = new XSRandom(4242L);

        for (int i = 0; i < 1000; i++) {
            double d = random.nextDouble();
            float f = random.nextFloat();
            Assert.assertTrue("nextDouble returned " + d, d >= 0.0 && d < 1.0);
            Assert.assertTrue("nextFloat returned " + f, f >= 0.0F && f < 1.0F);
        }
    }
}
