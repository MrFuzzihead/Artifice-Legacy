package com.mrfuzzihead.artifice.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.util.BlockCoord;

public class VolcanoHelper {

    public static boolean canGenHere(World world, BlockCoord b) {
        return getComposition(world, b, 32) > 0.25 ? false : !(getComposition(world, b, -32) < 0.65);
    }

    public static float getComposition(World world, BlockCoord b, int yDistance) {
        BlockCoord c = b.copy();
        int solidCount = 0;
        int nonSolidCount = 0;

        while (c.y < b.y + yDistance) {
            if (c.getBlock(world)
                .isOpaqueCube()) {
                solidCount++;
            } else {
                nonSolidCount++;
            }

            for (BlockCoord n : c.getAdjacent()) {
                if (n.getBlock(world)
                    .isOpaqueCube()) {
                    solidCount++;
                } else {
                    nonSolidCount++;
                }
            }

            if (yDistance > 0) {
                c.y++;
            } else {
                c.y--;
            }
        }

        return solidCount / ((float) nonSolidCount + solidCount);
    }

    public static int getHighestSolidOrFillBlock(World world, int x, int z) {
        int y = 255;
        Block b = world.getBlock(x, y, z);

        while (y > 0 && !b.isOpaqueCube()
            && b != Blocks.lava
            && b != Blocks.flowing_lava
            && !b.isWood(world, x, y, z)
            && b != ArtificeBlocks.blockCharredLog) {
            b = world.getBlock(x, --y, z);
        }

        return y;
    }

    public static boolean isVolcanicBlock(Block b) {
        return b == ArtificeBlocks.blockBasalt || b == ArtificeBlocks.blockTephra
            || b == ArtificeBlocks.blockCharredLog;
    }

    public static boolean isVolcanicFill(Block b) {
        return b == Blocks.obsidian || b == Blocks.lava || b == Blocks.flowing_lava;
    }

    public static void setVolcanoBlock(World world, BlockCoord coord) {
        Random rand = world.rand;

        for (BlockCoord t : coord.getAdjacent()) {
            if (!t.isAir(world) && t.getBlock(world)
                .isWood(world, t.x, t.y, t.z)) {
                charTree(world, t);
            }
        }

        if (coord.getBlock(world) != Blocks.bedrock) {
            coord.setBlock(world, ArtificeBlocks.blockBasalt, rand.nextInt(4) == 0 ? 1 : 0, false);
        }
    }

    public static void setVolcanoTephra(World world, BlockCoord coord) {
        Random rand = world.rand;

        for (BlockCoord t : coord.getAdjacent()) {
            if (!t.isAir(world) && t.getBlock(world)
                .isWood(world, t.x, t.y, t.z)) {
                charTree(world, t);
            }
        }

        if (coord.getBlock(world) != Blocks.bedrock) {
            coord.setBlock(world, ArtificeBlocks.blockTephra, rand.nextInt(4) == 0 ? 0 : 1, false);
        }
    }

    public static void setVolcanoFill(World world, BlockCoord coord, boolean active) {
        if (coord.getBlock(world) != Blocks.bedrock) {
            coord.setBlock(world, active ? Blocks.lava : Blocks.obsidian, 0, false);
        }
    }

    public static boolean isReplaceable(World world, BlockCoord b) {
        Block k = b.getBlock(world);
        return b.y > 0 && b.y < 256 && (b.isAir(world) || !k.isOpaqueCube() && !isVolcanicFill(k));
    }

    public static List<BlockCoord> getOpenSides(World world, BlockCoord b) {
        ArrayList<BlockCoord> open = new ArrayList<>();

        for (BlockCoord t : b.getAdjacent()) {
            if (t.y <= b.y) {
                if (isReplaceable(world, t)) {
                    open.add(t);
                } else if (isVolcanicFill(t.getBlock(world))) {
                    for (BlockCoord n : t.getAdjacent()) {
                        if (n != t && n.y <= t.y && !open.contains(n) && isReplaceable(world, n)) {
                            open.add(n);
                        }
                    }
                }
            }
        }

        return open;
    }

    public static BlockCoord snapToHighest(World world, BlockCoord b) {
        b.y = getHighestSolidOrFillBlock(world, b.x, b.z);
        return b;
    }

    public static BlockCoord get2dINVERSEDistributedCoord(World world, BlockCoord origin, int maxDistance) {
        Random rand = world.rand;

        double distance;
        do {
            distance = rand.nextDouble() * maxDistance;
        } while (!(distance > 0.0) || !(rand.nextDouble() * maxDistance <= maxDistance / 8 + maxDistance / distance));

        BlockCoord b = origin.copy();
        double xDistance = rand.nextDouble() * distance * (rand.nextBoolean() ? -1 : 1);
        double zDistance = Math.sqrt(Math.pow(distance, 2.0) - Math.pow(xDistance, 2.0))
            * (rand.nextBoolean() ? -1 : 1);
        b.x = (int) (b.x + (xDistance > 0.0 ? Math.floor(xDistance) : Math.ceil(xDistance)));
        b.z = (int) (b.z + (zDistance > 0.0 ? Math.floor(zDistance) : Math.ceil(zDistance)));
        return b;
    }

    public static BlockCoord get2dSINDistributedCoord(World world, BlockCoord origin, int maxDistance) {
        Random rand = world.rand;

        double distance;
        double point;
        do {
            distance = rand.nextDouble() * maxDistance;
            point = 2.0 + Math.sin((distance + rand.nextDouble() * 3.0) / (maxDistance / 4));
            if (Double.isNaN(point)) {
                point = 1.0;
            }
        } while (!(distance > 0.0) || !(rand.nextDouble() * 3.0 <= point));

        BlockCoord b = origin.copy();
        double xDistance = rand.nextDouble() * distance * (rand.nextBoolean() ? -1 : 1);
        double zDistance = Math.sqrt(Math.pow(distance, 2.0) - Math.pow(xDistance, 2.0))
            * (rand.nextBoolean() ? -1 : 1);
        b.x = (int) (b.x + (xDistance > 0.0 ? Math.floor(xDistance) : Math.ceil(xDistance)));
        b.z = (int) (b.z + (zDistance > 0.0 ? Math.floor(zDistance) : Math.ceil(zDistance)));
        return b;
    }

    public static void charTree(World world, BlockCoord start) {
        start.setBlock(world, ArtificeBlocks.blockCharredLog, start.getMeta(world), false);

        for (BlockCoord b : start.getRadiusBlocks(2)) {
            if (b.getBlock(world)
                .isWood(world, b.x, b.y, b.z)) {
                charTree(world, b);
            } else if (b.getBlock(world)
                .isLeaves(world, b.x, b.y, b.z)) {
                    b.setBlock(world, Blocks.air, 0, false);
                }
        }
    }
}
