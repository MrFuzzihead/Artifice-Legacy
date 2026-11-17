package com.mrfuzzihead.artifice.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.api.world.IFeatureGenerator;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
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
                int total;
                if (this.frequency >= 100) {
                    total = rand.nextInt(100)
                        < Math.abs(this.frequency % (int) Math.pow(10.0, (int) Math.log10(this.frequency)))
                            ? this.frequency / 100 + 1
                            : this.frequency / 100;
                } else {
                    total = rand.nextInt(100) < this.frequency ? 0 : 1;
                }

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

    private class NiterGen implements ISuspendableGen {

        private World world;
        private Random rand;
        private BlockCoord coord;
        private TMap<ChunkCoord, ArrayList<BlockCoord>> toGen;
        private int size;

        public NiterGen(World world, Random rand, BlockCoord start, int size) {
            this.world = world;
            this.rand = rand;
            this.coord = start;
            this.toGen = new THashMap();
            this.size = size;
        }

        @Override
        public Set<ChunkCoord> getChunksToGen() {
            return this.toGen.keySet();
        }

        @Override
        public boolean doGeneration(int chunkX, int chunkZ) {
            ChunkCoord chunk = new ChunkCoord(chunkX, chunkZ);
            int genned = 0;
            int threshold = this.size / 8;

            while (genned < this.size) {
                for (BlockCoord t : this.coord.getAdjacent()) {
                    ChunkCoord c = new ChunkCoord(t);
                    if (!this.toGen.containsKey(c)) {
                        this.toGen.put(c, new ArrayList());
                    }

                    if (!chunk.contains(t) && !this.world.blockExists(t.x, t.y, t.z)) {
                        return true;
                    }

                    if (WorldGenDesert.this.canGenHere(this.world, t)
                        && !((ArrayList) this.toGen.get(chunk)).contains(t)) {
                        if (this.toGen.get(chunk) == null) {
                            this.toGen.put(chunk, new ArrayList());
                        }

                        ((ArrayList) this.toGen.get(chunk)).add(t);
                    }
                }

                if (((ArrayList) this.toGen.get(chunk)).size() == 0) {
                    break;
                }

                this.coord.set(
                    (BlockCoord) ((ArrayList) this.toGen.get(chunk))
                        .get(this.rand.nextInt(((ArrayList) this.toGen.get(chunk)).size())));
                ((ArrayList) this.toGen.get(chunk)).remove(this.coord);
                this.world.setBlock(
                    this.coord.x,
                    this.coord.y,
                    this.coord.z,
                    WorldGenDesert.this.block,
                    WorldGenDesert.this.meta,
                    0);
                genned++;

                while (((ArrayList) this.toGen.get(chunk)).size() > threshold && genned < this.size
                    && ((ArrayList) this.toGen.get(chunk)).size() != 0) {
                    this.coord.set(
                        (BlockCoord) ((ArrayList) this.toGen.get(chunk))
                            .get(this.rand.nextInt(((ArrayList) this.toGen.get(chunk)).size())));
                    ((ArrayList) this.toGen.get(chunk)).remove(this.coord);
                    this.world.setBlock(
                        this.coord.x,
                        this.coord.y,
                        this.coord.z,
                        WorldGenDesert.this.block,
                        WorldGenDesert.this.meta,
                        0);
                    genned++;
                }
            }

            return true;
        }
    }
}
