package com.mrfuzzihead.artifice;

import net.minecraft.enchantment.Enchantment;

import com.mrfuzzihead.artifice.enchant.EnchantmentInvisible;
import com.mrfuzzihead.artifice.enchant.EnchantmentResistance;
import com.mrfuzzihead.artifice.enchant.EnchantmentSoulstealing;

public class ArtificeEnchants {

    public static EnchantmentInvisible enchantmentInvisible;
    public static EnchantmentSoulstealing enchantmentSoulstealing;
    public static EnchantmentResistance enchantmentResistance;

    /**
     * Registers the three enchantments at the lowest ids at or after {@link ArtificeConfig#enchantmentStartID} that
     * are free in {@link Enchantment#enchantmentsList}, in the order invisible, soulstealing, resistance.
     *
     * <p>
     * The id has to be chosen and verified here rather than by constructing and catching - vanilla's
     * {@code Enchantment(int, int, EnumEnchantmentType)} constructor throws {@code IllegalArgumentException} on a
     * duplicate id, which used to be the only collision test. Id-conflict detector coremods (EndlessIDs, and the
     * conflict reporters in most large packs) replace that constructor with a hook that records the duplicate and
     * overwrites the slot instead of throwing, so the constructor stops being a signal: all three probes used to
     * succeed on their first try and every enchantment landed on the same id, leaving three effects collapsed into
     * one and the losers unreachable.
     * </p>
     *
     * <p>
     * In a clean environment this assigns exactly the ids the old probe produced (the lowest free id at or after the
     * start id, in registration order), so existing worlds keep their enchantments. The array length is read at run
     * time because mods are allowed to replace or grow the store.
     * </p>
     */
    public static void initEnchants() {
        // Ids claimed during this pass, so the three probes cannot converge on one id even if the constructor does
        // not write the slot into enchantmentsList itself.
        final boolean[] claimed = new boolean[Enchantment.enchantmentsList.length];

        if (ArtificeConfig.enchantmentInvisibleEnable) {
            final int id = findFreeEnchantID(ArtificeConfig.enchantmentStartID, claimed);
            if (id < 0) {
                ArtificeCore.logger.warn("No available enchantment IDs for invisible enchant!");
            } else {
                claimed[id] = true;
                try {
                    enchantmentInvisible = new EnchantmentInvisible(id, ArtificeConfig.enchantmentInvisibleWeight);
                    ArtificeCore.logger.info("Registered invisible enchant to ID " + id);
                } catch (IllegalArgumentException e) {
                    ArtificeCore.logger.error("Invisible enchant refused id " + id + ", it is disabled", e);
                }
            }
        }

        if (ArtificeConfig.enchantmentSoulstealingEnable) {
            final int id = findFreeEnchantID(ArtificeConfig.enchantmentStartID, claimed);
            if (id < 0) {
                ArtificeCore.logger.warn("No available enchantment IDs for soulstealing enchant!");
            } else {
                claimed[id] = true;
                try {
                    enchantmentSoulstealing = new EnchantmentSoulstealing(
                        id,
                        ArtificeConfig.enchantmentSoulstealingWeight);
                    ArtificeCore.logger.info("Registered soulstealing enchant to ID " + id);
                } catch (IllegalArgumentException e) {
                    ArtificeCore.logger.error("Soulstealing enchant refused id " + id + ", it is disabled", e);
                }
            }
        }

        if (ArtificeConfig.enchantmentResistanceEnable) {
            final int id = findFreeEnchantID(ArtificeConfig.enchantmentStartID, claimed);
            if (id < 0) {
                ArtificeCore.logger.warn("No available enchantment IDs for resistance enchant!");
            } else {
                claimed[id] = true;
                try {
                    enchantmentResistance = new EnchantmentResistance(id, ArtificeConfig.enchantmentResistanceWeight);
                    ArtificeCore.logger.info("Registered resistance enchant to ID " + id);
                } catch (IllegalArgumentException e) {
                    ArtificeCore.logger.error("Resistance enchant refused id " + id + ", it is disabled", e);
                }
            }
        }
    }

    /**
     * @return the lowest id at or after {@code startID} that is neither occupied in
     *         {@link Enchantment#enchantmentsList} nor already claimed during this pass, or {@code -1} when the store
     *         is full.
     */
    private static int findFreeEnchantID(int startID, boolean[] claimed) {
        final Enchantment[] registered = Enchantment.enchantmentsList;
        // Mods that relocate this store can leave the two arrays different lengths; trust the shorter one.
        final int limit = Math.min(claimed.length, registered.length);
        int id = Math.max(0, startID);
        while (id < limit && (registered[id] != null || claimed[id])) {
            id++;
        }
        return id < limit ? id : -1;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Availability
    //
    // Registration is allowed to fail - the id window can be full in a large pack - and every use site below used
    // to dereference the field unconditionally, so "enabled in the config" is not the same question as "registered".
    // These three answer the second one.
    // ---------------------------------------------------------------------------------------------------------

    public static boolean hasInvisible() {
        return ArtificeConfig.enchantmentInvisibleEnable && enchantmentInvisible != null;
    }

    public static boolean hasSoulstealing() {
        return ArtificeConfig.enchantmentSoulstealingEnable && enchantmentSoulstealing != null;
    }

    public static boolean hasResistance() {
        return ArtificeConfig.enchantmentResistanceEnable && enchantmentResistance != null;
    }
}
