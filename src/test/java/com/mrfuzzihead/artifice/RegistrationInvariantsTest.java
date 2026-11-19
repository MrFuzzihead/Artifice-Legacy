package com.mrfuzzihead.artifice;

import net.minecraft.item.Item.ToolMaterial;

import org.junit.Assert;
import org.junit.Test;

import com.mrfuzzihead.artifice.recipe.EnumUpgrades;
import com.mrfuzzihead.artifice.util.MinecraftColors;

import cpw.mods.fml.common.Mod;

/**
 * Guards the metadata tables that the block and item classes index without bounds checks.
 *
 * <p>
 * Almost every block/item in this mod is a single registry entry with a metadata subtype table, and those tables
 * are read positionally ({@code rocks[meta]}, {@code tiers[meta]}, {@code blockLimestones[meta]}). A mismatch
 * between the <em>n</em> parallel arrays is an {@code ArrayIndexOutOfBoundsException} at runtime, usually inside a
 * renderer, which is the hardest possible place to diagnose. These assertions make the invariants explicit and
 * cost nothing.
 * </p>
 *
 * <p>
 * Nothing here touches a Minecraft registry, so it runs headlessly; the in-game test covers the registered names.
 * </p>
 */
public class RegistrationInvariantsTest {

    @Test
    public void modAnnotationAndConstantsAgree() {
        Mod mod = ArtificeCore.class.getAnnotation(Mod.class);
        Assert.assertNotNull("ArtificeCore is missing its @Mod annotation", mod);

        // Deliberately not pinning the literal mod id here: the planned "Artifice" -> "artifice" rename should touch
        // one place, not break the test suite. What must never drift is the annotation/constant pair and the
        // @Instance value that FML uses for instance injection.
        Assert.assertEquals(mod.modid(), ArtificeCore.modID);
        Assert.assertEquals(mod.name(), ArtificeCore.modName);
        // The annotation reads the constant now, so this cannot drift - but the constant is the build's git version
        // (Tags.VERSION, the same token mcmod.info quotes), so pin that it is still the token and not a literal that
        // was typed in once and left behind. A hardcoded string differs from Tags.VERSION as soon as the build stamps
        // a different tag, which is the failure this catches.
        Assert.assertEquals(mod.version(), ArtificeCore.modVersion);
        Assert.assertEquals(
            "the mod version must be the build's git version, not a literal",
            Tags.VERSION,
            ArtificeCore.modVersion);
        Assert.assertFalse(
            "the git version should not be blank",
            ArtificeCore.modVersion.trim()
                .isEmpty());

        Mod.Instance instance = findInstanceAnnotation();
        Assert.assertNotNull("@Instance is missing on ArtificeCore.instance", instance);
        Assert.assertEquals(mod.modid(), instance.value());
    }

    @Test
    public void tierTableMatchesTheFourFrameAndWallVariants() {
        Assert.assertEquals(4, ArtificeConfig.tiers.length);
        Assert.assertEquals("Basic", ArtificeConfig.tiers[0]);
        Assert.assertEquals("Reinforced", ArtificeConfig.tiers[1]);
        Assert.assertEquals("Industrial", ArtificeConfig.tiers[2]);
        Assert.assertEquals("Advanced", ArtificeConfig.tiers[3]);
    }

    @Test
    public void rockSubtypeTableMatchesTheSixRockTextures() {
        Assert.assertEquals(6, ArtificeConfig.rocks.length);
        Assert.assertEquals("", ArtificeConfig.rocks[0]);
        Assert.assertEquals("Cobblestone", ArtificeConfig.rocks[1]);
        Assert.assertEquals("Brick", ArtificeConfig.rocks[2]);
        Assert.assertEquals("Paver", ArtificeConfig.rocks[3]);
        Assert.assertEquals("Antipaver", ArtificeConfig.rocks[4]);
        Assert.assertEquals("Chiseled", ArtificeConfig.rocks[5]);
    }

    @Test
    public void stoneTypeArraysAreParallel() {
        // Basalt + marble + 7 limestones = 9 worldgen rock blocks, addressed by index in several places.
        Assert.assertEquals(9, ArtificeConfig.rockNames.length);
        Assert.assertEquals(9, ArtificeBlocks.rockBlockNames.length);
        // The colour tables only cover the seven limestones: basalt and marble use the 0xFFFFFF default.
        Assert.assertEquals(7, ArtificeBlocks.rockColorNames.length);
        Assert.assertEquals(7, ArtificeBlocks.rockColors.length);
        Assert.assertEquals(9, ArtificeBlocks.rockBlocks.length);
        Assert.assertEquals(7, ArtificeBlocks.blockLimestones.length);
        Assert.assertEquals(ArtificeBlocks.rockBlockNames.length - 2, ArtificeBlocks.blockLimestones.length);
        Assert.assertEquals(7, ArtificeBlocks.blockLimestoneSlabs.length);
        Assert.assertEquals(7, ArtificeBlocks.blockLimestoneDoubleSlabs.length);
        Assert.assertEquals(7, ArtificeBlocks.blockLimestoneBrickStairs.length);
        Assert.assertEquals(7, ArtificeBlocks.blockLimestoneCobbleStairs.length);
    }

    @Test
    public void limestoneColourOrderMatchesTheBiomeSelectionTable() {
        // WorldGenCluster/WorldGenCave pick a limestone variant by biome using hardcoded indices 0..6 into
        // blockLimestones. Keep the names and the palette aligned with those indices.
        String[] expected = { "gray", "lightgray", "brown", "tan", "reddish", "bluish", "greenish" };
        Assert.assertArrayEquals(expected, ArtificeBlocks.rockColorNames);

        Assert.assertEquals(7, ArtificeBlocks.rockColors.length);
        Assert.assertEquals(11579568, ArtificeBlocks.rockColors[0]);
        Assert.assertEquals(15853509, ArtificeBlocks.rockColors[3]);
    }

    @Test
    public void oreTableOrderMatchesTheHardcodedSparkleIndices() {
        // BlockOre.INDEX_REDSTONE / INDEX_ENDER drive the two particle effects and are indices into this table, as are
        // the rockBlockNames lookups in BlockRock and ItemBlockOre. All of them depend on this exact ordering.
        Assert.assertEquals(15, ArtificeBlocks.oreNames.length);
        Assert.assertEquals("oreRedstone", ArtificeBlocks.oreNames[5]);
        Assert.assertEquals("oreEnder", ArtificeBlocks.oreNames[13]);
        Assert.assertEquals("oreUranium", ArtificeBlocks.oreNames[14]);

        Assert.assertEquals(ArtificeBlocks.oreNames.length, ArtificeBlocks.blockOres.length);
        Assert.assertEquals(ArtificeBlocks.oreNames.length, ArtificeBlocks.blockOresGlowing.length);
    }

    @Test
    public void lampTablesMatchTheColourEnum() {
        Assert.assertEquals(16, ArtificeBlocks.blockLamps.length);
        Assert.assertEquals(16, ArtificeBlocks.blockLampsInverted.length);
        Assert.assertEquals(16, ArtificeBlocks.blockColoredTorches.length);
        Assert.assertEquals(MinecraftColors.values().length, ArtificeBlocks.blockLamps.length);
    }

    @Test
    public void floraTableMatchesTheBlockFlowerSubtypes() {
        Assert.assertEquals(5, ArtificeConfig.flora.length);
        Assert.assertEquals("Bluebell", ArtificeConfig.flora[0]);
        Assert.assertEquals("Lotus", ArtificeConfig.flora[3]);
        Assert.assertEquals("LotusClosed", ArtificeConfig.flora[4]);
    }

    @Test
    public void upgradeItemDamageValuesMapOntoTheEnchantmentTable() {
        // ItemUpgrade exposes 13 damage values (0 = the split Sharpening Kit, 1..12 = EnumUpgrades[1..12]).
        // RecipeUpgrade indexes EnumUpgrades with getItemDamage() + 1, so the table must be exactly one longer.
        Assert.assertEquals(14, EnumUpgrades.values().length);
        Assert.assertEquals(0, EnumUpgrades.SharpeningKitS.ordinal());
        Assert.assertEquals(1, EnumUpgrades.SharpeningKitE.ordinal());
        Assert.assertEquals(2, EnumUpgrades.Reinforcement.ordinal());
        Assert.assertEquals(13, EnumUpgrades.DiveKit.ordinal());
    }

    @Test
    public void toolMaterialsCoverEverySledgeAndSickleTier() {
        // ItemSledge/ItemSickle switch on the material; an unmapped material returns 0 for both damage and radius.
        ToolMaterial[] materials = { ToolMaterial.WOOD, ToolMaterial.STONE, ToolMaterial.IRON, ToolMaterial.GOLD,
            ToolMaterial.EMERALD };
        Assert.assertEquals(5, materials.length);

        for (ToolMaterial material : materials) {
            Assert.assertNotNull(material.name(), material);
            Assert.assertTrue(material.name() + " must have uses", material.getMaxUses() > 0);
        }
    }

    private static Mod.Instance findInstanceAnnotation() {
        try {
            return ArtificeCore.class.getField("instance")
                .getAnnotation(Mod.Instance.class);
        } catch (NoSuchFieldException e) {
            throw new AssertionError(e);
        }
    }
}
