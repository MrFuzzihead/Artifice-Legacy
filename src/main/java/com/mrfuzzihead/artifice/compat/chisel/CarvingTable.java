package com.mrfuzzihead.artifice.compat.chisel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.decorative.BlockRockSlab;
import com.mrfuzzihead.artifice.util.Names;

/**
 * The Chisel carving groups this mod would like to be in, as data (CODE_REVIEW §9.9 / Appendix E.4).
 *
 * <p>
 * Chisel 2 ships its own Artifice table, and every one of its eight calls has always returned early: it asks
 * {@code GameRegistry.findBlock("Artifice", "artifice.marble")}, but this mod registers {@code tile.artifice.marble},
 * so the lookup is null and the integration has never done anything in any pack. The names below are the ones that
 * actually exist, and they are built from {@link Names} rather than retyped, so they cannot drift from registration.
 * </p>
 *
 * <p>
 * This class deliberately holds no Minecraft or Chisel types beyond a {@code String} and two {@code int}s: the table is
 * the part that was wrong before, so it is the part that has to be testable headlessly
 * ({@code CarvingTableTest}). {@link ChiselCompat} resolves each {@link Entry#registryName} against the real registry
 * and hands it to Chisel.
 * </p>
 *
 * <p>
 * Two deliberate differences from Chisel's table:
 * </p>
 * <ul>
 * <li>Every carvable form of a rock is registered, not just the raw stone. A group is looked up by block <em>and</em>
 * metadata, so registering meta 0 alone would leave a player holding marble bricks with a chisel that does nothing -
 * which is the same experience as the broken table, just for a different reason.</li>
 * <li>Basalt and the limestone slabs are included. Chisel never knew about them, and the groups cost nothing.</li>
 * </ul>
 */
public final class CarvingTable {

    /**
     * The {@code order} Chisel's own table used for Artifice. Chisel's blocks are added at lower orders, so ours sort
     * after them inside a shared group.
     */
    public static final int BASE_ORDER = 99;

    public static final String GROUP_BASALT = "basalt";
    public static final String GROUP_BASALT_SLAB = "basalt_slab";
    public static final String GROUP_MARBLE = "marble";
    public static final String GROUP_MARBLE_SLAB = "marble_slab";
    public static final String GROUP_LIMESTONE = "limestone";
    public static final String GROUP_LIMESTONE_SLAB = "limestone_slab";

    /**
     * The {@code tile.} prefix {@code Block.getUnlocalizedName()} adds and {@code GameRegistry.registerBlock} therefore
     * keeps. Chisel's broken table was missing exactly this.
     */
    public static final String BLOCK_PREFIX = "tile.";

    private CarvingTable() {}

    /** One block form in one group: what Chisel calls a {@code SimpleCarvingVariation}. */
    public static final class Entry {

        /** The registry name without the mod id, e.g. {@code tile.artifice.marble}. */
        public final String registryName;
        public final String group;
        public final int meta;
        public final int order;

        Entry(String registryName, String group, int meta, int order) {
            this.registryName = registryName;
            this.group = group;
            this.meta = meta;
            this.order = order;
        }

        /** The {@code modId:name} key Chisel's table was trying to build by hand. */
        public String qualifiedName() {
            return ArtificeCore.modID + ":" + this.registryName;
        }

        @Override
        public String toString() {
            return this.group + " <- " + this.qualifiedName() + "@" + this.meta + " (order " + this.order + ")";
        }
    }

    /**
     * @return every variation this mod contributes, in registration order
     */
    public static List<Entry> entries() {
        List<Entry> out = new ArrayList<>();

        addRocks(out, GROUP_BASALT, "basalt");
        addRocks(out, GROUP_MARBLE, "marble");
        for (String colour : ArtificeBlocks.rockColorNames) {
            addRocks(out, GROUP_LIMESTONE, "limestone." + colour);
        }

        addSlabs(out, GROUP_BASALT_SLAB, "basalt");
        addSlabs(out, GROUP_MARBLE_SLAB, "marble");
        for (String colour : ArtificeBlocks.rockColorNames) {
            addSlabs(out, GROUP_LIMESTONE_SLAB, "limestone." + colour);
        }

        return Collections.unmodifiableList(out);
    }

    /** @return the distinct group names, in the order they first appear */
    public static List<String> groups() {
        List<String> out = new ArrayList<>();
        for (Entry e : entries()) {
            if (!out.contains(e.group)) {
                out.add(e.group);
            }
        }
        return Collections.unmodifiableList(out);
    }

    /**
     * @return how many metadata forms a rock block has: raw, cobblestone, brick, paver, antipaver, chiseled
     */
    public static int rockForms() {
        return ArtificeConfig.rocks.length;
    }

    /**
     * @return how many metadata forms a rock slab has: brick, cobblestone, paver, antipaver
     */
    public static int slabForms() {
        return BlockRockSlab.TYPES.length;
    }

    private static void addRocks(List<Entry> out, String group, String rockName) {
        String registryName = BLOCK_PREFIX + Names.rock(rockName);
        for (int meta = 0; meta < rockForms(); meta++) {
            out.add(new Entry(registryName, group, meta, BASE_ORDER + meta));
        }
    }

    private static void addSlabs(List<Entry> out, String group, String rockName) {
        String registryName = BLOCK_PREFIX + Names.slab(rockName, false);
        for (int meta = 0; meta < slabForms(); meta++) {
            out.add(new Entry(registryName, group, meta, BASE_ORDER + meta));
        }
    }
}
