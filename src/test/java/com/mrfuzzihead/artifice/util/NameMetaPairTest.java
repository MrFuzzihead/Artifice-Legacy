package com.mrfuzzihead.artifice.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link NameMetaPair}, the (registry name, metadata) pair that keys the mod's ore/stone/tooltip lookups.
 *
 * <p>
 * The pair deliberately treats metadata {@code -1} as a wildcard in {@link NameMetaPair#equals(Object)} so that
 * "any metadata" queries work. That is also where the class's one real defect lives: {@link
 * NameMetaPair#hashCode()} does not apply the same wildcard, so the class violates the equals/hashCode contract
 * and a {@code HashSet}/{@code HashMap} lookup with a wildcard key silently misses. See CODE_REVIEW.md section 5.
 * </p>
 */
public class NameMetaPairTest {

    @Test
    public void equalsMatchesNameAndMetadata() {
        NameMetaPair a = new NameMetaPair("minecraft:stone", 0);
        NameMetaPair b = new NameMetaPair("minecraft:stone", 0);
        NameMetaPair c = new NameMetaPair("minecraft:stone", 1);
        NameMetaPair d = new NameMetaPair("minecraft:dirt", 0);

        Assert.assertEquals(a, b);
        Assert.assertNotEquals(a, c);
        Assert.assertNotEquals(a, d);
    }

    @Test
    public void negativeMetadataIsAWildcardForEquality() {
        NameMetaPair anyMeta = new NameMetaPair("minecraft:stone");
        Assert.assertEquals(-1, anyMeta.getMetadata());

        Assert.assertEquals(anyMeta, new NameMetaPair("minecraft:stone", 0));
        Assert.assertEquals(new NameMetaPair("minecraft:stone", 0), anyMeta);
        Assert.assertEquals(anyMeta, new NameMetaPair("minecraft:stone", 15));
    }

    @Test
    public void equalityIsReflexiveAndRejectsForeignTypes() {
        NameMetaPair pair = new NameMetaPair("minecraft:stone", 3);

        Assert.assertEquals(pair, pair);
        Assert.assertNotEquals(pair, null);
        Assert.assertNotEquals(pair, "minecraft:stone@3");
    }

    @Test
    public void equalPairsHaveEqualHashCodes() {
        Assert.assertEquals(
            new NameMetaPair("minecraft:stone", 0).hashCode(),
            new NameMetaPair("minecraft:stone", 0).hashCode());
        Assert.assertEquals(
            new NameMetaPair("minecraft:stone", 15).hashCode(),
            new NameMetaPair("minecraft:stone", 15).hashCode());
    }

    /**
     * Regression (CODE_REVIEW 5.1): a wildcard pair must be findable in a hash-based collection, and a concrete
     * pair must be findable when the set holds the wildcard. {@code WorldGenVolcano#replaced} is built from
     * {@code new NameMetaPair(biome.topBlock)} - wildcard metadata - and then queried with concrete
     * {@code (block, meta)} pairs, so before the fix the set could never match and the volcano's surface
     * replacement was dead even with the feature enabled.
     */
    @Test
    public void wildcardPairIsFindableInAHashSet() {
        Set<NameMetaPair> concreteOnly = new HashSet<>();
        concreteOnly.add(new NameMetaPair("minecraft:stone", 0));
        Assert.assertTrue(
            "wildcard lookup missed a concrete entry",
            concreteOnly.contains(new NameMetaPair("minecraft:stone")));

        Set<NameMetaPair> wildcardOnly = new HashSet<>();
        wildcardOnly.add(new NameMetaPair("minecraft:stone"));
        Assert.assertTrue(
            "concrete lookup missed a wildcard entry",
            wildcardOnly.contains(new NameMetaPair("minecraft:stone", 7)));

        Map<NameMetaPair, String> map = new HashMap<>();
        map.put(new NameMetaPair("minecraft:stone", 3), "stone");
        Assert.assertEquals("stone", map.get(new NameMetaPair("minecraft:stone")));
    }

    /**
     * The other half of the contract: whatever {@code equals} says, the hashes have to agree, or a lookup finds
     * nothing no matter which side of the comparison the wildcard is on.
     */
    @Test
    public void equalPairsAlwaysAgreeOnTheHash() {
        NameMetaPair wildcard = new NameMetaPair("minecraft:stone");

        for (int meta = 0; meta < 16; meta++) {
            NameMetaPair concrete = new NameMetaPair("minecraft:stone", meta);
            Assert.assertEquals(concrete, wildcard);
            Assert.assertEquals(wildcard, concrete);
            Assert.assertEquals(
                "equal pairs hashed differently for meta " + meta,
                concrete.hashCode(),
                wildcard.hashCode());
        }
    }

    /**
     * Pairs that {@code equals} keeps apart may still hash together - that is allowed and unavoidable once the
     * wildcard is honoured - but they must stay distinct in a set and a map.
     */
    @Test
    public void distinctMetadataIsStillDistinctInHashCollections() {
        Set<NameMetaPair> set = new HashSet<>();
        set.add(new NameMetaPair("minecraft:stone", 0));
        set.add(new NameMetaPair("minecraft:stone", 1));
        Assert.assertEquals(2, set.size());

        Map<NameMetaPair, String> map = new HashMap<>();
        map.put(new NameMetaPair("minecraft:stone", 0), "granite");
        map.put(new NameMetaPair("minecraft:stone", 1), "diorite");
        Assert.assertEquals("granite", map.get(new NameMetaPair("minecraft:stone", 0)));
        Assert.assertEquals("diorite", map.get(new NameMetaPair("minecraft:stone", 1)));
    }

    @Test
    public void toStringIsNameAtMetadata() {
        Assert.assertEquals("minecraft:stone@0", new NameMetaPair("minecraft:stone", 0).toString());
        Assert.assertEquals("minecraft:stone@-1", new NameMetaPair("minecraft:stone").toString());
    }

    @Test
    public void metadataIsMutable() {
        NameMetaPair pair = new NameMetaPair("minecraft:stone", 0);
        pair.setMeta(4);
        Assert.assertEquals(4, pair.getMetadata());

        pair.setName("minecraft:dirt");
        Assert.assertEquals("minecraft:dirt", pair.getName());
    }
}
