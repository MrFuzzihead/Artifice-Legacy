package com.mrfuzzihead.artifice.world;

import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.block.world.BlockOre;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.ChunkCoord;
import com.mrfuzzihead.artifice.util.NameMetaPair;
import com.mrfuzzihead.artifice.util.PendingChunks;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;

/**
 * The blob generator behind the cluster, sulfur, niter and cave features (CODE_REVIEW §7.1).
 *
 * <p>
 * All four grew the same walk: keep a per-chunk queue of candidate coordinates, expand the neighbours of the current
 * position, pick one of the candidates at random, replace it, repeat until the blob is the configured size. Each copy
 * was ~100 lines and they had drifted in two places - one kept its generation budget in a field that survives a
 * suspension, the others in a local that restarts - so the differences are now explicit arguments rather than
 * duplicated loops.
 * </p>
 *
 * <p>
 * What stays with the caller is the part that is genuinely different: which blocks may be replaced
 * ({@link #canGen}) and what a replacement looks like ({@link #place}). The fingerprints in
 * {@code ServerGameTests.checkWorldGenFingerprints} pin the output of all four, so this can be refactored without
 * changing what a world looks like.
 * </p>
 */
abstract class BlobGen implements ISuspendableGen {

    protected final World world;
    protected final Random rand;

    private final TMap<ChunkCoord, ArrayList<BlockCoord>> toGen = new THashMap<>();
    private final BlockCoord coord;
    private final int size;

    /**
     * Whether the generation budget survives a suspension.
     *
     * <p>
     * {@code CaveGen} counted into a field, so a generator that suspended and resumed stopped at {@code size} blocks
     * in total; the other three counted into a local, so each resume got a fresh budget and could place {@code size}
     * more. That is a difference nobody chose - it is kept because this is a refactor, not a rebalance.
     * </p>
     */
    private final boolean budgetPersists;

    private int genned;

    BlobGen(World world, Random rand, BlockCoord start, int size, boolean budgetPersists) {
        this.world = world;
        this.rand = rand;
        this.coord = start;
        this.size = size;
        this.budgetPersists = budgetPersists;
        this.genned = 0;
    }

    /** Whether the block at this coordinate is a candidate for replacement. */
    protected abstract boolean canGen(BlockCoord c);

    /** Replaces the block at this coordinate. */
    protected abstract void place(BlockCoord c);

    /** Replace a stone type with the generator's block, or convert another mod's ore into this rock's variant of it. */
    protected final void placeOreVariant(BlockCoord c, Set<NameMetaPair> replaced, Block block, int meta) {
        NameMetaPair ore = new NameMetaPair(c.getBlock(this.world), c.getMeta(this.world));
        if (replaced.contains(ore)) {
            this.world.setBlock(c.x, c.y, c.z, block, meta, 0);
        } else if (ArtificeBlocks.oreSet.contains(ore)) {
            NameMetaPair newOre = BlockOre.getOre(ore, block);
            if (newOre != null) {
                this.world.setBlock(c.x, c.y, c.z, newOre.getBlock(), newOre.getMetadata(), 0);
            }
        }
    }

    /** Replace whatever is there, unconditionally. */
    protected final void placePlain(BlockCoord c, Block block, int meta) {
        this.world.setBlock(c.x, c.y, c.z, block, meta, 0);
    }

    @Override
    public Set<ChunkCoord> getChunksToGen() {
        return this.toGen.keySet();
    }

    @Override
    public boolean doGeneration(int chunkX, int chunkZ) {
        ChunkCoord chunk = new ChunkCoord(chunkX, chunkZ);
        if (!this.toGen.containsKey(chunk)) {
            this.toGen.put(chunk, new ArrayList<BlockCoord>());
        }

        int genned = this.budgetPersists ? this.genned : 0;
        int threshold = this.size / 8;

        while (genned < this.size) {
            for (BlockCoord t : this.coord.getAdjacent()) {
                ChunkCoord c = new ChunkCoord(t);
                if (!this.toGen.containsKey(c)) {
                    this.toGen.put(c, new ArrayList<BlockCoord>());
                }

                if (!chunk.contains(t) && !this.world.blockExists(t.x, t.y, t.z)) {
                    // Suspended, not finished: keep whatever is still queued and retry next tick.
                    if (this.budgetPersists) {
                        this.genned = genned;
                    }
                    return PendingChunks.hasWork(this.toGen);
                }

                if (this.canGen(t) && !this.toGen.get(chunk)
                    .contains(t)) {
                    this.toGen.get(chunk)
                        .add(t);
                }
            }

            if (this.toGen.get(chunk)
                .size() == 0) {
                break;
            }

            this.coord.set(pick(chunk));
            this.toGen.get(chunk)
                .remove(this.coord);
            this.place(this.coord);
            genned++;

            // Once the queue is small, keep going from what is left rather than expanding again.
            while (this.toGen.get(chunk)
                .size() > threshold && genned < this.size
                && this.toGen.get(chunk)
                    .size() != 0) {
                this.coord.set(pick(chunk));
                this.toGen.get(chunk)
                    .remove(this.coord);
                this.place(this.coord);
                genned++;
            }
        }

        if (this.budgetPersists) {
            this.genned = genned;
        }

        return PendingChunks.hasWork(this.toGen);
    }

    private BlockCoord pick(ChunkCoord chunk) {
        return this.toGen.get(chunk)
            .get(
                this.rand.nextInt(
                    this.toGen.get(chunk)
                        .size()));
    }
}
