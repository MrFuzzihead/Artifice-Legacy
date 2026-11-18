package com.mrfuzzihead.artifice.block.functional;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.tile.TileEntityNuclearBattery;
import com.mrfuzzihead.artifice.util.ArrayUtils;
import com.mrfuzzihead.artifice.util.FormatCodes;
import com.mrfuzzihead.artifice.util.MiscUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockNuclearBattery extends Block implements ITileEntityProvider {

    private IIcon[] icons = new IIcon[4];

    public BlockNuclearBattery() {
        super(Material.iron);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setBlockName(Names.NUCLEAR_BATTERY);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = TextureHandler.registerIcon(reg, "full", "nuclearbattery");
        this.icons[1] = TextureHandler.registerIcon(reg, "partial", "nuclearbattery");
        this.icons[2] = TextureHandler.registerIcon(reg, "low", "nuclearbattery");
        this.icons[3] = TextureHandler.registerIcon(reg, "empty", "nuclearbattery");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.icons[ArrayUtils.clampIndex(meta, this.icons.length)];
    }

    public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
        ItemStack stack = new ItemStack(item, 1, 0);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("charge", ArtificeConfig.nuclearBatteryCapacity);
        stack.setTagCompound(tag);
        list.add(stack);
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote && player.getHeldItem() == null) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityNuclearBattery) {
                TileEntityNuclearBattery teb = (TileEntityNuclearBattery) te;
                MiscUtils.addChatMessage(
                    player,
                    FormatCodes.Yellow.code + teb.getEnergyStored(ForgeDirection.DOWN)
                        + FormatCodes.Reset.code
                        + " "
                        + StatCollector.translateToLocal("chat.artifice.rf")
                        + " @ "
                        + FormatCodes.Aqua.code
                        + teb.getMaxRate()
                        + FormatCodes.Reset.code
                        + " "
                        + StatCollector.translateToLocal("chat.artifice.rft"));
                return true;
            }
        }

        return false;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return (3 - meta) * 2;
    }

    public int damageDropped(int meta) {
        return meta;
    }

    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack) {
        super.onBlockPlacedBy(world, x, y, z, living, stack);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityNuclearBattery) {
            TileEntityNuclearBattery teb = (TileEntityNuclearBattery) te;
            if (stack.hasTagCompound()) {
                teb.setEnergy(
                    stack.getTagCompound()
                        .getInteger("charge"));
            } else {
                teb.setEnergy(ArtificeConfig.nuclearBatteryCapacity);
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
            if (te instanceof TileEntityNuclearBattery) {
                ItemStack stack = new ItemStack(world.getBlock(x, y, z), 1, 0);
                TileEntityNuclearBattery teb = (TileEntityNuclearBattery) te;
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("charge", teb.getEnergyStored(ForgeDirection.DOWN));
                stack.setTagCompound(tag);
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

    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityNuclearBattery();
    }

    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
