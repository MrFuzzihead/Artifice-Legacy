package shukaro.artifice.compat;

import java.util.List;
import net.minecraft.item.ItemStack;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeCore;

public class Forestry implements ICompat {
   @Override
   public String getModID() {
      return "Forestry";
   }

   @Override
   public void load() {
      try {
         Class<?> flowerClass = Class.forName("forestry.api.apiculture.FlowerManager");
         List<ItemStack> flowerList = (List<ItemStack>)flowerClass.getField("plainFlowers").get(null);
         ArtificeCore.logger.info("Adding flowers to the Flower Manager");

         for (int i = 0; i < 4; i++) {
            flowerList.add(new ItemStack(ArtificeBlocks.blockFlora, 1, i));
         }

         ArtificeCore.logger.info("Forestry Compat Initialized");
      } catch (Throwable var4) {
         ArtificeCore.logger.warn("Couldn't initialize Forestry compat");
         var4.printStackTrace();
      }
   }
}
