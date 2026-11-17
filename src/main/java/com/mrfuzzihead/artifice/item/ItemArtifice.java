package shukaro.artifice.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.ArtificeRegistry;
import shukaro.artifice.ArtificeTooltips;
import shukaro.artifice.util.NameMetaPair;

public abstract class ItemArtifice extends Item {
   public ItemArtifice() {
      this.setHasSubtypes(true);
      this.setCreativeTab(ArtificeCore.mainTab);
   }

   @SideOnly(Side.CLIENT)
   public abstract IIcon getIconFromDamage(int var1);

   @SideOnly(Side.CLIENT)
   public abstract void registerIcons(IIconRegister var1);

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
      if (ArtificeConfig.tooltips) {
         NameMetaPair pair = new NameMetaPair(stack.getItem(), stack.getItemDamage());
         if (ArtificeRegistry.getTooltipMap().get(pair) != null) {
            for (String s : ArtificeRegistry.getTooltipMap().get(pair)) {
               if (ArtificeConfig.flavorText || !s.startsWith(ArtificeTooltips.commentCode)) {
                  infoList.add(s);
               }
            }
         }
      }
   }
}
