package com.mrfuzzihead.artifice.block.fluid;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeFluids;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.util.BlockCoord;

import cofh.core.fluid.BlockFluidCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import gnu.trove.set.hash.THashSet;

public class BlockFluidFuel extends BlockFluidCoFHBase {

    public static THashSet<Block> ignitionSources = new THashSet();

    public BlockFluidFuel() {
        super("Artifice".toLowerCase(Locale.ENGLISH), ArtificeFluids.fluidFuel, Material.water, "fuel");
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setQuantaPerBlock(8);
        this.setTickRate(20);
        this.setHardness(100.0F);
        this.setLightOpacity(4);
        this.setParticleColor(0.957F, 0.886F, 0.176F);
        ignitionSources.add(Blocks.fire);
        ignitionSources.add(Blocks.lava);
        ignitionSources.add(Blocks.flowing_lava);
        ignitionSources.add(Blocks.torch);
        ignitionSources.add(Blocks.lit_furnace);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public MapColor getMapColor(int meta) {
        return MapColor.yellowColor;
    }

    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!ServerHelper.isClientWorld(world)) {
            if (world.getTotalWorldTime() % 8L == 0L) {
                if (entity instanceof EntityLivingBase && (entity.isBurning() || entity instanceof EntityBlaze)) {
                    this.doIgnition(world, x, y, z);
                }
            }
        }
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        if (ignitionSources.contains(neighbor)) {
            this.doIgnition(world, x, y, z);
        }
    }

    private void doIgnition(World world, int x, int y, int z) {
        world.setBlockToAir(x, y, z);
        world.newExplosion(null, x, y, z, 4.0F, true, true);
        BlockCoord c = new BlockCoord(x, y, z);

        for (BlockCoord n : c.getAdjacent()) {
            if (n.getBlock(world)
                .equals(ArtificeBlocks.blockFuel)) {
                this.doIgnition(world, n.x, n.y, n.z);
            }
        }
    }
}
