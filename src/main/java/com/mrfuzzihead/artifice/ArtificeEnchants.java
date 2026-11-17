package shukaro.artifice;

import shukaro.artifice.enchant.EnchantmentInvisible;
import shukaro.artifice.enchant.EnchantmentResistance;
import shukaro.artifice.enchant.EnchantmentSoulstealing;

public class ArtificeEnchants {
   public static EnchantmentInvisible enchantmentInvisible;
   public static EnchantmentSoulstealing enchantmentSoulstealing;
   public static EnchantmentResistance enchantmentResistance;

   public static void initEnchants() {
      if (ArtificeConfig.enchantmentInvisibleEnable) {
         for (int id = ArtificeConfig.enchantmentStartID; id < 256; id++) {
            try {
               enchantmentInvisible = new EnchantmentInvisible(id, ArtificeConfig.enchantmentInvisibleWeight);
               ArtificeCore.logger.info("Registered invisible enchant to ID " + id);
               break;
            } catch (IllegalArgumentException var4) {
            }
         }

         if (enchantmentInvisible == null) {
            ArtificeCore.logger.warn("No available enchantment IDs for invisible enchant!");
         }
      }

      if (ArtificeConfig.enchantmentSoulstealingEnable) {
         for (int id = ArtificeConfig.enchantmentStartID; id < 256; id++) {
            try {
               enchantmentSoulstealing = new EnchantmentSoulstealing(id, ArtificeConfig.enchantmentSoulstealingWeight);
               ArtificeCore.logger.info("Registered soulstealing enchant to ID " + id);
               break;
            } catch (IllegalArgumentException var3) {
            }
         }

         if (enchantmentSoulstealing == null) {
            ArtificeCore.logger.warn("No available enchantment IDs for soulstealing enchant!");
         }
      }

      if (ArtificeConfig.enchantmentResistanceEnable) {
         for (int id = ArtificeConfig.enchantmentStartID; id < 256; id++) {
            try {
               enchantmentResistance = new EnchantmentResistance(id, ArtificeConfig.enchantmentResistanceWeight);
               ArtificeCore.logger.info("Registered resistance enchant to ID " + id);
               break;
            } catch (IllegalArgumentException var2) {
            }
         }

         if (enchantmentResistance == null) {
            ArtificeCore.logger.warn("No available enchantment IDs for resistance enchant!");
         }
      }
   }
}
