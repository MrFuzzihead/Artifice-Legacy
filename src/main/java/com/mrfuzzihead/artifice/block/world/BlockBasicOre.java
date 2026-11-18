package com.mrfuzzihead.artifice.block.world;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;

public class BlockBasicOre extends Block {

    private IIcon[] icons = new IIcon[2];
    private String name;

    public BlockBasicOre(String name, int harvestLevel) {
        super(Material.rock);
        this.setCreativeTab(ArtificeCore.worldTab);
        this.name = name;
        this.setBlockName(Names.rock(name));
        this.setHarvestLevel("pickaxe", harvestLevel);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        GameRegistry.registerBlock(this, ItemBlockBasicOre.class, this.getUnlocalizedName());
    }

    public void registerBlockIcons(IIconRegister reg) {
        this.icons[0] = TextureHandler.registerIcon(reg, this.name + "Ore", "ores/" + this.name);
        this.icons[1] = TextureHandler.registerIcon(reg, this.name + "Block", "ores/" + this.name);
    }

    public IIcon getIcon(int side, int meta) {
        return meta == 0 ? this.icons[0] : this.icons[1];
    }

    public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
        list.add(new ItemStack(i, 1, 0));
        list.add(new ItemStack(i, 1, 1));
    }

    public int damageDropped(int meta) {
        return meta;
    }
}
