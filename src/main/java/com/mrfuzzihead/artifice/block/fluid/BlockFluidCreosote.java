package com.mrfuzzihead.artifice.block.fluid;

import java.util.Locale;

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

import cofh.core.fluid.BlockFluidCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cpw.mods.fml.common.registry.GameRegistry;

public class BlockFluidCreosote extends BlockFluidCoFHBase {

    public BlockFluidCreosote() {
        super("Artifice".toLowerCase(Locale.ENGLISH), ArtificeFluids.fluidCreosote, Material.water, "creosote");
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setQuantaPerBlock(6);
        this.setTickRate(20);
        this.setHardness(100.0F);
        this.setLightOpacity(7);
        this.setParticleColor(0.176F, 0.302F, 0.0F);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public MapColor getMapColor(int meta) {
        return MapColor.greenColor;
    }

    public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
        if (!ServerHelper.isClientWorld(world)) {
            if (world.getTotalWorldTime() % 8L == 0L) {
                if (entity instanceof EntityLivingBase) {
                    ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.confusion.id, 120, 0));
                }
            }
        }
    }

    public int getFireSpreadSpeed(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 300;
    }

    public int getFlammability(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return 25;
    }

    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return true;
    }

    public boolean isFireSource(World world, int x, int y, int z, ForgeDirection side) {
        return true;
    }
}
