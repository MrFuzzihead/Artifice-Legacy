package com.mrfuzzihead.artifice.gametest;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.World;

/**
 * A test-only block that behaves like the neighbour that turns a noisy block into a neighbour-update storm: it counts
 * every neighbour change it receives and echoes it back to its own neighbours.
 *
 * <p>
 * That echo is the whole mechanism behind the review's §5.19/§5.20 findings. Redstone dust does the same thing: a
 * block that notifies its neighbours on a transition it repeats forever re-notifies the dust, the dust recomputes and
 * notifies back, and the pair re-arm each other. With this block next to the block under test, a test can tell a
 * block that settles from a block that does not, by counting how many times the pair keeps going.
 * </p>
 *
 * <p>
 * Registered by {@link GameTestMod#preInit} and never shipped: it lives in the {@code gameTest} source set.
 * </p>
 */
public class NeighborProbe extends Block {

    /** How many neighbour changes the probe has seen since the last {@link #reset()}. */
    private static int observations;
    /** How many of those it echoed back. */
    private static int echoes;

    private boolean echoing;

    public NeighborProbe() {
        super(Material.rock);
        this.setBlockName("artifice.probe");
        this.setHardness(1.0F);
    }

    static void reset() {
        observations = 0;
        echoes = 0;
    }

    static int observations() {
        return observations;
    }

    static int echoes() {
        return echoes;
    }

    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (world.isRemote) return;

        observations++;
        if (this.echoing) return;

        // The echo deliberately happens inside the callback, so the block under test sees it exactly as it would see
        // a redstone wire's reaction: re-entrant, in the same notification chain, with no tick in between.
        this.echoing = true;
        try {
            echoes++;
            world.notifyBlocksOfNeighborChange(x, y, z, this);
        } finally {
            this.echoing = false;
        }
    }
}
