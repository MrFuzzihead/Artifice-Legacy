package com.mrfuzzihead.artifice;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

    /**
     * Constructs and registers every block in the mod.
     *
     * <p>
     * Split into the groups it always was, in the order it always ran. The order is load-bearing: block ids are
     * assigned as blocks register, so reordering these calls renumbers every block after the change (CODE_REVIEW
     * §7.4).
     * </p>
     */
    public static void initBlocks() {
        initFlora();
        initRocks();
        initOres();
        initFluids();
        initDecorative();
        registerRockBlocks();
        initFrames();
        initFunctional();
    }

    /**
     * Flora first: the lily pad is a subtype of the flora block, and both register in their own constructors.
     */
    private static void initFlora() {
        blockFlora = new BlockFlora();
        blockLotus = new BlockLotus();
    }

    /**
     * Basalt, marble, the limestone variants, and the stairs and slabs that hang off them.
     */
    private static void initRocks() {
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
    }

    /**
     * The fifteen ore variants, the render-proxy dummy, and the three ores that are not in that table: sulfur, ender
     * * and niter.
     */
    private static void initOres() {
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
    }

    /**
     * The four fluid blocks. Each defers to an existing block if another mod already registered the fluid.
     */
    private static void initFluids() {
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
    }

    /**
     * Blocks that exist for their look or their drop: glow sand, coloured torches, charred log, tephra, and the two
     * * attuned redstone blocks.
     */
    private static void initDecorative() {
        blockGlowSand = new BlockGlowSand();

        for (int ix = 0; ix < blockColoredTorches.length; ix++) {
            blockColoredTorches[ix] = new BlockColoredTorch(ix);
        }

        blockCharredLog = new BlockCharredLog();
        blockTephra = new BlockTephra();
        blockAttunedRedstoneTransmitter = new BlockAttunedRedstone(true);
        blockAttunedRedstoneReceiver = new BlockAttunedRedstone(false);
        blockLogicRedstone = new BlockLogicRedstone();
    }

    /**
     * Slab registration and the ore-dictionary entries for the rocks and the three loose ores. This runs after the
     * * blocks exist and before the frames, which is the order 1.1.x shipped with: numeric block ids follow
     * * registration order, so keeping the order stable keeps existing worlds' id tables unchanged.
     */
    private static void registerRockBlocks() {
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
    }

    /**
     * The frame family, the glass ore-dictionary entries, and the steel block.
     */
    private static void initFrames() {
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
    }

    /**
     * The blocks that do something: lamps, uranium, the nuclear battery and the heating coil.
     */
    private static void initFunctional() {
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

    /**
     * The (item, metadata) pairs the rock generators may convert into an Artifice ore: everything the ore dictionary
     * has under our ore names, which by {@code postInit} is every mod's copper, tin, iron and so on.
     *
     * <p>
     * With {@code Compatibility > Convert Other Mods' Ores} off, only vanilla ore blocks and Artifice's own are
     * collected (CODE_REVIEW 9.1). Another mod's ore block is its terrain, its drop and whatever bookkeeping it keeps
     * about it, and this is the only switch that decides whether a blob may rewrite it.
     * </p>
     */
    public static void initOreSet() {
        oreSet = new THashSet();
        int skipped = 0;

        for (String s : oreNames) {
            if (OreDictionaryArbiter.getOres(s) == null) {
                continue;
            }

            for (ItemStack stack : OreDictionaryArbiter.getOres(s)) {
                if (stack == null) {
                    continue;
                }

                if (!ArtificeConfig.convertOtherModOres && !isVanillaOre(stack) && !isOurOre(stack)) {
                    skipped++;
                    continue;
                }

                oreSet.add(new NameMetaPair(stack.getItem(), stack.getItemDamage()));
            }
        }

        if (!ArtificeConfig.convertOtherModOres) {
            ArtificeCore.modLog()
                .info(
                    "Ore variants: ignoring " + skipped
                        + " stack(s) registered by other mods; only vanilla and Artifice ore blocks will be converted.");
        } else {
            ArtificeCore.modLog()
                .debug("Ore variants: " + oreSet.size() + " ore stack(s) eligible for conversion, all mods included.");
        }
    }

    /** The seven ore blocks vanilla ships. Everything else under an ore name belongs to some mod. */
    private static boolean isVanillaOre(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        return block == Blocks.coal_ore || block == Blocks.iron_ore
            || block == Blocks.lapis_ore
            || block == Blocks.gold_ore
            || block == Blocks.diamond_ore
            || block == Blocks.redstone_ore
            || block == Blocks.emerald_ore;
    }

    /**
     * Artifice's own ores: the rock variants, plus the four that are their own block (sulfur, niter, uranium, ender).
     * They stay convertible when the gate is closed - they are ours, and a blob that meets one should still give it
     * the surrounding rock's metadata.
     */
    private static boolean isOurOre(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        return block != null && (block instanceof BlockOre || block == blockSulfur
            || block == blockNiter
            || block == blockUranium
            || block == blockEnderOre);
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
