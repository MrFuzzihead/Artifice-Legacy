package com.mrfuzzihead.artifice.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link FormatCodes}, the {@code §x} colour table used by every tooltip and chat message in the mod.
 *
 * <p>
 * A wrong code here is invisible in code review and very visible in game, so the codes are pinned exactly.
 * </p>
 */
public class FormatCodesTest {

    @Test
    public void legacyColourCodesAreExact() {
        Assert.assertEquals("\u00a70", FormatCodes.Black.code);
        Assert.assertEquals("\u00a71", FormatCodes.DarkBlue.code);
        Assert.assertEquals("\u00a72", FormatCodes.DarkGreen.code);
        Assert.assertEquals("\u00a73", FormatCodes.DarkAqua.code);
        Assert.assertEquals("\u00a74", FormatCodes.DarkRed.code);
        Assert.assertEquals("\u00a75", FormatCodes.Purple.code);
        Assert.assertEquals("\u00a76", FormatCodes.Gold.code);
        Assert.assertEquals("\u00a77", FormatCodes.Grey.code);
        Assert.assertEquals("\u00a78", FormatCodes.DarkGrey.code);
        Assert.assertEquals("\u00a79", FormatCodes.Indigo.code);
        Assert.assertEquals("\u00a7a", FormatCodes.BrightGreen.code);
        Assert.assertEquals("\u00a7b", FormatCodes.Aqua.code);
        Assert.assertEquals("\u00a7c", FormatCodes.Red.code);
        Assert.assertEquals("\u00a7d", FormatCodes.Pink.code);
        Assert.assertEquals("\u00a7e", FormatCodes.Yellow.code);
        Assert.assertEquals("\u00a7f", FormatCodes.White.code);
    }

    @Test
    public void styleCodesAreExact() {
        Assert.assertEquals("\u00a7k", FormatCodes.RandomChar.code);
        Assert.assertEquals("\u00a7l", FormatCodes.Bold.code);
        Assert.assertEquals("\u00a7m", FormatCodes.Strike.code);
        Assert.assertEquals("\u00a7n", FormatCodes.Underlined.code);
        Assert.assertEquals("\u00a7o", FormatCodes.Italic.code);
        Assert.assertEquals("\u00a7r", FormatCodes.Reset.code);
    }

    @Test
    public void everyCodeIsTheTwoCharacterEscapeSequence() {
        for (FormatCodes code : FormatCodes.values()) {
            Assert.assertEquals(code.name(), 2, code.code.length());
            Assert.assertEquals(code.name(), '\u00a7', code.code.charAt(0));
        }
    }

    @Test
    public void everyCodeIsUnique() {
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (FormatCodes code : FormatCodes.values()) {
            Assert.assertTrue("duplicate code for " + code, seen.add(code.code));
        }
    }
}
