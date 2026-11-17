package shukaro.artifice.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeRegistry;
import shukaro.artifice.ArtificeTooltips;
import shukaro.artifice.util.NameMetaPair;

public class ItemBlockArtifice extends ItemBlock {
   public ItemBlockArtifice(Block block) {
      super(block);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int meta) {
      return meta;
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
      if (ArtificeConfig.tooltips) {
         NameMetaPair pair = new NameMetaPair(stack.getItem(), stack.getItemDamage());
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
