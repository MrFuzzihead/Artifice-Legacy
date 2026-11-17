package com.mrfuzzihead.artifice.block.frame;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.block.BlockArtifice;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public abstract class BlockFrame extends BlockArtifice {

    protected List<String> validTiers = new ArrayList<>();

    public BlockFrame() {
        super(Material.rock);
        this.validTiers.add(ArtificeConfig.tiers[0]);
        this.validTiers.add(ArtificeConfig.tiers[1]);
        this.validTiers.add(ArtificeConfig.tiers[2]);
        this.validTiers.add(ArtificeConfig.tiers[3]);
    }

    public boolean isOpaqueCube() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
        for (int j = 0; j < ArtificeConfig.tiers.length; j++) {
            if (this.validTiers.contains(ArtificeConfig.tiers[j])) {
                list.add(new ItemStack(i, 1, j));
            }
        }
    }

    public float getBlockHardness(World world, int x, int y, int z) {
        int meta = world.getBlockMetadata(x, y, z);
        return meta + 5.0F;
    }

    public boolean renderAsNormalBlock() {
        return false;
    }
}
