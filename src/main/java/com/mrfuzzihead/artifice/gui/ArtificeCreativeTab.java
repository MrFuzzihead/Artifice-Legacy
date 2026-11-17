package com.mrfuzzihead.artifice.gui;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeEnchants;

public class ArtificeCreativeTab extends CreativeTabs {

    public ArtificeCreativeTab(String label) {
        super(label);
    }

    public Item getTabIconItem() {
        if (this.getTabLabel()
            .equals(ArtificeCore.mainTab.getTabLabel())) {
            return Item.getItemFromBlock(ArtificeBlocks.blockFrame);
        } else {
            return this.getTabLabel()
                .equals(ArtificeCore.worldTab.getTabLabel()) ? Item.getItemFromBlock(ArtificeBlocks.blockBasalt) : null;
        }
    }

    public String getTranslatedTabLabel() {
        return this.getTabLabel();
    }

    public void displayAllReleventItems(List itemList) {
        super.displayAllReleventItems(itemList);
        if (this.getTabLabel()
            .equals(ArtificeCore.mainTab.getTabLabel())) {
            if (ArtificeEnchants.hasInvisible()) {
                itemList.add(
                    Items.enchanted_book.getEnchantedItemStack(
                        new EnchantmentData(
                            ArtificeEnchants.enchantmentInvisible.effectId,
                            ArtificeEnchants.enchantmentInvisible.getMaxLevel())));
            }

            if (ArtificeEnchants.hasSoulstealing()) {
                for (int i = 1; i < ArtificeEnchants.enchantmentSoulstealing.getMaxLevel() + 1; i++) {
                    itemList.add(
                        Items.enchanted_book.getEnchantedItemStack(
                            new EnchantmentData(ArtificeEnchants.enchantmentSoulstealing.effectId, i)));
                }
            }

            if (ArtificeEnchants.hasResistance()) {
                for (int i = 1; i < ArtificeEnchants.enchantmentResistance.getMaxLevel() + 1; i++) {
                    itemList.add(
                        Items.enchanted_book.getEnchantedItemStack(
                            new EnchantmentData(ArtificeEnchants.enchantmentResistance.effectId, i)));
                }
            }
        }
    }
}
