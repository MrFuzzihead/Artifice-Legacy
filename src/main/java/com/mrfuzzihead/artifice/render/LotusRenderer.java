package com.mrfuzzihead.artifice.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.init.Blocks;
import net.minecraft.world.IBlockAccess;

import com.mrfuzzihead.artifice.ArtificeConfig;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class LotusRenderer implements ISimpleBlockRenderingHandler {

    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {}

    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
        RenderBlocks renderer) {
        renderer.renderBlockLilyPad(Blocks.waterlily, x, y, z);
        renderer.renderCrossedSquares(block, x, y, z);
        return true;
    }

    public boolean shouldRender3DInInventory(int modelId) {
        return false;
    }

    public int getRenderId() {
        return ArtificeConfig.lotusRenderID;
    }
}
