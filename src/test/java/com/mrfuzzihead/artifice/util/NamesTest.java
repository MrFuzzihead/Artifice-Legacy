package com.mrfuzzihead.artifice.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;

import org.junit.Assert;
import org.junit.Test;

/**
 * The name builders in {@link Names}. The strings themselves are frozen by the in-game registry test; this pins the
 * shape of the builders, so a change to how a family is composed is caught here rather than in a world.
 */
public class NamesTest {

    @Test
    public void rocksAndSlabsFollowTheFrozenShape() {
        Assert.assertEquals("artifice.basalt", Names.rock("basalt"));
        Assert.assertEquals("artifice.limestone.gray", Names.rock("limestone.gray"));

        Assert.assertEquals("artifice.basalt.slab", Names.slab("basalt", false));
        Assert.assertEquals("artifice.basalt.doubleslab", Names.slab("basalt", true));
    }

    @Test
    public void stairsTakeTheRockNameFromTheBlockAndDropTheTilePrefix() {
        Block rock = mock(Block.class);
        when(rock.getUnlocalizedName()).thenReturn("tile.artifice.basalt");

        Assert.assertEquals("artifice.basalt.stairs.cobblestone", Names.stairs(rock, "Cobblestone"));
        Assert.assertEquals("artifice.basalt.stairs.brick", Names.stairs(rock, "Brick"));
    }

    @Test
    public void oresAreNamedAfterTheirOreDictionaryEntry() {
        // The dictionary names are camel case; the registry names are not, and have been that way since 1.1.0.
        Assert.assertEquals("artifice.oreredstone", Names.ore("oreRedstone", false));
        Assert.assertEquals("artifice.oreredstone.glowing", Names.ore("oreRedstone", true));
        Assert.assertEquals("artifice.oreender.glowing", Names.ore("oreEnder", true));
    }

    @Test
    public void lampsAndTorchesCarryTheirColour() {
        Assert.assertEquals("artifice.lamp.white", Names.lamp("WHITE", false));
        Assert.assertEquals("artifice.lampinverted.white", Names.lamp("WHITE", true));

        Assert.assertEquals("artifice.coloredtorch0", Names.torch(0));
        Assert.assertEquals("artifice.coloredtorch15", Names.torch(15));
    }

    @Test
    public void toolsAreNamedAfterTheirMaterial() {
        Assert.assertEquals("artifice.sledge.iron", Names.tool("sledge", "IRON"));
        Assert.assertEquals("artifice.sickle.emerald", Names.tool("sickle", "EMERALD"));
    }

    @Test
    public void everyConstantIsUniqueAndNamespaced() throws IllegalAccessException {
        List<String> constants = new ArrayList<>();
        for (Field field : Names.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == String.class) {
                constants.add((String) field.get(null));
            }
        }

        Assert.assertTrue("Names has no constants", constants.size() > 20);

        // Block and item names live in separate registries, so one string may appear twice on purpose: the steel
        // block and the steel item are both "artifice.steel" and always have been. Any other duplicate is a mistake.
        Map<String, Integer> counts = new HashMap<>();
        for (String name : constants) {
            Integer seen = counts.get(name);
            counts.put(name, seen == null ? 1 : seen + 1);
        }

        Set<String> duplicates = new HashSet<>();
        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > 1) {
                duplicates.add(entry.getKey());
            }
        }

        Assert.assertEquals(
            "only the steel block/item pair may share a name",
            Collections.singleton("artifice.steel"),
            duplicates);

        for (String name : constants) {
            Assert.assertNotNull(name);
            Assert.assertFalse("empty name", name.isEmpty());
            // BUCKET is deliberately the bare vanilla-style name, and FLUID_NAMESPACE is a namespace rather than a
            // name; everything else carries the mod's root.
            if (!name.equals(Names.BUCKET) && !name.equals(Names.FLUID_NAMESPACE)) {
                Assert.assertTrue(name + " should start with " + Names.ROOT, name.startsWith(Names.ROOT));
            }
        }
    }
}
