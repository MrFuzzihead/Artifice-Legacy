package com.mrfuzzihead.artifice.event;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraftforge.client.event.RenderPlayerEvent.SetArmorModel;
import net.minecraftforge.client.event.TextureStitchEvent.Post;
import net.minecraftforge.client.event.TextureStitchEvent.Pre;
import net.minecraftforge.fluids.Fluid;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeEnchants;
import com.mrfuzzihead.artifice.ArtificeFluids;

import cofh.core.render.IconRegistry;
import cofh.lib.util.helpers.StringHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ArtificeClientEventHandler {

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerIcons(Pre event) {
        if (event.map.getTextureType() == 0) {
            registerFluidIcons(ArtificeFluids.fluidBitumen, event.map);
            registerFluidIcons(ArtificeFluids.fluidCreosote, event.map);
            registerFluidIcons(ArtificeFluids.fluidFuel, event.map);
            registerFluidIcons(ArtificeFluids.fluidOil, event.map);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void initializeIcons(Post event) {
        setFluidIcons(ArtificeFluids.fluidBitumen);
        setFluidIcons(ArtificeFluids.fluidCreosote);
        setFluidIcons(ArtificeFluids.fluidFuel);
        setFluidIcons(ArtificeFluids.fluidOil);
    }

    private static void registerFluidIcons(Fluid fluid, IIconRegister ir) {
        String name = StringHelper.titleCase(fluid.getName());
        IconRegistry.addIcon("Fluid" + name, "artifice:fluid/Fluid_" + name + "_Still", ir);
        IconRegistry.addIcon("Fluid" + name + 1, "artifice:fluid/Fluid_" + name + "_Flow", ir);
    }

    private static void setFluidIcons(Fluid fluid) {
        String name = StringHelper.titleCase(fluid.getName());
        fluid.setIcons(IconRegistry.getIcon("Fluid" + name), IconRegistry.getIcon("Fluid" + name, 1));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderInvisibleArmor(SetArmorModel event) {
        if (ArtificeConfig.enchantmentInvisibleEnable && event.stack != null
            && EnchantmentHelper.getEnchantmentLevel(ArtificeEnchants.enchantmentInvisible.effectId, event.stack) > 0) {
            event.result = 0;
        }
    }
}
