package com.mrfuzzihead.artifice.event;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeEnchants;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ArtificeEventHandler {

    @SubscribeEvent
    public void onDeath(LivingDeathEvent event) {
        if (event.source.getEntity() instanceof EntityPlayer && ArtificeEnchants.hasSoulstealing()) {
            EntityPlayer player = (EntityPlayer) event.source.getEntity();
            ItemStack equipped = player.getCurrentEquippedItem();
            if (equipped != null) {
                int sslvl = EnchantmentHelper
                    .getEnchantmentLevel(ArtificeEnchants.enchantmentSoulstealing.effectId, equipped);
                if (sslvl > 0) {
                    EntityXPOrb xp = new EntityXPOrb(
                        event.entityLiving.worldObj,
                        event.entityLiving.posX,
                        event.entityLiving.posY,
                        event.entityLiving.posZ,
                        ArtificeConfig.enchantmentSoulstealingBonus * sslvl);
                    event.entityLiving.worldObj.spawnEntityInWorld(xp);
                }
            }
        }
    }

    @SubscribeEvent
    public void onDamage(LivingHurtEvent event) {
        if ((event.source.isMagicDamage() || event.source.getDamageType()
            .equals("wither")) && ArtificeEnchants.hasResistance()) {
            int reslvl = 0;

            for (int i = 1; i <= 4; i++) {
                reslvl += EnchantmentHelper.getEnchantmentLevel(
                    ArtificeEnchants.enchantmentResistance.effectId,
                    event.entityLiving.getEquipmentInSlot(i));
            }

            if (reslvl != 0) {
                double maxResistance = reslvl * 7 / 100.0F;
                double minResistance = maxResistance / 2.0;
                double finalResistance = minResistance + event.entityLiving.getRNG()
                    .nextDouble() * (maxResistance - minResistance);
                double reduction = 1.0 - finalResistance;
                event.ammount = (float) (event.ammount * reduction);
                if (event.ammount % 1.0F != 0.0F) {
                    double floor = Math.floor(event.ammount);
                    double remainder = event.ammount - floor;
                    if (event.entityLiving.getRNG()
                        .nextDouble() > remainder) {
                        event.ammount = (float) floor;
                    } else {
                        event.ammount = (float) floor + 1.0F;
                    }
                }

                int toDamage;
                do {
                    toDamage = event.entityLiving.worldObj.rand.nextInt(4) + 1;
                } while (event.entityLiving.getEquipmentInSlot(toDamage) == null);

                event.entityLiving.getEquipmentInSlot(toDamage)
                    .damageItem(1, event.entityLiving);
            }
        }
    }
}
