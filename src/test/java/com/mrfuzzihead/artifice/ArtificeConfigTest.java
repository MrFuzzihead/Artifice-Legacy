package com.mrfuzzihead.artifice;

import net.minecraftforge.common.config.Configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link ArtificeConfig#readCommon(Configuration)} - the whole common config surface, read without a game.
 *
 * <p>
 * Two things matter here and neither is visible in a running pack: the defaults are the contract other tests and
 * the docs assert against, and the machine values are dangerous if a user writes a hostile number
 * (CODE_REVIEW 5.21): {@code RFperFurnaceHeat} is an integer divisor, so 0 threw {@code ArithmeticException} out of
 * the heating coil on every tick it found a furnace.
 * </p>
 */
public class ArtificeConfigTest {

    private static Configuration hostile() {
        Configuration c = new Configuration();

        c.get("Machines", "Maximum RF capacity of nuclear batteries", 1)
            .set(0);
        c.get("Machines", "Maximum rate at which RF is produced by nuclear batteries", 1)
            .set(-20);
        c.get("Machines", "Rate at which heating coils can transfer heat, in RF/t", 1)
            .set(-80);
        c.get("Machines", "Amount of RF each unit of furnace burn time costs", 1)
            .set(0);
        c.get("Machines", "Rate at which heating coils bleed energy, in RF/t", 1)
            .set(-8);

        c.get("Resource Generation", "Ender Ore Minimum Height", 0)
            .set(80);
        c.get("Resource Generation", "Ender Ore Maximum Height", 0)
            .set(10);
        c.get("Resource Generation", "Uranium minimum height", 0)
            .set(30);
        c.get("Resource Generation", "Uranium maximum height", 0)
            .set(-40);

        return c;
    }

    @Test
    public void defaultsAreTheDocumentedOnes() {
        ArtificeConfig.readCommon(new Configuration());

        Assert.assertEquals(4, ArtificeConfig.tiers.length);
        Assert.assertEquals(5, ArtificeConfig.flora.length);
        Assert.assertEquals(6, ArtificeConfig.rocks.length);
        Assert.assertEquals(9, ArtificeConfig.rockNames.length);
        Assert.assertEquals(70, ArtificeConfig.enchantmentStartID);
        Assert.assertEquals(8, ArtificeConfig.RFperFurnaceHeat);
        Assert.assertEquals(80, ArtificeConfig.heatingCoilRate);
        Assert.assertEquals(20, ArtificeConfig.nuclearBatteryRate);
        Assert.assertTrue(ArtificeConfig.enchantmentInvisibleEnable);
    }

    @Test
    public void theFurnaceDivisorIsNeverZeroOrNegative() {
        ArtificeConfig.readCommon(hostile());
        Assert.assertTrue(
            "RFperFurnaceHeat is a divisor, 0 is an ArithmeticException in the heating coil",
            ArtificeConfig.RFperFurnaceHeat >= 1);
    }

    @Test
    public void machineValuesAreClampedToUsableRanges() {
        ArtificeConfig.readCommon(hostile());

        Assert.assertTrue("a capacity of 0 makes the charge ratio NaN", ArtificeConfig.nuclearBatteryCapacity >= 1);
        Assert.assertTrue(ArtificeConfig.nuclearBatteryRate >= 0);
        Assert.assertTrue(ArtificeConfig.heatingCoilRate >= 0);
        Assert.assertTrue(ArtificeConfig.coilLoss >= 0);
    }

    @Test
    public void invertedHeightRangesAreRepaired() {
        ArtificeConfig.readCommon(hostile());

        Assert.assertTrue(
            "ender ore range still inverted: " + ArtificeConfig.enderOreMinHeight
                + ".."
                + ArtificeConfig.enderOreMaxHeight,
            ArtificeConfig.enderOreMaxHeight >= ArtificeConfig.enderOreMinHeight);
        Assert.assertTrue(
            "uranium range still inverted: " + ArtificeConfig.uraniumOreMinHeight
                + ".."
                + ArtificeConfig.uraniumOreMaxHeight,
            ArtificeConfig.uraniumOreMaxHeight >= ArtificeConfig.uraniumOreMinHeight);

        for (int i = 0; i < ArtificeConfig.rockClustersMinHeight.length; i++) {
            Assert.assertTrue(
                "cluster range " + i + " still inverted",
                ArtificeConfig.rockClustersMaxHeight[i] >= ArtificeConfig.rockClustersMinHeight[i]);
            Assert.assertTrue(
                "layer range " + i + " still inverted",
                ArtificeConfig.rockLayersMaxHeight[i] >= ArtificeConfig.rockLayersMinHeight[i]);
            Assert.assertTrue(
                "cave range " + i + " still inverted",
                ArtificeConfig.rockCavesMaxHeight[i] >= ArtificeConfig.rockCavesMinHeight[i]);
        }
    }

    @Test
    public void readingIsRepeatableAndDoesNotAccumulate() {
        // The config object is rebuilt on every game start; a second read must produce the same values, not a
        // value scaled or clamped twice.
        ArtificeConfig.readCommon(new Configuration());
        int rf = ArtificeConfig.RFperFurnaceHeat;
        int coil = ArtificeConfig.heatingCoilRate;

        ArtificeConfig.readCommon(new Configuration());
        Assert.assertEquals(rf, ArtificeConfig.RFperFurnaceHeat);
        Assert.assertEquals(coil, ArtificeConfig.heatingCoilRate);
    }

    @Test
    public void anEmptyDimensionListIsSilent() {
        // A fresh install writes "", which used to split to [""], fail parseInt and log a warning on every launch.
        Assert.assertEquals(0, ArtificeConfig.parseDimensionList("").length);
        Assert.assertEquals(0, ArtificeConfig.parseDimensionList("   ").length);
        Assert.assertEquals(0, ArtificeConfig.parseDimensionList(null).length);
        Assert.assertEquals(0, ArtificeConfig.parseDimensionList(",").length);
    }

    @Test
    public void theDimensionListIgnoresBlanksAndJunk() {
        int[] ids = ArtificeConfig.parseDimensionList("0, -1 , 7");

        Assert.assertArrayEquals(new int[] { 0, -1, 7 }, ids);
        Assert.assertArrayEquals(new int[] { 0, 7 }, ArtificeConfig.parseDimensionList("0,,7"));
        Assert.assertArrayEquals(new int[] { 3 }, ArtificeConfig.parseDimensionList("3,nonsense"));
        Assert.assertArrayEquals(new int[] { 0, -1 }, ArtificeConfig.parseDimensionList("0,,-1,"));
    }

    @Test
    public void theStoneListUsesSemicolons() {
        // The config comment used to say "Name:Meta" while the parser split on ';' - anyone following the comment
        // got a parse failure and a warning.
        java.util.List<String[]> pairs = ArtificeConfig.parseStonePairs("minecraft:stone;0,modid:block;3");

        Assert.assertEquals(2, pairs.size());
        Assert.assertArrayEquals(new String[] { "minecraft:stone", "0" }, pairs.get(0));
        Assert.assertArrayEquals(new String[] { "modid:block", "3" }, pairs.get(1));
    }

    @Test
    public void theStoneListToleratesBlanksAndBadSeparators() {
        Assert.assertTrue(
            ArtificeConfig.parseStonePairs("")
                .isEmpty());
        Assert.assertTrue(
            ArtificeConfig.parseStonePairs(null)
                .isEmpty());
        Assert.assertTrue(
            "only blanks",
            ArtificeConfig.parseStonePairs(" , ")
                .isEmpty());

        java.util.List<String[]> bad = ArtificeConfig.parseStonePairs("minecraft:stone");
        Assert.assertEquals("a pair with no separator is reported with a null meta", 1, bad.size());
        Assert.assertNull(bad.get(0)[1]);
    }

    @Test
    public void clientKeysAreReadByTheCommonPass() {
        // One Configuration per launch now: the client keys are read here instead of by a second initClient pass.
        ArtificeConfig.readCommon(new Configuration());
        Assert.assertTrue("tooltips default to on", ArtificeConfig.tooltips);

        Configuration c = new Configuration();
        c.get("Client", "Enable Tooltips", true)
            .set(false);
        ArtificeConfig.readCommon(c);
        Assert.assertFalse(ArtificeConfig.tooltips);
    }
}
