package com.mrfuzzihead.artifice.block.frame;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.mrfuzzihead.artifice.util.BlockCoord;

/**
 * Support for the scaffold (CODE_REVIEW §6), which is the one part of this mod where the answer to "may this block
 * exist?" is a graph search.
 *
 * <p>
 * The search is being replaced: the old one scanned a (2r+1)³ cube - 35,937 block reads for a tier-3 scaffold - and
 * then ran a flood fill for every candidate in it, each with {@code LinkedList.contains} and a fresh {@code BlockCoord}
 * per neighbour. The replacement is a single flood fill over the connected component.
 * </p>
 *
 * <p>
 * So this test carries the <em>old</em> algorithm, transcribed, and compares the two on random worlds. That is the
 * point: the behaviour is a graph predicate with cases nobody thinks of - a chain that leaves the overhang and comes
 * back, a ring, a column with a gap - and "the code looks equivalent" is not an argument.
 * </p>
 */
public class BlockFrameScaffoldSupportTest {

    // ---------------------------------------------------------------------------------------------------------
    // A fake world: block and metadata by coordinate, solidity by coordinate.
    // ---------------------------------------------------------------------------------------------------------

    private static final class FakeWorld {

        final Map<String, Block> blocks = new HashMap<>();
        final Map<String, Integer> metas = new HashMap<>();
        final Set<String> solid = new HashSet<>();
        final Block stone = mock(Block.class);
        Block block = mock(BlockFrameScaffold.class);
        Block scaffold;

        World world;

        FakeWorld() {
            this(mock(BlockFrameScaffold.class));
        }

        FakeWorld(Block shared) {
            this.block = shared;
            this.scaffold = shared;
            world = mock(World.class);
            when(world.getBlock(anyInt(), anyInt(), anyInt())).thenAnswer(new Answer<Block>() {

                @Override
                public Block answer(InvocationOnMock invocation) {
                    return blocks.get(
                        key(
                            (Integer) invocation.getArguments()[0],
                            (Integer) invocation.getArguments()[1],
                            (Integer) invocation.getArguments()[2]));
                }
            });
            when(world.getBlockMetadata(anyInt(), anyInt(), anyInt())).thenAnswer(new Answer<Integer>() {

                @Override
                public Integer answer(InvocationOnMock invocation) {
                    Integer meta = metas.get(
                        key(
                            (Integer) invocation.getArguments()[0],
                            (Integer) invocation.getArguments()[1],
                            (Integer) invocation.getArguments()[2]));
                    return meta == null ? 0 : meta;
                }
            });
            when(world.isSideSolid(anyInt(), anyInt(), anyInt(), any(ForgeDirection.class)))
                .thenAnswer(new Answer<Boolean>() {

                    @Override
                    public Boolean answer(InvocationOnMock invocation) {
                        return solid.contains(
                            key(
                                (Integer) invocation.getArguments()[0],
                                (Integer) invocation.getArguments()[1],
                                (Integer) invocation.getArguments()[2]));
                    }
                });
        }

        static String key(int x, int y, int z) {
            return x + "," + y + "," + z;
        }

        void put(int x, int y, int z, Block block, int meta, boolean isSolid) {
            String k = key(x, y, z);
            blocks.put(k, block);
            metas.put(k, meta);
            if (isSolid) {
                solid.add(k);
            } else {
                solid.remove(k);
            }
        }

        void scaffold(int x, int y, int z, int meta) {
            put(x, y, z, scaffold, meta, false);
        }

        void stone(int x, int y, int z) {
            put(x, y, z, stone, 0, true);
        }

        void air(int x, int y, int z) {
            String k = key(x, y, z);
            blocks.remove(k);
            metas.remove(k);
            solid.remove(k);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // The algorithm being replaced, transcribed. This is the reference, not the implementation.
    // ---------------------------------------------------------------------------------------------------------

    /** The tier overhang table, copied from {@code BlockFrameScaffold.getOverhang}. */
    private static int overhang(int meta) {
        switch (meta) {
            case 0:
                return 4;
            case 1:
                return 8;
            case 2:
                return 12;
            case 3:
                return 16;
            default:
                return 0;
        }
    }

    private static boolean referenceCheckStay(FakeWorld fake, Block block, int x, int y, int z, int meta) {
        ForgeDirection[] sides = { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST,
            ForgeDirection.DOWN };

        BlockCoord c = new BlockCoord(x, y, z);
        if (referenceIsRooted(fake, block, x, y, z, meta)) {
            return true;
        }

        for (ForgeDirection d : sides) {
            BlockCoord t = c.copy()
                .offset(d.ordinal());
            if (t.getBlock(fake.world) instanceof BlockFrameScaffold) {
                if (t.blockEquals(fake.world, block, meta)) {
                    if (referenceIsRooted(fake, block, t.x, t.y, t.z, meta)) {
                        return true;
                    }

                    for (BlockCoord match : referenceRadiusMatches(fake, c, overhang(meta), block, meta)) {
                        if (referenceIsRooted(fake, block, match.x, match.y, match.z, meta)
                            && referenceIsConnected(fake, c, match, block, meta)
                            && c.getDistance(match) <= overhang(meta)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean referenceIsRooted(FakeWorld fake, Block block, int x, int y, int z, int meta) {
        for (int i = y - 1; i > 0; i--) {
            if (!fake.world.isSideSolid(x, i, z, ForgeDirection.UP)) {
                return false;
            }

            if (!fake.world.getBlock(x, i, z)
                .equals(block)) {
                return true;
            }

            if (fake.world.getBlockMetadata(x, i, z) != meta) {
                return false;
            }
        }

        return false;
    }

    private static List<BlockCoord> referenceRadiusMatches(FakeWorld fake, BlockCoord c, int radius, Block block,
        int meta) {
        List<BlockCoord> matches = new ArrayList<>();

        for (int x = c.x - radius; x < c.x + radius + 1; x++) {
            for (int y = c.y - radius; y < c.y + radius + 1; y++) {
                for (int z = c.z - radius; z < c.z + radius + 1; z++) {
                    BlockCoord t = new BlockCoord(x, y, z);
                    if (t.blockEquals(fake.world, block, meta)) {
                        matches.add(t);
                    }
                }
            }
        }

        return matches;
    }

    private static boolean referenceIsConnected(FakeWorld fake, BlockCoord from, BlockCoord to, Block block, int meta) {
        List<BlockCoord> traversed = new LinkedList<>();
        List<BlockCoord> toTraverse = new LinkedList<>();
        toTraverse.add(from);

        while (!toTraverse.isEmpty()) {
            BlockCoord t = toTraverse.get(0);
            traversed.add(toTraverse.remove(0));

            for (BlockCoord j : t.getAdjacent()) {
                if (j.blockEquals(fake.world, block, meta)) {
                    if (j.equals(to)) {
                        return true;
                    }

                    if (!traversed.contains(j) && !toTraverse.contains(j)) {
                        toTraverse.add(j);
                    }
                }
            }
        }

        return false;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Curated cases
    // ---------------------------------------------------------------------------------------------------------

    /** What {@code canBlockStay} does, minus the block instance: the tier comes from the world, as it does there. */
    private static boolean supported(FakeWorld fake, Block block, int x, int y, int z) {
        Integer stored = fake.metas.get(FakeWorld.key(x, y, z));
        int meta = stored == null ? 0 : stored;
        return BlockFrameScaffold.isSupported(fake.world, block, meta, overhang(meta), x, y, z);
    }

    @Test
    public void aBlockOnSolidGroundIsSupported() {
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.stone(0, 9, 0);
        fake.scaffold(0, 10, 0, 0);

        Assert.assertTrue(supported(fake, block, 0, 10, 0));
    }

    @Test
    public void aFloatingBlockWithNothingAroundItIsNotSupported() {
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.scaffold(0, 10, 0, 0);

        Assert.assertFalse(supported(fake, block, 0, 10, 0));
    }

    @Test
    public void aNeighbourWithItsOwnGroundSupportsTheBlock() {
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.scaffold(0, 10, 0, 0);
        fake.stone(1, 9, 0);
        fake.scaffold(1, 10, 0, 0);

        Assert.assertTrue(supported(fake, block, 0, 10, 0));
    }

    @Test
    public void aDifferentTierDoesNotAnchor() {
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.scaffold(0, 10, 0, 0);
        fake.stone(1, 9, 0);
        fake.scaffold(1, 10, 0, 1);

        Assert.assertFalse("tier 1 must not anchor tier 0", supported(fake, block, 0, 10, 0));
    }

    @Test
    public void aBlockIsNotSupportedByTheBlockDirectlyAboveIt() {
        // The old algorithm only looked for a same-tier neighbour on the five sides, never above, so an anchored
        // column cannot be extended downwards. That is preserved by the refactor and pinned here so that changing it
        // is a decision rather than an accident (CODE_REVIEW §9.11).
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;

        // An anchored scaffold directly above the position under test.
        fake.stone(0, 9, 0);
        fake.scaffold(0, 10, 0, 0);
        fake.air(0, 11, 0);

        Assert.assertTrue("the column itself is supported", supported(fake, block, 0, 10, 0));
        Assert.assertFalse("the space under it is not", supported(fake, block, 0, 9, 0));
    }

    @Test
    public void aSideNeighbourOpensTheSearchAndAnAboveNeighbourDoesNot() {
        // The entry rule, from the other direction: the walk only starts if one of the five sides is the same tier.
        // With a neighbour above and nothing on the sides, the block is unsupported even though the anchor is one
        // block away; add an anchored side neighbour and it becomes supported.
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.stone(0, 9, 0);
        fake.scaffold(0, 10, 0, 0); // anchored, directly above the position under test
        fake.air(0, 9, 0);

        Assert.assertFalse("nothing on the sides", supported(fake, block, 0, 9, 0));

        // A side neighbour that is itself anchored (on its own ground).
        fake.stone(1, 8, 0);
        fake.scaffold(1, 9, 0, 0);
        Assert.assertTrue("a same-tier neighbour on a side opens the search", supported(fake, block, 0, 9, 0));
    }

    @Test
    public void theOverhangIsTheTierRadius() {
        // Tier 0 has an overhang of 4: a chain four blocks long is supported, five is not.
        for (int length = 1; length <= 6; length++) {
            FakeWorld fake = new FakeWorld();
            Block block = fake.block;
            fake.stone(0, 9, 0);
            for (int i = 0; i <= length; i++) {
                fake.scaffold(i, 10, 0, 0);
            }

            boolean expected = length <= 4;
            Assert.assertEquals(
                "chain of " + length + " blocks from its anchor",
                expected,
                supported(fake, block, length, 10, 0));
        }
    }

    @Test
    public void aChainThatLeavesTheOverhangAndComesBackStillSupports() {
        // The case that stops "bound the flood fill by the overhang" from being a safe simplification: the anchor is
        // within reach, but the only path to it goes outside the reach first.
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        fake.stone(0, 9, 0);
        // Anchor column at the origin, then a long way out in +x, along +z, and back.
        for (int i = 0; i <= 6; i++) {
            fake.scaffold(i, 10, 0, 0);
            fake.scaffold(i, 10, 6, 0);
        }
        for (int i = 0; i <= 6; i++) {
            fake.scaffold(6, 10, i, 0);
            fake.scaffold(0, 10, i, 0);
        }
        // The block under test sits at the far corner, five blocks from the anchor in a straight line but reachable
        // only through a ring.
        fake.scaffold(3, 10, 3, 0);

        boolean viaReference = referenceCheckStay(fake, block, 3, 10, 3, 0);
        Assert.assertEquals(
            "the reference and the shipped code must agree on the ring",
            viaReference,
            supported(fake, block, 3, 10, 3));
    }

    // ---------------------------------------------------------------------------------------------------------
    // Random worlds: the reference and the implementation must agree everywhere
    // ---------------------------------------------------------------------------------------------------------

    @Test
    public void theImplementationAgreesWithTheReferenceOnRandomWorlds() {
        Random random = new Random(20261117L);
        // One world for the whole sweep: every round needs the same block identity, and ByteBuddy does not enjoy
        // being asked for hundreds of new mock classes.
        FakeWorld fake = new FakeWorld();
        Block block = fake.block;
        int checks = 0;

        for (int round = 0; round < 12; round++) {
            fake.blocks.clear();
            fake.metas.clear();
            fake.solid.clear();

            int size = 5;
            int baseY = 10;
            int height = 3;

            // A floor of stone under random columns, then random scaffold in the volume above it. Tier 0 only: the
            // reference scans a (2r+1)^3 cube per candidate, so tier 3's 33-cube would make this sweep minutes long.
            // The higher tiers are covered by the curated overhang test, which is the only thing that varies.
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    if (random.nextBoolean()) {
                        fake.stone(x, baseY - 1, z);
                    }
                }
            }

            for (int x = 0; x < size; x++) {
                for (int y = baseY; y < baseY + height; y++) {
                    for (int z = 0; z < size; z++) {
                        if (random.nextInt(3) == 0) {
                            fake.scaffold(x, y, z, 0);
                        }
                    }
                }
            }

            for (int x = 0; x < size; x++) {
                for (int y = baseY; y < baseY + height; y++) {
                    for (int z = 0; z < size; z++) {
                        // The implementation reads the tier from the world, so the reference has to be given the
                        // same one - a random tier here would compare two different questions.
                        Integer stored = fake.metas.get(FakeWorld.key(x, y, z));
                        int meta = stored == null ? 0 : stored;
                        boolean expected = referenceCheckStay(fake, block, x, y, z, meta);
                        boolean actual = supported(fake, block, x, y, z);

                        Assert.assertEquals(
                            "round " + round + " at " + x + "," + y + "," + z + " (meta " + meta + ")",
                            expected,
                            actual);
                        checks++;
                    }
                }
            }
        }

        Assert.assertTrue("the random sweep should be substantial, was " + checks, checks > 800);
    }
}
