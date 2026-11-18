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

    @Test
    public void clampIndexIsAlwaysASafeSubscript() {
        for (int index = -8; index < 24; index++) {
            int clamped = ArrayUtils.clampIndex(index, ICONS.length);

            Assert.assertTrue(
                "clampIndex(" + index + ") is a subscript for a " + ICONS.length + "-entry table",
                clamped >= 0 && clamped < ICONS.length);
            Assert.assertEquals(
                "clampIndex(" + index + ") is the index itself when it is in range",
                index >= 0 && index < ICONS.length ? index : 0,
                clamped);
        }
    }

    @Test
    public void clampIndexHandlesAnEmptyTableAndTheExtremes() {
        // An empty table has no subscript at all; callers check length first, and 0 is still the least surprising
        // answer if one does not.
        Assert.assertEquals(0, ArrayUtils.clampIndex(0, 0));
        Assert.assertEquals(0, ArrayUtils.clampIndex(5, 0));
        Assert.assertEquals(0, ArrayUtils.clampIndex(Integer.MIN_VALUE, 4));
        Assert.assertEquals(0, ArrayUtils.clampIndex(Integer.MAX_VALUE, 4));
        Assert.assertEquals(3, ArrayUtils.clampIndex(3, 4));
    }

    @Test
    public void theFrameTableClampsLikeEveryOtherIconTable() {
        // BlockFrameBase indexed icons[meta] unchecked, and metadata 4..15 is reachable - the in-game suite places a
        // frame at metadata 7 to check the hardness clamp, which is exactly the value that would have thrown here.
        int tiers = 4;
        for (int meta = tiers; meta <= 15; meta++) {
            Assert.assertEquals(0, ArrayUtils.clampIndex(meta, tiers));
        }
    }
}
