package shukaro.artifice.tile;

import cofh.api.energy.EnergyStorage;
import cofh.api.energy.IEnergyProvider;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.util.MiscUtils;

public class TileEntityNuclearBattery extends TileEntity implements IEnergyProvider {
   private EnergyStorage charge = new EnergyStorage(ArtificeConfig.nuclearBatteryCapacity);
   private int tickGen = 0;

   public boolean canUpdate() {
      return true;
   }

   public void updateEntity() {
      this.tickGen = 0;
      int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
      int power = this.charge.getEnergyStored();
      if (power == 0 && meta != 3) {
         this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 3, 3);
      } else if (power > 0 && power <= ArtificeConfig.nuclearBatteryCapacity * 0.4 && meta != 2) {
         this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 2, 3);
      } else if (power > ArtificeConfig.nuclearBatteryCapacity * 0.4 && power <= ArtificeConfig.nuclearBatteryCapacity * 0.7 && meta != 1) {
         this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 1, 3);
      } else if (power > ArtificeConfig.nuclearBatteryCapacity * 0.7 && meta != 0) {
         this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, 0, 3);
      }

      this.charge
         .receiveEnergy(
            MiscUtils.provideEnergyToNeighbors(
               this.charge.extractEnergy(ArtificeConfig.nuclearBatteryRate, false),
               this.worldObj,
               this.xCoord,
               this.yCoord,
               this.zCoord
            ),
            false
         );
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
      this.charge.readFromNBT(tag);
   }

   public void writeToNBT(NBTTagCompound tag) {
      super.writeToNBT(tag);
      this.charge.writeToNBT(tag);
   }

   public void setEnergy(int amount) {
      this.charge.setEnergyStored(amount);
   }

   public int getMaxRate() {
      float ratio = (float)this.charge.getEnergyStored() / this.charge.getMaxEnergyStored();
      return (int)Math.ceil(ArtificeConfig.nuclearBatteryRate * ratio);
   }

   public int extractEnergy(ForgeDirection dir, int amount, boolean simulate) {
      int maxRate = this.getMaxRate();
      if (this.tickGen >= maxRate) {
         return 0;
      } else {
         if (amount > maxRate) {
            amount = maxRate;
         }

         int x = this.charge.extractEnergy(amount, simulate);
         if (!simulate) {
            this.tickGen += x;
         }

         return x;
      }
   }

   public int getEnergyStored(ForgeDirection dir) {
      return this.charge.getEnergyStored();
   }

   public int getMaxEnergyStored(ForgeDirection dir) {
      return this.charge.getMaxEnergyStored();
   }

   public boolean canConnectEnergy(ForgeDirection dir) {
      return true;
   }
}
