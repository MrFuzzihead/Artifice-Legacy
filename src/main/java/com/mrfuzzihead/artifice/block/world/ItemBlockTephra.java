package shukaro.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import shukaro.artifice.block.ItemBlockArtifice;

public class ItemBlockTephra extends ItemBlockArtifice {
   public ItemBlockTephra(Block block) {
      super(block);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return stack.getItemDamage() == 0
         ? Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + ".sand"
         : Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + ".gravel";
   }
}
