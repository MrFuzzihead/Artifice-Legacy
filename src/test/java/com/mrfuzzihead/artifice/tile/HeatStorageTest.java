package com.mrfuzzihead.artifice.tile;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link HeatStorage}, the buffer used by the heating coil and the nuclear battery to talk to furnaces.
 *
 * <p>
 * The invariants are easy to get wrong and impossible to see in game: heat must never exceed the capacity, must
 * never go negative, and must survive a chunk save/load. {@code readFromNBT} clamps an oversized stored value but
 * the write side clamps a negative one, so a corrupted save can only ever lose energy, never create it.
 * </p>
 */
public class HeatStorageTest {

    @Test
    public void startsEmpty() {
        HeatStorage storage = new HeatStorage(1000);
        Assert.assertEquals(0, storage.getHeat());
        Assert.assertEquals(1000, storage.getMaxHeat());
    }

    @Test
    public void receiveIsClampedToTheRemainingCapacity() {
        HeatStorage storage = new HeatStorage(100);

        Assert.assertEquals(40, storage.receiveHeat(40));
        Assert.assertEquals(40, storage.getHeat());

        Assert.assertEquals(60, storage.receiveHeat(1000));
        Assert.assertEquals(100, storage.getHeat());

        Assert.assertEquals(0, storage.receiveHeat(1));
        Assert.assertEquals(100, storage.getHeat());
    }

    @Test
    public void extractIsClampedToTheStoredAmount() {
        HeatStorage storage = new HeatStorage(100);
        storage.receiveHeat(30);

        Assert.assertEquals(10, storage.extractHeat(10));
        Assert.assertEquals(20, storage.getHeat());

        Assert.assertEquals(20, storage.extractHeat(1000));
        Assert.assertEquals(0, storage.getHeat());

        Assert.assertEquals(0, storage.extractHeat(1));
    }

    @Test
    public void nbtRoundTripPreservesHeat() {
        HeatStorage storage = new HeatStorage(500);
        storage.receiveHeat(123);

        NBTTagCompound tag = new NBTTagCompound();
        storage.writeToNBT(tag);

        HeatStorage loaded = new HeatStorage(500);
        loaded.readFromNBT(tag);

        Assert.assertEquals(123, loaded.getHeat());
    }

    @Test
    public void writeClampsNegativeHeatToZero() {
        HeatStorage storage = new HeatStorage(100);
        storage.readFromNBT(withHeat(-50));

        NBTTagCompound tag = new NBTTagCompound();
        storage.writeToNBT(tag);

        Assert.assertEquals(0, tag.getInteger("Heat"));
    }

    @Test
    public void readClampsOverCapacityHeatToTheMaximum() {
        HeatStorage storage = new HeatStorage(100);
        storage.readFromNBT(withHeat(5000));

        Assert.assertEquals(100, storage.getHeat());
    }

    @Test
    public void missingHeatTagReadsAsEmpty() {
        HeatStorage storage = new HeatStorage(100);
        storage.receiveHeat(50);
        storage.readFromNBT(new NBTTagCompound());

        Assert.assertEquals(0, storage.getHeat());
    }

    private static NBTTagCompound withHeat(int heat) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Heat", heat);
        return tag;
    }
}
