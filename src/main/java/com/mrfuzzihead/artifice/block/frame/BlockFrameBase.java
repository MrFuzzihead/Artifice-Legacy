package shukaro.artifice.block.frame;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Locale;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.render.TextureHandler;

public class BlockFrameBase extends BlockFrame {
   private IIcon[] icons = new IIcon[ArtificeConfig.tiers.length];

   public BlockFrameBase() {
      this.setBlockName("artifice.frame");
      this.setBlockBounds(0.01F, 0.01F, 0.01F, 0.99F, 0.99F, 0.99F);
      GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerBlockIcons(IIconRegister reg) {
      for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
         this.icons[i] = TextureHandler.registerIcon(reg, ArtificeConfig.tiers[i].toLowerCase(Locale.ENGLISH), "frame");
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public IIcon getIcon(int side, int meta) {
      return this.icons[meta];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
      return this.icons[block.getBlockMetadata(x, y, z)];
   }

   public int getRenderType() {
      return ArtificeConfig.frameRenderID;
   }

   @Override
   public boolean isOpaqueCube() {
      return false;
   }

   @Override
   public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
      return false;
   }
}
