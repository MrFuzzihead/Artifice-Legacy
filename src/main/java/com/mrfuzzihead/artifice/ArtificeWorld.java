package shukaro.artifice;

import cofh.core.world.WorldHandler;
import shukaro.artifice.world.WorldGenCave;
import shukaro.artifice.world.WorldGenCluster;
import shukaro.artifice.world.WorldGenDesert;
import shukaro.artifice.world.WorldGenFlowers;
import shukaro.artifice.world.WorldGenLake;
import shukaro.artifice.world.WorldGenLayer;
import shukaro.artifice.world.WorldGenLily;
import shukaro.artifice.world.WorldGenSulfur;

public class ArtificeWorld {
   public static void initWorldGen() {
      if (ArtificeConfig.oilLakeGen) {
         WorldHandler.addFeature(new WorldGenLake(ArtificeBlocks.blockOil, ArtificeConfig.oilLakeFrequency));
      }

      if (ArtificeConfig.sulfurGen) {
         WorldHandler.addFeature(new WorldGenSulfur(ArtificeBlocks.blockSulfur, 0, ArtificeConfig.sulfurSize, ArtificeConfig.sulfurFrequency));
      }

      if (ArtificeConfig.niterGen) {
         WorldHandler.addFeature(new WorldGenDesert(ArtificeBlocks.blockNiter, 0, ArtificeConfig.niterSize, ArtificeConfig.niterFrequency));
      }

      if (ArtificeConfig.uraniumOreGen) {
         WorldHandler.addFeature(
            new WorldGenCluster(
               ArtificeBlocks.blockUranium,
               0,
               ArtificeConfig.uraniumOreMinHeight,
               ArtificeConfig.uraniumOreMaxHeight,
               ArtificeConfig.uraniumOreSize,
               ArtificeConfig.uraniumOreFrequency
            )
         );
      }

      if (ArtificeConfig.enderOreGen) {
         WorldHandler.addFeature(
            new WorldGenCluster(
               ArtificeBlocks.blockEnderOre,
               0,
               ArtificeConfig.enderOreMinHeight,
               ArtificeConfig.enderOreMaxHeight,
               ArtificeConfig.enderOreSize,
               ArtificeConfig.enderOreFrequency
            )
         );
      }

      for (int i = 0; i < ArtificeConfig.rockNames.length; i++) {
         if (ArtificeConfig.rockLayersGen[i]) {
            WorldHandler.addFeature(
               new WorldGenLayer(ArtificeBlocks.rockBlocks[i], 0, ArtificeConfig.rockLayersMinHeight[i], ArtificeConfig.rockLayersMaxHeight[i])
            );
         }
      }

      for (int ix = 0; ix < 3; ix++) {
         if (ArtificeConfig.rockClustersGen[ix]) {
            WorldHandler.addFeature(
               new WorldGenCluster(
                  ArtificeBlocks.rockBlocks[ix],
                  0,
                  ArtificeConfig.rockClustersMinHeight[ix],
                  ArtificeConfig.rockClustersMaxHeight[ix],
                  ArtificeConfig.rockClustersSize[ix],
                  ArtificeConfig.rockClustersFrequency[ix]
               )
            );
         }

         if (ArtificeConfig.rockCavesGen[ix]) {
            WorldHandler.addFeature(
               new WorldGenCave(
                  ArtificeBlocks.rockBlocks[ix],
                  0,
                  ArtificeConfig.rockCavesMinHeight[ix],
                  ArtificeConfig.rockCavesMaxHeight[ix],
                  ArtificeConfig.rockCavesSize[ix],
                  ArtificeConfig.rockCavesFrequency[ix]
               )
            );
         }
      }

      if (ArtificeConfig.floraWorldGen) {
         WorldHandler.addFeature(new WorldGenFlowers());
      }

      if (ArtificeConfig.lotusWorldGen) {
         WorldHandler.addFeature(new WorldGenLily());
      }
   }
}
