package com.mrfuzzihead.artifice.compat.chisel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;

/**
 * Pins the carving table ChiselCompat hands to Chisel (CODE_REVIEW §9.9 / Appendix E.4).
 *
 * <p>
 * The bug this guards against is entirely a naming bug: Chisel's own table asked for {@code artifice.marble} while the
 * block registers as {@code tile.artifice.marble}, so every lookup returned null and the integration silently did
 * nothing. The names are therefore asserted as literals here rather than rebuilt from {@code Names} - a test that
 * derives the name the same way the code does would have passed just as happily with the broken one.
 * </p>
 *
 * <p>
 * The in-game half of this lives in {@code ServerGameTests.checkChiselCarvingNames}, which resolves every name below
 * against the real block registry; Chisel itself is not needed for either half.
 * </p>
 */
public class CarvingTableTest {

    @Test
    public void rockNamesCarryTheTilePrefixChiselsTableWasMissing() {
        List<CarvingTable.Entry> marble = group(CarvingTable.GROUP_MARBLE);

        Assert.assertFalse("the group must not be empty", marble.isEmpty());
        for (CarvingTable.Entry entry : marble) {
            Assert.assertEquals("tile.artifice.marble", entry.registryName);
            Assert.assertEquals("Artifice:tile.artifice.marble", entry.qualifiedName());
            // The exact string Chisel shipped, and the reason its integration never worked.
            Assert.assertNotEquals("artifice.marble", entry.registryName);
        }
    }

    @Test
    public void everyRockAndSlabIsNamedAsItRegisters() {
        Assert.assertEquals(Arrays.asList("tile.artifice.basalt"), names(CarvingTable.GROUP_BASALT));
        Assert.assertEquals(Arrays.asList("tile.artifice.marble"), names(CarvingTable.GROUP_MARBLE));
        Assert.assertEquals(Arrays.asList("tile.artifice.basalt.slab"), names(CarvingTable.GROUP_BASALT_SLAB));
        Assert.assertEquals(Arrays.asList("tile.artifice.marble.slab"), names(CarvingTable.GROUP_MARBLE_SLAB));

        // The seven limestone colours, in the order ArtificeBlocks registers them, sharing one group so they chisel
        // into each other.
        Assert.assertEquals(
            Arrays.asList(
                "tile.artifice.limestone.gray",
                "tile.artifice.limestone.lightgray",
                "tile.artifice.limestone.brown",
                "tile.artifice.limestone.tan",
                "tile.artifice.limestone.reddish",
                "tile.artifice.limestone.bluish",
                "tile.artifice.limestone.greenish"),
            names(CarvingTable.GROUP_LIMESTONE));
        Assert.assertEquals(
            Arrays.asList(
                "tile.artifice.limestone.gray.slab",
                "tile.artifice.limestone.lightgray.slab",
                "tile.artifice.limestone.brown.slab",
                "tile.artifice.limestone.tan.slab",
                "tile.artifice.limestone.reddish.slab",
                "tile.artifice.limestone.bluish.slab",
                "tile.artifice.limestone.greenish.slab"),
            names(CarvingTable.GROUP_LIMESTONE_SLAB));
    }

    @Test
    public void theGroupsAreTheOnesChiselUsedPlusTheOnesItNeverHad() {
        Assert.assertEquals(
            Arrays.asList(
                CarvingTable.GROUP_BASALT,
                CarvingTable.GROUP_MARBLE,
                CarvingTable.GROUP_LIMESTONE,
                CarvingTable.GROUP_BASALT_SLAB,
                CarvingTable.GROUP_MARBLE_SLAB,
                CarvingTable.GROUP_LIMESTONE_SLAB),
            CarvingTable.groups());

        // Chisel's own table used these two group names for marble and its slab; keep them so an existing world's
        // chisel recipes keep working.
        Assert.assertEquals("marble", CarvingTable.GROUP_MARBLE);
        Assert.assertEquals("marble_slab", CarvingTable.GROUP_MARBLE_SLAB);
        Assert.assertEquals("limestone", CarvingTable.GROUP_LIMESTONE);
    }

    @Test
    public void everyFormOfEveryRockIsRegistered() {
        Assert.assertEquals(6, CarvingTable.rockForms());
        Assert.assertEquals(4, CarvingTable.slabForms());

        for (String group : new String[] { CarvingTable.GROUP_BASALT, CarvingTable.GROUP_MARBLE,
            CarvingTable.GROUP_LIMESTONE }) {
            for (String name : names(group)) {
                Assert.assertEquals(name + " should carry every rock metadata form", metas(group, name), metas(0, 5));
            }
        }

        for (String group : new String[] { CarvingTable.GROUP_BASALT_SLAB, CarvingTable.GROUP_MARBLE_SLAB,
            CarvingTable.GROUP_LIMESTONE_SLAB }) {
            for (String name : names(group)) {
                Assert.assertEquals(name + " should carry every slab metadata form", metas(group, name), metas(0, 3));
            }
        }
    }

    @Test
    public void orderKeepsChiselsValueAndSortsAfterItsOwnBlocks() {
        Assert.assertEquals(99, CarvingTable.BASE_ORDER);

        // Order rises with metadata within one block; the next block in the same group starts over at the base, which
        // keeps the seven limestones adjacent instead of interleaving their forms.
        for (String group : CarvingTable.groups()) {
            for (String name : names(group)) {
                int previous = Integer.MIN_VALUE;
                for (CarvingTable.Entry entry : group(group)) {
                    if (!entry.registryName.equals(name)) {
                        continue;
                    }
                    Assert.assertTrue(
                        "order must rise with metadata inside " + group + ": " + entry,
                        entry.order > previous);
                    previous = entry.order;
                }
            }
            Assert.assertEquals(CarvingTable.BASE_ORDER, group(group).get(0).order);
        }
    }

    @Test
    public void noVariationIsRegisteredTwice() {
        Set<String> seen = new HashSet<>();
        for (CarvingTable.Entry entry : CarvingTable.entries()) {
            String key = entry.group + "|" + entry.registryName + "|" + entry.meta;
            Assert.assertTrue("duplicate carving variation " + key, seen.add(key));
        }
        Assert.assertEquals(
            seen.size(),
            CarvingTable.entries()
                .size());
    }

    @Test
    public void theColourOrderComesFromTheBlockTable() {
        // The table reads ArtificeBlocks.rockColorNames rather than a copy, so a new colour cannot be half-added.
        Assert.assertEquals(ArtificeBlocks.rockColorNames.length, names(CarvingTable.GROUP_LIMESTONE).size());
        for (int i = 0; i < ArtificeBlocks.rockColorNames.length; i++) {
            Assert.assertEquals(
                "tile.artifice.limestone." + ArtificeBlocks.rockColorNames[i],
                names(CarvingTable.GROUP_LIMESTONE).get(i));
        }
    }

    @Test
    public void everyEntryNamesThisMod() {
        for (CarvingTable.Entry entry : CarvingTable.entries()) {
            Assert.assertTrue(entry.registryName, entry.registryName.startsWith(CarvingTable.BLOCK_PREFIX));
            Assert.assertEquals(ArtificeCore.modID + ":" + entry.registryName, entry.qualifiedName());
            Assert.assertTrue(entry.meta >= 0);
        }
    }

    // -------------------------------------------------------------------------------------------------------------

    private static List<CarvingTable.Entry> group(String group) {
        List<CarvingTable.Entry> out = new ArrayList<>();
        for (CarvingTable.Entry entry : CarvingTable.entries()) {
            if (entry.group.equals(group)) {
                out.add(entry);
            }
        }
        return out;
    }

    /** @return the distinct registry names in a group, in first-seen order */
    private static List<String> names(String group) {
        List<String> out = new ArrayList<>();
        for (CarvingTable.Entry entry : group(group)) {
            if (!out.contains(entry.registryName)) {
                out.add(entry.registryName);
            }
        }
        return out;
    }

    private static List<Integer> metas(String group, String registryName) {
        List<Integer> out = new ArrayList<>();
        for (CarvingTable.Entry entry : group(group)) {
            if (entry.registryName.equals(registryName)) {
                out.add(entry.meta);
            }
        }
        return out;
    }

    private static List<Integer> metas(int from, int to) {
        List<Integer> out = new ArrayList<>();
        for (int i = from; i <= to; i++) {
            out.add(i);
        }
        return out;
    }
}
