package com.mrfuzzihead.artifice.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.BiomeDictionary.Type;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.block.world.BlockOre;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.api.world.IFeatureGenerator;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

public class WorldGenCave implements IFeatureGenerator {

    protected Block block;
    protected int meta;
    private int minHeight;
    private int maxHeight;
    private int size;
    private int frequency;
    protected Set<NameMetaPair> replaced;

    public WorldGenCave(Block block, int meta, int minHeight, int maxHeight, int size, int frequency) {
        this.block = block;
        this.meta = meta;
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.size = size;
        this.frequency = frequency;
        this.replaced = ArtificeRegistry.getStoneTypes();
    }

    public String getFeatureName() {
        return "Artifice: " + this.block.getUnlocalizedName() + "@" + this.meta + " Caves";
    }

    protected boolean canGenHere(World world, BlockCoord c) {
        NameMetaPair pair = new NameMetaPair(c.getBlock(world), c.getMeta(world));
        if (!c.isAir(world) && (this.replaced.contains(pair) || ArtificeBlocks.oreSet.contains(pair))) {
            for (BlockCoord n : c.getNearby()) {
                if (world.blockExists(n.x, n.y, n.z) && (n.isAir(world) || !n.getBlock(world)
                    .isOpaqueCube())) {
                    return true;
                }
            }
        }

        return false;
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

                for (int j = 0; j < total; j++) {
                    boolean doGen = false;
                    BlockCoord b = new BlockCoord();
                    int tries = rand.nextInt(16) + 8;

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
                    WorldGenCave.CaveGen cave = new WorldGenCave.CaveGen(world, rand, b, num);
                    cave.doGeneration(chunkX, chunkZ);
                    ArtificeTickHandler.queueGeneration(dim, cave);
                }

                return true;
            }
        }
    }

    private class CaveGen implements ISuspendableGen {

        private World world;
        private Random rand;
        private BlockCoord coord;
        private TMap<ChunkCoord, ArrayList<BlockCoord>> toGen;
        private int size;
        private int genned;

        public CaveGen(World world, Random rand, BlockCoord start, int size) {
            this.world = world;
            this.rand = rand;
            this.coord = start;
            this.toGen = new THashMap();
            this.size = size;
            this.genned = 0;
        }

        @Override
        public Set<ChunkCoord> getChunksToGen() {
            return this.toGen.keySet();
        }

        @Override
        public boolean doGeneration(int chunkX, int chunkZ) {
            ChunkCoord chunk = new ChunkCoord(chunkX, chunkZ);
            int threshold = this.size / 8;

            while (this.genned < this.size) {
                for (BlockCoord t : this.coord.getAdjacent()) {
                    ChunkCoord c = new ChunkCoord(t);
                    if (!this.toGen.containsKey(c)) {
                        this.toGen.put(c, new ArrayList());
                    }

                    if (!chunk.contains(t) && !this.world.blockExists(t.x, t.y, t.z)) {
                        return true;
                    }

                    if (WorldGenCave.this.canGenHere(this.world, t)
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
                NameMetaPair ore = new NameMetaPair(this.coord.getBlock(this.world), this.coord.getMeta(this.world));
                if (WorldGenCave.this.replaced.contains(ore)) {
                    this.world.setBlock(
                        this.coord.x,
                        this.coord.y,
                        this.coord.z,
                        WorldGenCave.this.block,
                        WorldGenCave.this.meta,
                        0);
                } else if (ArtificeBlocks.oreSet.contains(ore)) {
                    NameMetaPair newOre = BlockOre.getOre(ore, WorldGenCave.this.block);
                    if (newOre != null) {
                        this.world.setBlock(
                            this.coord.x,
                            this.coord.y,
                            this.coord.z,
                            newOre.getBlock(),
                            newOre.getMetadata(),
                            0);
                    }
                }

                this.genned++;

                for (; ((ArrayList) this.toGen.get(chunk)).size() > threshold && this.genned < this.size
                    && ((ArrayList) this.toGen.get(chunk)).size() != 0; this.genned++) {
                    this.coord.set(
                        (BlockCoord) ((ArrayList) this.toGen.get(chunk))
                            .get(this.rand.nextInt(((ArrayList) this.toGen.get(chunk)).size())));
                    ((ArrayList) this.toGen.get(chunk)).remove(this.coord);
                    ore = new NameMetaPair(this.coord.getBlock(this.world), this.coord.getMeta(this.world));
                    if (WorldGenCave.this.replaced.contains(ore)) {
                        this.world.setBlock(
                            this.coord.x,
                            this.coord.y,
                            this.coord.z,
                            WorldGenCave.this.block,
                            WorldGenCave.this.meta,
                            0);
                    } else if (ArtificeBlocks.oreSet.contains(ore)) {
                        NameMetaPair newOre = BlockOre.getOre(ore, WorldGenCave.this.block);
                        if (newOre != null) {
                            this.world.setBlock(
                                this.coord.x,
                                this.coord.y,
                                this.coord.z,
                                newOre.getBlock(),
                                newOre.getMetadata(),
                                0);
                        }
                    }
                }
            }

            return true;
        }
    }
}
