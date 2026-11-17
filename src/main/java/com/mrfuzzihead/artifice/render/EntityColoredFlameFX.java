package com.mrfuzzihead.artifice.render;

import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;

public class EntityColoredFlameFX extends EntityFX {

    private int color;

    public EntityColoredFlameFX(World world, int color, double posX, double posY, double posZ, double velX, double velY,
        double velZ) {
        super(world, posX, posY, posZ, velX, velY, velZ);
        this.motionX = this.motionX * 0.01F + velX;
        this.motionY = this.motionY * 0.01F + velY;
        this.motionZ = this.motionZ * 0.01F + velZ;
        double d6 = posX + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        d6 = posY + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        d6 = posZ + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.05F;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleMaxAge = (int) (8.0 / (Math.random() * 0.8 + 0.2)) + 4;
        this.noClip = true;
        this.color = color;
        this.setParticleIcon(ArtificeBlocks.blockColoredTorches[color].particleIcon);
    }

    public int getFXLayer() {
        return 1;
    }

    public void renderParticle(Tessellator p_70539_1_, float p_70539_2_, float p_70539_3_, float p_70539_4_,
        float p_70539_5_, float p_70539_6_, float p_70539_7_) {
        float f6 = (this.particleAge + p_70539_2_) / this.particleMaxAge;
        this.particleScale *= 1.0F - f6 * f6 * 0.5F;
        super.renderParticle(p_70539_1_, p_70539_2_, p_70539_3_, p_70539_4_, p_70539_5_, p_70539_6_, p_70539_7_);
    }

    public int getBrightnessForRender(float p_70070_1_) {
        float f1 = (this.particleAge + p_70070_1_) / this.particleMaxAge;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        int i = super.getBrightnessForRender(p_70070_1_);
        int j = i & 0xFF;
        int k = i >> 16 & 0xFF;
        j += (int) (f1 * 15.0F * 16.0F);
        if (j > 240) {
            j = 240;
        }

        return j | k << 16;
    }

    public float getBrightness(float p_70013_1_) {
        float f1 = (this.particleAge + p_70013_1_) / this.particleMaxAge;
        if (f1 < 0.0F) {
            f1 = 0.0F;
        }

        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        float f2 = super.getBrightness(p_70013_1_);
        return f2 * f1 + (1.0F - f1);
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.96F;
        this.motionY *= 0.96F;
        this.motionZ *= 0.96F;
        if (this.onGround) {
            this.motionX *= 0.7F;
            this.motionZ *= 0.7F;
        }
    }
}
