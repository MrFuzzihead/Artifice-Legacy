package shukaro.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import shukaro.artifice.block.ItemBlockArtifice;

public class ItemBlockBasicOre extends ItemBlockArtifice {
   public ItemBlockBasicOre(Block block) {
      super(block);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return stack.getItemDamage() == 0
         ? Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + ".ore"
         : Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + ".block";
   }
}
