package com.mrfuzzihead.artifice.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Covers {@link RomanUtil}, which renders enchantment levels in the tooltips and in the upgrade-chat messages.
 */
public class RomanUtilTest {

    @Test
    public void convertsTheCanonicalNumerals() {
        Assert.assertEquals("", RomanUtil.convertToRoman(0));
        Assert.assertEquals("I", RomanUtil.convertToRoman(1));
        Assert.assertEquals("II", RomanUtil.convertToRoman(2));
        Assert.assertEquals("III", RomanUtil.convertToRoman(3));
        Assert.assertEquals("IV", RomanUtil.convertToRoman(4));
        Assert.assertEquals("V", RomanUtil.convertToRoman(5));
        Assert.assertEquals("IX", RomanUtil.convertToRoman(9));
        Assert.assertEquals("X", RomanUtil.convertToRoman(10));
        Assert.assertEquals("XIV", RomanUtil.convertToRoman(14));
        Assert.assertEquals("XL", RomanUtil.convertToRoman(40));
        Assert.assertEquals("XLIX", RomanUtil.convertToRoman(49));
        Assert.assertEquals("L", RomanUtil.convertToRoman(50));
        Assert.assertEquals("XC", RomanUtil.convertToRoman(90));
        Assert.assertEquals("CD", RomanUtil.convertToRoman(400));
        Assert.assertEquals("CM", RomanUtil.convertToRoman(900));
        Assert.assertEquals("M", RomanUtil.convertToRoman(1000));
        Assert.assertEquals("MCMXC", RomanUtil.convertToRoman(1990));
        Assert.assertEquals("MMMCMXCIX", RomanUtil.convertToRoman(3999));
    }

    @Test
    public void neverUsesMoreThanThreeRepeats() {
        for (int i = 1; i <= 3999; i++) {
            String roman = RomanUtil.convertToRoman(i);
            Assert.assertFalse(roman + " for " + i, roman.contains("IIII"));
            Assert.assertFalse(roman + " for " + i, roman.contains("VV"));
            Assert.assertFalse(roman + " for " + i, roman.contains("XXXX"));
            Assert.assertFalse(roman + " for " + i, roman.contains("LL"));
            Assert.assertFalse(roman + " for " + i, roman.contains("CCCC"));
            Assert.assertFalse(roman + " for " + i, roman.contains("DD"));
        }
    }

    @Test
    public void handlesEveryValueInTheEnchantmentRange() {
        // Enchantment levels are 1..10 in practice; none of them may produce an empty or multi-character-per-digit
        // numeral, because the tooltip renders the result verbatim.
        for (int i = 1; i <= 20; i++) {
            String roman = RomanUtil.convertToRoman(i);
            Assert.assertFalse("empty numeral for " + i, roman.isEmpty());
            Assert.assertTrue(roman + " for " + i, roman.length() <= 8);
        }
    }
}
