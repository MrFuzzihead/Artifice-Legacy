package shukaro.artifice.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.block.world.BlockOre;
import shukaro.artifice.net.ClientProxy;

public class OreRenderer implements ISimpleBlockRenderingHandler {
   public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
      Tessellator tessellator = Tessellator.instance;
      boolean color = false;
      if (block instanceof BlockOre) {
         this.renderInventoryBlock(ArtificeBlocks.blockDummy, metadata, this.getRenderId(), renderer);
      } else if (block instanceof BlockOre.BlockOreDummy) {
         color = true;
      }

      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      tessellator.startDrawingQuads();
      tessellator.setNormal(0.0F, -1.0F, 0.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceYNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 0, metadata));
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setNormal(0.0F, 1.0F, 0.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceYPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 1, metadata));
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setNormal(0.0F, 0.0F, -1.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceZNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 2, metadata));
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setNormal(0.0F, 0.0F, 1.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceZPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 3, metadata));
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setNormal(-1.0F, 0.0F, 0.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceXNeg(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 4, metadata));
      tessellator.draw();
      tessellator.startDrawingQuads();
      tessellator.setNormal(1.0F, 0.0F, 0.0F);
      if (color) {
         tessellator.setColorOpaque_I(block.getRenderColor(metadata));
      }

      renderer.renderFaceXPos(block, 0.0, 0.0, 0.0, renderer.getBlockIconFromSideAndMetadata(block, 5, metadata));
      tessellator.draw();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer) {
      return ClientProxy.renderPass == 0 ? renderer.renderStandardBlock(ArtificeBlocks.blockDummy, x, y, z) : renderer.renderStandardBlock(block, x, y, z);
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ArtificeConfig.oreRenderID;
   }
}
