package com.mrfuzzihead.artifice.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.event.Tracking;

public class TileEntityAttuned extends TileEntity {

    /**
     * Private and set through {@link #setFrequency(String)}: the frequency index files this tile by it, so a direct
     * assignment would leave the tile under its old key and silently stop coupling (CODE_REVIEW §6).
     */
    private String frequency = "";
    public int power = 0;

    public boolean canUpdate() {
        return true;
    }

    // Indexed by frequency for as long as the tile is loaded (CODE_REVIEW §6). validate() runs when the tile is added
    // to a chunk and when its chunk loads; invalidate() runs on removal and on chunk unload.
    public void validate() {
        super.validate();
        Tracking.track(this);
    }

    public void invalidate() {
        Tracking.untrack(this);
        super.invalidate();
    }

    public String getFrequency() {
        return this.frequency;
    }

    /**
     * Re-files the tile under its new frequency. Callers must not assign the field: the index would keep the tile
     * under the old key, and a transmitter on that frequency would keep reading it.
     */
    public void setFrequency(String frequency) {
        String next = frequency == null ? "" : frequency;
        if (next.equals(this.frequency)) {
            return;
        }

        String previous = this.frequency;
        this.frequency = next;
        Tracking.reindex(this, previous);
    }

    public void updateEntity() {
        if (this.frequency.length() > 0) {
            int newPower = this.getPower();
            if (this.power != newPower) {
                this.power = newPower;
                if (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)
                    == ArtificeBlocks.blockAttunedRedstoneTransmitter) {
                    this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
                    this.worldObj.notifyBlocksOfNeighborChange(
                        this.xCoord,
                        this.yCoord,
                        this.zCoord,
                        this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord));
                }
            }
        }
    }

    public int getPower() {
        return this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord)
            == ArtificeBlocks.blockAttunedRedstoneReceiver
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
        // Not setFrequency(): this may run while the chunk is still loading, and the tile has not been indexed yet,
        // so a reindex here would file it prematurely. validate() files it, once the tile is really in a chunk.
        this.frequency = tag.getString("frequency");
        this.power = tag.getInteger("power");
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setString("frequency", this.frequency);
        tag.setInteger("power", this.power);
    }
}
