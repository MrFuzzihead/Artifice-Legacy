package com.mrfuzzihead.artifice.block.world;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.BlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockRock extends BlockArtifice {

    private IIcon[] icons = new IIcon[ArtificeConfig.rocks.length];
    private String name;
    private int color = 16777215;

    public BlockRock(String name) {
        super(Material.rock);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setBlockName("artifice." + name);
        this.setCreativeTab(ArtificeCore.worldTab);
        this.name = name;
        GameRegistry.registerBlock(this, ItemBlockRock.class, this.getUnlocalizedName());
    }

    public BlockRock(String name, int color) {
        super(Material.rock);
        this.setHardness(1.5F);
        this.setResistance(10.0F);
        this.setBlockName("artifice." + name);
        this.setCreativeTab(ArtificeCore.worldTab);
        this.name = name.split("[.]")[0];
        this.color = color;
        GameRegistry.registerBlock(this, ItemBlockRock.class, this.getUnlocalizedName());
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7,
        float par8, float par9) {
        if (!world.isRemote) {
            ItemStack held = player.getHeldItem();
            if (held == null && player.isSneaking()) {
                if (world.getBlockMetadata(x, y, z) == 3) {
                    return world.setBlock(x, y, z, this, 4, 3);
                }

                if (world.getBlockMetadata(x, y, z) == 4) {
                    return world.setBlock(x, y, z, this, 3, 3);
                }
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = TextureHandler.registerIcon(reg, this.name, this.name);
        this.icons[1] = TextureHandler.registerIcon(reg, "cobblestone", this.name);
        this.icons[2] = TextureHandler.registerIcon(reg, "bricks", this.name);
        TextureHandler.registerConnectedTexture(reg, this, 3, "paver", this.name);
        this.icons[3] = TextureHandler.getConnectedTexture(this, 3, 0).icon;
        TextureHandler.registerConnectedTexture(reg, this, 4, "antipaver", this.name);
        this.icons[4] = TextureHandler.getConnectedTexture(this, 4, 0).icon;
        this.icons[5] = TextureHandler.registerIcon(reg, "chiseled", this.name);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(int side, int meta) {
        if (meta >= ArtificeConfig.rocks.length) {
            meta = 0;
        }

        return this.icons[meta];
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        int meta = block.getBlockMetadata(x, y, z);
        return this.getIcon(side, meta);
    }

    @Override
    public int damageDropped(int meta) {
        return meta == 0 ? 1 : meta;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
        for (int j = 0; j < ArtificeConfig.rocks.length; j++) {
            list.add(new ItemStack(i, 1, j));
        }
    }

    public boolean isOpaqueCube() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public int getRenderColor(int meta) {
        return this.color;
    }

    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess block, int x, int y, int z) {
        return this.color;
    }

    public boolean renderAsNormalBlock() {
        return true;
    }

    public int getRenderType() {
        return ArtificeConfig.ctmRenderID;
    }

    public boolean isReplaceableOreGen(World world, int x, int y, int z, Block target) {
        return target == this || target == Blocks.stone;
    }
}
