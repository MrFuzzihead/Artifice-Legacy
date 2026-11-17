package shukaro.artifice.block.world;

import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.block.ItemBlockArtifice;

public class ItemBlockRock extends ItemBlockArtifice {
   public ItemBlockRock(Block block) {
      super(block);
   }

   public String getUnlocalizedName(ItemStack stack) {
      if (stack.getItemDamage() == 0) {
         return Block.getBlockFromItem(stack.getItem()).getUnlocalizedName();
      } else {
         return stack.getItemDamage() >= ArtificeConfig.rocks.length
            ? Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + "." + ArtificeConfig.rocks[0].toLowerCase(Locale.ENGLISH)
            : Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + "." + ArtificeConfig.rocks[stack.getItemDamage()].toLowerCase(Locale.ENGLISH);
      }
   }
}
