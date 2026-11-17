package shukaro.artifice.world;

import cofh.api.world.IFeatureGenerator;
import java.util.Random;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeRegistry;
import shukaro.artifice.util.ChunkCoord;

public class WorldGenLily implements IFeatureGenerator {
   private int frequency = ArtificeConfig.lotusFrequency;

   public String getFeatureName() {
      return "Artifice: Lilies";
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
               int xMin = chunkX << 4;
               int zMin = chunkZ << 4;
               int startX = xMin + rand.nextInt(16);
               int startZ = zMin + rand.nextInt(16);
               BiomeGenBase biome = world.getBiomeGenForCoords(startX, startZ);
               if (!BiomeDictionary.isBiomeOfType(biome, Type.SWAMP)) {
                  return false;
               }

               int tries = rand.nextInt(16) + 8;
               ChunkCoord c = new ChunkCoord(chunkX, chunkZ);

               for (int l = 0; l < tries; l++) {
                  int x = startX + rand.nextInt(8) - rand.nextInt(8);
                  int z = startZ + rand.nextInt(8) - rand.nextInt(8);
                  int y = world.getHeightValue(x, z);
                  if (c.contains(x, z) && world.isAirBlock(x, y, z) && ArtificeBlocks.blockLotus.canPlaceBlockAt(world, x, y, z)) {
                     if (world.getWorldTime() > 12000L) {
                        world.setBlock(x, y, z, ArtificeBlocks.blockLotus, 1, 0);
                     } else {
                        world.setBlock(x, y, z, ArtificeBlocks.blockLotus, 0, 0);
                     }
                  }
               }
            }

            return true;
         }
      }
   }
}
