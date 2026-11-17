package shukaro.artifice;

import cofh.core.util.fluid.BucketHandler;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.Item.ToolMaterial;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import shukaro.artifice.item.DispenserBehaviorBox;
import shukaro.artifice.item.DispenserBehaviorSledge;
import shukaro.artifice.item.ItemBox;
import shukaro.artifice.item.ItemBucket;
import shukaro.artifice.item.ItemCoin;
import shukaro.artifice.item.ItemCraftKit;
import shukaro.artifice.item.ItemDye;
import shukaro.artifice.item.ItemNugget;
import shukaro.artifice.item.ItemResource;
import shukaro.artifice.item.ItemSickle;
import shukaro.artifice.item.ItemSledge;
import shukaro.artifice.item.ItemSteel;
import shukaro.artifice.item.ItemUpgrade;

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
      itemBucket = (ItemBucket)new ItemBucket("artifice").setUnlocalizedName("bucket").setCreativeTab(ArtificeCore.mainTab);
      bucketOil = itemBucket.addItem(0, "bucketOil");
      bucketFuel = itemBucket.addItem(1, "bucketFuel");
      bucketCreosote = itemBucket.addItem(2, "bucketCreosote");
      bucketBitumen = itemBucket.addItem(3, "bucketBitumen");
      BucketHandler.registerBucket(ArtificeBlocks.blockOil, 0, bucketOil);
      BucketHandler.registerBucket(ArtificeBlocks.blockFuel, 0, bucketFuel);
      BucketHandler.registerBucket(ArtificeBlocks.blockCreosote, 0, bucketCreosote);
      BucketHandler.registerBucket(ArtificeBlocks.blockBitumen, 0, bucketBitumen);
      FluidContainerRegistry.registerFluidContainer(ArtificeFluids.fluidOil, bucketOil, FluidContainerRegistry.EMPTY_BUCKET);
      FluidContainerRegistry.registerFluidContainer(ArtificeFluids.fluidFuel, bucketFuel, FluidContainerRegistry.EMPTY_BUCKET);
      FluidContainerRegistry.registerFluidContainer(ArtificeFluids.fluidCreosote, bucketCreosote, FluidContainerRegistry.EMPTY_BUCKET);
      FluidContainerRegistry.registerFluidContainer(ArtificeFluids.fluidBitumen, bucketBitumen, FluidContainerRegistry.EMPTY_BUCKET);
      itemCraftKit = new ItemCraftKit();
   }
}
