package shukaro.artifice.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.event.Tracking;

public class TileEntityAttuned extends TileEntity {
   public String frequency = "";
   public int power = 0;

   public boolean canUpdate() {
      return true;
   }

   public void updateEntity() {
      if (this.frequency.length() > 0) {
         int newPower = this.getPower();
         if (this.power != newPower) {
            this.power = newPower;
            if (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)
               == ArtificeBlocks.blockAttunedRedstoneTransmitter) {
               this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
               this.worldObj
                  .notifyBlocksOfNeighborChange(
                     this.xCoord,
                     this.yCoord,
                     this.zCoord,
                     this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)
                  );
            }
         }
      }
   }

   public int getPower() {
      return this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord) == ArtificeBlocks.blockAttunedRedstoneReceiver
         ? this.worldObj.getBlockPowerInput(this.xCoord, this.yCoord, this.zCoord)
         : Tracking.getHighestFrequencyPower(this.frequency);
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
      this.frequency = tag.getString("frequency");
      this.power = tag.getInteger("power");
   }

   public void writeToNBT(NBTTagCompound tag) {
      super.writeToNBT(tag);
      tag.setString("frequency", this.frequency);
      tag.setInteger("power", this.power);
   }
}
