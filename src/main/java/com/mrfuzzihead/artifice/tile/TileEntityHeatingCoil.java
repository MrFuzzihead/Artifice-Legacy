package com.mrfuzzihead.artifice.tile;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.util.MiscUtils;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyReceiver;

public class TileEntityHeatingCoil extends TileEntity implements IEnergyReceiver, IHeatProvider {

    private EnergyStorage energyStorage = new EnergyStorage(1600);

    public boolean canUpdate() {
        return true;
    }

    public void updateEntity() {
        if (this.energyStorage.getEnergyStored() > this.energyStorage.getMaxEnergyStored() / 2) {
            this.energyStorage.receiveEnergy(
                MiscUtils.provideEnergyToNeighbors(
                    this.energyStorage.extractEnergy(ArtificeConfig.heatingCoilRate, false),
                    this.worldObj,
                    this.xCoord,
                    this.yCoord,
                    this.zCoord),
                false);
        }

        IHeatReceiver target = MiscUtils.getHeatReceiverNeighbor(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        if (target != null && this.energyStorage.getEnergyStored() > 0 && target.getHeat() < target.getMaxHeat()) {
            int sent = target.receiveHeat(this.energyStorage.extractEnergy(ArtificeConfig.heatingCoilRate, false));
            this.energyStorage.receiveEnergy(ArtificeConfig.heatingCoilRate - sent, false);
        }

        if (this.energyStorage.getEnergyStored() > 0
            && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) != 1) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
        } else if (this.energyStorage.getEnergyStored() == 0
            && this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) != 0) {
                this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
            }

        this.energyStorage.extractEnergy(ArtificeConfig.coilLoss, false);
        if (this.energyStorage.getEnergyStored() > 0) {
            this.doFire();
        }
    }

    private void doFire() {
        AxisAlignedBB around = this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)
            .getCollisionBoundingBoxFromPool(this.worldObj, this.xCoord, this.yCoord, this.zCoord)
            .expand(0.1, 0.1, 0.1);

        for (EntityLivingBase entity : this.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, around)) {
            entity.setFire(1);
        }
    }

    public int receiveEnergy(ForgeDirection forgeDirection, int i, boolean b) {
        return this.energyStorage.receiveEnergy(i, b);
    }

    public int getEnergyStored(ForgeDirection forgeDirection) {
        return this.energyStorage.getEnergyStored();
    }

    public int getMaxEnergyStored(ForgeDirection forgeDirection) {
        return this.energyStorage.getMaxEnergyStored();
    }

    public boolean canConnectEnergy(ForgeDirection forgeDirection) {
        return true;
    }

    @Override
    public int extractHeat(int amount) {
        return this.energyStorage.extractEnergy(amount, false);
    }

    @Override
    public int getHeat() {
        return this.energyStorage.getEnergyStored();
    }

    @Override
    public int getMaxHeat() {
        return this.energyStorage.getMaxEnergyStored();
    }

    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        this.writeToNBT(tag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, tag);
    }

    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        this.energyStorage.readFromNBT(tag);
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        this.energyStorage.writeToNBT(tag);
    }
}
