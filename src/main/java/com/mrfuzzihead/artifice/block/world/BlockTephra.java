package shukaro.artifice.block.world;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.render.TextureHandler;

public class BlockTephra extends BlockFalling {
   private IIcon blackSand;
   private IIcon blackGravel;

   public BlockTephra() {
      this.setBlockName("artifice.tephra");
      this.setCreativeTab(ArtificeCore.worldTab);
      this.setStepSound(Block.soundTypeGravel);
      this.setHardness(0.5F);
      GameRegistry.registerBlock(this, ItemBlockTephra.class, this.getUnlocalizedName());
   }

   @SideOnly(Side.CLIENT)
   public void registerBlockIcons(IIconRegister reg) {
      this.blackSand = TextureHandler.registerIcon(reg, "blackSand", "misc");
      this.blackGravel = TextureHandler.registerIcon(reg, "blackGravel", "misc");
   }

   @SideOnly(Side.CLIENT)
   public IIcon getIcon(int side, int meta) {
      return meta > 0 ? this.blackGravel : this.blackSand;
   }

   public int damageDropped(int meta) {
      return meta;
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
      list.add(new ItemStack(this, 1, 0));
      list.add(new ItemStack(this, 1, 1));
   }
}
