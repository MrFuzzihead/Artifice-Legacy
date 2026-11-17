package shukaro.artifice.block.world;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.render.TextureHandler;

public class BlockLotus extends BlockLilyPad {
   private static IIcon lotus;
   private static IIcon lotusClosed;

   public BlockLotus() {
      this.setCreativeTab(ArtificeCore.worldTab);
      this.setBlockName("artifice.flora.lily");
      this.setStepSound(soundTypeGrass);
      GameRegistry.registerBlock(this, ItemBlockLotus.class, this.getUnlocalizedName());
   }

   public int getRenderType() {
      return ArtificeConfig.lotusRenderID;
   }

   public void updateTick(World world, int x, int y, int z, Random random) {
      int meta = world.getBlockMetadata(x, y, z);
      if (meta == 0 && world.getBlockLightValue(x, y, z) < 8) {
         world.setBlockMetadataWithNotify(x, y, z, 1, 2);
      }

      if (meta == 1 && world.getBlockLightValue(x, y, z) >= 8) {
         world.setBlockMetadataWithNotify(x, y, z, 0, 2);
      }
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIcon(int side, int meta) {
      return meta == 0 ? lotus : lotusClosed;
   }

   @SideOnly(Side.CLIENT)
   public void registerBlockIcons(IIconRegister reg) {
      lotus = TextureHandler.registerIcon(reg, "waterlotus", "flora");
      lotusClosed = TextureHandler.registerIcon(reg, "waterlotusclosed", "flora");
   }

   public int damageDropped(int meta) {
      return meta == 1 ? 0 : meta;
   }

   public int getDamageValue(World world, int x, int y, int z) {
      return world.getBlockMetadata(x, y, z) == 1 ? 0 : world.getBlockMetadata(x, y, z);
   }

   @SideOnly(Side.CLIENT)
   public int getBlockColor() {
      return 16777215;
   }

   @SideOnly(Side.CLIENT)
   public int getRenderColor(int par1) {
      return 16777215;
   }

   @SideOnly(Side.CLIENT)
   public int colorMultiplier(IBlockAccess par1IBlockAccess, int par2, int par3, int par4) {
      return 16777215;
   }
}
