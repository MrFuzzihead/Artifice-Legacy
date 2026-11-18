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

                // Damage one piece per hit, but only pieces that actually carry the enchantment. The old loop picked
                // a random armour slot and accepted any non-empty one, so it could wear out a piece that was not
                // providing the resistance at all (and it spun until a slot happened to be non-empty)
                // (CODE_REVIEW 5.26).
                for (int slot = 1; slot <= 4; slot++) {
                    ItemStack armour = event.entityLiving.getEquipmentInSlot(slot);
                    if (armour != null && EnchantmentHelper
                        .getEnchantmentLevel(ArtificeEnchants.enchantmentResistance.effectId, armour) > 0) {
                        armour.damageItem(1, event.entityLiving);
                    }
                }
            }
        }
    }
}
