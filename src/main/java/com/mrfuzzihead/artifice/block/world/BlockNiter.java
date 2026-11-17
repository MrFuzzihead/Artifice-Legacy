package com.mrfuzzihead.artifice.block.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.render.TextureHandler;

import cpw.mods.fml.common.registry.GameRegistry;

public class BlockNiter extends BlockFalling {

    private IIcon[] icons = new IIcon[2];
    private Random rand = new Random();

    public BlockNiter() {
        super(Material.sand);
        this.setStepSound(Block.soundTypeSand);
        this.setBlockName("artifice.niter");
        this.setCreativeTab(ArtificeCore.worldTab);
        this.setHardness(0.5F);
        GameRegistry.registerBlock(this, ItemBlockNiter.class, this.getUnlocalizedName());
    }

    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = TextureHandler.registerIcon(reg, "oreNiter", "ores/niter");
        this.icons[1] = TextureHandler.registerIcon(reg, "blockNiter", "ores/niter");
    }

    public IIcon getIcon(int side, int meta) {
        if (meta > 1) {
            meta = 0;
        }

        return this.icons[meta];
    }

    public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
        list.add(new ItemStack(i, 1, 0));
        list.add(new ItemStack(i, 1, 1));
    }

    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = new ArrayList<>();
        int meta = world.getBlockMetadata(x, y, z);
        if (meta == 0) {
            drops.add(new ItemStack(Blocks.sand));
            drops.add(new ItemStack(ArtificeItems.itemResource, this.quantityDropped(meta, fortune, world.rand), 1));
        } else {
            drops.add(new ItemStack(ArtificeBlocks.blockNiter, 1, 1));
        }

        return drops;
    }

    public int quantityDropped(int meta, int fortune, Random rand) {
        return meta == 0 ? 1 + rand.nextInt(3) + rand.nextInt(fortune + 1) : 1;
    }

    public Item getItemDropped(int meta, Random rand, int fortune) {
        return (Item) (meta == 0 ? ArtificeItems.itemResource : Item.getItemFromBlock(this));
    }

    public int getExpDrop(IBlockAccess world, int meta, int fortune) {
        return meta == 0 && this.getItemDropped(meta, this.rand, fortune) != Item.getItemFromBlock(this)
            ? 1 + this.rand.nextInt(2)
            : 0;
    }

    public int getDamageValue(World world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z);
    }

    public int damageDropped(int meta) {
        return 1;
    }
}
