package com.mrfuzzihead.artifice.block.world;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.block.ItemBlockArtifice;
import com.mrfuzzihead.artifice.render.TextureHandler;
import com.mrfuzzihead.artifice.util.Names;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockEnderOre extends Block {

    public IIcon icon;
    public boolean glowing;
    private Random rand = new Random();

    public BlockEnderOre(boolean glowing) {
        super(Material.rock);
        this.setHardness(3.0F);
        this.setResistance(5.0F);
        this.setCreativeTab(ArtificeCore.worldTab);
        if (glowing) {
            this.setBlockName(Names.ENDER_ORE_GLOWING);
        } else {
            this.setBlockName(Names.ENDER_ORE);
        }

        this.setHarvestLevel("pickaxe", 2);
        this.glowing = glowing;
        if (this.glowing) {
            this.setLightLevel(0.625F);
            this.setTickRandomly(true);
        }

        GameRegistry.registerBlock(this, ItemBlockArtifice.class, this.getUnlocalizedName());
    }

    public void harvestBlock(World world, EntityPlayer entityplayer, int x, int y, int z, int fortune) {
        super.harvestBlock(world, entityplayer, x, y, z, fortune);
        if (!world.isRemote && world.rand.nextInt(100) < 20
            && ArtificeConfig.spawnEndermen
            && !EnchantmentHelper.getSilkTouchModifier(entityplayer)) {
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
                        world.rand.nextFloat());
                    world.spawnEntityInWorld(ender);
                    ender.spawnExplosionParticle();
                    ender.playSound("mob.endermen.portal", 1.0F, 1.0F);
                    break;
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
        this.sparkle(world, x, y, z);
        if (!this.glowing) {
            world.setBlock(x, y, z, ArtificeBlocks.blockEnderOreGlowing);
        }
    }

    public void updateTick(World world, int x, int y, int z, Random rand) {
        if (this.glowing) {
            world.setBlock(x, y, z, ArtificeBlocks.blockEnderOre);
        }
    }

    private void sparkle(World world, int x, int y, int z) {
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
                world.rand.nextDouble() - world.rand.nextDouble());
        }
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (this.glowing) {
            this.sparkle(world, x, y, z);
        }
    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
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
        return 2 + rand.nextInt(3) + rand.nextInt(fortune + 1);
    }

    public Item getItemDropped(int meta, Random rand, int fortune) {
        return ArtificeItems.itemResource;
    }

    public int getDamageValue(World world, int x, int y, int z) {
        return world.getBlockMetadata(x, y, z);
    }

    public int damageDropped(int meta) {
        return 2;
    }

    public int getExpDrop(IBlockAccess world, int meta, int fortune) {
        return this.getItemDropped(meta, this.rand, fortune) != Item.getItemFromBlock(this) ? 1 + this.rand.nextInt(5)
            : 0;
    }

    public ItemStack createStackedBlock(int p_149644_1_) {
        return new ItemStack(ArtificeBlocks.blockEnderOre);
    }

    public void registerBlockIcons(IIconRegister reg) {
        this.icon = TextureHandler.registerIcon(reg, "oreEnder", "ores/ender");
    }

    public IIcon getIcon(int side, int meta) {
        return this.icon;
    }

    public IIcon getIcon(IBlockAccess block, int x, int y, int z, int side) {
        return this.icon;
    }

    public void getSubBlocks(Item i, CreativeTabs tabs, List list) {
        if (!this.glowing) {
            list.add(new ItemStack(i, 1, 0));
        }
    }
}
