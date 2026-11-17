package shukaro.artifice.block.frame;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Locale;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.render.TextureHandler;
import shukaro.artifice.util.BlockCoord;

public class BlockFrameScaffold extends BlockFrame {
   private static final ForgeDirection[] sides = new ForgeDirection[]{
      ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST, ForgeDirection.DOWN
   };
   private IIcon[] icons = new IIcon[ArtificeConfig.tiers.length];
   private IIcon[] sideIcons = new IIcon[ArtificeConfig.tiers.length];

   public BlockFrameScaffold() {
      this.setBlockName("artifice.scaffold");
      this.setBlockBounds(0.01F, 0.01F, 0.01F, 0.99F, 0.99F, 0.99F);
      GameRegistry.registerBlock(this, ItemBlockFrame.class, this.getUnlocalizedName());
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerBlockIcons(IIconRegister reg) {
      TextureHandler.registerConnectedTexture(reg, this, 0, 0, "basic", "scaffold");
      TextureHandler.registerConnectedTexture(reg, this, 0, 1, "basic", "scaffold");
      this.icons[0] = TextureHandler.getConnectedTexture(this, 0, 1).icon;
      TextureHandler.registerConnectedTexture(reg, this, 1, 0, "reinforced", "scaffold");
      TextureHandler.registerConnectedTexture(reg, this, 1, 1, "reinforced", "scaffold");
      this.icons[1] = TextureHandler.getConnectedTexture(this, 1, 1).icon;
      TextureHandler.registerConnectedTexture(reg, this, 2, 0, "industrial", "scaffold");
      TextureHandler.registerConnectedTexture(reg, this, 2, 1, "industrial", "scaffold");
      this.icons[2] = TextureHandler.getConnectedTexture(this, 2, 1).icon;
      TextureHandler.registerConnectedTexture(reg, this, 3, 0, "advanced", "scaffold");
      TextureHandler.registerConnectedTexture(reg, this, 3, 1, "advanced", "scaffold");
      this.icons[3] = TextureHandler.getConnectedTexture(this, 3, 1).icon;

      for (int i = 0; i < ArtificeConfig.tiers.length; i++) {
         this.sideIcons[i] = TextureHandler.registerIcon(reg, ArtificeConfig.tiers[i].toLowerCase(Locale.ENGLISH), "scaffold/sides");
      }
   }

   @SideOnly(Side.CLIENT)
   @Override
   public IIcon getIcon(int side, int meta) {
      if (meta >= ArtificeConfig.tiers.length) {
         meta = 0;
      }

      return side != 0 && side != 1 ? this.sideIcons[meta] : this.icons[meta];
   }

   @SideOnly(Side.CLIENT)
   @Override
   public IIcon getIcon(IBlockAccess access, int x, int y, int z, int side) {
      int meta = access.getBlockMetadata(x, y, z);
      if (meta > ArtificeConfig.tiers.length) {
         meta = 0;
      }

      return side != 0 && side != 1 ? this.sideIcons[access.getBlockMetadata(x, y, z)] : this.getIcon(side, meta);
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockAccess par1IBlockAccess, int par2, int par3, int par4, int par5) {
      return true;
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
      return side.ordinal() < 2;
   }

   public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
      float shrinkAmount = 0.022222223F;
      if (!(entity.boundingBox.minY >= y + (1.0F - shrinkAmount)) && !(entity.boundingBox.maxY <= y + shrinkAmount)) {
         entity.fallDistance = 0.0F;
         if (entity.isCollidedHorizontally) {
            entity.motionY = 0.2;
         } else if (entity.isSneaking()) {
            double diff = entity.prevPosY - entity.posY;
            entity.boundingBox.minY += diff;
            entity.boundingBox.maxY += diff;
            entity.posY = entity.prevPosY;
         } else {
            entity.motionY = -0.12;
         }
      }
   }

   public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
      float shrinkAmount = 0.125F;
      return AxisAlignedBB.getBoundingBox(
         x + this.minX + shrinkAmount,
         y + this.minY,
         z + this.minZ + shrinkAmount,
         x + this.maxX - shrinkAmount,
         y + this.maxY,
         z + this.maxZ - shrinkAmount
      );
   }

   public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
      ItemStack held = player.inventory.mainInventory[player.inventory.currentItem];
      if (held != null && held.getItem() != null && Block.getBlockFromItem(held.getItem()) != null) {
         Block hb = Block.getBlockFromItem(held.getItem());
         if (held != null && hb != null && hb.equals(this) && held.getItemDamage() == world.getBlockMetadata(x, y, z)) {
            while (world.getBlock(x, y, z).equals(this)) {
               y++;
            }

            if (this.checkStay(world, x, y, z, held.getItemDamage()) && world.isAirBlock(x, y, z)) {
               world.setBlock(x, y, z, this, held.getItemDamage(), 3);
               if (!player.capabilities.isCreativeMode) {
                  held.stackSize--;
                  if (held.stackSize <= 0) {
                     player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                  }
               }
            }
         }
      }
   }

   public boolean canReplace(World world, int x, int y, int z, int side, ItemStack stack) {
      return this.checkStay(world, x, y, z, stack.getItemDamage());
   }

   public int getOverhang(int meta) {
      switch (meta) {
         case 0:
            return 4;
         case 1:
            return 8;
         case 2:
            return 12;
         case 3:
            return 16;
         default:
            return 0;
      }
   }

   public boolean canBlockStay(World world, int x, int y, int z) {
      return this.checkStay(world, x, y, z, world.getBlockMetadata(x, y, z));
   }

   private boolean checkStay(World world, int x, int y, int z, int meta) {
      BlockCoord c = new BlockCoord(x, y, z);
      if (this.isRooted(world, x, y, z, meta)) {
         return true;
      } else {
         for (ForgeDirection d : sides) {
            BlockCoord t = c.copy().offset(d.ordinal());
            if (t.getBlock(world) instanceof BlockFrameScaffold) {
               if (t.blockEquals(world, this, meta)) {
                  if (this.isRooted(world, t.x, t.y, t.z, meta)) {
                     return true;
                  }

                  for (BlockCoord match : c.getRadiusMatches(world, this.getOverhang(meta), this, meta)) {
                     if (this.isRooted(world, match.x, match.y, match.z, meta)
                        && c.isConnected(world, match, this, meta)
                        && c.getDistance(match) <= this.getOverhang(meta)) {
                        return true;
                     }
                  }
               }

               return false;
            }
         }

         return false;
      }
   }

   private boolean isRooted(World world, int x, int y, int z, int meta) {
      for (int i = y - 1; i > 0; i--) {
         if (!world.isSideSolid(x, i, z, ForgeDirection.UP)) {
            return false;
         }

         if (!world.getBlock(x, i, z).equals(this)) {
            return true;
         }

         if (world.getBlockMetadata(x, i, z) != meta) {
            return false;
         }
      }

      return false;
   }

   public void onNeighborBlockChange(World world, int x, int y, int z, Block neighbor) {
      if (!this.canBlockStay(world, x, y, z)) {
         this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
         world.setBlockToAir(x, y, z);
      }
   }
}
