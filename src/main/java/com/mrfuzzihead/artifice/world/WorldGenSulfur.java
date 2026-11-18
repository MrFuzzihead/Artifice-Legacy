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

public class WorldGenSulfur implements IFeatureGenerator {

    protected Block block;
    protected int meta;
    private int size;
    private int frequency;
    protected Set<NameMetaPair> replaced;

    public WorldGenSulfur(Block block, int meta, int size, int frequency) {
        this.block = block;
        this.meta = meta;
        this.size = size;
        this.frequency = frequency;
        this.replaced = ArtificeRegistry.getStoneTypes();
    }

    public String getFeatureName() {
        return "Artifice: " + this.block.getUnlocalizedName() + "@" + this.meta + " Lava Deposits";
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
                    int tries = rand.nextInt(16);

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
                    WorldGenSulfur.SulfurGen sulfur = new WorldGenSulfur.SulfurGen(world, rand, b, num);
                    sulfur.doGeneration(chunkX, chunkZ);
                    ArtificeTickHandler.queueGeneration(dim, sulfur);
                }

                return true;
            }
        }
    }

    protected boolean canGenHere(World world, BlockCoord b) {
        NameMetaPair pair = new NameMetaPair(b.getBlock(world), b.getMeta(world));
        if (!b.isAir(world) && this.replaced.contains(pair)) {
            for (BlockCoord n : b.getAdjacent()) {
                if (n.getBlock(world)
                    .equals(Blocks.lava)
                    || n.getBlock(world)
                        .equals(Blocks.flowing_lava)
                    || n.getBlock(world)
                        .equals(this.block)) {
                    return true;
                }
            }
        }

        return false;
    }

    /** sulfur replaces whatever is there rather than only stone types - it has already checked for lava next door. */
    private class SulfurGen extends BlobGen {

        SulfurGen(World world, Random rand, BlockCoord start, int size) {
            super(world, rand, start, size, false);
        }

        @Override
        protected boolean canGen(BlockCoord c) {
            return WorldGenSulfur.this.canGenHere(this.world, c);
        }

        @Override
        protected void place(BlockCoord c) {
            placePlain(c, WorldGenSulfur.this.block, WorldGenSulfur.this.meta);
        }
    }
}
