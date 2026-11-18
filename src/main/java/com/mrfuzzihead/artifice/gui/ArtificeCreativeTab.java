package com.mrfuzzihead.artifice.gui;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeEnchants;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ArtificeCreativeTab extends CreativeTabs {

    public ArtificeCreativeTab(String label) {
        super(label);
    }

    // getTabIconItem, getTabLabel, getTranslatedTabLabel and displayAllReleventItems are @SideOnly(CLIENT) on
    // CreativeTabs, and FML's SideTransformer removes them from the server jar. Without the matching annotation
    // these overrides stop being overrides on a dedicated server - they become new methods that call a super method
    // which no longer exists, so any server-side mod that walks the tab list gets
    // NoSuchMethodError: CreativeTabs.displayAllReleventItems (CODE_REVIEW 5.11).
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        if (this.getTabLabel()
            .equals(ArtificeCore.mainTab.getTabLabel())) {
            return Item.getItemFromBlock(ArtificeBlocks.blockFrame);
        } else {
            return this.getTabLabel()
                .equals(ArtificeCore.worldTab.getTabLabel()) ? Item.getItemFromBlock(ArtificeBlocks.blockBasalt) : null;
        }
    }

    @SideOnly(Side.CLIENT)
    public String getTranslatedTabLabel() {
        return this.getTabLabel();
    }

    @SideOnly(Side.CLIENT)
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
