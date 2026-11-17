package shukaro.artifice.block.world;

import cpw.mods.fml.common.registry.GameRegistry;
import java.util.List;
import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.ArtificeItems;
import shukaro.artifice.block.BlockArtifice;
import shukaro.artifice.render.TextureHandler;

public class BlockSulfur extends BlockArtifice {
   private IIcon[] icons = new IIcon[2];
   private Random rand = new Random();

   public BlockSulfur() {
      super(Material.rock);
      this.setHardness(3.0F);
      this.setResistance(5.0F);
      this.setCreativeTab(ArtificeCore.worldTab);
      this.setBlockName("artifice.sulfur");
      this.setHarvestLevel("pickaxe", 0, 0);
      GameRegistry.registerBlock(this, ItemBlockSulfur.class, this.getUnlocalizedName());
   }

   public int quantityDropped(int meta, int fortune, Random rand) {
      return meta == 0 ? 1 + rand.nextInt(3) + rand.nextInt(fortune + 1) : 1;
   }

   public Item getItemDropped(int meta, Random rand, int fortune) {
      return (Item)(meta == 0 ? ArtificeItems.itemResource : Item.getItemFromBlock(this));
   }

   public int getExpDrop(IBlockAccess world, int meta, int fortune) {
      return meta == 0 && this.getItemDropped(meta, this.rand, fortune) != Item.getItemFromBlock(this) ? 1 + this.rand.nextInt(5) : 0;
   }

   public ItemStack createStackedBlock(int p_149644_1_) {
      return new ItemStack(ArtificeBlocks.blockSulfur);
   }

   @Override
   public void registerBlockIcons(IIconRegister reg) {
      this.icons[0] = TextureHandler.registerIcon(reg, "oreSulfur", "ores/sulfur");
      this.icons[1] = TextureHandler.registerIcon(reg, "blockSulfur", "ores/sulfur");
   }

   @Override
   public IIcon getIcon(int side, int meta) {
      return meta > 1 ? this.icons[0] : this.icons[meta];
   }

   @Override
   public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
      int meta = block.getBlockMetadata(x, y, z);
      return meta > 1 ? this.icons[0] : this.icons[meta];
   }

   @Override
   public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
      list.add(new ItemStack(i, 1, 0));
      list.add(new ItemStack(i, 1, 1));
   }
}
