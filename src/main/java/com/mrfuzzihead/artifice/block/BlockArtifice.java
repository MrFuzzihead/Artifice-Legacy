package shukaro.artifice.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import shukaro.artifice.ArtificeCore;

public abstract class BlockArtifice extends Block {
   protected BlockArtifice(Material mat) {
      super(mat);
      this.setCreativeTab(ArtificeCore.mainTab);
   }

   @SideOnly(Side.CLIENT)
   public abstract void registerBlockIcons(IIconRegister var1);

   @SideOnly(Side.CLIENT)
   public abstract IIcon getIcon(int var1, int var2);

   @SideOnly(Side.CLIENT)
   public abstract IIcon getIcon(IBlockAccess var1, int var2, int var3, int var4, int var5);

   @SideOnly(Side.CLIENT)
   public abstract void getSubBlocks(Item var1, CreativeTabs var2, List var3);

   public int damageDropped(int meta) {
      return meta;
   }

   public int getDamageValue(World world, int x, int y, int z) {
      return world.getBlockMetadata(x, y, z);
   }

   public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
      return true;
   }

   public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int metadata) {
      return true;
   }
}
