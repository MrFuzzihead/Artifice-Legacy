package com.mrfuzzihead.artifice.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.event.ArtificeTickHandler;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.util.MiscUtils;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.api.world.IFeatureGenerator;
import gnu.trove.set.hash.THashSet;

public class WorldGenVolcano implements IFeatureGenerator {

    private int frequency = ArtificeConfig.volcanoFrequency;
    protected Set<NameMetaPair> replaced = new HashSet<>();

    public WorldGenVolcano() {
        for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()) {
            if (biome != null) {
                if (biome.topBlock != null) {
                    this.replaced.add(new NameMetaPair(biome.topBlock));
                }

                if (biome.fillerBlock != null) {
                    this.replaced.add(new NameMetaPair(biome.fillerBlock));
                }
            }
        }
    }

    public String getFeatureName() {
        return "Artifice: Volcanos";
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
            } else if (rand.nextInt(20) != 0) {
                return false;
            } else {
                int total = GenFrequency.rollsThisChunk(rand, this.frequency);

                for (int j = 0; j < total; j++) {
                    boolean doGen = false;
                    BlockCoord b = new BlockCoord();
                    int tries = rand.nextInt(8);

                    for (int i = 0; i < tries; i++) {
                        int x = (chunkX << 4) + rand.nextInt(16);
                        int z = (chunkZ << 4) + rand.nextInt(16);
                        int y = VolcanoHelper.getHighestSolidOrFillBlock(world, x, z);
                        b.set(x, y, z);
                        if (VolcanoHelper.canGenHere(world, b)) {
                            doGen = true;
                            break;
                        }
                    }

                    if (!doGen || rand.nextInt(10) != 0) {
                        return false;
                    }

                    WorldGenVolcano.VolcanoGen volcano = new WorldGenVolcano.VolcanoGen(world, b, rand.nextBoolean());
                    ArtificeTickHandler.queueGeneration(world.provider.dimensionId, volcano);
                }

                return true;
            }
        }
    }

    private class VolcanoGen implements IDelayedGen {

        private World world;
        private Random rand;
        private boolean active;
        private BlockCoord start;
        private static final int CHAMBER_SIZE = 32768;
        private static final int CHAMBER_DEPTH = 32;
        private static final int CHAMBER_THICKNESS = 2;
        private static final int LAYERS = 6;
        private static final int CINDER_PASSES = 3;
        private static final int CINDERS = 64;
        private static final int FLOW_PASSES = 2;
        private static final int FLOW_SIZE = 8;
        private static final int LAVA_BITS = 64;

        public VolcanoGen(World world, BlockCoord start, boolean active) {
            this.world = world;
            this.rand = world.rand;
            this.active = active;
            this.start = start;
        }

        @Override
        public ChunkCoord getChunk() {
            return new ChunkCoord(this.start);
        }

        @Override
        public boolean doGeneration() {
            BlockCoord blobCenter = this.start.copy()
                .offset(ForgeDirection.DOWN, MiscUtils.randWithin50(this.rand, 32));
            int blobSize = MiscUtils.randWithin50(this.rand, 32768);
            this.genBlob(blobCenter, blobSize);
            this.encaseBlob(blobCenter, 2);
            this.genTube(blobCenter);
            int numLayers = 6;
            int flowSize = MiscUtils.randWithin50(this.rand, 8);

            for (int i = 0; i < numLayers; i++) {
                int cp = 3;
                int fp = 2;

                while (cp > 0 || fp > 0) {
                    boolean task = this.rand.nextBoolean();
                    if (task && cp > 0) {
                        cp--;
                    } else if (!task && fp > 0) {
                        this.genFlow(this.start, flowSize);
                        flowSize = (int) (flowSize * (1.0 + (0.65 * this.rand.nextDouble() + 0.1)));
                        fp--;
                    }
                }
            }

            if (this.active) {
                this.genLavaBits(this.start, MiscUtils.randWithin50(this.rand, 64));
            }

            return true;
        }

        private void genBlob(BlockCoord center, int size) {
            int filled = 0;
            BlockCoord b = null;
            ArrayList<BlockCoord> workplace = new ArrayList<>();
            THashSet<BlockCoord> done = new THashSet();
            workplace.add(center);

            while (filled < size && !workplace.isEmpty()) {
                b = workplace.remove(this.rand.nextInt(workplace.size()));
                VolcanoHelper.setVolcanoFill(this.world, b, this.active);
                done.add(b);

                for (BlockCoord t : b.getAdjacent()) {
                    if (!done.contains(t)) {
                        workplace.add(t);
                    }
                }

                filled++;
            }
        }

        private void encaseBlob(BlockCoord origin, int thickness) {
            ArrayList<BlockCoord> workplace = new ArrayList<>();
            THashSet<BlockCoord> done = new THashSet();
            workplace.add(origin);

            while (!workplace.isEmpty()) {
                BlockCoord b = workplace.remove(this.rand.nextInt(workplace.size()));
                done.add(b);

                for (BlockCoord t : b.getRadiusBlocks(1 + this.rand.nextInt(thickness - 1))) {
                    Block k = t.getBlock(this.world);
                    if (b.getDistance(t) <= thickness && !VolcanoHelper.isVolcanicFill(k)
                        && !VolcanoHelper.isVolcanicBlock(k)) {
                        VolcanoHelper.setVolcanoBlock(this.world, t);
                    } else if (VolcanoHelper.isVolcanicFill(k) && !done.contains(t) && !workplace.contains(t)) {
                        workplace.add(t);
                    }
                }
            }
        }

        private List<BlockCoord> genTube(BlockCoord start) {
            BlockCoord b = start.copy();
            int height = VolcanoHelper.getHighestSolidOrFillBlock(this.world, b.x, b.z) + 1;
            int tubeSize = this.rand.nextInt(5) + 1;
            List<BlockCoord> tubeBlocks = new ArrayList<>();
            tubeBlocks.add(b);

            while (tubeBlocks.size() < tubeSize) {
                int side = this.rand.nextInt(4) + 2;
                if (!tubeBlocks.contains(b.getAdjacent()[side])) {
                    tubeBlocks.add(b.getAdjacent()[side]);
                }
            }

            while (b.y < height) {
                for (BlockCoord t : tubeBlocks) {
                    if (!VolcanoHelper.isVolcanicFill(t.getBlock(this.world))) {
                        VolcanoHelper.setVolcanoFill(this.world, t, this.active);
                    }

                    for (BlockCoord n : t.getNearby()) {
                        if (n.y <= t.y && !VolcanoHelper.isVolcanicFill(n.getBlock(this.world))) {
                            VolcanoHelper.setVolcanoBlock(this.world, n);
                        }
                    }

                    t.y++;
                }
            }

            return tubeBlocks;
        }

        private void genFlow(BlockCoord start, int flowSize) {
            int flowed = 0;
            List<BlockCoord> open = VolcanoHelper.getOpenSides(this.world, start);

            while (open.size() == 0) {
                start.offset(ForgeDirection.UP);
                open = VolcanoHelper.getOpenSides(this.world, start);
                VolcanoHelper.setVolcanoFill(this.world, start, this.active);

                for (BlockCoord n : start.getAdjacent()) {
                    if (n.y == start.y && VolcanoHelper.isVolcanicFill(
                        n.copy()
                            .offset(ForgeDirection.DOWN)
                            .getBlock(this.world))) {
                        VolcanoHelper.setVolcanoFill(this.world, n, this.active);
                    }
                }
            }

            while (flowed < flowSize && open.size() > 0) {
                BlockCoord b = open.get(0);
                BlockCoord c = b.copy();
                VolcanoHelper.setVolcanoBlock(this.world, b);

                while (VolcanoHelper.isReplaceable(this.world, c.offset(ForgeDirection.DOWN))) {
                    VolcanoHelper.setVolcanoBlock(this.world, c);
                }

                open = VolcanoHelper.getOpenSides(this.world, c);
                flowed++;
            }
        }

        private void genTephra(BlockCoord start, int amount) {
            int dropped = 0;
            int radius = amount / 3;
            List<BlockCoord> done = new ArrayList<>();

            while (dropped < amount) {
                BlockCoord b = VolcanoHelper.snapToHighest(
                    this.world,
                    VolcanoHelper
                        .get2dINVERSEDistributedCoord(this.world, start, MiscUtils.randWithin50(this.rand, amount)));
                if (!done.contains(b) && !VolcanoHelper.isVolcanicFill(b.getBlock(this.world))) {
                    VolcanoHelper.setVolcanoTephra(this.world, b.offset(ForgeDirection.UP));
                    done.add(b);
                    dropped++;
                }
            }
        }

        private void genLavaBits(BlockCoord start, int amount) {
            BlockCoord b = start.copy();

            while (VolcanoHelper.isVolcanicFill(b.getBlock(this.world))
                || VolcanoHelper.isVolcanicBlock(b.getBlock(this.world))) {
                if (b.y > 255) {
                    return;
                }

                if (!VolcanoHelper.isVolcanicFill(
                    b.copy()
                        .offset(ForgeDirection.UP)
                        .getBlock(this.world))
                    && !VolcanoHelper.isVolcanicBlock(
                        b.copy()
                            .offset(ForgeDirection.UP)
                            .getBlock(this.world))) {
                    break;
                }

                b.offset(ForgeDirection.UP);
            }

            ArrayList<BlockCoord> toCover = new ArrayList<>();
            ArrayList<BlockCoord> done = new ArrayList<>();
            int num = 0;
            toCover.add(b);

            while (num < amount && !toCover.isEmpty()) {
                b = toCover.remove(this.rand.nextInt(toCover.size()));
                if (this.world.isAirBlock(b.x, b.y + 1, b.z) && this.rand.nextInt(16) == 0) {
                    this.world.setBlock(b.x, b.y, b.z, Blocks.lava, 0, 3);
                    num++;
                }

                done.add(b);

                for (BlockCoord t : b.getNearby()) {
                    if (!toCover.contains(t) && !done.contains(t)
                        && VolcanoHelper.isVolcanicBlock(t.getBlock(this.world))
                        && !t.copy()
                            .offset(ForgeDirection.UP)
                            .getBlock(this.world)
                            .isOpaqueCube()) {
                        toCover.add(t);
                    }
                }
            }
        }

        private void genCaldera(BlockCoord start, int amount) {}

        private void coverFeature(BlockCoord start) {
            BlockCoord b = start.copy();

            while (VolcanoHelper.isVolcanicFill(b.getBlock(this.world))
                || VolcanoHelper.isVolcanicBlock(b.getBlock(this.world))) {
                if (b.y > 255) {
                    return;
                }

                if (!VolcanoHelper.isVolcanicFill(
                    b.copy()
                        .offset(ForgeDirection.UP)
                        .getBlock(this.world))
                    && !VolcanoHelper.isVolcanicBlock(
                        b.copy()
                            .offset(ForgeDirection.UP)
                            .getBlock(this.world))) {
                    break;
                }

                b.offset(ForgeDirection.UP);
            }

            ArrayDeque<BlockCoord> toCover = new ArrayDeque<>();
            toCover.add(b);

            while (!toCover.isEmpty()) {
                b = toCover.pop();
                BiomeGenBase biome = this.world.getBiomeGenForCoords(b.x, b.y);
                if (biome.topBlock != null && !b.copy()
                    .offset(ForgeDirection.UP)
                    .getBlock(this.world)
                    .isOpaqueCube()
                    && b.copy()
                        .offset(ForgeDirection.UP)
                        .isAir(this.world)) {
                    b.setBlock(this.world, biome.topBlock, 0, false);
                } else if (biome.fillerBlock != null && !b.copy()
                    .offset(ForgeDirection.UP)
                    .getBlock(this.world)
                    .isOpaqueCube()) {
                        b.setBlock(this.world, biome.fillerBlock, 0, false);
                    }

                if (this.world.isAirBlock(b.x, b.y + 1, b.z)) {
                    if (this.rand.nextInt(16) == 0) {
                        biome.getRandomWorldGenForGrass(this.rand)
                            .generate(this.world, this.rand, b.x, b.y, b.z);
                    } else if (this.rand.nextInt(16) == 0) {
                        biome.plantFlower(this.world, this.rand, b.x, b.y + 1, b.z);
                    }
                }

                for (BlockCoord t : b.getNearby()) {
                    if (!toCover.contains(t) && VolcanoHelper.isVolcanicBlock(t.getBlock(this.world))
                        && !t.copy()
                            .offset(ForgeDirection.UP)
                            .getBlock(this.world)
                            .isOpaqueCube()) {
                        toCover.add(t);
                    }
                }
            }
        }
    }
}
