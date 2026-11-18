package com.mrfuzzihead.artifice.block.fluid;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeFluids;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.util.Names;

import cofh.core.fluid.BlockFluidCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockFluidBitumen extends BlockFluidCoFHBase {

    public BlockFluidBitumen() {
        super(Names.FLUID_NAMESPACE, ArtificeFluids.fluidBitumen, Material.water, "bitumen");
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setQuantaPerBlock(2);
        this.setTickRate(50);
        this.setHardness(100.0F);
        this.setLightOpacity(7);
        this.setParticleColor(0.0F, 0.0F, 0.0F);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public MapColor getMapColor(int meta) {
        return MapColor.blackColor;
    }

    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!ServerHelper.isClientWorld(world)) {
            if (world.getTotalWorldTime() % 8L == 0L) {
                if (entity instanceof EntityLivingBase) {
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 120, 5));
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.jump.id, 120, -4));
                }
            }
        }
    }

    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 5;
    }

    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 5;
    }

    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return true;
    }

    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }
}
