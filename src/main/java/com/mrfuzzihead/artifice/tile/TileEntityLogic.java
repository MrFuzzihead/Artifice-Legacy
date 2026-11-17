package com.mrfuzzihead.artifice.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import cofh.lib.util.helpers.MathHelper;

public class TileEntityLogic extends TileEntity {

    public TileEntityLogic.SideState[] sides = new TileEntityLogic.SideState[6];
    public int power = 0;

    public TileEntityLogic() {
        for (int i = 0; i < 6; i++) {
            this.sides[i] = TileEntityLogic.SideState.DISABLED;
        }
    }

    public boolean canUpdate() {
        return true;
    }

    public void updateEntity() {
        int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);
        int power = 0;
        if (meta == 0) {
            boolean hasInput = false;

            for (int i = 0; i < 6; i++) {
                if (this.sides[i] == TileEntityLogic.SideState.POSITIVE
                    || this.sides[i] == TileEntityLogic.SideState.NEGATIVE) {
                    hasInput = true;
                    break;
                }
            }

            boolean powered = hasInput;

            for (int ix = 0; ix < 6; ix++) {
                if (this.sides[ix] == TileEntityLogic.SideState.POSITIVE && this.getIndirectPowerFrom(ix) == 0) {
                    powered = false;
                } else if (this.sides[ix] == TileEntityLogic.SideState.NEGATIVE && this.getIndirectPowerFrom(ix) > 0) {
                    powered = false;
                }
            }

            power = powered ? 15 : 0;
        } else {
            for (int ixx = 0; ixx < 6; ixx++) {
                if (this.sides[ixx] == TileEntityLogic.SideState.POSITIVE) {
                    power += this.getIndirectPowerFrom(ixx);
                }

                if (this.sides[ixx] == TileEntityLogic.SideState.NEGATIVE) {
                    power -= this.getIndirectPowerFrom(ixx);
                }
            }

            power = MathHelper.clampI(power, 0, 15);
        }

        boolean changed = this.power != power;
        if (changed) {
            this.power = power;
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            this.worldObj.notifyBlocksOfNeighborChange(
                this.xCoord,
                this.yCoord,
                this.zCoord,
                this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord));
        }
    }

    private int getIndirectPowerFrom(int side) {
        ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[side];
        return this.worldObj.getIndirectPowerLevelTo(
            this.xCoord + dir.offsetX,
            this.yCoord + dir.offsetY,
            this.zCoord + dir.offsetZ,
            dir.ordinal());
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
        int[] sidesInt = tag.getIntArray("sides");

        for (int i = 0; i < 6; i++) {
            this.sides[i] = TileEntityLogic.SideState.values()[sidesInt[i]];
        }

        this.power = tag.getInteger("power");
    }

    public void writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        int[] sidesInt = new int[6];

        for (int i = 0; i < 6; i++) {
            sidesInt[i] = this.sides[i].ordinal();
        }

        tag.setIntArray("sides", sidesInt);
        tag.setInteger("power", this.power);
    }

    public static enum SideState {
        DISABLED,
        OUTPUT,
        POSITIVE,
        NEGATIVE;
    }
}
