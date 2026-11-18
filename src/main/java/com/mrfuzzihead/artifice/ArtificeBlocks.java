package com.mrfuzzihead.artifice;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.OreDictionary;

import com.mrfuzzihead.artifice.block.decorative.BlockColoredTorch;
import com.mrfuzzihead.artifice.block.decorative.BlockLamp;
import com.mrfuzzihead.artifice.block.decorative.BlockRockSlab;
import com.mrfuzzihead.artifice.block.decorative.BlockStairsArtifice;
import com.mrfuzzihead.artifice.block.decorative.BlockSteel;
import com.mrfuzzihead.artifice.block.decorative.ItemBlockSlabArtifice;
import com.mrfuzzihead.artifice.block.fluid.BlockFluidBitumen;
import com.mrfuzzihead.artifice.block.fluid.BlockFluidCreosote;
import com.mrfuzzihead.artifice.block.fluid.BlockFluidFuel;
import com.mrfuzzihead.artifice.block.fluid.BlockFluidOil;
import com.mrfuzzihead.artifice.block.frame.BlockFrame;
import com.mrfuzzihead.artifice.block.frame.BlockFrameBase;
import com.mrfuzzihead.artifice.block.frame.BlockFrameBlastWall;
import com.mrfuzzihead.artifice.block.frame.BlockFrameDetector;
import com.mrfuzzihead.artifice.block.frame.BlockFrameGlassWall;
import com.mrfuzzihead.artifice.block.frame.BlockFrameScaffold;
import com.mrfuzzihead.artifice.block.functional.BlockAttunedRedstone;
import com.mrfuzzihead.artifice.block.functional.BlockHeatingCoil;
import com.mrfuzzihead.artifice.block.functional.BlockLogicRedstone;
import com.mrfuzzihead.artifice.block.functional.BlockNuclearBattery;
import com.mrfuzzihead.artifice.block.world.BlockBasicOre;
import com.mrfuzzihead.artifice.block.world.BlockCharredLog;
import com.mrfuzzihead.artifice.block.world.BlockEnderOre;
import com.mrfuzzihead.artifice.block.world.BlockFlora;
import com.mrfuzzihead.artifice.block.world.BlockGlowSand;
import com.mrfuzzihead.artifice.block.world.BlockLotus;
import com.mrfuzzihead.artifice.block.world.BlockNiter;
import com.mrfuzzihead.artifice.block.world.BlockOre;
import com.mrfuzzihead.artifice.block.world.BlockRock;
import com.mrfuzzihead.artifice.block.world.BlockSulfur;
import com.mrfuzzihead.artifice.block.world.BlockTephra;
import com.mrfuzzihead.artifice.util.MinecraftColors;
import com.mrfuzzihead.artifice.util.NameMetaPair;

import cofh.core.util.oredict.OreDictionaryArbiter;
import cpw.mods.fml.common.registry.GameRegistry;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;

public class ArtificeBlocks {

    public static BlockFrame blockFrame;
    public static BlockFlora blockFlora;
    public static BlockLotus blockLotus;
    public static BlockRock blockBasalt;
    public static BlockRock blockMarble;
    public static BlockStairsArtifice blockBasaltBrickStairs;
    public static BlockStairsArtifice blockMarbleBrickStairs;
    public static BlockStairsArtifice blockBasaltCobbleStairs;
    public static BlockStairsArtifice blockMarbleCobbleStairs;
    public static BlockRockSlab blockBasaltSlab;
    public static BlockRockSlab blockBasaltDoubleSlab;
    public static BlockRockSlab blockMarbleSlab;
    public static BlockRockSlab blockMarbleDoubleSlab;
    public static BlockFrame blockDetector;
    public static BlockSteel blockSteel;
    public static BlockFrameBlastWall blockReinforced;
    public static BlockFrameGlassWall blockGlassWall;
    public static BlockFrameGlassWall blockGlassWallDark;
    public static BlockFrameScaffold blockScaffold;
    public static BlockLamp[] blockLamps = new BlockLamp[16];
    public static BlockLamp[] blockLampsInverted = new BlockLamp[16];
    public static BlockRock[] blockLimestones = new BlockRock[7];
    public static BlockRockSlab[] blockLimestoneSlabs = new BlockRockSlab[7];
    public static BlockRockSlab[] blockLimestoneDoubleSlabs = new BlockRockSlab[7];
    public static BlockStairsArtifice[] blockLimestoneBrickStairs = new BlockStairsArtifice[7];
    public static BlockStairsArtifice[] blockLimestoneCobbleStairs = new BlockStairsArtifice[7];
    public static String[] oreNames = new String[] { "oreCoal", "oreIron", "oreLapis", "oreGold", "oreDiamond",
        "oreRedstone", "oreEmerald", "oreCopper", "oreTin", "oreSilver", "oreLead", "oreNickel", "oreSulfur",
        "oreEnder", "oreUranium" };
    public static BlockOre[] blockOres = new BlockOre[oreNames.length];
    public static BlockOre[] blockOresGlowing = new BlockOre[oreNames.length];
    public static BlockSulfur blockSulfur;
    public static BlockEnderOre blockEnderOre;
    public static BlockEnderOre blockEnderOreGlowing;
    public static BlockNiter blockNiter;
    public static Block blockOil;
    public static Block blockFuel;
    public static Block blockBitumen;
    public static Block blockCreosote;
    public static BlockGlowSand blockGlowSand;
    public static BlockColoredTorch[] blockColoredTorches = new BlockColoredTorch[16];
    public static BlockCharredLog blockCharredLog;
    public static BlockTephra blockTephra;
    public static BlockAttunedRedstone blockAttunedRedstoneTransmitter;
    public static BlockAttunedRedstone blockAttunedRedstoneReceiver;
    public static BlockLogicRedstone blockLogicRedstone;
    public static BlockBasicOre blockUranium;
    public static BlockNuclearBattery blockNuclearBattery;
    public static BlockHeatingCoil blockHeatingCoil;
    public static String[] rockColorNames = new String[] { "gray", "lightgray", "brown", "tan", "reddish", "bluish",
        "greenish" };
    public static int[] rockColors = new int[] { 11579568, 16777215, 12362119, 15853509, 11706528, 10526898, 10531488 };
    public static Block[] rockBlocks = new Block[9];
    public static String[] rockBlockNames = new String[] { "basalt", "marble", "limestone.gray", "limestone.lightgray",
        "limestone.brown", "limestone.tan", "limestone.reddish", "limestone.bluish", "limestone.greenish" };
    /**
     * Never registered and never placed: it exists so {@code OreRenderer} has a block to ask for the ore icons, and
     * the game test deliberately excludes it from the registry assertions (CODE_REVIEW §8).
     */
    public static Block blockDummy;
    public static TMap<String, Block> oreMappings;
    public static Set<NameMetaPair> oreSet;

    public static void initBlocks() {
        blockFlora = new BlockFlora();
        blockLotus = new BlockLotus();
        blockBasalt = new BlockRock("basalt");
        rockBlocks[0] = blockBasalt;
        blockMarble = new BlockRock("marble");
        rockBlocks[1] = blockMarble;
        blockBasaltBrickStairs = new BlockStairsArtifice(blockBasalt, 2);
        blockMarbleBrickStairs = new BlockStairsArtifice(blockMarble, 2);
        blockBasaltCobbleStairs = new BlockStairsArtifice(blockBasalt, 1);
        blockMarbleCobbleStairs = new BlockStairsArtifice(blockMarble, 1);
        blockBasaltSlab = new BlockRockSlab("basalt", false);
        blockBasaltDoubleSlab = new BlockRockSlab("basalt", true);
        blockMarbleSlab = new BlockRockSlab("marble", false);
        blockMarbleDoubleSlab = new BlockRockSlab("marble", true);

        for (int i = 0; i < blockLimestones.length; i++) {
            blockLimestones[i] = new BlockRock("limestone." + rockColorNames[i], rockColors[i]);
            rockBlocks[i + 2] = blockLimestones[i];
            blockLimestoneSlabs[i] = new BlockRockSlab("limestone." + rockColorNames[i], rockColors[i], false);
            blockLimestoneDoubleSlabs[i] = new BlockRockSlab("limestone." + rockColorNames[i], rockColors[i], true);
            blockLimestoneBrickStairs[i] = new BlockStairsArtifice(blockLimestones[i], 2, rockColors[i]);
            blockLimestoneCobbleStairs[i] = new BlockStairsArtifice(blockLimestones[i], 1, rockColors[i]);
        }

        for (int i = 0; i < oreNames.length; i++) {
            if (!oreNames[i].equals("oreRedstone") && !oreNames[i].equals("oreEnder")) {
                blockOres[i] = new BlockOre(oreNames[i], i);
            } else {
                blockOres[i] = new BlockOre(oreNames[i], false, 0, i);
                blockOresGlowing[i] = new BlockOre(oreNames[i], true, 0, i);
            }
        }

        blockDummy = new BlockOre.BlockOreDummy();
        blockSulfur = new BlockSulfur();
        blockEnderOre = new BlockEnderOre(false);
        blockEnderOreGlowing = new BlockEnderOre(true);
        blockNiter = new BlockNiter();
        if (FluidRegistry.getFluid("oil")
            .getBlock() == null) {
            blockOil = new BlockFluidOil();
        } else {
            blockOil = FluidRegistry.getFluid("oil")
                .getBlock();
        }

        if (FluidRegistry.getFluid("fuel")
            .getBlock() == null) {
            blockFuel = new BlockFluidFuel();
        } else {
            blockFuel = FluidRegistry.getFluid("fuel")
                .getBlock();
        }

        if (FluidRegistry.getFluid("bitumen")
            .getBlock() == null) {
            blockBitumen = new BlockFluidBitumen();
        } else {
            blockBitumen = FluidRegistry.getFluid("bitumen")
                .getBlock();
        }

        if (FluidRegistry.getFluid("creosote")
            .getBlock() == null) {
            blockCreosote = new BlockFluidCreosote();
        } else {
            blockCreosote = FluidRegistry.getFluid("creosote")
                .getBlock();
        }

        blockGlowSand = new BlockGlowSand();

        for (int ix = 0; ix < blockColoredTorches.length; ix++) {
            blockColoredTorches[ix] = new BlockColoredTorch(ix);
        }

        blockCharredLog = new BlockCharredLog();
        blockTephra = new BlockTephra();
        blockAttunedRedstoneTransmitter = new BlockAttunedRedstone(true);
        blockAttunedRedstoneReceiver = new BlockAttunedRedstone(false);
        blockLogicRedstone = new BlockLogicRedstone();
        GameRegistry
            .registerBlock(blockBasaltSlab, ItemBlockSlabArtifice.class, blockBasalt.getUnlocalizedName() + ".slab");
        GameRegistry.registerBlock(
            blockBasaltDoubleSlab,
            ItemBlockSlabArtifice.class,
            blockBasalt.getUnlocalizedName() + ".doubleslab");
        GameRegistry
            .registerBlock(blockMarbleSlab, ItemBlockSlabArtifice.class, blockMarble.getUnlocalizedName() + ".slab");
        GameRegistry.registerBlock(
            blockMarbleDoubleSlab,
            ItemBlockSlabArtifice.class,
            blockMarble.getUnlocalizedName() + ".doubleslab");

        for (int ix = 0; ix < blockLimestones.length; ix++) {
            GameRegistry.registerBlock(
                blockLimestoneSlabs[ix],
                ItemBlockSlabArtifice.class,
                blockLimestones[ix].getUnlocalizedName() + ".slab");
            GameRegistry.registerBlock(
                blockLimestoneDoubleSlabs[ix],
                ItemBlockSlabArtifice.class,
                blockLimestones[ix].getUnlocalizedName() + ".doubleslab");
        }

        OreDictionary.registerOre("blockMarble", new ItemStack(blockMarble));
        OreDictionary.registerOre("blockBasalt", new ItemStack(blockBasalt));

        for (BlockRock limeStone : blockLimestones) {
            OreDictionary.registerOre("blockLimestone", new ItemStack(limeStone));
        }

        OreDictionary.registerOre("oreSulfur", new ItemStack(blockSulfur, 1, 0));
        OreDictionary.registerOre("oreSaltpeter", new ItemStack(blockNiter, 1, 1));
        OreDictionary.registerOre("oreEnder", new ItemStack(blockEnderOre, 1, 0));
        blockFrame = new BlockFrameBase();
        blockDetector = new BlockFrameDetector();
        blockReinforced = new BlockFrameBlastWall();
        blockGlassWall = new BlockFrameGlassWall(false);
        blockGlassWallDark = new BlockFrameGlassWall(true);
        blockScaffold = new BlockFrameScaffold();

        for (int ix = 0; ix < ArtificeConfig.tiers.length; ix++) {
            OreDictionary.registerOre("blockGlass", new ItemStack(blockGlassWall, 1, ix));
            OreDictionary.registerOre("blockGlass", new ItemStack(blockGlassWallDark, 1, ix));
        }

        blockSteel = new BlockSteel();
        if (ArtificeConfig.steelOreDict) {
            OreDictionary.registerOre("blockSteel", new ItemStack(blockSteel));
        }

        for (int ix = 0; ix < 16; ix++) {
            blockLamps[ix] = new BlockLamp(MinecraftColors.values()[ix], false);
            blockLampsInverted[ix] = new BlockLamp(MinecraftColors.values()[ix], true);
        }

        blockUranium = new BlockBasicOre("uranium", 2);
        OreDictionary.registerOre("oreUranium", new ItemStack(blockUranium, 1, 0));
        OreDictionary.registerOre("blockUranium", new ItemStack(blockUranium, 1, 1));
        blockNuclearBattery = new BlockNuclearBattery();
        blockHeatingCoil = new BlockHeatingCoil();
    }

    public static void initOreMappings() {
        oreMappings = new THashMap<>();

        for (int j = 0; j < oreNames.length; j++) {
            oreMappings.put(oreNames[j], blockOres[j]);
        }
    }

    public static void initOreSet() {
        oreSet = new THashSet();

        for (String s : oreNames) {
            if (OreDictionaryArbiter.getOres(s) != null) {
                for (ItemStack stack : OreDictionaryArbiter.getOres(s)) {
                    if (stack != null) {
                        oreSet.add(new NameMetaPair(stack.getItem(), stack.getItemDamage()));
                    }
                }
            }
        }
    }

    public static void registerOreVariants() {
        for (int i = 0; i < oreNames.length; i++) {
            for (int j = 0; j < rockBlocks.length; j++) {
                OreDictionary.registerOre(oreNames[i], new ItemStack(blockOres[i], 1, j));
            }
        }
    }

    public static void registerBasaltsAndMarbles() {
        for (ItemStack ore : OreDictionary.getOres("blockMarble")) {
            ArtificeRegistry.registerMarbleType(ore);
        }

        for (ItemStack ore : OreDictionary.getOres("marble")) {
            ArtificeRegistry.registerMarbleType(ore);
        }

        for (ItemStack ore : OreDictionary.getOres("blockBasalt")) {
            ArtificeRegistry.registerBasaltType(ore);
        }

        for (ItemStack ore : OreDictionary.getOres("basalt")) {
            ArtificeRegistry.registerBasaltType(ore);
        }
    }
}
