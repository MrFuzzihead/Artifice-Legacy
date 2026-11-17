package shukaro.artifice.recipe;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeItems;

public class RecipeUpgrade implements IRecipe {
   private ItemStack output;

   public boolean matches(InventoryCrafting craft, World world) {
      this.output = null;
      int numTools = 0;

      for (int i = 0; i < craft.getSizeInventory(); i++) {
         ItemStack stack = craft.getStackInSlot(i);
         if (stack != null) {
            if (!stack.getItem().isItemTool(stack) && !stack.getItem().equals(ArtificeItems.itemUpgrade)) {
               return false;
            }

            if (stack.getItem().isItemTool(stack)) {
               numTools++;
            }
         }
      }

      if (numTools != 1) {
         return false;
      } else {
         for (int ix = 0; ix < craft.getSizeInventory(); ix++) {
            ItemStack stack = craft.getStackInSlot(ix);
            if (stack != null && stack.getItem().isItemTool(stack)) {
               this.output = stack.copy();
            }
         }

         int numUpgrades = 0;

         for (int ixx = 0; ixx < craft.getSizeInventory(); ixx++) {
            ItemStack stack = craft.getStackInSlot(ixx);
            if (stack != null && stack.getItem().equals(ArtificeItems.itemUpgrade)) {
               numUpgrades++;
            }
         }

         if (numUpgrades == 0) {
            return false;
         } else if (this.output == null) {
            return false;
         } else {
            for (int ixxx = 0; ixxx < craft.getSizeInventory(); ixxx++) {
               ItemStack stack = craft.getStackInSlot(ixxx);
               if (stack != null && stack.getItem().equals(ArtificeItems.itemUpgrade)) {
                  int maxLevel = 0;
                  Enchantment enchant;
                  if (stack.getItemDamage() != 0) {
                     enchant = EnumUpgrades.values()[stack.getItemDamage() + 1].enchant;
                     if (ArtificeConfig.limitUpgrades) {
                        maxLevel = EnumUpgrades.values()[stack.getItemDamage() + 1].maxLevel;
                     } else {
                        maxLevel = EnumUpgrades.values()[stack.getItemDamage() + 1].enchant.getMaxLevel();
                     }
                  } else {
                     if (Enchantment.sharpness.canApply(this.output) && Enchantment.efficiency.canApply(this.output)) {
                        if (ixxx % 2 == 0) {
                           enchant = Enchantment.sharpness;
                        } else {
                           enchant = Enchantment.efficiency;
                        }
                     } else if (Enchantment.sharpness.canApply(this.output)) {
                        enchant = Enchantment.sharpness;
                     } else {
                        if (!Enchantment.efficiency.canApply(this.output)) {
                           return false;
                        }

                        enchant = Enchantment.efficiency;
                     }

                     if (ArtificeConfig.limitUpgrades) {
                        if (enchant == Enchantment.sharpness) {
                           maxLevel = EnumUpgrades.SharpeningKitS.maxLevel;
                        } else {
                           maxLevel = EnumUpgrades.SharpeningKitE.maxLevel;
                        }
                     } else if (enchant == Enchantment.sharpness) {
                        maxLevel = EnumUpgrades.SharpeningKitS.enchant.getMaxLevel();
                     } else {
                        maxLevel = EnumUpgrades.SharpeningKitE.enchant.getMaxLevel();
                     }
                  }

                  if (!enchant.canApply(this.output) || this.output.getItem().getItemEnchantability() <= 0) {
                     return false;
                  }

                  if (EnchantmentHelper.getEnchantmentLevel(enchant.effectId, this.output) >= maxLevel) {
                     return false;
                  }

                  if (EnchantmentHelper.getEnchantmentLevel(enchant.effectId, this.output) == 0) {
                     if (this.output.stackTagCompound != null) {
                        NBTTagList enchants = (NBTTagList)this.output.stackTagCompound.getTag("ench");
                        if (enchants != null && enchants.tagCount() > 0) {
                           for (int k = 0; k < enchants.tagCount(); k++) {
                              NBTTagCompound tag = enchants.getCompoundTagAt(k);
                              Enchantment other = Enchantment.enchantmentsList[tag.getShort("id")];
                              if (!enchant.canApplyTogether(other)) {
                                 return false;
                              }
                           }
                        }
                     }

                     this.output.addEnchantment(enchant, 1);
                  } else {
                     NBTTagList enchants = (NBTTagList)this.output.stackTagCompound.getTag("ench");

                     for (int j = 0; j < enchants.tagCount(); j++) {
                        NBTTagCompound tag = enchants.getCompoundTagAt(j);
                        if (tag.getShort("id") == enchant.effectId) {
                           tag.setShort("lvl", (short)(tag.getShort("lvl") + 1));
                        }
                     }
                  }
               }
            }

            return true;
         }
      }
   }

   public ItemStack getCraftingResult(InventoryCrafting craft) {
      return this.output.copy();
   }

   public int getRecipeSize() {
      return 10;
   }

   public ItemStack getRecipeOutput() {
      return this.output;
   }
}
