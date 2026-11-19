package com.mrfuzzihead.artifice;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;

import com.mrfuzzihead.artifice.item.DispenserBehaviorBox;
import com.mrfuzzihead.artifice.item.DispenserBehaviorSledge;
import com.mrfuzzihead.artifice.item.ItemBox;
import com.mrfuzzihead.artifice.item.ItemBucket;
import com.mrfuzzihead.artifice.item.ItemCoin;
import com.mrfuzzihead.artifice.item.ItemCraftKit;
import com.mrfuzzihead.artifice.item.ItemDye;
import com.mrfuzzihead.artifice.item.ItemNugget;
import com.mrfuzzihead.artifice.item.ItemResource;
import com.mrfuzzihead.artifice.item.ItemSickle;
import com.mrfuzzihead.artifice.item.ItemSledge;
import com.mrfuzzihead.artifice.item.ItemSteel;
import com.mrfuzzihead.artifice.item.ItemUpgrade;
import com.mrfuzzihead.artifice.util.Names;

import cofh.core.util.fluid.BucketHandler;

public class ArtificeItems {

    public static ItemTool itemSledgeWood;
    public static ItemTool itemSledgeStone;
    public static ItemTool itemSledgeIron;
    public static ItemTool itemSledgeGold;
    public static ItemTool itemSledgeDiamond;
    public static ItemSteel itemSteelIngot;
    public static ItemBox itemBox;
    public static ItemDye itemDye;
    public static ItemTool itemSickleWood;
    public static ItemTool itemSickleStone;
    public static ItemTool itemSickleIron;
    public static ItemTool itemSickleGold;
    public static ItemTool itemSickleDiamond;
    public static ItemNugget itemNugget;
    public static ItemCoin itemCoin;
    public static ItemUpgrade itemUpgrade;
    public static ItemResource itemResource;
    public static ItemBucket itemBucket;
    public static ItemStack bucketOil;
    public static ItemStack bucketFuel;
    public static ItemStack bucketCreosote;
    public static ItemStack bucketBitumen;
    public static ItemCraftKit itemCraftKit;

    public static void initItems() {
        itemSledgeWood = new ItemSledge(ToolMaterial.WOOD);
        itemSledgeStone = new ItemSledge(ToolMaterial.STONE);
        itemSledgeIron = new ItemSledge(ToolMaterial.IRON);
        itemSledgeGold = new ItemSledge(ToolMaterial.GOLD);
        itemSledgeDiamond = new ItemSledge(ToolMaterial.EMERALD);
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSledgeWood, new DispenserBehaviorSledge());
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSledgeStone, new DispenserBehaviorSledge());
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSledgeIron, new DispenserBehaviorSledge());
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSledgeGold, new DispenserBehaviorSledge());
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemSledgeDiamond, new DispenserBehaviorSledge());
        itemSteelIngot = new ItemSteel();
        if (ArtificeConfig.steelOreDict) {
            OreDictionary.registerOre("ingotSteel", new ItemStack(itemSteelIngot, 1, 0));
            OreDictionary.registerOre("dustSteel", new ItemStack(itemSteelIngot, 1, 1));
        }

        itemBox = new ItemBox();
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemBox, new DispenserBehaviorBox());
        itemDye = new ItemDye();
        OreDictionary.registerOre("dyeBlue", new ItemStack(itemDye, 1, 0));
        OreDictionary.registerOre("dyeBlack", new ItemStack(itemDye, 1, 1));
        OreDictionary.registerOre("dyeBrown", new ItemStack(itemDye, 1, 2));
        OreDictionary.registerOre("dyeWhite", new ItemStack(itemDye, 1, 3));
        itemResource = new ItemResource();
        OreDictionary.registerOre("dustSulfur", new ItemStack(itemResource, 1, 0));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemResource, 1, 1));
        OreDictionary.registerOre("dustEnder", new ItemStack(itemResource, 1, 2));
        OreDictionary.registerOre("ingotUranium", new ItemStack(itemResource, 1, 3));
        itemSickleWood = new ItemSickle(ToolMaterial.WOOD);
        itemSickleStone = new ItemSickle(ToolMaterial.STONE);
        itemSickleIron = new ItemSickle(ToolMaterial.IRON);
        itemSickleGold = new ItemSickle(ToolMaterial.GOLD);
        itemSickleDiamond = new ItemSickle(ToolMaterial.EMERALD);
        itemNugget = new ItemNugget();
        OreDictionary.registerOre("nuggetCopper", new ItemStack(itemNugget, 1, 0));
        OreDictionary.registerOre("nuggetSilver", new ItemStack(itemNugget, 1, 1));
        OreDictionary.registerOre("nuggetPlatinum", new ItemStack(itemNugget, 1, 2));
        itemCoin = new ItemCoin();
        OreDictionary.registerOre("coinCopper", new ItemStack(itemCoin, 1, 0));
        OreDictionary.registerOre("coinSilver", new ItemStack(itemCoin, 1, 1));
        OreDictionary.registerOre("coinGold", new ItemStack(itemCoin, 1, 2));
        OreDictionary.registerOre("coinPlatinum", new ItemStack(itemCoin, 1, 3));
        itemUpgrade = new ItemUpgrade();
        itemBucket = (ItemBucket) new ItemBucket(Names.FLUID_NAMESPACE).setUnlocalizedName(Names.BUCKET)
            .setCreativeTab(ArtificeCore.mainTab);
        bucketOil = itemBucket.addItem(0, "bucketOil");
        bucketFuel = itemBucket.addItem(1, "bucketFuel");
        bucketCreosote = itemBucket.addItem(2, "bucketCreosote");
        bucketBitumen = itemBucket.addItem(3, "bucketBitumen");
        registerBucket(0, ArtificeBlocks.blockOil, bucketOil, ArtificeFluids.fluidOil);
        registerBucket(1, ArtificeBlocks.blockFuel, bucketFuel, ArtificeFluids.fluidFuel);
        registerBucket(2, ArtificeBlocks.blockCreosote, bucketCreosote, ArtificeFluids.fluidCreosote);
        registerBucket(3, ArtificeBlocks.blockBitumen, bucketBitumen, ArtificeFluids.fluidBitumen);
        itemCraftKit = new ItemCraftKit();
    }

    /**
     * Gives a fluid a bucket, but only if Artifice created that fluid (CODE_REVIEW 9.7).
     *
     * <p>
     * All four subtypes are registered above whatever happens: metadata is what a bucket already sitting in a save
     * points at, and CoFH's {@code ItemBase.getRenderPasses} dereferences the entry for the stack's metadata, so an
     * unknown subtype is a client crash rather than a missing texture. What a borrowed fluid does not get is a bucket
     * that <em>works</em>, and one thing follows from that: it is dropped from the creative tab, because an oil bucket
     * that cannot hold oil is exactly the confusion this replaces.
     * </p>
     */
    private static void registerBucket(int meta, Block block, ItemStack bucket, Fluid fluid) {
        if (!ArtificeFluids.owns(fluid)) {
            itemBucket.itemList.remove(Integer.valueOf(meta));
            return;
        }

        BucketHandler.registerBucket(block, 0, bucket);
        FluidContainerRegistry.registerFluidContainer(fluid, bucket, FluidContainerRegistry.EMPTY_BUCKET);
    }
}
