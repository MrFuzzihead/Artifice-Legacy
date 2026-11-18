package com.mrfuzzihead.artifice.block.functional;

import java.util.List;
import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.tile.TileEntityLogic;
import com.mrfuzzihead.artifice.util.FormatCodes;
import com.mrfuzzihead.artifice.util.MiscUtils;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockLogicRedstone extends Block implements ITileEntityProvider {

    private IIcon disabled;
    private IIcon output;
    private IIcon positive;
    private IIcon negative;

    public BlockLogicRedstone() {
        super(Material.rock);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setBlockName(Names.LOGIC);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        this.disabled = TextureHandler.registerIcon(reg, "disabled", "logic");
        this.output = TextureHandler.registerIcon(reg, "output", "logic");
        this.positive = TextureHandler.registerIcon(reg, "positive", "logic");
        this.negative = TextureHandler.registerIcon(reg, "negative", "logic");
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return this.disabled;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLogic) {
            TileEntityLogic tel = (TileEntityLogic) te;
            switch (tel.sides[side]) {
                case DISABLED:
                    return this.disabled;
                case OUTPUT:
                    return this.output;
                case POSITIVE:
                    return this.positive;
                case NEGATIVE:
                    return this.negative;
                default:
                    return this.disabled;
            }
        } else {
            return this.disabled;
        }
    }

    public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
        list.add(new ItemStack(item, 1, 0));
        list.add(new ItemStack(item, 1, 1));
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (player.getHeldItem() == null) {
            if (player.isSneaking()) {
                side = ForgeDirection.OPPOSITES[side];
            }

            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityLogic) {
                TileEntityLogic tel = (TileEntityLogic) te;
                int i = tel.sides[side].ordinal() + 1;
                if (i > 3) {
                    i = 0;
                }

                tel.sides[side] = TileEntityLogic.SideState.values()[i];
                if (!world.isRemote) {
                    int meta = world.getBlockMetadata(x, y, z);
                    MiscUtils.addChatMessage(
                        player,
                        FormatCodes.Italic.code
                            + (meta == 0 ? StatCollector.translateToLocal("chat.artifice.digital")
                                : StatCollector.translateToLocal("chat.artifice.analog"))
                            + " "
                            + FormatCodes.Yellow.code
                            + StatCollector.translateToLocal("chat.artifice.side." + side)
                            + " "
                            + FormatCodes.Reset.code
                            + StatCollector.translateToLocal("chat.artifice.set")
                            + " "
                            + FormatCodes.Aqua.code
                            + StatCollector.translateToLocal(
                                "chat.artifice.set." + tel.sides[side].name()
                                    .toLowerCase(Locale.ENGLISH)));
                }

                tel.updateEntity();
                world.markBlockForUpdate(x, y, z);
                return true;
            }
        }

        return false;
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TileEntityLogic) {
            TileEntityLogic tel = (TileEntityLogic) te;
            tel.updateEntity();
        }
    }

    public int isProvidingWeakPower(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (te instanceof TileEntityLogic) {
            TileEntityLogic tel = (TileEntityLogic) te;
            return tel.sides[ForgeDirection.OPPOSITES[side]] == TileEntityLogic.SideState.OUTPUT ? tel.power : 0;
        } else {
            return 0;
        }
    }

    public boolean canConnectRedstone(IBlockAccess blockAccess, int x, int y, int z, int side) {
        TileEntity te = blockAccess.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityLogic)) {
            return false;
        }

        TileEntityLogic tel = (TileEntityLogic) te;

        if (side < 0 || side >= ForgeDirection.VALID_DIRECTIONS.length) {
            // Vanilla passes -1 when the direction is unknown ("do you connect at all?").
            for (TileEntityLogic.SideState state : tel.sides) {
                if (state != TileEntityLogic.SideState.DISABLED) {
                    return true;
                }
            }

            return false;
        }

        // Same mapping as isProvidingWeakPower, so the face that renders a connection is the face that emits
        // (CODE_REVIEW 5.8). The hand-written table this replaces agreed with neither.
        return tel.sides[ForgeDirection.OPPOSITES[side]] != TileEntityLogic.SideState.DISABLED;
    }

    public boolean canProvidePower() {
        return true;
    }

    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityLogic();
    }

    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
