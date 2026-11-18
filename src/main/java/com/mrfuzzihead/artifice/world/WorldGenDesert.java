package com.mrfuzzihead.artifice.world;

import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.api.world.IFeatureGenerator;
import gnu.trove.set.hash.THashSet;

public class WorldGenDesert implements IFeatureGenerator {

    protected Block block;
    protected int meta;
    private int size;
    private int frequency;
    protected Set<NameMetaPair> replaced;

    public WorldGenDesert(Block block, int meta, int size, int frequency) {
        this.block = block;
        this.meta = meta;
        this.size = size;
        this.frequency = frequency;
        this.replaced = new THashSet();
        this.replaced.add(new NameMetaPair(Blocks.sand, 0));
    }

    public String getFeatureName() {
        return "Artifice: " + this.block.getUnlocalizedName() + "@" + this.meta + " Desert Deposits";
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

                total += rand.nextInt(4);

                for (int j = 0; j < total; j++) {
                    boolean doGen = false;
                    BlockCoord b = new BlockCoord();
                    int tries = rand.nextInt(16) + 10;

                    for (int i = 0; i < tries; i++) {
                        int x = (chunkX << 4) + rand.nextInt(16);
                        int y = rand.nextInt(128) + 1;
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

                    int num = rand.nextInt((int) (this.size * 1.5) - (int) (this.size * 0.5) + 1)
                        + (int) (this.size * 0.5);
                    WorldGenDesert.NiterGen niter = new WorldGenDesert.NiterGen(world, rand, b, num);
                    niter.doGeneration(chunkX, chunkZ);
                    ArtificeTickHandler.queueGeneration(dim, niter);
                }

                return true;
            }
        }
    }

    protected boolean canGenHere(World world, BlockCoord b) {
        NameMetaPair pair = new NameMetaPair(b.getBlock(world), b.getMeta(world));
        return !b.isAir(world) && this.replaced.contains(pair);
    }

    /** niter is the same shape as sulfur: the candidate check decides, the placement is unconditional. */
    private class NiterGen extends BlobGen {

        NiterGen(World world, Random rand, BlockCoord start, int size) {
            super(world, rand, start, size, false);
        }

        @Override
        protected boolean canGen(BlockCoord c) {
            return WorldGenDesert.this.canGenHere(this.world, c);
        }

        @Override
        protected void place(BlockCoord c) {
            placePlain(c, WorldGenDesert.this.block, WorldGenDesert.this.meta);
        }
    }
}
