package shukaro.artifice.block.world;

import cofh.lib.util.helpers.ItemHelper;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import shukaro.artifice.ArtificeBlocks;
import shukaro.artifice.ArtificeConfig;
import shukaro.artifice.ArtificeCore;
import shukaro.artifice.block.BlockArtifice;
import shukaro.artifice.net.ClientProxy;
import shukaro.artifice.render.TextureHandler;
import shukaro.artifice.util.NameMetaPair;

public class BlockOre extends BlockArtifice {
   private IIcon icon;
   private String name;
   private Integer color;
   private boolean glowing;
   private int index;

   public BlockOre(String name, int index) {
      this(name, false, null, index);
   }

   public BlockOre(String name, boolean glowing, Integer color, int index) {
      super(Material.rock);
      this.setHardness(3.0F);
      this.setResistance(5.0F);
      if (glowing) {
         this.setBlockName("artifice." + name.toLowerCase(Locale.ENGLISH) + ".glowing");
      } else {
         this.setBlockName("artifice." + name.toLowerCase(Locale.ENGLISH));
      }

      this.setCreativeTab(ArtificeCore.worldTab);
      this.name = name;
      this.index = index;
      this.glowing = glowing;
      if (color != null) {
         this.color = color;
      }

      this.setHarvestLevel("pickaxe", 2);
      if (name.equals("oreCoal") || name.equals("oreSulfur")) {
         this.setHarvestLevel("pickaxe", 0);
      } else if (name.equals("oreIron") || name.equals("oreLapis") || name.equals("oreCopper") || name.equals("oreTin")) {
         this.setHarvestLevel("pickaxe", 1);
      } else if (name.equals("oreGold") || name.equals("oreDiamond") || name.equals("oreRedstone") || name.equals("oreEmerald")) {
         this.setHarvestLevel("pickaxe", 2);
      }

      if (this.glowing) {
         this.setLightLevel(0.625F);
         this.setTickRandomly(true);
      }

      GameRegistry.registerBlock(this, ItemBlockOre.class, this.getUnlocalizedName());
   }

   public void harvestBlock(World world, EntityPlayer entityplayer, int x, int y, int z, int fortune) {
      super.harvestBlock(world, entityplayer, x, y, z, fortune);
      if (this.name.equals("oreEnder")) {
         if (!world.isRemote && world.rand.nextInt(100) < 20 && ArtificeConfig.spawnEndermen && !EnchantmentHelper.getSilkTouchModifier(entityplayer)) {
            int tries = world.rand.nextInt(20);

            for (int i = 0; i < tries; i++) {
               int spawnX = x + world.rand.nextInt(3) - world.rand.nextInt(3);
               int spawnY = y + world.rand.nextInt(3) - world.rand.nextInt(3);
               int spawnZ = z + world.rand.nextInt(3) - world.rand.nextInt(3);
               if (this.canSpawnEnder(world, spawnX, spawnY, spawnZ)) {
                  EntityEnderman ender = new EntityEnderman(world);
                  ender.setLocationAndAngles(
                     spawnX + world.rand.nextDouble(),
                     spawnY + world.rand.nextDouble(),
                     spawnZ + world.rand.nextDouble(),
                     world.rand.nextFloat(),
                     world.rand.nextFloat()
                  );
                  world.spawnEntityInWorld(ender);
                  ender.spawnExplosionParticle();
                  ender.playSound("mob.endermen.portal", 1.0F, 1.0F);
                  break;
               }
            }
         }
      }
   }

   private boolean canSpawnEnder(World world, int x, int y, int z) {
      return (world.getBlock(x, y, z) == null || world.isAirBlock(x, y, z))
         && (world.getBlock(x, y + 1, z) == null || world.isAirBlock(x, y + 1, z))
         && (world.getBlock(x, y + 2, z) == null || world.isAirBlock(x, y + 2, z));
   }

   public int tickRate(World p_149738_1_) {
      return 30;
   }

   private void activate(World world, int x, int y, int z) {
      if (this.color != null) {
         this.sparkle(world, x, y, z);
         if (!this.glowing) {
            world.setBlock(x, y, z, ArtificeBlocks.blockOresGlowing[this.index], world.getBlockMetadata(x, y, z), 3);
         }
      }
   }

   public void updateTick(World world, int x, int y, int z, Random rand) {
      if (this.glowing) {
         world.setBlock(x, y, z, ArtificeBlocks.blockOres[this.index], world.getBlockMetadata(x, y, z), 3);
      }
   }

   private void sparkle(World world, int x, int y, int z) {
      if (this.index == 5) {
         Random random = world.rand;
         double d0 = 0.0625;

         for (int l = 0; l < 6; l++) {
            double d1 = x + random.nextFloat();
            double d2 = y + random.nextFloat();
            double d3 = z + random.nextFloat();
            if (l == 0 && !world.getBlock(x, y + 1, z).isOpaqueCube()) {
               d2 = y + 1 + d0;
            }

            if (l == 1 && !world.getBlock(x, y - 1, z).isOpaqueCube()) {
               d2 = y + 0 - d0;
            }

            if (l == 2 && !world.getBlock(x, y, z + 1).isOpaqueCube()) {
               d3 = z + 1 + d0;
            }

            if (l == 3 && !world.getBlock(x, y, z - 1).isOpaqueCube()) {
               d3 = z + 0 - d0;
            }

            if (l == 4 && !world.getBlock(x + 1, y, z).isOpaqueCube()) {
               d1 = x + 1 + d0;
            }

            if (l == 5 && !world.getBlock(x - 1, y, z).isOpaqueCube()) {
               d1 = x + 0 - d0;
            }

            if (d1 < x || d1 > x + 1 || d2 < 0.0 || d2 > y + 1 || d3 < z || d3 > z + 1) {
               world.spawnParticle("reddust", d1, d2, d3, 0.0, 0.0, 0.0);
            }
         }
      }

      if (this.index == 13) {
         for (int i = 0; i < 4; i++) {
            double dx = x + 0.5 + world.rand.nextDouble() - world.rand.nextDouble();
            double dy = y + 0.5 + world.rand.nextDouble() - world.rand.nextDouble();
            double dz = z + 0.5 + world.rand.nextDouble() - world.rand.nextDouble();
            world.spawnParticle(
               "portal",
               dx,
               dy,
               dz,
               world.rand.nextDouble() - world.rand.nextDouble(),
               world.rand.nextDouble() - world.rand.nextDouble(),
               world.rand.nextDouble() - world.rand.nextDouble()
            );
         }
      }
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
      if (this.glowing) {
         this.sparkle(world, x, y, z);
      }
   }

   public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
      this.activate(world, x, y, z);
      return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
   }

   public void onEntityWalking(World world, int x, int y, int z, Entity entity) {
      this.activate(world, x, y, z);
      super.onEntityWalking(world, x, y, z, entity);
   }

   public void onBlockClicked(World world, int x, int y, int z, EntityPlayer player) {
      this.activate(world, x, y, z);
      super.onBlockClicked(world, x, y, z, player);
   }

   public int quantityDropped(int meta, int fortune, Random rand) {
      if (this.name.equals("oreCoal")) {
         return Blocks.coal_ore.quantityDropped(0, fortune, rand);
      } else if (this.name.equals("oreLapis")) {
         return Blocks.lapis_ore.quantityDropped(0, fortune, rand);
      } else if (this.name.equals("oreDiamond")) {
         return Blocks.diamond_ore.quantityDropped(0, fortune, rand);
      } else if (this.name.equals("oreRedstone")) {
         return Blocks.redstone_ore.quantityDropped(0, fortune, rand);
      } else if (this.name.equals("oreEmerald")) {
         return Blocks.emerald_ore.quantityDropped(0, fortune, rand);
      } else if (this.name.equals("oreSulfur")) {
         return ArtificeBlocks.blockSulfur.quantityDropped(0, fortune, rand);
      } else {
         return this.name.equals("oreEnder") ? ArtificeBlocks.blockEnderOre.quantityDropped(0, fortune, rand) : 1;
      }
   }

   public Item getItemDropped(int meta, Random rand, int fortune) {
      if (this.name.equals("oreCoal")) {
         return Blocks.coal_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreIron")) {
         return Blocks.iron_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreLapis")) {
         return Blocks.lapis_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreGold")) {
         return Blocks.gold_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreDiamond")) {
         return Blocks.diamond_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreRedstone")) {
         return Blocks.redstone_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreEmerald")) {
         return Blocks.emerald_ore.getItemDropped(0, rand, fortune);
      } else if (this.name.equals("oreSulfur")) {
         return ArtificeBlocks.blockSulfur.getItemDropped(0, rand, fortune);
      } else {
         return this.name.equals("oreEnder") ? ArtificeBlocks.blockEnderOre.getItemDropped(0, rand, fortune) : ItemHelper.getOre(this.name).getItem();
      }
   }

   @Override
   public int damageDropped(int meta) {
      if (this.name.equals("oreLapis")) {
         return Blocks.lapis_ore.damageDropped(meta);
      } else if (this.name.equals("oreCoal")
         || this.name.equals("oreIron")
         || this.name.equals("oreGold")
         || this.name.equals("oreDiamond")
         || this.name.equals("oreRedstone")
         || this.name.equals("oreEmerald")
         || this.name.equals("oreSulfur")) {
         return 0;
      } else {
         return this.name.equals("oreEnder") ? ArtificeBlocks.blockEnderOre.damageDropped(meta) : ItemHelper.getOre(this.name).getItemDamage();
      }
   }

   public int getExpDrop(IBlockAccess world, int meta, int fortune) {
      if (this.name.equals("oreCoal")) {
         return Blocks.coal_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreIron")) {
         return Blocks.iron_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreLapis")) {
         return Blocks.lapis_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreGold")) {
         return Blocks.gold_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreDiamond")) {
         return Blocks.diamond_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreRedstone")) {
         return Blocks.redstone_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreEmerald")) {
         return Blocks.emerald_ore.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreSulfur")) {
         return ArtificeBlocks.blockSulfur.getExpDrop(world, meta, fortune);
      } else if (this.name.equals("oreEnder")) {
         return ArtificeBlocks.blockEnderOre.getExpDrop(world, meta, fortune);
      } else {
         return Block.getBlockFromItem(ItemHelper.getOre(this.name).getItem()) != this
            ? Block.getBlockFromItem(ItemHelper.getOre(this.name).getItem()).getExpDrop(world, meta, fortune)
            : 0;
      }
   }

   public ItemStack createStackedBlock(int p_149644_1_) {
      if (this.name.equals("oreCoal")) {
         return new ItemStack(Blocks.coal_ore);
      } else if (this.name.equals("oreIron")) {
         return new ItemStack(Blocks.iron_ore);
      } else if (this.name.equals("oreLapis")) {
         return new ItemStack(Blocks.lapis_ore);
      } else if (this.name.equals("oreGold")) {
         return new ItemStack(Blocks.gold_ore);
      } else if (this.name.equals("oreDiamond")) {
         return new ItemStack(Blocks.diamond_ore);
      } else if (this.name.equals("oreRedstone")) {
         return new ItemStack(Blocks.redstone_ore);
      } else if (this.name.equals("oreEmerald")) {
         return new ItemStack(Blocks.emerald_ore);
      } else if (this.name.equals("oreSulfur")) {
         return new ItemStack(ArtificeBlocks.blockSulfur);
      } else {
         return this.name.equals("oreEnder")
            ? new ItemStack(ArtificeBlocks.blockEnderOre)
            : new ItemStack(Block.getBlockFromItem(ItemHelper.getOre(this.name).getItem()));
      }
   }

   @Override
   public void registerBlockIcons(IIconRegister reg) {
      this.icon = TextureHandler.registerIcon(reg, this.name, "ores");
   }

   @Override
   public IIcon getIcon(int side, int meta) {
      return this.icon;
   }

   @Override
   public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
      return this.icon;
   }

   @Override
   public void getSubBlocks(Item item, CreativeTabs tabs, List list) {
      if (!this.glowing) {
         for (int i = 0; i < ArtificeBlocks.rockBlocks.length; i++) {
            list.add(new ItemStack(item, 1, i));
         }
      }
   }

   public boolean canRenderInPass(int pass) {
      ClientProxy.renderPass = pass;
      return true;
   }

   public int getRenderBlockPass() {
      return 1;
   }

   public boolean isOpaqueCube() {
      return true;
   }

   public int getRenderType() {
      return ArtificeConfig.oreRenderID;
   }

   public static NameMetaPair getOre(NameMetaPair ore, Block rock) {
      String oreName = ItemHelper.getOreName(ore.getStack());
      if (!oreName.equals("Unknown")) {
         for (int i = 0; i < ArtificeBlocks.rockBlocks.length; i++) {
            if (ArtificeBlocks.rockBlocks[i].equals(rock)) {
               return new NameMetaPair((Block)ArtificeBlocks.oreMappings.get(oreName), i);
            }
         }
      }

      return null;
   }

   public static class BlockOreDummy extends Block {
      public BlockOreDummy() {
         super(Material.rock);
      }

      public IIcon getIcon(int side, int meta) {
         return ArtificeBlocks.rockBlocks[meta].getIcon(side, 0);
      }

      public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
         int meta = block.getBlockMetadata(x, y, z);
         return ArtificeBlocks.rockBlocks[meta].getIcon(side, 0);
      }

      @SideOnly(Side.CLIENT)
      public int getRenderColor(int meta) {
         return ArtificeBlocks.rockBlocks[meta].getRenderColor(meta);
      }

      @SideOnly(Side.CLIENT)
      public int colorMultiplier(IBlockAccess block, int x, int y, int z) {
         int meta = block.getBlockMetadata(x, y, z);
         return ArtificeBlocks.rockBlocks[meta].getRenderColor(meta);
      }
   }
}
