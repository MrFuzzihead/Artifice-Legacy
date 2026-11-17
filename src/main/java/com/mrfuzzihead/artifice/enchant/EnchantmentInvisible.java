package com.mrfuzzihead.artifice.enchant;

import java.util.Locale;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;

public class EnchantmentInvisible extends Enchantment {

    public EnchantmentInvisible(int eid, int weight) {
        super(eid, weight, EnumEnchantmentType.armor);
        this.setName("Artifice".toLowerCase(Locale.ENGLISH) + ".invisible");
        Enchantment.addToBookList(this);
    }

    public int getMaxLevel() {
        return 1;
    }

    public int getMinEnchantability(int level) {
        return 10;
    }

    public int getMaxEnchantability(int level) {
        return this.getMinEnchantability(level) + 60;
    }
}
