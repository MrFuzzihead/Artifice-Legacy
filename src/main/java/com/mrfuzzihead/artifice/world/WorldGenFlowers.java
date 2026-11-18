package com.mrfuzzihead.artifice.world;

import java.util.Random;

import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.util.ChunkCoord;

import cofh.api.world.IFeatureGenerator;

public class WorldGenFlowers implements IFeatureGenerator {

    private int frequency = ArtificeConfig.floraFrequency;

    public String getFeatureName() {
        return "Artifice: Flowers";
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
                    int type = rand.nextInt(ArtificeConfig.flora.length - 1);
                    int xMin = chunkX << 4;
                    int zMin = chunkZ << 4;
                    int startX = xMin + rand.nextInt(16);
                    int startZ = zMin + rand.nextInt(16);
                    if (rand.nextInt(10) > 6) {
                        return false;
                    }

                    BiomeGenBase biome = world.getBiomeGenForCoords(startX, startZ);
                    boolean biomeRight = false;
                    if (!BiomeDictionary.isBiomeOfType(biome, Type.PLAINS)
                        && !BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE)) {
                        if (!BiomeDictionary.isBiomeOfType(biome, Type.FOREST)
                            && !BiomeDictionary.isBiomeOfType(biome, Type.HILLS) || type != 0 && type != 2) {
                            if (!BiomeDictionary.isBiomeOfType(biome, Type.MOUNTAIN) || type != 0 && type != 3) {
                                if (BiomeDictionary.isBiomeOfType(biome, Type.SWAMP) && type == 1) {
                                    biomeRight = true;
                                }
                            } else {
                                biomeRight = true;
                            }
                        } else {
                            biomeRight = true;
                        }
                    } else {
                        biomeRight = true;
                    }

                    if (!biomeRight) {
                        return false;
                    }

                    int tries = rand.nextInt(16) + 8;
                    ChunkCoord c = new ChunkCoord(chunkX, chunkZ);

                    for (int l = 0; l < tries; l++) {
                        int x = startX + rand.nextInt(8) - rand.nextInt(8);
                        int z = startZ + rand.nextInt(8) - rand.nextInt(8);
                        int y = world.getHeightValue(x, z);
                        if (c.contains(x, z) && (world.isAirBlock(x, y, z) || world.getBlock(x, y, z)
                            .equals(Blocks.snow_layer))
                            && ArtificeBlocks.blockFlora.canBlockStay(world, x, y, z)
                            && rand.nextInt(10) <= 5) {
                            if (type == 3) {
                                if (world.getWorldTime() > 12000L) {
                                    world.setBlock(x, y, z, ArtificeBlocks.blockFlora, 4, 0);
                                } else {
                                    world.setBlock(x, y, z, ArtificeBlocks.blockFlora, 3, 0);
                                }
                            } else {
                                world.setBlock(x, y, z, ArtificeBlocks.blockFlora, type, 0);
                            }
                        }
                    }
                }

                return true;
            }
        }
    }
}
