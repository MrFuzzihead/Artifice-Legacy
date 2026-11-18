package com.mrfuzzihead.artifice.block.functional;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.event.Tracking;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.tile.TileEntityAttuned;
import com.mrfuzzihead.artifice.util.FormatCodes;
import com.mrfuzzihead.artifice.util.MiscUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockAttunedRedstone extends Block implements ITileEntityProvider {

    private IIcon powered;
    private IIcon unpowered;
    private boolean transmitter;

    public BlockAttunedRedstone(boolean transmitter) {
        super(Material.rock);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setBlockName(transmitter ? Names.ATTUNED_TRANSMITTER : Names.ATTUNED_RECEIVER);
        this.transmitter = transmitter;
        GameRegistry.registerBlock(this, ItemBlockAttunedRedstone.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.powered = TextureHandler
            .registerIcon(reg, "powered" + (this.transmitter ? "transmitter" : "receiver"), "attuned");
        this.unpowered = TextureHandler
            .registerIcon(reg, "unpowered" + (this.transmitter ? "transmitter" : "receiver"), "attuned");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.unpowered;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityAttuned) {
            return ((TileEntityAttuned) te).power > 0 ? this.powered : this.unpowered;
        } else {
            return this.unpowered;
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote && player.getHeldItem() == null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityAttuned) {
                TileEntityAttuned tea = (TileEntityAttuned) te;
                if (tea.getFrequency()
                    .length() > 0) {
                    MiscUtils.addChatMessage(
                        player,
                        FormatCodes.Italic.code + StatCollector.translateToLocal("chat.artifice.frequency")
                            + " "
                            + FormatCodes.Aqua.code
                            + tea.getFrequency());
                } else {
                    MiscUtils.addChatMessage(
                        player,
                        FormatCodes.Italic.code + StatCollector.translateToLocal("chat.artifice.nofrequency"));
                }

                return true;
            }
        }

        return false;
    }

    public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
        ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityAttuned) {
            TileEntityAttuned tea = (TileEntityAttuned) te;
            if (tea.getFrequency()
                .length() > 0) {
                stack.setStackDisplayName(tea.getFrequency());
            }
        }

        return stack;
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (te instanceof TileEntityAttuned) {
            TileEntityAttuned tea = (TileEntityAttuned) te;
            return this.transmitter ? tea.power : 0;
        } else {
            return 0;
        }
    }

    public boolean canConnectRedstone(IBlockAccess blockAccess, int x, int y, int z, int side) {
        return true;
    }

    public boolean canProvidePower() {
        return this.transmitter;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityAttuned) {
            TileEntityAttuned tea = (TileEntityAttuned) te;
            if (tea.getFrequency()
                .length() > 0) {
                Tracking.updateFrequency(tea.getFrequency());
            }
        }
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, living, stack);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityAttuned) {
            TileEntityAttuned tea = (TileEntityAttuned) te;
            if (stack.hasDisplayName()) {
                tea.setFrequency(stack.getDisplayName());
                Tracking.updateFrequency(tea.getFrequency());
            }
        }
    }

    public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
        if (!player.capabilities.isCreativeMode) {
            player.addExhaustion(0.025F);
        }

        if (!world.isRemote && world.getGameRules()
            .getGameRuleBooleanValue("doTileDrops")) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityAttuned) {
                ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
                TileEntityAttuned tea = (TileEntityAttuned) te;
                if (tea.getFrequency()
                    .length() > 0) {
                    stack.setStackDisplayName(tea.getFrequency());
                    Tracking.updateFrequency(stack.getDisplayName());
                }

                if (!player.capabilities.isCreativeMode) {
                    MiscUtils.dropStack(world, x, y, z, stack);
                }
            }
        }

        return world.setBlockToAir(x, y, z);
    }

    public int quantityDropped(Random rand) {
        return 0;
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityAttuned();
    }

    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
