package com.mrfuzzihead.artifice.enchant;

import java.util.Locale;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;

public class EnchantmentSoulstealing extends Enchantment {

    public EnchantmentSoulstealing(int eid, int weight) {
        super(eid, weight, EnumEnchantmentType.weapon);
        this.setName("Artifice".toLowerCase(Locale.ENGLISH) + ".soulstealing");
        Enchantment.addToBookList(this);
    }

    public int getMaxLevel() {
        return 3;
    }

    public int getMinEnchantability(int level) {
        return 15 + (level - 1) * 9;
    }

    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 50;
    }
}
