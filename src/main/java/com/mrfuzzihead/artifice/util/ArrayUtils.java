package com.mrfuzzihead.artifice.util;

/**
 * Bounds-safe array access for the icon and texture tables.
 *
 * <p>
 * Six {@code getIcon} implementations clamped with {@code meta > array.length} instead of {@code >=}, so the last
 * valid index plus one fell through to an array access one past the end, and a couple more had no check at all.
 * Metadata comes from NBT and from other mods' tools, so "impossible" values do reach these methods - a corrupt
 * save or a world-editing tool is enough to crash the client on chunk render (CODE_REVIEW 5.12).
 * </p>
 */
public final class ArrayUtils {

    private ArrayUtils() {}

    /**
     * @return {@code array[index]} when the index is in range, otherwise {@code fallback}
     */
    public static <T> T getOrDefault(T[] array, int index, T fallback) {
        return array != null && index >= 0 && index < array.length ? array[index] : fallback;
    }

    /**
     * @return {@code array[index]} when the index is in range, otherwise {@code array[0]}
     */
    public static <T> T getOrFirst(T[] array, int index) {
        if (array == null || array.length == 0) {
            return null;
        }

        return index >= 0 && index < array.length ? array[index] : array[0];
    }
}
