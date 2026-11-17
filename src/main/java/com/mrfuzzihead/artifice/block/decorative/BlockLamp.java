package shukaro.artifice.block.decorative;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.block.BlockArtifice;
import shukaro.artifice.block.ItemBlockArtifice;
import shukaro.artifice.render.TextureHandler;
import shukaro.artifice.util.MinecraftColors;

public class BlockLamp extends BlockArtifice {
   MinecraftColors color;
   private boolean inverted;
   private IIcon icon;

   public BlockLamp(MinecraftColors color, boolean inverted) {
      super(Material.glass);
      this.color = color;
      this.inverted = inverted;
      this.setHardness(0.3F);
      this.setStepSound(soundTypeGlass);
      if (inverted) {
         this.setBlockName("artifice.lampinverted." + color.name().toLowerCase(Locale.ENGLISH));
      } else {
         this.setBlockName("artifice.lamp." + color.name().toLowerCase(Locale.ENGLISH));
      }

      GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
   }

   public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int par6, float par7, float par8, float par9) {
      if (!world.isRemote) {
         ItemStack held = player.getHeldItem();
         if (held == null && player.isSneaking()) {
            if (this.inverted) {
               return world.setBlock(x, y, z, ArtificeBlocks.blockLamps[this.getLampID(world, x, y, z)], 0, 3);
            }

            return world.setBlock(x, y, z, ArtificeBlocks.blockLampsInverted[this.getLampID(world, x, y, z)], 0, 3);
         }
      }

      return false;
   }

   private int getLampID(World world, int x, int y, int z) {
      if (!world.isAirBlock(x, y, z) && world.getBlock(x, y, z) instanceof BlockLamp) {
         MinecraftColors c = ((BlockLamp)world.getBlock(x, y, z)).color;

         for (int i = 0; i < 16; i++) {
            if (c.equals(MinecraftColors.values()[i])) {
               return i;
            }
         }
      }

      return -1;
   }

   @Override
   public void registerBlockIcons(IIconRegister reg) {
      for (int i = 0; i < 16; i++) {
         if (this.inverted) {
            TextureHandler.registerConnectedTexture(reg, this, i, "lampinverted", "lamp");
         } else {
            TextureHandler.registerConnectedTexture(reg, this, i, "lamp", "lamp");
         }
      }

      this.icon = TextureHandler.getConnectedTexture(this, 0, 0).icon;
   }

   @Override
   public IIcon getIcon(int side, int meta) {
      return this.icon;
   }

   @Override
   public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
      int meta = block.getBlockMetadata(x, y, z);
      if (meta > 15) {
         meta = 0;
      }

      return this.getIcon(side, meta);
   }

   public void updateTick(World world, int x, int y, int z, Random rand) {
      if (world.isBlockIndirectlyGettingPowered(x, y, z) || world.getBlockPowerInput(x, y, z) != 0) {
         int power = world.getStrongestIndirectPower(x, y, z) > world.getBlockPowerInput(x, y, z) ? world.getStrongestIndirectPower(x, y, z) : world.getBlockPowerInput(x, y, z);
         if (world.getBlockMetadata(x, y, z) != power) {
            world.setBlockMetadataWithNotify(x, y, z, power, 3);
         }
      } else if (world.getBlockMetadata(x, y, z) != 0) {
         world.setBlockMetadataWithNotify(x, y, z, 0, 3);
      }
   }

   public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
      if (!world.isRemote) {
         world.scheduleBlockUpdate(x, y, z, this, 2);
      }
   }

   @Override
   public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
      list.add(new ItemStack(item, 1, 0));
   }

   @Override
   public int damageDropped(int meta) {
      return 0;
   }

   @Override
   public int getDamageValue(World world, int x, int y, int z) {
      return 0;
   }

   @SideOnly(Side.CLIENT)
   public int getRenderColor(int meta) {
      return this.inverted ? this.color.toAdjusted(5) : this.color.toAdjusted(-40);
   }

   @SideOnly(Side.CLIENT)
   public int colorMultiplier(IBlockAccess block, int x, int y, int z) {
      return this.inverted ? this.color.toAdjusted(5 - block.getBlockMetadata(x, y, z)) : this.color.toAdjusted(block.getBlockMetadata(x, y, z) - 40);
   }

   public int getLightValue(IBlockAccess world, int x, int y, int z) {
      return this.inverted ? 15 - world.getBlockMetadata(x, y, z) : world.getBlockMetadata(x, y, z);
   }

   public boolean renderAsNormalBlock() {
      return false;
   }

   public int getRenderType() {
      return ArtificeConfig.ctmRenderID;
   }
}
