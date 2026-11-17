package shukaro.artifice.block.world;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.block.BlockFlower;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.render.TextureHandler;

public class BlockFlora extends BlockFlower {
   public static IIcon[] icons = new IIcon[ArtificeConfig.flora.length];

   public BlockFlora() {
      super(0);
      this.setTickRandomly(true);
      this.setCreativeTab(ArtificeCore.worldTab);
      this.setHardness(0.0F);
      this.setBlockName("artifice.flora");
      GameRegistry.registerBlock(this, ItemBlockFlora.class, this.getUnlocalizedName());
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIcon(int side, int meta) {
      return meta > ArtificeConfig.flora.length ? icons[0] : icons[meta];
   }

   @SideOnly(Side.CLIENT)
   public void registerBlockIcons(IIconRegister reg) {
      for (int i = 0; i < ArtificeConfig.flora.length; i++) {
         icons[i] = TextureHandler.registerIcon(reg, ArtificeConfig.flora[i].toLowerCase(Locale.ENGLISH), "flora");
      }
   }

   public void updateTick(World world, int x, int y, int z, Random random) {
      int meta = world.getBlockMetadata(x, y, z);
      if (meta == 3 && world.getBlockLightValue(x, y, z) < 8) {
         world.setBlockMetadataWithNotify(x, y, z, 4, 2);
      }

      if (meta == 4 && world.getBlockLightValue(x, y, z) >= 8) {
         world.setBlockMetadataWithNotify(x, y, z, 3, 2);
      }
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
      for (int j = 0; j < ArtificeConfig.flora.length - 1; j++) {
         list.add(new ItemStack(i, 1, j));
      }
   }

   public int damageDropped(int meta) {
      return meta == 4 ? 3 : meta;
   }

   public int getDamageValue(World world, int x, int y, int z) {
      return world.getBlockMetadata(x, y, z) == 4 ? 3 : world.getBlockMetadata(x, y, z);
   }
}
