package shukaro.artifice.block.functional;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeRegistry;
import shukaro.artifice.ArtificeTooltips;
import shukaro.artifice.util.NameMetaPair;

public class ItemBlockAttunedRedstone extends ItemBlock {
   public ItemBlockAttunedRedstone(Block block) {
      super(block);
   }

   public EnumRarity getRarity(ItemStack stack) {
      return stack.hasDisplayName() ? EnumRarity.rare : EnumRarity.common;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
      if (ArtificeConfig.tooltips) {
         NameMetaPair pair = new NameMetaPair(stack.getItem(), stack.getItemDamage());
         if (stack.hasDisplayName()) {
            infoList.add(
               StatCollector.translateToLocal("tooltip.artifice.attuned")
                  + " "
                  + (
                     stack.getUnlocalizedName().equals(ArtificeBlocks.blockAttunedRedstoneReceiver.getUnlocalizedName())
                        ? StatCollector.translateToLocal("tooltip.artifice.attunedreciever")
                        : StatCollector.translateToLocal("tooltip.artifice.attunedtransmitter")
                  )
            );
         }

         if (ArtificeRegistry.getTooltipMap().containsKey(pair)) {
            for (String s : ArtificeRegistry.getTooltipMap().get(pair)) {
               if (ArtificeConfig.flavorText || !s.startsWith(ArtificeTooltips.commentCode)) {
                  infoList.add(s);
               }
            }
         }
      }
   }
}
