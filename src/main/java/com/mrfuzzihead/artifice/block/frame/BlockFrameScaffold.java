package com.mrfuzzihead.artifice.block.frame;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.ArrayUtils;
import com.mrfuzzihead.artifice.util.BlockCoord;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameScaffold extends BlockFrame {

    /**
     * The directions a same-block, same-tier neighbour may be in for the support search to start at all. All six:
     * the block directly above counts, so an anchored column can be extended downwards (CODE_REVIEW §9.11).
     */
    private static final ForgeDirection[] entryDirections = ForgeDirection.VALID_DIRECTIONS;

    /**
     * How far inside the block the climbable face starts: a climber is only slowed when it is actually against the
     * scaffold, not when it merely brushes the corner. The collision box is inset by an eighth of a block on x and z
     * (on top of the block's own bounds, which are already inset by 0.01) so the scaffold does not swallow the
     * player's feet.
     */
    private static final float CLIMB_EDGE = 0.022222223F;
    private static final float COLLISION_INSET = 0.125F;
    private IIcon[] icons = new IIcon[ArtificeConfig.tiers.length];
    private IIcon[] sideIcons = new IIcon[ArtificeConfig.tiers.length];

    public BlockFrameScaffold() {
        this.setBlockName(Names.SCAFFOLD);
        this.setBlockBounds(0.01F, 0.01F, 0.01F, 0.99F, 0.99F, 0.99F);
        GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        TextureHandler.registerConnectedTexture(reg, this, 0, 0, "basic", "scaffold");
        TextureHandler.registerConnectedTexture(reg, this, 0, 1, "basic", "scaffold");
        this.icons[0] = TextureHandler.getConnectedTexture(this, 0, 1).icon;
        TextureHandler.registerConnectedTexture(reg, this, 1, 0, "reinforced", "scaffold");
        TextureHandler.registerConnectedTexture(reg, this, 1, 1, "reinforced", "scaffold");
        this.icons[1] = TextureHandler.getConnectedTexture(this, 1, 1).icon;
        TextureHandler.registerConnectedTexture(reg, this, 2, 0, "industrial", "scaffold");
        TextureHandler.registerConnectedTexture(reg, this, 2, 1, "industrial", "scaffold");
        this.icons[2] = TextureHandler.getConnectedTexture(this, 2, 1).icon;
        TextureHandler.registerConnectedTexture(reg, this, 3, 0, "advanced", "scaffold");
        TextureHandler.registerConnectedTexture(reg, this, 3, 1, "advanced", "scaffold");
        this.icons[3] = TextureHandler.getConnectedTexture(this, 3, 1).icon;

        for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
            this.sideIcons[i] = TextureHandler
                .registerIcon(reg, ArtificeConfig.tiers[i].toLowerCase(Locale.ENGLISH), "scaffold/sides");
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        return ArrayUtils.getOrFirst(side != 0 && side != 1 ? this.sideIcons : this.icons, meta);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
        // The side branch used to index sideIcons with the raw metadata, unchecked - a corrupt meta was an AIOOBE
        // from inside chunk rendering.
        return ArrayUtils
            .getOrFirst(side != 0 && side != 1 ? this.sideIcons : this.icons, access.getBlockMetadata(x, y, z));
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
        return true;
    }

    public int getRenderType() {
        return ArtificeConfig.frameRenderID;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return side.ordinal() < 2;
    }

    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        float shrinkAmount = CLIMB_EDGE;
        if (!(entity.boundingBox.minY >= y + (1.0F - shrinkAmount)) && !(entity.boundingBox.maxY <= y + shrinkAmount)) {
            entity.fallDistance = 0.0F;
            if (entity.isCollidedHorizontally) {
                entity.motionY = 0.2;
            } else if (entity.isSneaking()) {
                double diff = entity.prevPosY - entity.posY;
                entity.boundingBox.minY += diff;
                entity.boundingBox.maxY += diff;
                entity.posY = entity.prevPosY;
            } else {
                entity.motionY = -0.12;
            }
        }
    }

    public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
        float shrinkAmount = COLLISION_INSET;
        return AxisAlignedBB.getBoundingBox(
            x + this.minX + shrinkAmount,
            y + this.minY,
            z + this.minZ + shrinkAmount,
            x + this.maxX - shrinkAmount,
            y + this.maxY,
            z + this.maxZ - shrinkAmount);
    }

    public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
        ItemStack held = player.inventory.mainInventory[player.inventory.currentItem];
        if (held != null && held.getItem() != null && Block.getBlockFromItem(held.getItem()) != null) {
            Block hb = Block.getBlockFromItem(held.getItem());
            if (held != null && hb != null
                && hb.equals(this)
                && held.getItemDamage() == world.getBlockMetadata(x, y, z)) {
                // Bound the climb: getBlock above y=255 is not guaranteed to be anything, and a pillar built to the
                // build limit used to walk past it.
                while (y < 256 && world.getBlock(x, y, z)
                    .equals(this)) {
                    y++;
                }

                if (this.checkStay(world, x, y, z, held.getItemDamage()) && world.isAirBlock(x, y, z)) {
                    world.setBlock(x, y, z, this, held.getItemDamage(), 3);
                    if (!player.capabilities.isCreativeMode) {
                        held.stackSize--;
                        if (held.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }
                    }
                }
            }
        }
    }

    public boolean canReplace(World world, int x, int y, int z, int side, ItemStack stack) {
        return this.checkStay(world, x, y, z, stack.getItemDamage());
    }

    public int getOverhang(int meta) {
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

    public boolean canBlockStay(World world, int x, int y, int z) {
        return this.checkStay(world, x, y, z, world.getBlockMetadata(x, y, z));
    }

    private boolean checkStay(World world, int x, int y, int z, int meta) {
        return isSupported(world, this, meta, this.getOverhang(meta), x, y, z);
    }

    /**
     * Is a scaffold of this block and tier supported at that position?
     *
     * <p>
     * Static and package-private so it can be tested without a registered block: the support question is a graph
     * predicate, and {@code BlockFrameScaffoldSupportTest} checks it against a transcription of the algorithm this
     * replaced (CODE_REVIEW §6).
     * </p>
     */
    static boolean isSupported(World world, Block block, int meta, int overhang, int x, int y, int z) {
        BlockCoord start = new BlockCoord(x, y, z);
        if (isRooted(world, block, meta, x, y, z)) {
            return true;
        }

        // One flood fill over the connected run of same-block, same-tier scaffolds. The old shape was a full
        // (2r+1)^3 cube scan - 35,937 block reads at tier 3 - followed by a flood fill per candidate, each with
        // LinkedList.contains and a fresh BlockCoord per neighbour: tens of thousands of block reads and millions of
        // list comparisons for a question with one answer.
        //
        // Two details keep this the same question the old code asked, except where §9.11 changed it:
        //
        // - the search only starts if one of the six neighbours is the same block and tier. It was five - the sides
        // and below - until §9.11, which deliberately made the block above count so that a column can be built
        // downwards from an anchored one. That is the only intended difference from the old algorithm, and the
        // support test's reference copy carries the same sixth direction;
        // - once started, the walk covers the whole connected run, not just what is within the overhang. An anchor
        // within reach can be connected only through blocks outside it (a ring, a U), and the old code found
        // those because isConnected() was unbounded while the distance check was not.
        boolean touchesANeighbour = false;
        for (ForgeDirection d : entryDirections) {
            if (start.copy()
                .offset(d.ordinal())
                .blockEquals(world, block, meta)) {
                touchesANeighbour = true;
                break;
            }
        }

        if (!touchesANeighbour) {
            return false;
        }

        Set<BlockCoord> seen = new HashSet<>();
        ArrayDeque<BlockCoord> frontier = new ArrayDeque<>();
        seen.add(start);
        frontier.add(start);

        while (!frontier.isEmpty()) {
            BlockCoord c = frontier.poll();
            if (start.getDistance(c) <= overhang && isRooted(world, block, meta, c.x, c.y, c.z)) {
                return true;
            }

            for (BlockCoord neighbour : c.getAdjacent()) {
                if (neighbour.blockEquals(world, block, meta) && seen.add(neighbour)) {
                    frontier.add(neighbour);
                }
            }
        }

        return false;
    }

    static boolean isRooted(World world, Block block, int meta, int x, int y, int z) {
        for (int i = y - 1; i > 0; i--) {
            if (!world.isSideSolid(x, i, z, ForgeDirection.UP)) {
                return false;
            }

            if (!world.getBlock(x, i, z)
                .equals(block)) {
                return true;
            }

            if (world.getBlockMetadata(x, i, z) != meta) {
                return false;
            }
        }

        return false;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (!this.canBlockStay(world, x, y, z)) {
            this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
            world.setBlockToAir(x, y, z);
        }
    }
}
