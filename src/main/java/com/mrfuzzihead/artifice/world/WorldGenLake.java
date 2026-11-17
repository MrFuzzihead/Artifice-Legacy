package shukaro.artifice.world;

import cofh.api.world.IFeatureGenerator;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeRegistry;

public class WorldGenLake implements IFeatureGenerator {
   private Block block;
   private int frequency;
   private WorldGenLakes lakeGen;

   public WorldGenLake(Block block, int frequency) {
      this.block = block;
      this.frequency = frequency;
      this.lakeGen = new WorldGenLakes(block);
   }

   public String getFeatureName() {
      return "Artifice: " + this.block.getUnlocalizedName() + " Lakes";
   }

   public boolean generateFeature(Random rand, int chunkX, int chunkZ, World world, boolean newGen) {
      int dim = world.provider.dimensionId;
      if (ArtificeRegistry.getDimensionBlacklist().contains(dim)) {
         return false;
      } else {
         String worldType = world.provider.terrainType.getWorldTypeName();
         if (ArtificeRegistry.getWorldTypeBlacklist().contains(worldType)) {
            return false;
         } else {
            int total;
            if (this.frequency >= 100) {
               total = rand.nextInt(100) < Math.abs(this.frequency % (int)Math.pow(10.0, (int)Math.log10(this.frequency)))
                  ? this.frequency / 100 + 1
                  : this.frequency / 100;
            } else {
               total = rand.nextInt(100) < this.frequency ? 0 : 1;
            }

            for (int j = 0; j < total; j++) {
               if (rand.nextInt(100) > 10) {
                  return true;
               }

               int x = chunkX * 16 + rand.nextInt(16);
               int z = chunkZ * 16 + rand.nextInt(16);
               int y = world.getHeightValue(x, z);
               BiomeGenBase biome = world.getBiomeGenForCoords(x, z);
               if (this.block.equals(ArtificeBlocks.blockOil)
                  && (
                     BiomeDictionary.isBiomeOfType(biome, Type.SAVANNA)
                        || BiomeDictionary.isBiomeOfType(biome, Type.SANDY)
                        || BiomeDictionary.isBiomeOfType(biome, Type.WASTELAND)
                  )) {
                  this.lakeGen.generate(world, rand, x, y, z);
               }
            }

            return true;
         }
      }
   }
}
