package com.mrfuzzihead.artifice.block.decorative;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSlab;
import net.minecraft.item.ItemStack;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeRegistry;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemBlockSlabArtifice extends ItemSlab {

    public ItemBlockSlabArtifice(Block block) {
        super(block, getHalfSlab(block), getDoubleSlab(block), block.isOpaqueCube());
    }

    private static BlockSlab getHalfSlab(Block block) {
        if (block instanceof BlockRockSlab && block.getUnlocalizedName()
            .contains("basalt")) {
            return ArtificeBlocks.blockBasaltSlab;
        } else if (block instanceof BlockRockSlab && block.getUnlocalizedName()
            .contains("marble")) {
                return ArtificeBlocks.blockMarbleSlab;
            } else if (block.getUnlocalizedName()
                .contains("lightgray")) {
                    return ArtificeBlocks.blockLimestoneSlabs[1];
                } else if (block.getUnlocalizedName()
                    .contains("gray")) {
                        return ArtificeBlocks.blockLimestoneSlabs[0];
                    } else if (block.getUnlocalizedName()
                        .contains("brown")) {
                            return ArtificeBlocks.blockLimestoneSlabs[2];
                        } else if (block.getUnlocalizedName()
                            .contains("tan")) {
                                return ArtificeBlocks.blockLimestoneSlabs[3];
                            } else if (block.getUnlocalizedName()
                                .contains("reddish")) {
                                    return ArtificeBlocks.blockLimestoneSlabs[4];
                                } else {
                                    return block.getUnlocalizedName()
                                        .contains("bluish") ? ArtificeBlocks.blockLimestoneSlabs[5]
                                            : ArtificeBlocks.blockLimestoneSlabs[6];
                                }
    }

    private static BlockSlab getDoubleSlab(Block block) {
        if (block instanceof BlockRockSlab && block.getUnlocalizedName()
            .contains("basalt")) {
            return ArtificeBlocks.blockBasaltDoubleSlab;
        } else if (block instanceof BlockRockSlab && block.getUnlocalizedName()
            .contains("marble")) {
                return ArtificeBlocks.blockMarbleDoubleSlab;
            } else if (block.getUnlocalizedName()
                .contains("lightgray")) {
                    return ArtificeBlocks.blockLimestoneDoubleSlabs[1];
                } else if (block.getUnlocalizedName()
                    .contains("gray")) {
                        return ArtificeBlocks.blockLimestoneDoubleSlabs[0];
                    } else if (block.getUnlocalizedName()
                        .contains("brown")) {
                            return ArtificeBlocks.blockLimestoneDoubleSlabs[2];
                        } else if (block.getUnlocalizedName()
                            .contains("tan")) {
                                return ArtificeBlocks.blockLimestoneDoubleSlabs[3];
                            } else if (block.getUnlocalizedName()
                                .contains("reddish")) {
                                    return ArtificeBlocks.blockLimestoneDoubleSlabs[4];
                                } else {
                                    return block.getUnlocalizedName()
                                        .contains("bluish") ? ArtificeBlocks.blockLimestoneDoubleSlabs[5]
                                            : ArtificeBlocks.blockLimestoneDoubleSlabs[6];
                                }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List infoList, boolean advancedTooltips) {
        if (ArtificeConfig.tooltips) {
            ArtificeRegistry.appendTooltips(stack, infoList);
        }
    }
}
