package shukaro.artifice.block.world;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.block.ItemBlockArtifice;

public class ItemBlockOre extends ItemBlockArtifice {
   public ItemBlockOre(Block block) {
      super(block);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return stack.getItemDamage() >= ArtificeBlocks.rockBlockNames.length
         ? Block.getBlockFromItem(stack.getItem()).getUnlocalizedName()
         : Block.getBlockFromItem(stack.getItem()).getUnlocalizedName() + "." + ArtificeBlocks.rockBlockNames[stack.getItemDamage()];
   }
}
