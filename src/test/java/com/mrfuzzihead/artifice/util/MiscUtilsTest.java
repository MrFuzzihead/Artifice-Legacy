package com.mrfuzzihead.artifice.util;

import java.util.Arrays;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers the two pure helpers in {@link MiscUtils} that the heating coil and the volcano generator lean on.
 */
public class MiscUtilsTest {

    @Test
    public void shuffleArrayKeepsExactlyTheOriginalElements() {
        Integer[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        for (int run = 0; run < 200; run++) {
            Integer[] shuffled = original.clone();
            MiscUtils.shuffleArray(shuffled);

            Assert.assertEquals("shuffle changed the element count", original.length, shuffled.length);
            Integer[] sortedOriginal = original.clone();
            Integer[] sortedShuffled = shuffled.clone();
            Arrays.sort(sortedOriginal);
            Arrays.sort(sortedShuffled);
            Assert.assertArrayEquals("shuffle lost or duplicated an element", sortedOriginal, sortedShuffled);
        }
    }

    @Test
    public void shuffleArrayToleratesEmptyAndSingleElementArrays() {
        Integer[] empty = {};
        MiscUtils.shuffleArray(empty);
        Assert.assertEquals(0, empty.length);

        Integer[] single = { 7 };
        MiscUtils.shuffleArray(single);
        Assert.assertArrayEquals(new Integer[] { 7 }, single);
    }

    @Test
    public void shuffleArrayActuallyMovesElements() {
        // Probability of the identity permutation in 200 draws is (1/10!)^200; this cannot flake.
        boolean moved = false;
        Integer[] original = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        for (int run = 0; run < 200 && !moved; run++) {
            Integer[] shuffled = original.clone();
            MiscUtils.shuffleArray(shuffled);
            moved = !Arrays.equals(original, shuffled);
        }

        Assert.assertTrue("shuffleArray never changed the order in 200 runs", moved);
    }

    @Test
    public void randWithin50NeverReturnsNonPositive() {
        Random rand = new Random(1234L);

        for (int input = -5; input < 200; input++) {
            for (int i = 0; i < 20; i++) {
                int value = MiscUtils.randWithin50(rand, input);
                Assert.assertTrue("randWithin50(" + input + ") returned " + value, value > 0);
            }
        }
    }

    @Test
    public void randWithin50IsAtLeastOneForDegenerateInput() {
        Random rand = new Random(1234L);
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, 0));
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, 1));
        Assert.assertEquals(1, MiscUtils.randWithin50(rand, -100));
    }

    @Test
    public void randWithin50StaysWithinTheDocumentedBand() {
        Random rand = new Random(99L);

        for (int input = 2; input < 500; input++) {
            int half = input / 2;
            int min = input - (half - 1);
            int max = input + (half - 1);

            for (int i = 0; i < 10; i++) {
                int value = MiscUtils.randWithin50(rand, input);
                Assert.assertTrue(
                    "randWithin50(" + input + ")=" + value + " outside [" + min + ", " + max + "]",
                    value >= min && value <= max);
            }
        }
    }

    @Test
    public void randWithin50IsDeterministicForASeed() {
        Assert.assertEquals(MiscUtils.randWithin50(new Random(42L), 100), MiscUtils.randWithin50(new Random(42L), 100));
    }
}
