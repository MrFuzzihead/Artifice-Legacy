package com.mrfuzzihead.artifice.block.frame;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockFrameDetector extends BlockFrame {

    /** Idle: no power, no pulse in flight. This is also the only state that a neighbour change can arm. */
    public static final int META_IDLE = 0;
    /** The pulse. The only state that emits power. */
    public static final int META_ACTIVE = 1;
    /**
     * Pulse tail: the power is already back off, but the block is deliberately not idle yet. It exists to absorb
     * the echo of the falling-edge notification - a neighbour that reacts to that notification reaches us while we
     * are in this state, the guard in {@link #onNeighborBlockChange} drops it, and the pulse then ends silently.
     */
    public static final int META_TAIL = 2;

    /** Notify neighbours (flag 1) and send the change to clients (flag 2). Neighbours recompute redstone on this. */
    private static final int FLAGS_NOTIFY = 1 | 2;
    /**
     * Send to clients only. The power level did not change, so no neighbour needs to recompute anything - and if we
     * told them anyway, the reactive ones would notify us back, find us idle and re-arm the pulse, forever.
     */
    private static final int FLAGS_SILENT = 2;

    private static final int tickRate = 2;
    private IIcon icon;

    public BlockFrameDetector() {
        this.setBlockName(Names.DETECTOR);

        for (int i = 1; i <= 3; i++) {
            this.validTiers.remove(ArtificeConfig.tiers[i]);
        }

        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @Override
    public void registerBlockIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "detector", "misc");
    }

    @Override
    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        return this.icon;
    }

    public boolean canProvidePower() {
        return true;
    }

    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return emitsPower(blockAccess.getBlockMetadata(x, y, z)) ? 15 : 0;
    }

    public boolean canConnectRedstone(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return true;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    /** @return the state the pulse moves to, and where an unknown (corrupt, older save) metadata heals to */
    public static int nextMetadata(int meta) {
        switch (meta) {
            case META_IDLE:
                return META_ACTIVE;
            case META_ACTIVE:
                return META_TAIL;
            case META_TAIL:
            default:
                return META_IDLE;
        }
    }

    /** @return true only for the state that powers redstone */
    public static boolean emitsPower(int meta) {
        return meta == META_ACTIVE;
    }

    /** @return true when moving between these two states changes what a redstone consumer sees */
    static boolean isPowerEdge(int from, int to) {
        return emitsPower(from) != emitsPower(to);
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        // Both of these are our own echo, not a change we should react to: `neighbor == this` is the state change we
        // just wrote, and a non-idle metadata means a pulse is in flight. Without the second guard the pulse would
        // restart itself for as long as anything nearby reacts to its own output.
        if (neighbor == this) return;
        if (world.getBlockMetadata(x, y, z) != META_IDLE) return;

        world.scheduleBlockUpdate(x, y, z, this, tickRate);
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        int meta = world.getBlockMetadata(x, y, z);
        int next = nextMetadata(meta);

        // Notify on the two power edges only, and stay silent on the tail. The metadata write is what tells the
        // neighbours (flag 1), so there is no separate notify call: doing both meant three notifications per pulse,
        // one of which arrived with the power already off and re-armed every reactive neighbour in the area.
        world.setBlockMetadataWithNotify(x, y, z, next, isPowerEdge(meta, next) ? FLAGS_NOTIFY : FLAGS_SILENT);

        if (isPowerEdge(meta, next)) {
            // The pulse continues only while it still has a step that matters; the tail write above is the last one.
            world.scheduleBlockUpdate(x, y, z, this, tickRate);
        }
    }
}
