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

        return array[clampIndex(index, array.length)];
    }

    /**
     * A subscript that is always safe for a table of {@code length}.
     *
     * <p>
     * The icon tables are read with metadata that came from a save file, another mod's tool or a world edit, so the
     * index is not the subtype count - and the renderer is the one place a stray value is fatal, because it runs while
     * a chunk is being drawn. Clamping here means the subscript cannot be wrong even where the array access itself
     * cannot be exercised: a dedicated server cannot load {@code IIcon} or {@code IIconRegister} at all, so the
     * in-game harness can never call {@code getIcon}.
     * </p>
     *
     * @return {@code index} when it is a valid subscript, otherwise {@code 0}
     */
    public static int clampIndex(int index, int length) {
        return index >= 0 && index < length ? index : 0;
    }
}
