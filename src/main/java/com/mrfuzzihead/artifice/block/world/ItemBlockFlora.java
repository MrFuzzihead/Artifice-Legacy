package shukaro.artifice.block.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.block.ItemBlockArtifice;

public class ItemBlockFlora extends ItemBlockArtifice {
   public ItemBlockFlora(Block block) {
      super(block);
   }

   @Override
   public int getMetadata(int meta) {
      return meta >= ArtificeConfig.flora.length ? 0 : meta;
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIconFromDamage(int meta) {
      return meta >= ArtificeConfig.flora.length ? BlockFlora.icons[0] : BlockFlora.icons[meta];
   }

   public String getUnlocalizedName(ItemStack stack) {
      return stack.getItemDamage() >= ArtificeConfig.flora.length
         ? Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + "." + ArtificeConfig.flora[0].toLowerCase(Locale.ENGLISH)
         : Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + "." + ArtificeConfig.flora[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
   }
}
