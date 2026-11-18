package com.mrfuzzihead.artifice.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link ArrayUtils}, the bounds-safe access the icon and texture tables use (CODE_REVIEW 5.12).
 *
 * <p>
 * Six {@code getIcon} implementations clamped with {@code meta > array.length}, so the value one past the last
 * valid index fell through to {@code array[meta]} and threw {@code ArrayIndexOutOfBoundsException} from inside
 * chunk rendering. Metadata is persisted in NBT and can be written by other mods, so an invalid value is reachable.
 * </p>
 */
public class ArrayUtilsTest {

    private static final String[] ICONS = { "zero", "one", "two" };

    @Test
    public void inRangeIndexesAreReturned() {
        Assert.assertEquals("zero", ArrayUtils.getOrDefault(ICONS, 0, "fallback"));
        Assert.assertEquals("two", ArrayUtils.getOrDefault(ICONS, 2, "fallback"));
        Assert.assertEquals("one", ArrayUtils.getOrFirst(ICONS, 1));
    }

    @Test
    public void theIndexOnePastTheEndUsesTheFallback() {
        // The exact off-by-one that used to throw: length is 3, so 3 must not index.
        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(ICONS, ICONS.length, "fallback"));
        Assert.assertEquals("zero", ArrayUtils.getOrFirst(ICONS, ICONS.length));
    }

    @Test
    public void negativeIndexesUseTheFallback() {
        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(ICONS, -1, "fallback"));
        Assert.assertEquals("zero", ArrayUtils.getOrFirst(ICONS, -1));
        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(ICONS, Integer.MIN_VALUE, "fallback"));
        Assert.assertEquals("zero", ArrayUtils.getOrFirst(ICONS, Integer.MIN_VALUE));
    }

    @Test
    public void aHugeIndexUsesTheFallback() {
        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(ICONS, Integer.MAX_VALUE, "fallback"));
        Assert.assertEquals("zero", ArrayUtils.getOrFirst(ICONS, Integer.MAX_VALUE));
    }

    @Test
    public void aNullArrayIsNotThrownAt() {
        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(null, 0, "fallback"));
        Assert.assertNull(ArrayUtils.getOrFirst((String[]) null, 0));
    }

    @Test
    public void anEmptyArrayIsNotThrownAt() {
        String[] empty = {};

        Assert.assertEquals("fallback", ArrayUtils.getOrDefault(empty, 0, "fallback"));
        Assert.assertNull("there is no first element to fall back to", ArrayUtils.getOrFirst(empty, 0));
        Assert.assertNull(ArrayUtils.getOrFirst(empty, 7));
    }

    @Test
    public void aNullFallbackIsPreserved() {
        Assert.assertNull(ArrayUtils.getOrDefault(ICONS, 9, null));
    }

    @Test
    public void everyIndexOfATableIsReachable() {
        // The pattern the icon tables rely on: 0..length-1 all work, nothing else does.
        for (int index = 0; index < ICONS.length; index++) {
            Assert.assertEquals(ICONS[index], ArrayUtils.getOrFirst(ICONS, index));
        }

        for (int index = -5; index < ICONS.length + 5; index++) {
            if (index >= 0 && index < ICONS.length) {
                continue;
            }
            Assert.assertEquals(ICONS[0], ArrayUtils.getOrFirst(ICONS, index));
        }
    }
}
