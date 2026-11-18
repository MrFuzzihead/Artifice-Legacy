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

    /**
     * Hardness per tier, in the same order as {@link ArtificeConfig#tiers}: Basic, Reinforced, Industrial, Advanced.
     * It replaces `meta + 5`, which produced a plausible number for a valid tier and an arbitrary one for anything
     * else - metadata 7, which a corrupt chunk or a careless setBlock can produce, came out as hardness 12
     * (CODE_REVIEW §9.3).
     */
    private static final float[] TIER_HARDNESS = { 5.0F, 6.0F, 7.0F, 8.0F };

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
        return hardnessFor(world.getBlockMetadata(x, y, z));
    }

    /** The table lookup, clamped to the first tier for metadata that is not a tier at all (§5.12 does the same). */
    public static float hardnessFor(int meta) {
        return meta >= 0 && meta < TIER_HARDNESS.length ? TIER_HARDNESS[meta] : TIER_HARDNESS[0];
    }

    public boolean renderAsNormalBlock() {
        return false;
    }
}
