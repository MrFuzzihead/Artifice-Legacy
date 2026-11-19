package com.mrfuzzihead.artifice.world;

import java.util.Random;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.util.ChunkCoord;

import cofh.api.world.IFeatureGenerator;

public class WorldGenLily implements IFeatureGenerator {

    private int frequency = ArtificeConfig.lotusFrequency;

    public String getFeatureName() {
        return "Artifice: Lilies";
    }

    public boolean generateFeature(Random rand, int chunkX, int chunkZ, World world, boolean newGen) {
        int dim = world.provider.dimensionId;
        if (ArtificeRegistry.getDimensionBlacklist()
            .contains(dim)) {
            return false;
        } else {
            String worldType = world.provider.terrainType.getWorldTypeName();
            if (ArtificeRegistry.getWorldTypeBlacklist()
                .contains(worldType)) {
                return false;
            } else {
                int total = GenFrequency.rollsThisChunk(rand, this.frequency);

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
                        if (c.contains(x, z) && world.isAirBlock(x, y, z)
                            && ArtificeBlocks.blockLotus.canPlaceBlockAt(world, x, y, z)) {
                            // Always the open lotus, for the reason in WorldGenFlowers: the clock was a
                            // worldgen-determinism bug, and BlockLotus.updateTick closes it on the next tick if
                            // the light there is below 8 (CODE_REVIEW 9.5).
                            world.setBlock(x, y, z, ArtificeBlocks.blockLotus, 0, 0);
                        }
                    }
                }

                return true;
            }
        }
    }
}
