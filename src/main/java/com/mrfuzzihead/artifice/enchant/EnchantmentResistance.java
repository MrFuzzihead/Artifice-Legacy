package shukaro.artifice.enchant;

import java.util.Locale;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;

public class EnchantmentResistance extends Enchantment {
   public EnchantmentResistance(int eid, int weight) {
      super(eid, weight, EnumEnchantmentType.armor);
      this.setName("Artifice".toLowerCase(Locale.ENGLISH) + ".resistance");
      Enchantment.addToBookList(this);
   }

   public int getMaxLevel() {
      return 3;
   }

   public int getMinEnchantability(int level) {
      return 10 + 20 * (level - 1);
   }

   public int getMaxEnchantability(int level) {
      return this.getMinEnchantability(level) + 50;
   }
}
