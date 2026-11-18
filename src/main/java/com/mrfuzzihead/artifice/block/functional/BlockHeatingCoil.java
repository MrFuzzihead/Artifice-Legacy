package com.mrfuzzihead.artifice.block.functional;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.tile.TileEntityHeatingCoil;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockHeatingCoil extends Block implements ITileEntityProvider {

    private IIcon powered;
    private IIcon unpowered;

    public BlockHeatingCoil() {
        super(Material.iron);
        this.setCreativeTab(ArtificeCore.mainTab);
        this.setHardness(5.0F);
        this.setResistance(10.0F);
        this.setBlockName(Names.HEATING_COIL);
        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        TextureHandler.registerConnectedTexture(reg, this, 0, "unpowered", "coil");
        TextureHandler.registerConnectedTexture(reg, this, 1, "powered", "coil");
        this.unpowered = TextureHandler.getConnectedTexture(this, 0, 0).icon;
        this.powered = TextureHandler.getConnectedTexture(this, 1, 0).icon;
    }

    public IIcon getIcon(int side, int meta) {
        return meta == 0 ? this.unpowered : this.powered;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }

    public int getRenderType() {
        return ArtificeConfig.ctmRenderID;
    }

    public int getLightValue(IBlockAccess world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta == 0 ? 0 : 7;
    }

    public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
        return true;
    }

    public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
        return new TileEntityHeatingCoil();
    }

    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
