package shukaro.artifice.render.connectedtexture;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import org.lwjgl.opengl.GL11;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.block.world.BlockRock;
import shukaro.artifice.render.TextureHandler;
import shukaro.artifice.util.Drawing;

public class CTMRenderer implements ISimpleBlockRenderingHandler {
   RenderBlocksCTM rendererCTM = new RenderBlocksCTM();

   public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer) {
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      Drawing.drawBlock(block, metadata, renderer);
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
   }

   public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks rendererOld) {
      int meta = world.getBlockMetadata(x, y, z);
      boolean ct = false;

      for (int i = 0; i < 6; i++) {
         if (TextureHandler.getConnectedTexture(block, meta, i) != null) {
            ct = true;
            break;
         }
      }

      if (ct) {
         this.rendererCTM.blockAccess = world;
         this.rendererCTM.rendererOld = rendererOld;
         this.rendererCTM.setRenderBoundsFromBlock(block);
         return this.rendererCTM.renderStandardBlock(block, x, y, z);
      } else {
         int oldBottom = rendererOld.uvRotateBottom;
         int oldEast = rendererOld.uvRotateEast;
         int oldNorth = rendererOld.uvRotateNorth;
         int oldSouth = rendererOld.uvRotateSouth;
         int oldTop = rendererOld.uvRotateTop;
         int oldWest = rendererOld.uvRotateWest;
         if (block instanceof BlockRock && world.getBlockMetadata(x, y, z) == 0) {
            long hash = x * 3129871 ^ z * 116129781L ^ y;
            int degree = (int)(hash & 3L);
            rendererOld.uvRotateBottom = degree;
            rendererOld.uvRotateEast = degree;
            rendererOld.uvRotateNorth = degree;
            rendererOld.uvRotateSouth = degree;
            rendererOld.uvRotateTop = degree;
            rendererOld.uvRotateWest = degree;
         }

         rendererOld.setRenderBoundsFromBlock(block);
         boolean res = rendererOld.renderStandardBlock(block, x, y, z);
         if (block instanceof BlockRock && world.getBlockMetadata(x, y, z) == 0) {
            rendererOld.uvRotateBottom = oldBottom;
            rendererOld.uvRotateEast = oldEast;
            rendererOld.uvRotateNorth = oldNorth;
            rendererOld.uvRotateSouth = oldSouth;
            rendererOld.uvRotateTop = oldTop;
            rendererOld.uvRotateWest = oldWest;
         }

         return res;
      }
   }

   public boolean shouldRender3DInInventory(int modelId) {
      return true;
   }

   public int getRenderId() {
      return ArtificeConfig.ctmRenderID;
   }
}
