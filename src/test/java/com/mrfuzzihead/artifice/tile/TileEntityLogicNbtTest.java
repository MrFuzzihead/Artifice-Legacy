package com.mrfuzzihead.artifice.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Covers {@link TileEntityLogic}'s persisted side configuration.
 *
 * <p>
 * The six {@code SideState} values are saved as an {@code int[]} under the {@code sides} key. That array is read
 * back positionally with no length check, so a save that is missing the key (an old world, a datapack tool, or a
 * future refactor that renames it) throws {@code ArrayIndexOutOfBoundsException} instead of defaulting. See
 * CODE_REVIEW.md section 5.10.
 * </p>
 */
public class TileEntityLogicNbtTest {

    private static final String TEST_ID = "artifice_test_logic";

    /**
     * {@code TileEntity#writeToNBT} refuses to serialise a class it has no id mapping for, which a headless JVM has
     * not registered (registration normally happens in {@code ArtificeTiles#initTiles}). Registering a throwaway id
     * here keeps the test independent of the mod's preInit.
     */
    @BeforeClass
    public static void registerMapping() {
        TileEntity.addMapping(TileEntityLogic.class, TEST_ID);
    }

    @Test
    public void constructorDisablesEverySide() {
        TileEntityLogic logic = new TileEntityLogic();

        Assert.assertEquals(6, logic.sides.length);
        for (TileEntityLogic.SideState state : logic.sides) {
            Assert.assertEquals(TileEntityLogic.SideState.DISABLED, state);
        }
        Assert.assertEquals(0, logic.power);
    }

    @Test
    public void nbtRoundTripPreservesSidesAndPower() {
        TileEntityLogic logic = new TileEntityLogic();
        logic.sides[0] = TileEntityLogic.SideState.OUTPUT;
        logic.sides[2] = TileEntityLogic.SideState.POSITIVE;
        logic.sides[5] = TileEntityLogic.SideState.NEGATIVE;
        logic.power = 11;

        NBTTagCompound tag = new NBTTagCompound();
        logic.writeToNBT(tag);

        TileEntityLogic loaded = new TileEntityLogic();
        loaded.readFromNBT(tag);

        Assert.assertArrayEquals(logic.sides, loaded.sides);
        Assert.assertEquals(11, loaded.power);
    }

    @Test
    public void nbtStoresSidesAsOrdinals() {
        TileEntityLogic logic = new TileEntityLogic();
        logic.sides[1] = TileEntityLogic.SideState.NEGATIVE;

        NBTTagCompound tag = new NBTTagCompound();
        logic.writeToNBT(tag);

        int[] stored = tag.getIntArray("sides");
        Assert.assertEquals(6, stored.length);
        Assert.assertEquals(TileEntityLogic.SideState.NEGATIVE.ordinal(), stored[1]);
    }

    /**
     * Regression (CODE_REVIEW 5.10): reading a tile entity that predates the {@code sides} key (or lost it) must
     * fall back to the constructor defaults rather than throwing out of chunk load.
     */
    @Test
    public void readToleratesAMissingSidesTag() {
        TileEntityLogic loaded = new TileEntityLogic();
        loaded.readFromNBT(new NBTTagCompound());

        for (TileEntityLogic.SideState state : loaded.sides) {
            Assert.assertEquals(TileEntityLogic.SideState.DISABLED, state);
        }
    }

    @Test
    public void readToleratesAShortSidesTag() {
        TileEntityLogic logic = new TileEntityLogic();
        logic.sides[1] = TileEntityLogic.SideState.OUTPUT;

        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray(
            "sides",
            new int[] { TileEntityLogic.SideState.NEGATIVE.ordinal(), TileEntityLogic.SideState.POSITIVE.ordinal() });

        TileEntityLogic loaded = new TileEntityLogic();
        loaded.readFromNBT(tag);

        Assert.assertEquals(TileEntityLogic.SideState.NEGATIVE, loaded.sides[0]);
        Assert.assertEquals(TileEntityLogic.SideState.POSITIVE, loaded.sides[1]);
        for (int i = 2; i < 6; i++) {
            Assert.assertEquals(
                "side " + i + " had no stored value",
                TileEntityLogic.SideState.DISABLED,
                loaded.sides[i]);
        }
    }

    @Test
    public void readToleratesOutOfRangeOrdinals() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray(
            "sides",
            new int[] { -1, 99, 0, Integer.MAX_VALUE, TileEntityLogic.SideState.NEGATIVE.ordinal(), 3 });

        TileEntityLogic loaded = new TileEntityLogic();
        loaded.readFromNBT(tag);

        Assert.assertEquals(TileEntityLogic.SideState.DISABLED, loaded.sides[0]);
        Assert.assertEquals(TileEntityLogic.SideState.DISABLED, loaded.sides[1]);
        Assert.assertEquals(TileEntityLogic.SideState.DISABLED, loaded.sides[2]);
        Assert.assertEquals(TileEntityLogic.SideState.DISABLED, loaded.sides[3]);
        Assert.assertEquals(TileEntityLogic.SideState.NEGATIVE, loaded.sides[4]);
        Assert.assertEquals(TileEntityLogic.SideState.values()[3], loaded.sides[5]);
    }

    @Test
    public void readToleratesAnEmptySidesTag() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("sides", new int[0]);

        TileEntityLogic loaded = new TileEntityLogic();
        loaded.readFromNBT(tag);

        for (TileEntityLogic.SideState state : loaded.sides) {
            Assert.assertEquals(TileEntityLogic.SideState.DISABLED, state);
        }
    }
}
