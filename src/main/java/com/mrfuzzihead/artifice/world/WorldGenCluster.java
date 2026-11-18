package com.mrfuzzihead.artifice.world;

import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.api.world.IFeatureGenerator;

public class WorldGenCluster implements IFeatureGenerator {

    protected Block block;
    protected int meta;
    private int minHeight;
    private int maxHeight;
    private int size;
    private int frequency;
    protected Set<NameMetaPair> replaced;

    public WorldGenCluster(Block block, int meta, int minHeight, int maxHeight, int size, int frequency) {
        this.block = block;
        this.meta = meta;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.size = size;
        this.frequency = frequency;
        this.replaced = ArtificeRegistry.getStoneTypes();
    }

    public String getFeatureName() {
        return "Artifice: " + this.block.getUnlocalizedName() + "@" + this.meta + " Clusters";
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
                    boolean doGen = false;
                    BlockCoord b = new BlockCoord();
                    int tries = rand.nextInt(16) + 4;

                    for (int i = 0; i < tries; i++) {
                        int x = (chunkX << 4) + rand.nextInt(16);
                        int y = rand.nextInt(this.maxHeight - this.minHeight + 1) + this.minHeight;
                        int z = (chunkZ << 4) + rand.nextInt(16);
                        b.set(x, y, z);
                        if (this.canGenHere(world, b)) {
                            doGen = true;
                            break;
                        }
                    }

                    if (!doGen) {
                        return false;
                    }

                    if (this.block.getUnlocalizedName()
                        .contains("limestone")) {
                        BiomeGenBase biome = world.getBiomeGenForCoords(b.x, b.z);
                        if (BiomeDictionary.isBiomeOfType(biome, Type.SANDY)) {
                            if (b.y > world.getHeightValue(b.x, b.z) / 2) {
                                this.block = ArtificeBlocks.blockLimestones[3];
                            } else {
                                this.block = ArtificeBlocks.blockLimestones[2];
                            }
                        } else if (BiomeDictionary.isBiomeOfType(biome, Type.WATER)) {
                            this.block = ArtificeBlocks.blockLimestones[5];
                        } else if (BiomeDictionary.isBiomeOfType(biome, Type.JUNGLE)
                            || BiomeDictionary.isBiomeOfType(biome, Type.SWAMP)) {
                                this.block = ArtificeBlocks.blockLimestones[6];
                            } else if (!BiomeDictionary.isBiomeOfType(biome, Type.MESA)
                                && !BiomeDictionary.isBiomeOfType(biome, Type.MOUNTAIN)) {
                                    if (b.y > world.getHeightValue(b.x, b.z) / 2) {
                                        this.block = ArtificeBlocks.blockLimestones[1];
                                    } else {
                                        this.block = ArtificeBlocks.blockLimestones[0];
                                    }
                                } else {
                                    this.block = ArtificeBlocks.blockLimestones[4];
                                }
                    }

                    int num = rand.nextInt((int) (this.size * 1.5) - (int) (this.size * 0.5) + 1)
                        + (int) (this.size * 0.5);
                    WorldGenCluster.ClusterGen cluster = new WorldGenCluster.ClusterGen(world, rand, b, num);
                    cluster.doGeneration(chunkX, chunkZ);
                    ArtificeTickHandler.queueGeneration(dim, cluster);
                }

                return true;
            }
        }
    }

    protected boolean canGenHere(World world, BlockCoord b) {
        NameMetaPair pair = new NameMetaPair(b.getBlock(world), b.getMeta(world));
        return !b.isAir(world) && (this.replaced.contains(pair) || ArtificeBlocks.oreSet.contains(pair));
    }

    /** clusters keep their budget in a local, so a suspended generator gets a fresh one when it resumes. */
    private class ClusterGen extends BlobGen {

        ClusterGen(World world, Random rand, BlockCoord start, int size) {
            super(world, rand, start, size, false);
        }

        @Override
        protected boolean canGen(BlockCoord c) {
            return WorldGenCluster.this.canGenHere(this.world, c);
        }

        @Override
        protected void place(BlockCoord c) {
            placeOreVariant(c, WorldGenCluster.this.replaced, WorldGenCluster.this.block, WorldGenCluster.this.meta);
        }
    }
}
