package com.mrfuzzihead.artifice.world;

import java.util.Random;

/**
 * Turns a configured "percentage frequency" into the number of features to place in one chunk.
 *
 * <p>
 * Every generator had its own copy of this expression, and it was wrong at both ends (CODE_REVIEW 7.2):
 * </p>
 *
 * <pre>
 * if (frequency &gt;= 100) {
 *     total = rand.nextInt(100) &lt; Math.abs(frequency % 10 ^ floor(log10(frequency))) ? frequency / 100 + 1
 *         : frequency / 100;
 * } else {
 *     total = rand.nextInt(100) &lt; frequency ? 0 : 1; // inverted
 * }
 * </pre>
 *
 * <ul>
 * <li>{@code frequency = 0} meant "one per chunk" - the maximum rate - instead of "off".</li>
 * <li>Below 100 the semantics were inverted: 99 gave a feature 1% of the time, 1 gave it 99% of the time, and
 * only 50 happened to read correctly.</li>
 * <li>Above 1000 the remainder test saturated: the divisor was {@code 10^floor(log10(f))} but the roll was
 * 0..99, so {@code 1500 % 1000 == 500} made the extra-roll branch always true and 1500 produced 16 per chunk
 * instead of 15.5.</li>
 * <li>The {@code Math.pow(Math.log10(f))} round trip is a floating-point trap for no reason.</li>
 * </ul>
 *
 * <p>
 * The contract below is the one the config file documents: {@code frequency} is a percentage, 100 = one feature per
 * chunk, 150 = one and a half, 0 = off.
 * </p>
 */
public final class GenFrequency {

    private GenFrequency() {}

    /**
     * @param frequency percentage of chunks that get a feature, 100 = one per chunk; anything else scales linearly
     * @return how many features to place in this chunk, 0 when the feature is disabled
     */
    public static int rollsThisChunk(Random rand, int frequency) {
        if (frequency <= 0) {
            return 0;
        }

        int whole = frequency / 100;
        int remainder = frequency % 100;
        return whole + (rand.nextInt(100) < remainder ? 1 : 0);
    }
}
