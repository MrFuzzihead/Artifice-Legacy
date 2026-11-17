package com.mrfuzzihead.artifice.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link MinecraftColors}, the 16-colour table that drives the lamp blocks' tint.
 */
public class MinecraftColorsTest {

    @Test
    public void hasTheSixteenMinecraftLampColoursInMetadataOrder() {
        Assert.assertEquals(16, MinecraftColors.values().length);
        Assert.assertEquals(MinecraftColors.White, MinecraftColors.values()[0]);
        Assert.assertEquals(MinecraftColors.Black, MinecraftColors.values()[15]);
    }

    @Test
    public void toNormalParsesTheHexTable() {
        Assert.assertEquals(0xDDDDDD, MinecraftColors.White.toNormal());
        Assert.assertEquals(0x191616, MinecraftColors.Black.toNormal());
        Assert.assertEquals(0x404040, MinecraftColors.Gray.toNormal());
    }

    @Test
    public void toAdjustedShiftsEveryChannelByTwiceTheAmount() {
        // 0xDD + 2 * 5 = 0xE7
        Assert.assertEquals(0xE7E7E7, MinecraftColors.White.toAdjusted(5));
        // 0x19 - 2 * 40 = -55, clamped to 0
        Assert.assertEquals(0x000000, MinecraftColors.Black.toAdjusted(-40));
    }

    @Test
    public void toAdjustedClampsAtBothEnds() {
        Assert.assertEquals(0xFFFFFF, MinecraftColors.White.toAdjusted(1000));
        Assert.assertEquals(0x000000, MinecraftColors.White.toAdjusted(-1000));
    }

    @Test
    public void toAdjustedPadsSingleDigitChannels() {
        // Green is 0x35461B. A -16 adjustment subtracts 32 from each channel: 0x35->0x15, 0x46->0x26, 0x1B->0.
        Assert.assertEquals(0x152600, MinecraftColors.Green.toAdjusted(-16));
    }
}
