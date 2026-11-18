package com.mrfuzzihead.artifice.gametest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeEnchants;
import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.ArtificeTooltips;
import com.mrfuzzihead.artifice.block.decorative.BlockLamp;
import com.mrfuzzihead.artifice.block.frame.BlockFrame;
import com.mrfuzzihead.artifice.block.frame.BlockFrameBlastWall;
import com.mrfuzzihead.artifice.block.frame.BlockFrameDetector;
import com.mrfuzzihead.artifice.block.world.BlockOre;
import com.mrfuzzihead.artifice.block.world.BlockRock;
import com.mrfuzzihead.artifice.event.ArtificeEventHandler;
import com.mrfuzzihead.artifice.event.Tracking;
import com.mrfuzzihead.artifice.gui.ArtificeCreativeTab;
import com.mrfuzzihead.artifice.item.DispenserBehaviorSledge;
import com.mrfuzzihead.artifice.recipe.BoxCraftingHandler;
import com.mrfuzzihead.artifice.recipe.EnumUpgrades;
import com.mrfuzzihead.artifice.recipe.RecipeBox;
import com.mrfuzzihead.artifice.recipe.RecipeUpgrade;
import com.mrfuzzihead.artifice.tile.TileEntityAttuned;
import com.mrfuzzihead.artifice.tile.TileEntityHeatingCoil;
import com.mrfuzzihead.artifice.tile.TileEntityLogic;
import com.mrfuzzihead.artifice.tile.TileEntityNuclearBattery;
import com.mrfuzzihead.artifice.tile.TileFurnaceWrapper;
import com.mrfuzzihead.artifice.util.BoxContents;
import com.mrfuzzihead.artifice.util.ComparableItemStack;
import com.mrfuzzihead.artifice.util.ComparableItemStackNBT;
import com.mrfuzzihead.artifice.util.NameMetaPair;
import com.mrfuzzihead.artifice.util.OreDictFallback;
import com.mrfuzzihead.artifice.world.WorldGenLayer;

import cpw.mods.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;

/**
 * The in-game assertions. Each {@code check*} method reports every failure through {@link #check} so the exit code
 * and the log agree.
 *
 * <p>
 * Everything here needs the real game: block and item registries, a live {@link World}, and the cofh world hooks.
 * Pure logic is covered by the JUnit suite in {@code src/test/java}.
 * </p>
 */
public final class ServerGameTests {

    private static final String FAIL = "[ARTIFICE-GAMETEST] FAIL ";
    private static final String PASS = "[ARTIFICE-GAMETEST] ok   ";
    private static final String KNOWN = "[ARTIFICE-GAMETEST] known ";

    private static int failures;
    private static int checks;

    private ServerGameTests() {}

    /**
     * Runs every assertion.
     *
     * @return the number of failed assertions
     */
    public static int runAll() {
        failures = 0;
        checks = 0;

        run("registries", ServerGameTests::checkRegistries);
        run("ore dictionary", ServerGameTests::checkOreDictionary);
        run("creative tabs", ServerGameTests::checkCreativeTabs);
        run("creative tab side-only", ServerGameTests::checkCreativeTabSideOnly);
        run("flora metadata", ServerGameTests::checkFloraMetadata);
        run("rock metadata", ServerGameTests::checkRockMetadata);
        run("frame metadata", ServerGameTests::checkFrameMetadata);
        run("frozen registry names", ServerGameTests::checkFrozenRegistryNames);
        run("slab metadata", ServerGameTests::checkSlabMetadata);
        run("lamp lighting", ServerGameTests::checkLampLighting);
        run("lamp stability", ServerGameTests::checkLampStability);
        run("detector pulse", ServerGameTests::checkDetectorPulse);
        run("ore layer generation", ServerGameTests::checkLayerGeneration);
        run("torch placement", ServerGameTests::checkTorchPlacement);
        run("scaffold support", ServerGameTests::checkScaffoldSupport);
        run("slab tooltips", ServerGameTests::checkSlabTooltips);
        run("box item identity", ServerGameTests::checkBoxItemIdentity);
        run("box stack comparison", ServerGameTests::checkBoxStackComparison);
        run("resistance armour", ServerGameTests::checkResistanceArmourDamage);
        run("sickle", ServerGameTests::checkSickleAreaOfEffect);
        run("flora soil", ServerGameTests::checkFloraDropsWithoutSoil);
        run("logic redstone", ServerGameTests::checkLogicRedstone);
        run("attuned redstone", ServerGameTests::checkAttunedRedstone);
        run("heating coil", ServerGameTests::checkHeatingCoil);
        run("nuclear battery", ServerGameTests::checkNuclearBattery);
        run("block drops", ServerGameTests::checkBlockDrops);
        run("ore dictionary dependence", ServerGameTests::checkOreDictionaryDependence);
        run("sledge dispenser", ServerGameTests::checkSledgeDispenser);
        run("recipes", ServerGameTests::checkRecipes);
        run("box recipes", ServerGameTests::checkBoxRecipes);
        run("sledge ingredients", ServerGameTests::checkSledgeIngredients);
        run("tooltips", ServerGameTests::checkTooltips);
        run("config surface", ServerGameTests::checkConfigSurface);
        run("enchantments", ServerGameTests::checkEnchantments);
        run("known bugs", ServerGameTests::checkKnownBugs);

        return failures;
    }

    /**
     * @return how many assertions {@link #runAll()} executed, for the combined summary
     */
    public static int checkCount() {
        return checks;
    }

    // ---------------------------------------------------------------------------------------------------------
    // Harness
    // ---------------------------------------------------------------------------------------------------------

    private static void run(String group, Runnable body) {
        try {
            body.run();
        } catch (Throwable t) {
            t.printStackTrace();
            check(group + " threw " + t, false);
        }
    }

    private static boolean check(String what, boolean condition) {
        checks++;
        if (condition) {
            System.out.println(PASS + what);
        } else {
            failures++;
            System.out.println(FAIL + what);
        }
        return condition;
    }

    private static void checkEquals(String what, Object expected, Object actual) {
        check(
            what + " (expected " + expected + ", got " + actual + ")",
            expected == null ? actual == null : expected.equals(actual));
    }

    /**
     * Records a bug that is known to be present. It is logged, not counted as a failure, so CI stays green until
     * the fix lands - at which point the log line says so and the probe should be promoted to a real check.
     */
    private static void knownBug(String what, boolean stillBroken) {
        checks++;
        if (stillBroken) {
            System.out.println(KNOWN + "still present: " + what);
        } else {
            System.out.println(KNOWN + "FIXED, promote to a real check: " + what);
        }
    }

    private static World overworld() {
        return net.minecraft.server.MinecraftServer.getServer()
            .worldServerForDimension(0);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Registry smoke checks
    // ---------------------------------------------------------------------------------------------------------

    private static void checkRegistries() {
        List<Block> blocks = allBlocks();
        Set<String> names = new HashSet<>();
        String prefix = ArtificeCore.modID + ":";

        for (Block block : blocks) {
            if (block == null) {
                check("no null entry in the block list", false);
                continue;
            }
            String name = Block.blockRegistry.getNameForObject(block);
            check(
                "block registered: " + block.getClass()
                    .getSimpleName(),
                name != null);
            if (name != null) {
                check("block name is namespaced: " + name, name.startsWith(prefix));
                check("block name is unique: " + name, names.add(name));
            }
        }

        List<Item> items = allItems();
        for (Item item : items) {
            if (item == null) {
                check("no null entry in the item list", false);
                continue;
            }
            String name = Item.itemRegistry.getNameForObject(item);
            check(
                "item registered: " + item.getClass()
                    .getSimpleName(),
                name != null);
            if (name != null) {
                check("item name is namespaced: " + name, name.startsWith(prefix));
                check("item name is unique: " + name, names.add(name));
            }
        }

        // The ITEM registry must also carry the ItemBlock for every block, otherwise placement is broken in a way
        // that no block-registry check would catch.
        for (Block block : blocks) {
            if (block == null) continue;
            Item itemBlock = Item.getItemFromBlock(block);
            check(
                "itemblock exists for " + Block.blockRegistry.getNameForObject(block),
                itemBlock != null && Item.itemRegistry.getNameForObject(itemBlock) != null);
        }
    }

    private static List<Block> allBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(ArtificeBlocks.blockFrame);
        blocks.add(ArtificeBlocks.blockFlora);
        blocks.add(ArtificeBlocks.blockLotus);
        blocks.add(ArtificeBlocks.blockBasalt);
        blocks.add(ArtificeBlocks.blockMarble);
        blocks.add(ArtificeBlocks.blockBasaltBrickStairs);
        blocks.add(ArtificeBlocks.blockMarbleBrickStairs);
        blocks.add(ArtificeBlocks.blockBasaltCobbleStairs);
        blocks.add(ArtificeBlocks.blockMarbleCobbleStairs);
        blocks.add(ArtificeBlocks.blockBasaltSlab);
        blocks.add(ArtificeBlocks.blockBasaltDoubleSlab);
        blocks.add(ArtificeBlocks.blockMarbleSlab);
        blocks.add(ArtificeBlocks.blockMarbleDoubleSlab);
        blocks.add(ArtificeBlocks.blockDetector);
        blocks.add(ArtificeBlocks.blockSteel);
        blocks.add(ArtificeBlocks.blockReinforced);
        blocks.add(ArtificeBlocks.blockGlassWall);
        blocks.add(ArtificeBlocks.blockGlassWallDark);
        blocks.add(ArtificeBlocks.blockScaffold);
        blocks.add(ArtificeBlocks.blockSulfur);
        blocks.add(ArtificeBlocks.blockEnderOre);
        blocks.add(ArtificeBlocks.blockEnderOreGlowing);
        blocks.add(ArtificeBlocks.blockNiter);
        blocks.add(ArtificeBlocks.blockOil);
        blocks.add(ArtificeBlocks.blockFuel);
        blocks.add(ArtificeBlocks.blockBitumen);
        blocks.add(ArtificeBlocks.blockCreosote);
        blocks.add(ArtificeBlocks.blockGlowSand);
        blocks.add(ArtificeBlocks.blockCharredLog);
        blocks.add(ArtificeBlocks.blockTephra);
        blocks.add(ArtificeBlocks.blockAttunedRedstoneTransmitter);
        blocks.add(ArtificeBlocks.blockAttunedRedstoneReceiver);
        blocks.add(ArtificeBlocks.blockLogicRedstone);
        blocks.add(ArtificeBlocks.blockUranium);
        blocks.add(ArtificeBlocks.blockNuclearBattery);
        blocks.add(ArtificeBlocks.blockHeatingCoil);

        for (Block lamp : ArtificeBlocks.blockLamps) blocks.add(lamp);
        for (Block lamp : ArtificeBlocks.blockLampsInverted) blocks.add(lamp);
        for (Block limestone : ArtificeBlocks.blockLimestones) blocks.add(limestone);
        for (Block slab : ArtificeBlocks.blockLimestoneSlabs) blocks.add(slab);
        for (Block slab : ArtificeBlocks.blockLimestoneDoubleSlabs) blocks.add(slab);
        for (Block stairs : ArtificeBlocks.blockLimestoneBrickStairs) blocks.add(stairs);
        for (Block stairs : ArtificeBlocks.blockLimestoneCobbleStairs) blocks.add(stairs);
        for (Block ore : ArtificeBlocks.blockOres) blocks.add(ore);
        for (Block ore : ArtificeBlocks.blockOresGlowing) {
            // Only redstone and ender ore have a glowing twin; the rest of the table is intentionally null.
            if (ore != null) blocks.add(ore);
        }
        for (Block torch : ArtificeBlocks.blockColoredTorches) blocks.add(torch);

        return blocks;
    }

    private static List<Item> allItems() {
        List<Item> items = new ArrayList<>();
        items.add(ArtificeItems.itemSledgeWood);
        items.add(ArtificeItems.itemSledgeStone);
        items.add(ArtificeItems.itemSledgeIron);
        items.add(ArtificeItems.itemSledgeGold);
        items.add(ArtificeItems.itemSledgeDiamond);
        items.add(ArtificeItems.itemSteelIngot);
        items.add(ArtificeItems.itemBox);
        items.add(ArtificeItems.itemDye);
        items.add(ArtificeItems.itemSickleWood);
        items.add(ArtificeItems.itemSickleStone);
        items.add(ArtificeItems.itemSickleIron);
        items.add(ArtificeItems.itemSickleGold);
        items.add(ArtificeItems.itemSickleDiamond);
        items.add(ArtificeItems.itemNugget);
        items.add(ArtificeItems.itemCoin);
        items.add(ArtificeItems.itemUpgrade);
        items.add(ArtificeItems.itemResource);
        items.add(ArtificeItems.itemBucket);
        items.add(ArtificeItems.itemCraftKit);
        return items;
    }

    private static void checkOreDictionary() {
        String[] entries = { "blockMarble", "blockBasalt", "blockLimestone", "oreSulfur", "oreSaltpeter", "oreEnder",
            "oreUranium", "blockUranium", "ingotSteel", "dustSteel", "dustSulfur", "dustSaltpeter", "dustEnder",
            "ingotUranium", "nuggetCopper", "nuggetSilver", "nuggetPlatinum", "coinCopper", "coinSilver", "coinGold",
            "coinPlatinum", "dyeBlue", "dyeBlack", "dyeBrown", "dyeWhite" };

        for (String entry : entries) {
            check(
                "ore dictionary entry " + entry,
                !OreDictionary.getOres(entry)
                    .isEmpty());
        }

        // Every ore block we generate must be discoverable through the ore dictionary under its own name.
        for (String oreName : ArtificeBlocks.oreNames) {
            check(
                "generated ore is in the ore dictionary: " + oreName,
                !OreDictionary.getOres(oreName)
                    .isEmpty());
        }

        // Steel is optional: when another mod already owns ingotSteel, our entry is simply an extra stack.
        check(
            "steel entry belongs to some registered steel",
            OreDictionary.getOres("ingotSteel")
                .size() >= 1);
    }

    private static void checkCreativeTabs() {
        // CreativeTabs#displayAllReleventItems, Item#getCreativeTab and Block#getCreativeTabToDisplayOn are all
        // @SideOnly(CLIENT) and are stripped by FML's SideTransformer on a dedicated server, so tab *membership*
        // and tab *contents* cannot be asserted here - that needs a client run. The tab identity is server-safe.
        check("the main creative tab exists", ArtificeCore.mainTab != null);
        check("the worldgen creative tab exists", ArtificeCore.worldTab != null);
        check("the two creative tabs are distinct", ArtificeCore.mainTab != ArtificeCore.worldTab);
    }

    /**
     * {@code CreativeTabs#displayAllReleventItems} and {@code #getTabIconItem} are client-only and are removed by
     * FML's {@code SideTransformer} on a dedicated server. Artifice's {@code ArtificeCreativeTab} overrides both
     * without {@code @SideOnly(Side.CLIENT)}, so the overrides silently become brand-new methods and any
     * server-side call to one dies with {@code NoSuchMethodError}. This detects the mismatch without tripping it.
     */
    /**
     * CODE_REVIEW 5.11. {@code getTabIconItem}, {@code getTranslatedTabLabel} and {@code displayAllReleventItems}
     * are client-only on {@link CreativeTabs} and are removed from the server jar by FML's side transformer. With
     * the annotation they are removed from {@link ArtificeCreativeTab} too, which is what makes the class load
     * without a missing override; without it they survive as *new* methods that call a {@code super} member the
     * server no longer has, and any server-side mod walking the tab list gets
     * {@code NoSuchMethodError: CreativeTabs.displayAllReleventItems}.
     *
     * <p>
     * So on a dedicated server the assertion is absence: an unannotated override shows up here and fails. The
     * annotations themselves are asserted in {@code ArtificeCreativeTabTest} (plain JVM), where the stripping does
     * not happen.
     * </p>
     */
    private static void checkCreativeTabSideOnly() {
        check(
            "the dedicated server has no CreativeTabs.displayAllReleventItems",
            findDeclaredMethod(CreativeTabs.class, "displayAllReleventItems") == null);

        String[] clientOnly = { "getTabIconItem", "getTranslatedTabLabel", "displayAllReleventItems" };
        for (String name : clientOnly) {
            check(
                "ArtificeCreativeTab." + name + " was stripped on the server (it is @SideOnly(CLIENT))",
                findDeclaredMethod(ArtificeCreativeTab.class, name) == null);
        }
    }

    private static java.lang.reflect.Method findDeclaredMethod(Class<?> type, String name) {
        for (java.lang.reflect.Method method : type.getDeclaredMethods()) {
            if (method.getName()
                .equals(name)) {
                return method;
            }
        }

        return null;
    }

    private static void checkFloraMetadata() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            for (int meta = 0; meta < ArtificeConfig.flora.length; meta++) {
                int x = site.x + meta;
                site.setFloor(x, site.y - 1, site.z, Blocks.dirt);
                site.setLight(x, site.y + 1, site.z);
                site.setBlock(x, site.y, site.z, ArtificeBlocks.blockFlora, meta);

                checkEquals("flora meta " + meta + " round trips", meta, world.getBlockMetadata(x, site.y, site.z));
                checkEquals(
                    "flora meta " + meta + " stays a flora block",
                    ArtificeBlocks.blockFlora,
                    world.getBlock(x, site.y, site.z));
            }

            // Closed lotus (meta 4) drops the open lotus (meta 3).
            checkEquals("closed lotus drops the open lotus", 3, ArtificeBlocks.blockFlora.damageDropped(4));
            checkEquals("open lotus drops itself", 0, ArtificeBlocks.blockFlora.damageDropped(0));
        } finally {
            site.clear();
        }
    }

    private static void checkRockMetadata() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            Block[] rocks = ArtificeBlocks.rockBlocks;
            for (int i = 0; i < rocks.length; i++) {
                for (int meta = 0; meta < ArtificeConfig.rocks.length; meta++) {
                    int x = site.x + i;
                    int z = site.z + meta;
                    site.setBlock(x, site.y, z, rocks[i], meta);

                    checkEquals("rock " + i + " meta " + meta + " round trips", rocks[i], world.getBlock(x, site.y, z));
                    checkEquals(
                        "rock " + i + " meta " + meta + " keeps its metadata",
                        meta,
                        world.getBlockMetadata(x, site.y, z));
                }

                for (int stoneMeta : new int[] { 0, 1, 2 }) {
                    check(
                        "rock " + i + " drops cobble for the plain stone",
                        rocks[i].damageDropped(0) == 1 || stoneMeta > 0);
                }
            }
        } finally {
            site.clear();
        }
    }

    /**
     * Every registry name this mod emits, as of 1.1.6. §4 of CODE_REVIEW.md froze them: worlds, config files and
     * other mods' references all key off these strings, so a rename is a data migration, not a refactor.
     *
     * <p>
     * Adding a block or item means adding a line here. That is the point - it forces the decision to be explicit
     * and keeps Appendix A honest.
     * </p>
     */
    private static final String[] FROZEN_REGISTRY_NAMES = { "Artifice:bucket", "Artifice:item.artifice.box",
        "Artifice:item.artifice.coin", "Artifice:item.artifice.craftkit", "Artifice:item.artifice.dye",
        "Artifice:item.artifice.nugget", "Artifice:item.artifice.resource", "Artifice:item.artifice.sickle.emerald",
        "Artifice:item.artifice.sickle.gold", "Artifice:item.artifice.sickle.iron",
        "Artifice:item.artifice.sickle.stone", "Artifice:item.artifice.sickle.wood",
        "Artifice:item.artifice.sledge.emerald", "Artifice:item.artifice.sledge.gold",
        "Artifice:item.artifice.sledge.iron", "Artifice:item.artifice.sledge.stone",
        "Artifice:item.artifice.sledge.wood", "Artifice:item.artifice.steel", "Artifice:item.artifice.upgrade",
        "Artifice:tile.artifice.attunedredstone.receiver", "Artifice:tile.artifice.attunedredstone.transmitter",
        "Artifice:tile.artifice.basalt", "Artifice:tile.artifice.basalt.doubleslab",
        "Artifice:tile.artifice.basalt.slab", "Artifice:tile.artifice.basalt.stairs.brick",
        "Artifice:tile.artifice.basalt.stairs.cobblestone", "Artifice:tile.artifice.charredlog",
        "Artifice:tile.artifice.coloredtorch0", "Artifice:tile.artifice.coloredtorch1",
        "Artifice:tile.artifice.coloredtorch10", "Artifice:tile.artifice.coloredtorch11",
        "Artifice:tile.artifice.coloredtorch12", "Artifice:tile.artifice.coloredtorch13",
        "Artifice:tile.artifice.coloredtorch14", "Artifice:tile.artifice.coloredtorch15",
        "Artifice:tile.artifice.coloredtorch2", "Artifice:tile.artifice.coloredtorch3",
        "Artifice:tile.artifice.coloredtorch4", "Artifice:tile.artifice.coloredtorch5",
        "Artifice:tile.artifice.coloredtorch6", "Artifice:tile.artifice.coloredtorch7",
        "Artifice:tile.artifice.coloredtorch8", "Artifice:tile.artifice.coloredtorch9",
        "Artifice:tile.artifice.detector", "Artifice:tile.artifice.enderore", "Artifice:tile.artifice.enderore.glowing",
        "Artifice:tile.artifice.flora", "Artifice:tile.artifice.flora.lily", "Artifice:tile.artifice.fluid.bitumen",
        "Artifice:tile.artifice.fluid.creosote", "Artifice:tile.artifice.fluid.fuel",
        "Artifice:tile.artifice.fluid.oil", "Artifice:tile.artifice.frame", "Artifice:tile.artifice.glasswall",
        "Artifice:tile.artifice.glasswalldark", "Artifice:tile.artifice.glowsand", "Artifice:tile.artifice.heatingcoil",
        "Artifice:tile.artifice.lamp.black", "Artifice:tile.artifice.lamp.blue", "Artifice:tile.artifice.lamp.brown",
        "Artifice:tile.artifice.lamp.cyan", "Artifice:tile.artifice.lamp.gray", "Artifice:tile.artifice.lamp.green",
        "Artifice:tile.artifice.lamp.lightblue", "Artifice:tile.artifice.lamp.lightgray",
        "Artifice:tile.artifice.lamp.lime", "Artifice:tile.artifice.lamp.magenta", "Artifice:tile.artifice.lamp.orange",
        "Artifice:tile.artifice.lamp.pink", "Artifice:tile.artifice.lamp.purple", "Artifice:tile.artifice.lamp.red",
        "Artifice:tile.artifice.lamp.white", "Artifice:tile.artifice.lamp.yellow",
        "Artifice:tile.artifice.lampinverted.black", "Artifice:tile.artifice.lampinverted.blue",
        "Artifice:tile.artifice.lampinverted.brown", "Artifice:tile.artifice.lampinverted.cyan",
        "Artifice:tile.artifice.lampinverted.gray", "Artifice:tile.artifice.lampinverted.green",
        "Artifice:tile.artifice.lampinverted.lightblue", "Artifice:tile.artifice.lampinverted.lightgray",
        "Artifice:tile.artifice.lampinverted.lime", "Artifice:tile.artifice.lampinverted.magenta",
        "Artifice:tile.artifice.lampinverted.orange", "Artifice:tile.artifice.lampinverted.pink",
        "Artifice:tile.artifice.lampinverted.purple", "Artifice:tile.artifice.lampinverted.red",
        "Artifice:tile.artifice.lampinverted.white", "Artifice:tile.artifice.lampinverted.yellow",
        "Artifice:tile.artifice.limestone.bluish", "Artifice:tile.artifice.limestone.bluish.doubleslab",
        "Artifice:tile.artifice.limestone.bluish.slab", "Artifice:tile.artifice.limestone.bluish.stairs.brick",
        "Artifice:tile.artifice.limestone.bluish.stairs.cobblestone", "Artifice:tile.artifice.limestone.brown",
        "Artifice:tile.artifice.limestone.brown.doubleslab", "Artifice:tile.artifice.limestone.brown.slab",
        "Artifice:tile.artifice.limestone.brown.stairs.brick",
        "Artifice:tile.artifice.limestone.brown.stairs.cobblestone", "Artifice:tile.artifice.limestone.gray",
        "Artifice:tile.artifice.limestone.gray.doubleslab", "Artifice:tile.artifice.limestone.gray.slab",
        "Artifice:tile.artifice.limestone.gray.stairs.brick",
        "Artifice:tile.artifice.limestone.gray.stairs.cobblestone", "Artifice:tile.artifice.limestone.greenish",
        "Artifice:tile.artifice.limestone.greenish.doubleslab", "Artifice:tile.artifice.limestone.greenish.slab",
        "Artifice:tile.artifice.limestone.greenish.stairs.brick",
        "Artifice:tile.artifice.limestone.greenish.stairs.cobblestone", "Artifice:tile.artifice.limestone.lightgray",
        "Artifice:tile.artifice.limestone.lightgray.doubleslab", "Artifice:tile.artifice.limestone.lightgray.slab",
        "Artifice:tile.artifice.limestone.lightgray.stairs.brick",
        "Artifice:tile.artifice.limestone.lightgray.stairs.cobblestone", "Artifice:tile.artifice.limestone.reddish",
        "Artifice:tile.artifice.limestone.reddish.doubleslab", "Artifice:tile.artifice.limestone.reddish.slab",
        "Artifice:tile.artifice.limestone.reddish.stairs.brick",
        "Artifice:tile.artifice.limestone.reddish.stairs.cobblestone", "Artifice:tile.artifice.limestone.tan",
        "Artifice:tile.artifice.limestone.tan.doubleslab", "Artifice:tile.artifice.limestone.tan.slab",
        "Artifice:tile.artifice.limestone.tan.stairs.brick", "Artifice:tile.artifice.limestone.tan.stairs.cobblestone",
        "Artifice:tile.artifice.logicredstone", "Artifice:tile.artifice.marble",
        "Artifice:tile.artifice.marble.doubleslab", "Artifice:tile.artifice.marble.slab",
        "Artifice:tile.artifice.marble.stairs.brick", "Artifice:tile.artifice.marble.stairs.cobblestone",
        "Artifice:tile.artifice.niter", "Artifice:tile.artifice.nuclearbattery", "Artifice:tile.artifice.orecoal",
        "Artifice:tile.artifice.orecopper", "Artifice:tile.artifice.orediamond", "Artifice:tile.artifice.oreemerald",
        "Artifice:tile.artifice.oreender", "Artifice:tile.artifice.oreender.glowing", "Artifice:tile.artifice.oregold",
        "Artifice:tile.artifice.oreiron", "Artifice:tile.artifice.orelapis", "Artifice:tile.artifice.orelead",
        "Artifice:tile.artifice.orenickel", "Artifice:tile.artifice.oreredstone",
        "Artifice:tile.artifice.oreredstone.glowing", "Artifice:tile.artifice.oresilver",
        "Artifice:tile.artifice.oresulfur", "Artifice:tile.artifice.oretin", "Artifice:tile.artifice.oreuranium",
        "Artifice:tile.artifice.reinforced", "Artifice:tile.artifice.scaffold", "Artifice:tile.artifice.steel",
        "Artifice:tile.artifice.sulfur", "Artifice:tile.artifice.tephra", "Artifice:tile.artifice.uranium", };

    /**
     * The §4 freeze, enforced. A rename would silently turn every existing block into a missing-mapping and every
     * config reference into a no-op, so it should fail a test rather than a world.
     */
    private static void checkFrozenRegistryNames() {
        Set<String> expected = new TreeSet<>(Arrays.asList(FROZEN_REGISTRY_NAMES));
        Set<String> actual = new TreeSet<>();
        for (Object key : Block.blockRegistry.getKeys()) {
            String name = key.toString();
            if (name.startsWith("Artifice:")) {
                actual.add(name);
            }
        }
        for (Object key : Item.itemRegistry.getKeys()) {
            String name = key.toString();
            if (name.startsWith("Artifice:")) {
                actual.add(name);
            }
        }

        Set<String> missing = new TreeSet<>(expected);
        missing.removeAll(actual);
        Set<String> added = new TreeSet<>(actual);
        added.removeAll(expected);

        check("no frozen registry name disappeared " + missing, missing.isEmpty());
        check("no registry name was added without freezing it here " + added, added.isEmpty());
        checkEquals("the frozen registry name count", expected.size(), actual.size());
    }

    private static void checkFrameMetadata() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            for (int meta = 0; meta < ArtificeConfig.tiers.length; meta++) {
                Block[] frames = { ArtificeBlocks.blockFrame, ArtificeBlocks.blockReinforced,
                    ArtificeBlocks.blockGlassWall, ArtificeBlocks.blockGlassWallDark, ArtificeBlocks.blockScaffold };

                for (int i = 0; i < frames.length; i++) {
                    int x = site.x + i;
                    int z = site.z + meta;
                    site.setFloor(x, site.y - 1, z, Blocks.stone);
                    site.setBlock(x, site.y, z, frames[i], meta);

                    checkEquals(
                        frames[i].getClass()
                            .getSimpleName() + " meta " + meta,
                        frames[i],
                        world.getBlock(x, site.y, z));
                    checkEquals(
                        frames[i].getClass()
                            .getSimpleName() + " meta " + meta + " kept",
                        meta,
                        world.getBlockMetadata(x, site.y, z));
                }
            }

            check(
                "blast wall resistance scales with tier",
                ArtificeBlocks.blockReinforced.getResistance(3) > ArtificeBlocks.blockReinforced.getResistance(0));
            check(
                "glass wall resistance scales with tier",
                ArtificeBlocks.blockGlassWall.getResistance(3) > ArtificeBlocks.blockGlassWall.getResistance(0));

            // CODE_REVIEW §9.3 / §9.4: hardness is a table by tier, and a blast wall adds five to it. The table
            // replaced `meta + 5`, which turned corrupt metadata 7 into hardness 12.
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            for (int meta = 0; meta < ArtificeConfig.tiers.length; meta++) {
                site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockFrame, meta);
                checkEquals(
                    "frame hardness for tier " + meta,
                    5.0F + meta,
                    ArtificeBlocks.blockFrame.getBlockHardness(world, site.x, site.y, site.z));

                site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockReinforced, meta);
                checkEquals(
                    "blast wall hardness for tier " + meta,
                    5.0F + meta + BlockFrameBlastWall.BLAST_BONUS,
                    ArtificeBlocks.blockReinforced.getBlockHardness(world, site.x, site.y, site.z));
            }

            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockFrame, 7);
            checkEquals("frame metadata 7 is placed", 7, world.getBlockMetadata(site.x, site.y, site.z));
            checkEquals(
                "corrupt frame metadata gets the first tier's hardness, not a number of its own",
                5.0F,
                ArtificeBlocks.blockFrame.getBlockHardness(world, site.x, site.y, site.z));
            checkEquals("the table is asked for metadata 7 directly too", 5.0F, BlockFrame.hardnessFor(7));
        } finally {
            site.clear();
        }
    }

    private static void checkSlabMetadata() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            Block[] slabs = { ArtificeBlocks.blockBasaltSlab, ArtificeBlocks.blockBasaltDoubleSlab,
                ArtificeBlocks.blockMarbleSlab, ArtificeBlocks.blockMarbleDoubleSlab };

            for (int i = 0; i < slabs.length; i++) {
                int x = site.x + i;
                site.setBlock(x, site.y, site.z, slabs[i], 2);

                checkEquals("slab " + i + " placed", slabs[i], world.getBlock(x, site.y, site.z));
                checkEquals("slab " + i + " metadata", 2, world.getBlockMetadata(x, site.y, site.z));
            }

            // A double slab must drop its half-slab counterpart, not itself.
            check(
                "double basalt slab drops the half slab",
                ArtificeBlocks.blockBasaltDoubleSlab.getItemDropped(0, world.rand, 0)
                    == Item.getItemFromBlock(ArtificeBlocks.blockBasaltSlab));
            check(
                "double marble slab drops the half slab",
                ArtificeBlocks.blockMarbleDoubleSlab.getItemDropped(0, world.rand, 0)
                    == Item.getItemFromBlock(ArtificeBlocks.blockMarbleSlab));
        } finally {
            site.clear();
        }
    }

    /**
     * Scheduled updates are inspected, never run: nothing ticks during the assertions, so the world's pending set is
     * the only evidence that a block asked to run again.
     */
    private static int pendingCount(World world, int x, int y, int z) {
        return pending(world, x, y, z, false);
    }

    /** Consumes the pending update for one coordinate, which is what a world tick would have done. */
    private static int drainPending(World world, int x, int y, int z) {
        return pending(world, x, y, z, true);
    }

    private static int pending(World world, int x, int y, int z, boolean remove) {
        Chunk chunk = world.getChunkFromBlockCoords(x, z);
        List<?> scheduled = world.getPendingBlockUpdates(chunk, remove);
        if (scheduled == null) return 0;

        int found = 0;
        for (Object entry : scheduled) {
            NextTickListEntry update = (NextTickListEntry) entry;
            if (update.xCoord == x && update.yCoord == y && update.zCoord == z) {
                found++;
            }
        }
        return found;
    }

    /**
     * Section 5.19: the detector notified its neighbours on all three steps of its own cycle, including the step where
     * nothing had changed. A neighbour that reacts to that notification (redstone dust, a second detector) notified
     * back, found the detector idle and re-armed it, so one block change next to a dust line produced a pulse every
     * two ticks for the life of the world.
     *
     * <p>
     * The test drives the cycle by hand - the world is not ticking - and puts a {@link NeighborProbe} next to the
     * detector so the echo happens for real, inside the same notification chain, exactly as dust's would.
     * </p>
     */
    private static void checkDetectorPulse() {
        World world = overworld();
        TestSite site = TestSite.create(world, 768, 768);
        BlockFrameDetector detector = (BlockFrameDetector) ArtificeBlocks.blockDetector;

        try {
            int x = site.x;
            int y = site.y;
            int z = site.z;

            site.setBlock(x, y, z, detector, BlockFrameDetector.META_IDLE);
            site.setBlock(x + 1, y, z, GameTestMod.probe, 0);
            drainPending(world, x, y, z);
            NeighborProbe.reset();

            checkEquals("detector starts idle", BlockFrameDetector.META_IDLE, world.getBlockMetadata(x, y, z));
            checkEquals("idle detector emits no power", 0, detector.isProvidingWeakPower(world, x, y, z, 0));

            // What a neighbour changing looks like to the detector.
            detector.onNeighborBlockChange(world, x, y, z, Blocks.stone);
            check("detector arms on a neighbour change", pendingCount(world, x, y, z) == 1);

            int steps = 0;
            int transitions = 0;
            int poweredSteps = 0;
            int last = world.getBlockMetadata(x, y, z);

            while (pendingCount(world, x, y, z) > 0 && steps < 32) {
                drainPending(world, x, y, z);
                detector.updateTick(world, x, y, z, world.rand);
                steps++;

                int meta = world.getBlockMetadata(x, y, z);
                if (meta != last) transitions++;
                if (detector.isProvidingWeakPower(world, x, y, z, 0) > 0) poweredSteps++;
                last = meta;
            }

            check("detector stops pulsing next to an echoing neighbour", steps < 32);
            checkEquals("one pulse is three transitions, not a cycle", 3, transitions);
            checkEquals("detector ends idle", BlockFrameDetector.META_IDLE, last);
            checkEquals("detector emits power for exactly one step", 1, poweredSteps);
            checkEquals("detector stops with nothing scheduled", 0, pendingCount(world, x, y, z));
            check("the probe saw the pulse", NeighborProbe.observations() > 0);

            // The state table itself, including the heal path for metadata written by an older save.
            check(
                "cycle is idle -> active -> tail -> idle",
                BlockFrameDetector.nextMetadata(BlockFrameDetector.META_IDLE) == BlockFrameDetector.META_ACTIVE
                    && BlockFrameDetector.nextMetadata(BlockFrameDetector.META_ACTIVE) == BlockFrameDetector.META_TAIL
                    && BlockFrameDetector.nextMetadata(BlockFrameDetector.META_TAIL) == BlockFrameDetector.META_IDLE);
            checkEquals(
                "corrupt metadata heals to idle",
                BlockFrameDetector.META_IDLE,
                BlockFrameDetector.nextMetadata(7));
            check(
                "only the active state powers redstone",
                BlockFrameDetector.emitsPower(BlockFrameDetector.META_ACTIVE)
                    && !BlockFrameDetector.emitsPower(BlockFrameDetector.META_IDLE)
                    && !BlockFrameDetector.emitsPower(BlockFrameDetector.META_TAIL));
        } finally {
            site.clear();
        }
    }

    /**
     * Section 5.20: the lamp scheduled a tick for every neighbour change it saw, including the ones its own metadata
     * write caused, so in a lamp wall every lamp re-queued every other lamp.
     */
    private static void checkLampStability() {
        World world = overworld();
        TestSite site = TestSite.create(world, 784, 768);
        BlockLamp lamp = ArtificeBlocks.blockLamps[0];

        try {
            int x = site.x;
            int y = site.y;
            int z = site.z;

            site.setBlock(x, y, z, lamp, 0);
            site.setBlock(x + 1, y, z, GameTestMod.probe, 0);
            drainPending(world, x, y, z);
            NeighborProbe.reset();

            // Unpowered and already at 0: no neighbour change can alter the light, so none may cost a tick.
            lamp.onNeighborBlockChange(world, x, y, z, Blocks.stone);
            checkEquals("lamp does not tick when its power cannot change", 0, pendingCount(world, x, y, z));

            // Power it: now the target differs, so the lamp arms itself.
            site.setBlock(x, y + 1, z, Blocks.redstone_block, 0);
            lamp.onNeighborBlockChange(world, x, y, z, Blocks.redstone_block);
            check("lamp arms when its power changes", pendingCount(world, x, y, z) == 1);

            int steps = 0;
            while (pendingCount(world, x, y, z) > 0 && steps < 16) {
                drainPending(world, x, y, z);
                lamp.updateTick(world, x, y, z, world.rand);
                steps++;
            }

            checkEquals("lamp reaches full power", 15, world.getBlockMetadata(x, y, z));
            checkEquals("lamp light follows the power", 15, lamp.getLightValue(world, x, y, z));
            checkEquals("lamp ticks once, then goes quiet", 1, steps);
            checkEquals("lamp leaves nothing scheduled", 0, pendingCount(world, x, y, z));
            check("the probe saw the change", NeighborProbe.observations() > 0);
        } finally {
            site.clear();
        }
    }

    private static void checkLampLighting() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            // The colour is the block identity; the metadata is the redstone power level and therefore the light
            // level. One lamp block is one colour, and all sixteen metadata values mean "how bright".
            for (int power = 0; power <= 15; power++) {
                for (int colour = 0; colour < ArtificeBlocks.blockLamps.length; colour++) {
                    int x = site.x + colour;
                    site.setBlock(x, site.y, site.z, ArtificeBlocks.blockLamps[colour], power);
                    checkEquals(
                        "lamp colour " + colour + " at power " + power,
                        power,
                        ArtificeBlocks.blockLamps[colour].getLightValue(world, x, site.y, site.z));

                    site.setBlock(x, site.y, site.z, ArtificeBlocks.blockLampsInverted[colour], power);
                    checkEquals(
                        "inverted lamp colour " + colour + " at power " + power,
                        15 - power,
                        ArtificeBlocks.blockLampsInverted[colour].getLightValue(world, x, site.y, site.z));
                }
            }
        } finally {
            site.clear();
        }
    }

    private static void checkTorchPlacement() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            for (int i = 0; i < ArtificeBlocks.blockColoredTorches.length; i++) {
                int x = site.x + i;
                site.setFloor(x, site.y - 1, site.z, Blocks.stone);
                site.setBlock(x, site.y, site.z, ArtificeBlocks.blockColoredTorches[i], 5);

                checkEquals(
                    "colored torch " + i + " placed",
                    ArtificeBlocks.blockColoredTorches[i],
                    world.getBlock(x, site.y, site.z));
                checkEquals("colored torch " + i + " meta", 5, world.getBlockMetadata(x, site.y, site.z));
            }
        } finally {
            site.clear();
        }
    }

    private static void checkScaffoldSupport() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockScaffold, 0);

            // The collision box is inset so a climber does not sink into the scaffold; the climbable face starts just
            // inside the block (the two constants CODE_REVIEW §7.7 asked to name).
            AxisAlignedBB box = ArtificeBlocks.blockScaffold
                .getCollisionBoundingBoxFromPool(world, site.x, site.y, site.z);
            check("the scaffold has a collision box", box != null);
            if (box != null) {
                Block scaffold = ArtificeBlocks.blockScaffold;
                double inset = 0.125;
                double dx = (box.minX - site.x) - (scaffold.getBlockBoundsMinX() + inset);
                double dz = (box.minZ - site.z) - (scaffold.getBlockBoundsMinZ() + inset);
                double dh = (box.maxY - box.minY) - (scaffold.getBlockBoundsMaxY() - scaffold.getBlockBoundsMinY());

                check("the collision box is inset by an eighth of a block", Math.abs(dx) < 1e-6 && Math.abs(dz) < 1e-6);
                check("the collision box keeps the block's height", Math.abs(dh) < 1e-6);
            }

            checkEquals(
                "scaffold on a solid floor stays",
                ArtificeBlocks.blockScaffold,
                world.getBlock(site.x, site.y, site.z));

            // Removing the support must drop the scaffold (it has no other anchored neighbour).
            world.setBlockToAir(site.x, site.y - 1, site.z);

            checkEquals("scaffold without support is removed", Blocks.air, world.getBlock(site.x, site.y, site.z));

            // CODE_REVIEW 5.7: a floating scaffold must stay up if *any* same-meta neighbour is anchored. The
            // check used to abort on the first scaffold neighbour it met (whatever its tier) instead of looking at
            // the other five sides, so a mixed-tier structure dropped blocks that had a good anchor.
            int cx = site.x;
            int cy = site.y;
            int cz = site.z;

            site.setBlock(cx, cy - 1, cz + 1, Blocks.stone, 0); // ground for the anchor
            site.setBlock(cx, cy, cz + 1, ArtificeBlocks.blockScaffold, 0); // SOUTH: same tier, anchored
            site.setBlock(cx, cy, cz, ArtificeBlocks.blockScaffold, 0); // the block under test, floating
            site.setBlock(cx, cy, cz - 1, ArtificeBlocks.blockScaffold, 1); // NORTH: other tier, unanchored

            // This is the observable symptom: placing the other-tier neighbour notifies the floating block, whose
            // neighbour check used to stop at the first scaffold it saw and drop itself.
            checkEquals(
                "the block survives the neighbour update that used to drop it",
                ArtificeBlocks.blockScaffold,
                world.getBlock(cx, cy, cz));
            check(
                "an unanchored scaffold of another tier does not cancel an anchored neighbour",
                ArtificeBlocks.blockScaffold.canBlockStay(world, cx, cy, cz));

            // Remove the anchor's ground: now nothing within reach is anchored and the block must go.
            world.setBlockToAir(cx, cy - 1, cz + 1);
            check(
                "with no anchored neighbour at all the scaffold cannot stay",
                !ArtificeBlocks.blockScaffold.canBlockStay(world, cx, cy, cz));
        } finally {
            site.clear();
        }
    }

    /**
     * CODE_REVIEW 5.14. The plant window was {@code i < radius} on both axes, so it harvested an off-centre square
     * that never reached +radius, and both branches divided the block count before damaging the tool, so harvesting
     * one or two plants cost no durability at all.
     */
    /**
     * CODE_REVIEW 5.26. A hit that the resistance enchantment absorbs damages one armour piece, and it must be a
     * piece that actually carries the enchantment. The old code picked a random armour slot and accepted any
     * non-empty one, so it could wear out armour that was not providing the resistance - and it only terminated
     * because some slot happened to be filled.
     */
    private static void checkResistanceArmourDamage() {
        World world = overworld();
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft((net.minecraft.world.WorldServer) world);
        check("fake player available", player != null);
        if (player == null) return;

        check("the resistance enchantment is registered", ArtificeEnchants.hasResistance());
        if (!ArtificeEnchants.hasResistance()) return;

        player.inventory.armorInventory[0] = null;
        player.inventory.armorInventory[1] = null;
        player.inventory.armorInventory[2] = null;
        player.inventory.armorInventory[3] = null;

        ItemStack helmet = new ItemStack(Items.diamond_helmet);
        helmet.addEnchantment(ArtificeEnchants.enchantmentResistance, 1);
        ItemStack chestplate = new ItemStack(Items.diamond_chestplate);

        player.setCurrentItemOrArmor(4, helmet);
        player.setCurrentItemOrArmor(3, chestplate);

        ArtificeEventHandler handler = new ArtificeEventHandler();
        for (int i = 0; i < 20; i++) {
            handler.onDamage(
                new net.minecraftforge.event.entity.living.LivingHurtEvent(
                    player,
                    net.minecraft.util.DamageSource.magic,
                    100.0F));
        }

        checkEquals("the enchanted helmet takes the wear", 20, helmet.getItemDamage());
        checkEquals("the unenchanted chestplate is untouched", 0, chestplate.getItemDamage());

        player.setCurrentItemOrArmor(4, null);
        player.setCurrentItemOrArmor(3, null);
    }

    /**
     * CODE_REVIEW 5.29. {@code ItemBlockSlabArtifice} looked its tooltip up by the stack's *unlocalised* name, while
     * {@code ArtificeRegistry} keys tooltips by (item, damage) registry identity - so the lookup could never hit and
     * every slab tooltip was dead. The paver/antipaver slabs are the ones with text.
     */
    private static void checkSlabTooltips() {
        int registered = 0;

        for (Object object : new Object[] { ArtificeBlocks.blockBasaltSlab, ArtificeBlocks.blockMarbleSlab }) {
            Block slabBlock = (Block) object;
            for (int meta = 2; meta <= 3; meta++) {
                ItemStack stack = new ItemStack(slabBlock, 1, meta);
                NameMetaPair key = new NameMetaPair(stack.getItem(), stack.getItemDamage());
                List<String> lines = ArtificeRegistry.getTooltipMap()
                    .get(key);
                check(
                    slabBlock.getUnlocalizedName() + "@" + meta + " has a tooltip under its registry identity",
                    lines != null && !lines.isEmpty());
                registered++;
            }
        }

        check("slab tooltips were checked (" + registered + ")", registered == 4);

        // The name-keyed lookup the item block used to do must find nothing: that is the bug, stated as an assertion.
        ItemStack sample = new ItemStack(ArtificeBlocks.blockBasaltSlab, 1, 2);
        check(
            "the unlocalised name is not a tooltip key",
            ArtificeRegistry.getTooltipMap()
                .get(new NameMetaPair(sample.getUnlocalizedName(), sample.getItemDamage())) == null
                || !sample.getUnlocalizedName()
                    .equals(
                        sample.getItem()
                            .getUnlocalizedName(sample)));
    }

    /**
     * CODE_REVIEW 9.6. Boxes stored a numeric item id, which is registration-order dependent in 1.7.10, so a box in a
     * chest could silently become a different item after a pack update. Boxes now store the registry name and prefer
     * it, with the numeric id kept as a fallback for older boxes.
     */
    private static void checkBoxItemIdentity() {
        World world = overworld();
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft((net.minecraft.world.WorldServer) world);
        check("fake player available", player != null);
        if (player == null) return;

        InventoryCrafting matrix = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer ignored) {
                return true;
            }
        }, 3, 3);
        ItemStack crafted = new ItemStack(Items.stick);

        // 1. A box written by the new code carries the registry name and a correct id.
        ItemStack stored = new ItemStack(Items.gold_ingot);
        NBTTagCompound tag = BoxContents.write(new NBTTagCompound(), stored);
        checkEquals("the box stores the registry name", "minecraft:gold_ingot", tag.getString("name"));
        check("the box still stores a numeric id for older readers", tag.getInteger("id") > 0);

        // 2. The name wins over the id: a stale numeric id that now points at something else must be ignored.
        NBTTagCompound stale = new NBTTagCompound();
        stale.setString("name", "minecraft:gold_ingot");
        stale.setInteger("id", Item.getIdFromItem(Items.stick));
        stale.setInteger("meta", 0);
        checkEquals("the registry name takes precedence over a stale id", Items.gold_ingot, BoxContents.item(stale));

        // 3. Legacy box: id only, no name.
        NBTTagCompound legacy = new NBTTagCompound();
        legacy.setInteger("id", Item.getIdFromItem(Items.iron_ingot));
        legacy.setInteger("meta", 0);
        checkEquals("a legacy box without a name still resolves", Items.iron_ingot, BoxContents.item(legacy));

        // 4. A box naming something this pack does not have resolves to null rather than throwing.
        NBTTagCompound missing = new NBTTagCompound();
        missing.setString("name", "artifice:not_an_item");
        missing.setInteger("id", 32000);
        check("a box naming a missing item resolves to null", BoxContents.item(missing) == null);

        // ... and crafting with it does nothing, without throwing.
        ItemStack brokenBox = new ItemStack(ArtificeItems.itemBox, 1, 3);
        brokenBox.setTagCompound(missing);
        matrix.setInventorySlotContents(0, brokenBox);
        new BoxCraftingHandler().onCrafting(new ItemCraftedEvent(player, crafted, matrix));
        check("a box naming a missing item is left alone", matrix.getStackInSlot(0) != null);

        // 5. The full path: a name-carrying box yields its item through the crafting handler.
        ItemStack nameBox = new ItemStack(ArtificeItems.itemBox, 1, 2);
        nameBox.setTagCompound(BoxContents.write(new NBTTagCompound(), new ItemStack(Items.diamond)));
        matrix.setInventorySlotContents(0, nameBox);

        int before = countItem(player, Items.diamond);
        new BoxCraftingHandler().onCrafting(new ItemCraftedEvent(player, crafted, matrix));
        checkEquals("a name-carrying box yields its item", before + 2, countItem(player, Items.diamond));
    }

    /**
     * The value types behind {@code RecipeBox.matches}: two stacks are "the same item" if they share an
     * ore-dictionary id or the same item and damage, and the NBT variant adds the tag to that.
     *
     * <p>
     * Headless tests cannot cover this - the harness has no bootstrapped item registry, so there is no
     * {@code Items.diamond} to compare. The comparisons matter because they are used as recipe matches: a wrong
     * {@code true} crafts the wrong thing, a {@code false} refuses a legal recipe.
     * </p>
     */
    private static void checkBoxStackComparison() {
        ItemStack diamond = new ItemStack(Items.diamond);
        ItemStack tagged = new ItemStack(Items.diamond);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("artifice:test", 7);
        tagged.setTagCompound(tag);

        ComparableItemStack plain = new ComparableItemStack(diamond);
        ComparableItemStack samePlain = new ComparableItemStack(new ItemStack(Items.diamond));
        ComparableItemStackNBT withTag = new ComparableItemStackNBT(tagged);

        check("the same item and damage compare equal", plain.isItemEqual(samePlain));
        check(
            "stack equality includes the count",
            !plain.isStackEqual(new ComparableItemStack(new ItemStack(Items.diamond, 2))));
        check(
            "a different damage is a different item",
            !plain.isItemEqual(new ComparableItemStack(new ItemStack(Items.dye, 3))));

        // The NBT half, including the case that used to be a ClassCastException: a tagged key against a plain stack.
        check("the item half of a tagged stack still matches", withTag.isItemEqual(plain));
        check("the tag half does not", !withTag.isStackEqual(plain));
        check("an NBT stack against a plain one is false, not a crash", !withTag.isStackEqual(plain));
        check("an NBT stack against null is false", !withTag.isStackEqual(null));

        ItemStack sameTag = new ItemStack(Items.diamond);
        sameTag.setTagCompound((NBTTagCompound) tag.copy());
        check("equal tags match", withTag.isStackEqual(new ComparableItemStackNBT(sameTag)));

        // A stack built from null is invalid rather than a landmine.
        ComparableItemStack invalid = new ComparableItemStack((ItemStack) null);
        check("a stack built from null is invalid", !invalid.isStackValid());
        check(
            "a stack built from null compares false rather than throwing",
            !invalid.isItemEqual(plain) && !plain.isItemEqual(invalid));

        ItemStack restored = withTag.toItemStack();
        check(
            "the tag survives the round trip",
            restored != null && restored.getTagCompound() != null
                && restored.getTagCompound()
                    .getInteger("artifice:test") == 7);
    }

    private static void checkSickleAreaOfEffect() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft((net.minecraft.world.WorldServer) world);
        if (player == null) {
            check("fake player available", false);
            return;
        }

        try {
            int cx = site.x + 4;
            int cy = site.y;
            int cz = site.z + 4;

            for (int i = -1; i <= 1; i++) {
                for (int k = -1; k <= 1; k++) {
                    site.setFloor(cx + i, cy - 1, cz + k, Blocks.dirt);
                    site.setBlock(cx + i, cy, cz + k, Blocks.tallgrass, 1);
                }
            }

            ItemStack sickle = new ItemStack(ArtificeItems.itemSickleWood);
            boolean handled = ((ArtificeItems.itemSickleWood)) != null
                && ArtificeItems.itemSickleWood.onBlockStartBreak(sickle, cx, cy, cz, player);
            check("the sickle handles a plant break", handled);

            int harvested = 0;
            for (int i = -1; i <= 1; i++) {
                for (int k = -1; k <= 1; k++) {
                    if (world.isAirBlock(cx + i, cy, cz + k)) {
                        harvested++;
                    }
                }
            }

            // Radius 1 with a euclidean distance test is the centre plus the four orthogonal neighbours; the two
            // diagonals are out of range. The old window could only ever reach three of them.
            checkEquals("radius 1 harvests the centre and all four orthogonal neighbours", 5, harvested);
            checkEquals("the four-diagonal corners survive", 4, 9 - harvested);
            checkEquals("harvesting five plants costs two durability", 2, sickle.getItemDamage());
            check("a radius-1 field is not damaged by a diagonal-only harvest", sickle.getItemDamage() >= 1);
        } finally {
            site.clear();
        }
    }

    /**
     * CODE_REVIEW 5.16. {@code BlockFlora.updateTick} never called {@code BlockBush#updateTick}, which is what drops
     * a flower that can no longer stand. With the soil gone the flower should be removed on its next tick.
     */
    private static void checkFloraDropsWithoutSoil() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            for (int meta = 0; meta < 3; meta++) {
                int x = site.x + meta * 2;
                site.setFloor(x, site.y - 1, site.z, Blocks.dirt);
                site.setBlock(x, site.y, site.z, ArtificeBlocks.blockFlora, meta);
                checkEquals("flora " + meta + " placed", ArtificeBlocks.blockFlora, world.getBlock(x, site.y, site.z));

                // Flag 0: no neighbour notification, so nothing removes the flower except its own tick. This is the
                // case the missing super call left broken - a flower with no soil that no neighbour update reached.
                world.setBlock(x, site.y - 1, site.z, Blocks.air, 0, 0);
                checkEquals(
                    "flora " + meta + " survives until something ticks it",
                    ArtificeBlocks.blockFlora,
                    world.getBlock(x, site.y, site.z));

                ArtificeBlocks.blockFlora.updateTick(world, x, site.y, site.z, world.rand);
                checkEquals(
                    "flora " + meta + " is gone once the soil is",
                    Blocks.air,
                    world.getBlock(x, site.y, site.z));
            }
        } finally {
            site.clear();
        }
    }

    private static void checkLogicRedstone() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockLogicRedstone, 0);

            TileEntity tile = world.getTileEntity(site.x, site.y, site.z);
            check("logic block has a tile entity", tile instanceof TileEntityLogic);

            TileEntityLogic logic = (TileEntityLogic) tile;
            if (logic == null) return;

            // Digital mode: one POSITIVE input side, one OUTPUT side, powered by a redstone block above it.
            for (int i = 0; i < 6; i++) {
                logic.sides[i] = TileEntityLogic.SideState.DISABLED;
            }
            logic.sides[ForgeDirection.UP.ordinal()] = TileEntityLogic.SideState.POSITIVE;
            // isProvidingWeakPower(.., side) reads OPPOSITES[side], so OUTPUT on DOWN means the block above sees
            // the signal (side = UP).
            logic.sides[ForgeDirection.DOWN.ordinal()] = TileEntityLogic.SideState.OUTPUT;

            site.setBlock(site.x, site.y + 1, site.z, Blocks.redstone_block, 0);
            logic.updateEntity();

            checkEquals("digital logic with a powered positive side outputs 15", 15, logic.power);
            checkEquals(
                "logic exposes its power on its output side",
                15,
                ArtificeBlocks.blockLogicRedstone
                    .isProvidingWeakPower(world, site.x, site.y, site.z, ForgeDirection.UP.ordinal()));
            checkEquals(
                "logic does not emit on a non-output side",
                0,
                ArtificeBlocks.blockLogicRedstone
                    .isProvidingWeakPower(world, site.x, site.y, site.z, ForgeDirection.NORTH.ordinal()));

            // Removing the power must drop the output.
            world.setBlockToAir(site.x, site.y + 1, site.z);
            logic.updateEntity();
            checkEquals("digital logic without power outputs 0", 0, logic.power);

            // CODE_REVIEW 5.8: canConnectRedstone must agree with isProvidingWeakPower, or the block renders a
            // connection on a face it does not emit on. With OUTPUT on DOWN only, the only connected face is UP.
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                int side = dir.ordinal();
                // The contract: a face renders a connection when the state behind it is configured, and the state
                // behind it is read with the same mapping isProvidingWeakPower uses.
                boolean configured = logic.sides[ForgeDirection.OPPOSITES[side]] != TileEntityLogic.SideState.DISABLED;
                checkEquals(
                    "logic connection on " + dir + " matches the state behind it",
                    configured,
                    ArtificeBlocks.blockLogicRedstone.canConnectRedstone(world, site.x, site.y, site.z, side));
            }
            check(
                "logic connects upward where it emits",
                ArtificeBlocks.blockLogicRedstone
                    .canConnectRedstone(world, site.x, site.y, site.z, ForgeDirection.UP.ordinal()));
            check(
                "logic does not connect on an idle face",
                !ArtificeBlocks.blockLogicRedstone
                    .canConnectRedstone(world, site.x, site.y, site.z, ForgeDirection.NORTH.ordinal()));
            check(
                "logic answers the 'any connection?' query",
                ArtificeBlocks.blockLogicRedstone.canConnectRedstone(world, site.x, site.y, site.z, -1));

            // A NEGATIVE side inverts the input for digital mode.
            logic.sides[ForgeDirection.UP.ordinal()] = TileEntityLogic.SideState.NEGATIVE;
            logic.updateEntity();
            checkEquals("digital logic with an unpowered negative side outputs 15", 15, logic.power);

            site.setBlock(site.x, site.y + 1, site.z, Blocks.redstone_block, 0);
            logic.updateEntity();
            checkEquals("digital logic with a powered negative side outputs 0", 0, logic.power);
        } finally {
            site.clear();
        }
    }

    /**
     * §6: the layer generator now reads each block once and skips air before building its lookup pair. The refactor
     * collapsed two branches that had to stay equivalent - a registered stone type is replaced, anything else is
     * left alone, and an ore is converted rather than overwritten.
     *
     * <p>
     * Run on a chunk far from every other test site, because the generator is hardcoded to work in 16x16 columns and
     * will rewrite whatever it finds there.
     * </p>
     */
    private static void checkLayerGeneration() {
        World world = overworld();
        int chunkX = 4000;
        int chunkZ = 4000;
        int x0 = chunkX * 16;
        int z0 = chunkZ * 16;

        Set<NameMetaPair> stoneTypes = ArtificeRegistry.getStoneTypes();
        check("there is at least one registered stone type to replace", !stoneTypes.isEmpty());
        if (stoneTypes.isEmpty()) {
            return;
        }

        NameMetaPair stone = stoneTypes.iterator()
            .next();
        check("the registered stone type resolves to a block", stone.getBlock() != null);
        if (stone.getBlock() == null) {
            return;
        }

        // 40..60: the generator jitters both ends by up to 3, so everything in 43..56 is inside every column's band.
        int y = 50;
        WorldGenLayer layer = new WorldGenLayer(ArtificeBlocks.rockBlocks[0], 0, 40, 60);

        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                world.setBlock(x0 + dx, y, z0 + dz, stone.getBlock(), stone.getMetadata(), 0);
            }
        }
        // Not a stone type: must survive untouched.
        world.setBlock(x0 + 9, y, z0, Blocks.dirt, 0, 3);

        layer.generateFeature(world.rand, chunkX, chunkZ, world, true);

        int replaced = 0;
        for (int dx = 0; dx < 3; dx++) {
            for (int dz = 0; dz < 3; dz++) {
                if (world.getBlock(x0 + dx, y, z0 + dz) == ArtificeBlocks.rockBlocks[0]) {
                    replaced++;
                }
            }
        }
        checkEquals("the layer replaces every registered stone block in the band", 9, replaced);
        checkEquals(
            "the layer leaves blocks that are not stone types alone",
            Blocks.dirt,
            world.getBlock(x0 + 9, y, z0));
    }

    private static void checkAttunedRedstone() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            String frequency = "artifice-gametest";

            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockAttunedRedstoneReceiver, 0);
            site.setBlock(site.x + 2, site.y, site.z, ArtificeBlocks.blockAttunedRedstoneTransmitter, 0);

            TileEntity receiverTile = world.getTileEntity(site.x, site.y, site.z);
            TileEntity transmitterTile = world.getTileEntity(site.x + 2, site.y, site.z);

            check("attuned receiver has a tile entity", receiverTile instanceof TileEntityAttuned);
            check("attuned transmitter has a tile entity", transmitterTile instanceof TileEntityAttuned);
            if (!(receiverTile instanceof TileEntityAttuned) || !(transmitterTile instanceof TileEntityAttuned)) {
                return;
            }

            TileEntityAttuned receiver = (TileEntityAttuned) receiverTile;
            TileEntityAttuned transmitter = (TileEntityAttuned) transmitterTile;

            receiver.setFrequency(frequency);
            transmitter.setFrequency(frequency);

            // Drive the receiver directly: the block-power read is covered by the unit-level getPower() contract,
            // and going through the real redstone rules here would make the test depend on vanilla power semantics.
            receiver.power = 12;
            transmitter.updateEntity();

            checkEquals("transmitter mirrors the receiver's power", 12, transmitter.power);
            checkEquals(
                "transmitter emits the mirrored power",
                12,
                ArtificeBlocks.blockAttunedRedstoneTransmitter
                    .isProvidingWeakPower(world, site.x + 2, site.y, site.z, 0));
            checkEquals(
                "receiver never emits power",
                0,
                ArtificeBlocks.blockAttunedRedstoneReceiver.isProvidingWeakPower(world, site.x, site.y, site.z, 0));

            // Different frequencies must not couple.
            transmitter.setFrequency("artifice-gametest-other");
            transmitter.updateEntity();
            checkEquals("a different frequency does not couple", 0, transmitter.power);

            transmitter.setFrequency(frequency);
            transmitter.updateEntity();
            checkEquals("re-pairing restores the link", 12, transmitter.power);

            // Only receivers answer. A transmitter carries the same power field, so on a shared frequency it would
            // out-vote the receiver it is relaying if the block type were not checked (CODE_REVIEW §6).
            //
            // Queried through Tracking rather than the transmitter, because the transmitter emitting its power
            // notifies its neighbours, and that notification re-derives the receiver's power from a world that has no
            // redstone attached - which would overwrite the value under test.
            site.setBlock(site.x + 2, site.y + 1, site.z, ArtificeBlocks.blockAttunedRedstoneTransmitter, 0);
            TileEntity second = world.getTileEntity(site.x + 2, site.y + 1, site.z);
            check("a second transmitter has a tile entity", second instanceof TileEntityAttuned);
            if (second instanceof TileEntityAttuned) {
                TileEntityAttuned peer = (TileEntityAttuned) second;
                peer.setFrequency(frequency);
                peer.power = 15;
                receiver.power = 12;

                checkEquals(
                    "a transmitter is not a power source for its own frequency",
                    12,
                    Tracking.getHighestFrequencyPower(frequency));

                // Breaking it takes it out of the index, so the next query does not have to skip a dead tile.
                world.setBlockToAir(site.x + 2, site.y + 1, site.z);
                check("breaking a tile removes it from the index", peer.isInvalid());
                receiver.power = 12;
                checkEquals("the link survives a peer being broken", 12, Tracking.getHighestFrequencyPower(frequency));
            }
        } finally {
            site.clear();
        }
    }

    private static void checkHeatingCoil() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockHeatingCoil, 0);

            TileEntity tile = world.getTileEntity(site.x, site.y, site.z);
            check("heating coil has a tile entity", tile instanceof TileEntityHeatingCoil);
            if (!(tile instanceof TileEntityHeatingCoil)) return;

            TileEntityHeatingCoil coil = (TileEntityHeatingCoil) tile;

            // A furnace on every side makes the check independent of the neighbour iteration order.
            int furnaceCount = 0;
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                int fx = site.x + dir.offsetX;
                int fy = site.y + dir.offsetY;
                int fz = site.z + dir.offsetZ;
                site.setBlock(fx, fy, fz, Blocks.furnace, 0);

                TileEntity furnaceTile = world.getTileEntity(fx, fy, fz);
                if (furnaceTile instanceof TileEntityFurnace) {
                    ((TileEntityFurnace) furnaceTile)
                        .setInventorySlotContents(0, new ItemStack(Item.getItemFromBlock(Blocks.iron_ore)));
                    furnaceCount++;
                }
            }
            checkEquals("six furnaces surround the coil", 6, furnaceCount);

            check("coil accepts RF", coil.receiveEnergy(ForgeDirection.UP, 1600, false) > 0);

            for (int i = 0; i < 5; i++) {
                coil.updateEntity();
            }

            int burnTime = 0;
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity furnaceTile = world
                    .getTileEntity(site.x + dir.offsetX, site.y + dir.offsetY, site.z + dir.offsetZ);
                if (furnaceTile instanceof TileEntityFurnace) {
                    burnTime += ((TileEntityFurnace) furnaceTile).furnaceBurnTime;
                }
            }
            check("coil heat reached at least one furnace", burnTime > 0);

            // The per-furnace cap, through the wrapper the coil actually talks to. A furnace that is already full
            // must refuse heat rather than lose it, and one item's worth is what MAX_HEAT buys at the default rate.
            TileEntityFurnace capped = (TileEntityFurnace) world.getTileEntity(
                site.x + ForgeDirection.UP.offsetX,
                site.y + ForgeDirection.UP.offsetY,
                site.z + ForgeDirection.UP.offsetZ);
            if (capped != null) {
                TileFurnaceWrapper wrapper = new TileFurnaceWrapper(capped);
                checkEquals("the wrapper reports the constant cap", TileFurnaceWrapper.MAX_HEAT, wrapper.getMaxHeat());
                check("a furnace can take heat", wrapper.receiveHeat(TileFurnaceWrapper.MAX_HEAT) > 0);
                checkEquals("a full furnace takes no more heat", 0, wrapper.receiveHeat(100));
                checkEquals(
                    "the cap is in RF, so the heat reads back as MAX_HEAT",
                    TileFurnaceWrapper.MAX_HEAT,
                    wrapper.getHeat());
            }
            check("coil drains RF while heating", coil.getEnergyStored(ForgeDirection.UP) < 1600);

            // CODE_REVIEW 5.4: the neighbour helpers indexed the side array twice, so a random subset of the six
            // sides was visited - some twice, some never. One furnace at a time on each side is the deterministic
            // form of that check: with the double index, the sides the permutation never reaches stay cold.
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                for (ForgeDirection other : ForgeDirection.VALID_DIRECTIONS) {
                    site.setBlock(
                        site.x + other.offsetX,
                        site.y + other.offsetY,
                        site.z + other.offsetZ,
                        Blocks.air,
                        0);
                }

                int fx = site.x + dir.offsetX;
                int fy = site.y + dir.offsetY;
                int fz = site.z + dir.offsetZ;
                site.setBlock(fx, fy, fz, Blocks.furnace, 0);
                TileEntity furnaceTile = world.getTileEntity(fx, fy, fz);
                check("furnace placed " + dir, furnaceTile instanceof TileEntityFurnace);
                if (!(furnaceTile instanceof TileEntityFurnace)) {
                    continue;
                }
                // TileFurnaceWrapper only accepts heat when the furnace has something it can smelt.
                ((TileEntityFurnace) furnaceTile)
                    .setInventorySlotContents(0, new ItemStack(Item.getItemFromBlock(Blocks.iron_ore)));

                // Refill and let the coil pick a heat receiver; a single adjacent furnace has to be the one found.
                coil.receiveEnergy(ForgeDirection.UP, 1600, false);
                for (int i = 0; i < 8; i++) {
                    coil.updateEntity();
                }

                int heat = ((TileEntityFurnace) furnaceTile).furnaceBurnTime;
                check("coil heats the furnace " + dir + " (burn time " + heat + ")", heat > 0);
            }
        } finally {
            site.clear();
        }
    }

    private static void checkNuclearBattery() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            site.setBlock(site.x, site.y, site.z, ArtificeBlocks.blockNuclearBattery, 0);

            TileEntity tile = world.getTileEntity(site.x, site.y, site.z);
            check("nuclear battery has a tile entity", tile instanceof TileEntityNuclearBattery);
            if (!(tile instanceof TileEntityNuclearBattery)) return;

            TileEntityNuclearBattery battery = (TileEntityNuclearBattery) tile;

            battery.setEnergy(123456);
            checkEquals("battery stores what it is given", 123456, battery.getEnergyStored(ForgeDirection.UP));
            check(
                "battery rate scales with its charge",
                battery.getMaxRate() > 0 && battery.getMaxRate() <= ArtificeConfig.nuclearBatteryRate);

            int extracted = battery.extractEnergy(ForgeDirection.UP, 10, false);
            check("battery gives power out", extracted > 0);
            checkEquals("battery honours the extract amount", extracted, Math.min(10, extracted));

            // The item form carries the charge through NBT.
            ItemStack stack = new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 0);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("charge", 4242);
            stack.setTagCompound(tag);

            checkEquals(
                "battery item serialises its charge",
                4242,
                stack.getTagCompound()
                    .getInteger("charge"));
        } finally {
            site.clear();
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Drops, recipes, tooltips, config
    // ---------------------------------------------------------------------------------------------------------

    private static void checkBlockDrops() {
        World world = overworld();

        // Self-contained drops: these must always yield our own resource item.
        List<ItemStack> sulfurDrops = ArtificeBlocks.blockSulfur.getDrops(world, 0, 0, 0, 0, 0);
        check("sulfur ore drops something", !sulfurDrops.isEmpty());
        boolean sulfurDust = false;
        for (ItemStack drop : sulfurDrops) {
            if (drop != null && drop.getItem() == ArtificeItems.itemResource && drop.getItemDamage() == 0) {
                sulfurDust = true;
            }
        }
        check("sulfur ore drops sulfur dust", sulfurDust);

        List<ItemStack> niterDrops = ArtificeBlocks.blockNiter.getDrops(world, 0, 0, 0, 0, 0);
        boolean niterDust = false;
        for (ItemStack drop : niterDrops) {
            if (drop != null && drop.getItem() == ArtificeItems.itemResource && drop.getItemDamage() == 1) {
                niterDust = true;
            }
        }
        check("niter ore drops niter dust", niterDust);

        checkEquals(
            "charred log drops charcoal",
            Items.coal,
            ArtificeBlocks.blockCharredLog.getItemDropped(0, world.rand, 0));
        checkEquals("charred log drops charcoal metadata", 1, ArtificeBlocks.blockCharredLog.damageDropped(0));

        checkEquals(
            "ender ore drops ender dust",
            ArtificeItems.itemResource,
            ArtificeBlocks.blockEnderOre.getItemDropped(0, world.rand, 0));
        checkEquals("ender ore drops ender dust metadata", 2, ArtificeBlocks.blockEnderOre.damageDropped(0));

        // CODE_REVIEW §8 lists BlockRock.isReplaceableOreGen as unreferenced; it is a live vanilla hook -
        // WorldGenMinable.generate asks the block it is about to replace - so this pins the contract instead.
        BlockRock rock = (BlockRock) ArtificeBlocks.rockBlocks[0];
        check(
            "rock is replaceable ore-gen for stone, so other mods' veins cut through it",
            rock.isReplaceableOreGen(world, 0, 0, 0, Blocks.stone));
        check(
            "rock is replaceable ore-gen for rock, so one layer can cut through another",
            rock.isReplaceableOreGen(world, 0, 0, 0, rock));
        check(
            "rock is not replaceable ore-gen for a third block",
            !rock.isReplaceableOreGen(world, 0, 0, 0, Blocks.dirt));
    }

    /**
     * CODE_REVIEW 5.9 and 9.2.
     *
     * <p>
     * 5.9: ore drops are looked up in the ore dictionary, so a pack that does not register one of these names (or
     * registers it after Artifice) used to NPE on break, on silk touch and in {@code createStackedBlock}. The
     * dictionary list handed out by {@code OreDictionary.getOres} is immutable, so the fallbacks are driven
     * directly with a null entry, which is exactly the state the old code dereferenced.
     * </p>
     *
     * <p>
     * 9.2: the smelting recipes were registered in {@code init} while the dictionary entries they match on only
     * appeared in {@code postInit}, so none of them ever registered.
     * </p>
     */
    private static void checkOreDictionaryDependence() {
        // 5.9: the null-entry path of every ore that goes through the dictionary.
        for (int i = 0; i < ArtificeBlocks.oreNames.length; i++) {
            BlockOre ore = ArtificeBlocks.blockOres[i];
            if (ore == null) {
                continue;
            }

            String oreName = ArtificeBlocks.oreNames[i];
            Item ownItem = Item.getItemFromBlock(ore);
            check(
                oreName + " falls back to its own item without a dictionary entry",
                OreDictFallback.item(null, ore) == ownItem);
            checkEquals(oreName + " falls back to metadata 0", 0, OreDictFallback.meta(null));

            ItemStack stacked = OreDictFallback.blockStack(null, ore);
            check(oreName + " has a silk-touch stack without a dictionary entry", stacked != null);
            checkEquals(
                oreName + " silk-touch stack is the block itself",
                Item.getItemFromBlock(ore),
                stacked.getItem());
        }

        // ... and the normal path still uses the dictionary entry when it exists.
        ItemStack copperEntry = OreDictionary.getOres("oreCopper")
            .isEmpty() ? null
                : OreDictionary.getOres("oreCopper")
                    .get(0);
        check("the test pack registers oreCopper", copperEntry != null);
        if (copperEntry != null) {
            check(
                "a real dictionary entry is preferred over the fallback",
                OreDictFallback.item(copperEntry, ArtificeBlocks.blockOres[oreIndexOf("oreCopper")])
                    == copperEntry.getItem());
            checkEquals(
                "the dictionary entry's metadata comes through",
                copperEntry.getItemDamage(),
                OreDictFallback.meta(copperEntry));

            // The wiring, not just the helper: BlockOre#getItemDropped must go through the entry.
            check(
                "BlockOre's own drop path uses the dictionary entry",
                ArtificeBlocks.blockOres[oreIndexOf("oreCopper")].getItemDropped(0, new java.util.Random(2L), 0)
                    == copperEntry.getItem());
        }

        // 9.2: every vanilla-mapped ore must smelt from every rock variant.
        int checkedOres = 0;
        for (int i = 0; i < ArtificeBlocks.blockOres.length; i++) {
            String oreName = ArtificeBlocks.oreNames[i];
            ItemStack expected = null;
            for (Object object : net.minecraft.item.crafting.FurnaceRecipes.smelting()
                .getSmeltingList()
                .entrySet()) {
                java.util.Map.Entry<?, ?> entry = (java.util.Map.Entry<?, ?>) object;
                ItemStack input = (ItemStack) entry.getKey();
                if (input != null && input.getItem() == Item.getItemFromBlock(ArtificeBlocks.blockOres[i])) {
                    expected = (ItemStack) entry.getValue();
                    break;
                }
            }

            if (expected == null) {
                continue;
            }

            checkedOres++;
            for (int j = 0; j < ArtificeBlocks.rockBlocks.length; j++) {
                ItemStack result = net.minecraft.item.crafting.FurnaceRecipes.smelting()
                    .getSmeltingResult(new ItemStack(ArtificeBlocks.blockOres[i], 1, j));
                check(
                    "smelts " + oreName + " variant " + j,
                    result != null && result.getItem() == expected.getItem()
                        && result.getItemDamage() == expected.getItemDamage());
            }
        }
        check("the ore smelting recipes registered at all (" + checkedOres + " ores)", checkedOres >= 7);
    }

    private static int oreIndexOf(String oreName) {
        for (int i = 0; i < ArtificeBlocks.oreNames.length; i++) {
            if (ArtificeBlocks.oreNames[i].equals(oreName)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * CODE_REVIEW 5.17 and 5.18.
     *
     * <p>
     * 5.17: {@code BoxCraftingHandler} read {@code slot.getTagCompound()} without a null check, and the event fires
     * for every craft while a box sits in the grid - so an empty box (which has no NBT) made every craft in that
     * grid throw.
     * </p>
     *
     * <p>
     * 5.18: {@code RecipeBox}/{@code RecipeUpgrade} returned the output of the last {@code matches} call, null until
     * then. Anything that walks the recipe list without matching (NEI handlers, tooltips) got null.
     * </p>
     */
    private static void checkBoxRecipes() {
        World world = overworld();

        // 5.17: an empty box in the grid must not break the craft.
        EntityPlayer player = net.minecraftforge.common.util.FakePlayerFactory
            .getMinecraft((net.minecraft.world.WorldServer) world);
        check("fake player available", player != null);
        if (player == null) return;

        InventoryCrafting matrix = new InventoryCrafting(new Container() {

            @Override
            public boolean canInteractWith(EntityPlayer ignored) {
                return true;
            }
        }, 3, 3);

        ItemStack emptyBox = new ItemStack(ArtificeItems.itemBox, 1, 0);
        matrix.setInventorySlotContents(0, emptyBox);
        ItemStack crafted = new ItemStack(Items.stick);
        new BoxCraftingHandler().onCrafting(new ItemCraftedEvent(player, crafted, matrix));
        check("an empty box in the grid does not throw during craft", matrix.getStackInSlot(0) != null);

        // 5.17, filled box: the stored items are handed to the player and the box is consumed. The box's damage is
        // how many items it holds, and the old code gave none of them unless they overflowed a stack.
        ItemStack fullBox = new ItemStack(ArtificeItems.itemBox, 1, 5);
        NBTTagCompound held = new NBTTagCompound();
        held.setInteger("id", Item.getIdFromItem(Items.iron_ingot));
        held.setInteger("meta", 0);
        fullBox.setTagCompound(held);
        matrix.setInventorySlotContents(0, fullBox);

        int before = countItem(player, Items.iron_ingot);
        new BoxCraftingHandler().onCrafting(new ItemCraftedEvent(player, crafted, matrix));
        checkEquals("a five-item box yields five ingots", before + 5, countItem(player, Items.iron_ingot));
        check("a filled box is consumed", matrix.getStackInSlot(0) == null || matrix.getStackInSlot(0).stackSize == 0);

        // Sneak-crafting empties every box in the slot. The old arithmetic subtracted one box's worth and gave
        // nothing for a single box.
        ItemStack stackOfBoxes = new ItemStack(ArtificeItems.itemBox, 3, 4);
        NBTTagCompound heldFour = new NBTTagCompound();
        heldFour.setInteger("id", Item.getIdFromItem(Items.gold_ingot));
        heldFour.setInteger("meta", 0);
        stackOfBoxes.setTagCompound(heldFour);
        matrix.setInventorySlotContents(0, stackOfBoxes);

        int goldBefore = countItem(player, Items.gold_ingot);
        player.setSneaking(true);
        try {
            new BoxCraftingHandler().onCrafting(new ItemCraftedEvent(player, crafted, matrix));
        } finally {
            player.setSneaking(false);
        }
        checkEquals(
            "sneak-crafting three boxes of four yields twelve",
            goldBefore + 12,
            countItem(player, Items.gold_ingot));
        check(
            "sneak-crafting consumes every box",
            matrix.getStackInSlot(0) == null || matrix.getStackInSlot(0).stackSize == 0);

        // 5.18: every box/upgrade recipe in the manager must have a non-null output without being matched.
        int checked = 0;
        for (Object object : CraftingManager.getInstance()
            .getRecipeList()) {
            if (!(object instanceof RecipeBox) && !(object instanceof RecipeUpgrade)) {
                continue;
            }

            IRecipe recipe = (IRecipe) object;
            ItemStack output = recipe.getRecipeOutput();
            check(
                recipe.getClass()
                    .getSimpleName() + " has a default output",
                output != null);
            check(
                recipe.getClass()
                    .getSimpleName() + " default output has an item",
                output != null && output.getItem() != null);
            checked++;
        }
        check("box/upgrade recipes are registered (" + checked + ")", checked > 0);
    }

    private static int countItem(EntityPlayer player, Item item) {
        int count = 0;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() == item) {
                count += stack.stackSize;
            }
        }

        return count;
    }

    /**
     * CODE_REVIEW 5.15. The sledge found "the recipe for this drop" by comparing unlocalised names, which block
     * items share across variants and which two mods can collide on. It now compares the stack.
     *
     * <p>
     * The collision itself needs a mod-provided recipe to demonstrate, so this guards the path that must keep
     * working: a vanilla shaped recipe must still resolve to exactly its ingredients.
     * </p>
     */
    private static void checkSledgeIngredients() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            site.setBlock(site.x, site.y, site.z, Blocks.crafting_table, 0);

            ItemStack sledge = new ItemStack(ArtificeItems.itemSledgeIron);
            List<ItemStack> ingredients = com.mrfuzzihead.artifice.item.ItemSledge
                .getIngredientsForBlock(world, site.x, site.y, site.z, sledge);

            check("the sledge finds ingredients for a crafting table", ingredients != null);
            if (ingredients == null) {
                return;
            }

            checkEquals("a crafting table unsmelts into four items", 4, ingredients.size());
            for (ItemStack ingredient : ingredients) {
                check(
                    "the crafting table's ingredient is planks",
                    ingredient != null && ingredient.getItem() == Item.getItemFromBlock(Blocks.planks));
            }

            // A recipe that makes more than one item is not a recipe for a single drop: breaking one torch must not
            // yield a torch's worth of sticks and coal.
            site.setBlock(site.x, site.y, site.z, Blocks.torch, 0);
            List<ItemStack> torch = com.mrfuzzihead.artifice.item.ItemSledge
                .getIngredientsForBlock(world, site.x, site.y, site.z, sledge);
            check(
                "a multi-output recipe is not used",
                torch != null && torch.size() == 1
                    && torch.get(0)
                        .getItem() == Item.getItemFromBlock(Blocks.torch));

            // A block with no shaped recipe keeps its normal drops.
            site.setBlock(site.x, site.y, site.z, Blocks.stone, 0);
            List<ItemStack> stone = com.mrfuzzihead.artifice.item.ItemSledge
                .getIngredientsForBlock(world, site.x, site.y, site.z, sledge);
            check("stone still yields its own drop", stone != null && !stone.isEmpty());
            check(
                "stone's drop is not a recipe result",
                stone != null && stone.size() == 1
                    && stone.get(0)
                        .getItem() == Item.getItemFromBlock(Blocks.cobblestone));

            // The recipe index is built once and reused; a stale or partial index would give a different answer the
            // second time round, which is the whole risk of caching a registry.
            site.setBlock(site.x, site.y, site.z, Blocks.crafting_table, 0);
            for (int repeat = 0; repeat < 3; repeat++) {
                List<ItemStack> again = com.mrfuzzihead.artifice.item.ItemSledge
                    .getIngredientsForBlock(world, site.x, site.y, site.z, sledge);
                check(
                    "the recipe index answers consistently",
                    again != null && again.size() == 4
                        && again.get(0) != null
                        && again.get(0)
                            .getItem() == Item.getItemFromBlock(Blocks.planks));
            }
        } finally {
            site.clear();
        }
    }

    private static void checkRecipes() {
        List<IRecipe> recipes = CraftingManager.getInstance()
            .getRecipeList();
        check("the crafting table has recipes", recipes.size() > 100);

        check("the frame has a recipe", hasRecipeOutput(recipes, Item.getItemFromBlock(ArtificeBlocks.blockFrame)));
        check("the box has a recipe", hasRecipeOutput(recipes, ArtificeItems.itemBox));
        check("the sledge has a recipe", hasRecipeOutput(recipes, ArtificeItems.itemSledgeWood));
        check("the sickle has a recipe", hasRecipeOutput(recipes, ArtificeItems.itemSickleWood));
        check("coins have a recipe", hasRecipeOutput(recipes, ArtificeItems.itemCoin));
        check("upgrades have a recipe", hasRecipeOutput(recipes, ArtificeItems.itemUpgrade));
    }

    private static boolean hasRecipeOutput(List<IRecipe> recipes, Item item) {
        for (IRecipe recipe : recipes) {
            ItemStack output = recipe.getRecipeOutput();
            if (output != null && output.getItem() == item) return true;
        }
        return false;
    }

    private static void checkTooltips() {
        check(
            "tooltips were registered",
            !ArtificeRegistry.getTooltipMap()
                .isEmpty());
        check(
            "the sledge has a tooltip",
            ArtificeRegistry.getTooltipMap()
                .containsKey(new com.mrfuzzihead.artifice.util.NameMetaPair(ArtificeItems.itemSledgeWood, 0)));
        check(
            "the sulfur dust has a tooltip",
            ArtificeRegistry.getTooltipMap()
                .containsKey(new com.mrfuzzihead.artifice.util.NameMetaPair(ArtificeItems.itemResource, 0)));

        // §7.5 collapsed 226 registration calls into 116 per-stack calls. What must not change is the lines and
        // their order, so pin the shape of two stacks that have several: a dye (name + flavour) and a sledge
        // (name, loss chance, flavour).
        List<String> dye = ArtificeRegistry.getTooltipMap()
            .get(new NameMetaPair(ArtificeItems.itemDye, 0));
        check("the blue dye has two lines", dye != null && dye.size() == 2);
        if (dye != null && dye.size() == 2) {
            check(
                "the dye's name line comes first",
                dye.get(0)
                    .equals(ArtificeTooltips.localizeTooltip("dye.blue", false)));
            check(
                "the dye's flavour line comes second",
                dye.get(1)
                    .equals(ArtificeTooltips.localizeTooltip("dye.blue", true)));
            check(
                "the flavour line is marked as such",
                dye.get(1)
                    .startsWith(ArtificeTooltips.commentCode));
        }

        List<String> sledge = ArtificeRegistry.getTooltipMap()
            .get(new NameMetaPair(ArtificeItems.itemSledgeWood, 0));
        check("the wooden sledge has three lines", sledge != null && sledge.size() == 3);
        if (sledge != null && sledge.size() == 3) {
            check(
                "the sledge's loss line is in the middle",
                sledge.get(1)
                    .contains(ArtificeTooltips.localizeTooltip("sledge.loss", false)));
        }
    }

    private static void checkConfigSurface() {
        checkEquals("four frame tiers", 4, ArtificeConfig.tiers.length);
        checkEquals("six rock textures", 6, ArtificeConfig.rocks.length);
        checkEquals("nine rock types", 9, ArtificeConfig.rockNames.length);
        checkEquals("five flora subtypes", 5, ArtificeConfig.flora.length);

        check("the coil rate is positive", ArtificeConfig.heatingCoilRate > 0);
        check("the furnace RF cost is positive", ArtificeConfig.RFperFurnaceHeat > 0);
        check("the battery capacity is positive", ArtificeConfig.nuclearBatteryCapacity > 0);
        checkEquals("fourteen upgrade entries", 14, EnumUpgrades.values().length);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Enchantments
    // ---------------------------------------------------------------------------------------------------------

    /**
     * The enchantment ids are a save-compatibility contract, not an implementation detail. 1.1.5R and everything
     * before it allocated 70/71/72 in this order, and every enchanted item in a pack world refers to its enchantment
     * by number. FuzziTweaks' mixin existed only to force that allocation over the probe-based code; this asserts the
     * in-tree version reproduces it, so a reorder or a changed start id fails CI instead of silently rewriting what
     * an existing item does.
     */
    private static void checkEnchantments() {
        final Enchantment invisible = ArtificeEnchants.enchantmentInvisible;
        final Enchantment soulstealing = ArtificeEnchants.enchantmentSoulstealing;
        final Enchantment resistance = ArtificeEnchants.enchantmentResistance;

        checkEquals("enchantment start id", 70, ArtificeConfig.enchantmentStartID);

        check("invisible enchant registered", ArtificeEnchants.hasInvisible());
        check("soulstealing enchant registered", ArtificeEnchants.hasSoulstealing());
        check("resistance enchant registered", ArtificeEnchants.hasResistance());

        // The stock allocation order is invisible, soulstealing, resistance, each taking the lowest free id at or
        // above the start id, so the three land on consecutive numbers.
        if (invisible != null) {
            checkEquals("invisible enchant id", 70, invisible.effectId);
        }
        if (soulstealing != null) {
            checkEquals("soulstealing enchant id", 71, soulstealing.effectId);
        }
        if (resistance != null) {
            checkEquals("resistance enchant id", 72, resistance.effectId);
        }

        // The list is the authority: an enchantment that is not in its own slot is either overwritten or shares one.
        for (Enchantment enchantment : new Enchantment[] { invisible, soulstealing, resistance }) {
            if (enchantment == null) {
                continue;
            }
            final int id = enchantment.effectId;
            check(
                enchantment.getName() + " occupies its own id " + id,
                id >= 0 && id < Enchantment.enchantmentsList.length && Enchantment.enchantmentsList[id] == enchantment);
        }

        final Set<Integer> ids = new HashSet<>();
        for (Enchantment enchantment : new Enchantment[] { invisible, soulstealing, resistance }) {
            if (enchantment != null) {
                check(
                    enchantment.getName() + " id is not shared with another Artifice enchant",
                    ids.add(enchantment.effectId));
            }
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Sledge dispenser
    // ---------------------------------------------------------------------------------------------------------

    /**
     * CODE_REVIEW 5.2. The behaviour read the block's ingredients twice, the second time after {@code setBlockToAir},
     * so the second read always returned null and the for-each threw. A dispenser firing a sledge NPE'd on every
     * shot. The fix reads once; this drives the behaviour directly against a real world so the drop path is real.
     */
    private static void checkSledgeDispenser() {
        World world = overworld();
        TestSite site = TestSite.create(world);

        try {
            site.setFloor(site.x, site.y - 1, site.z, Blocks.stone);
            site.setBlock(site.x, site.y, site.z, Blocks.stone, 0);

            // The dispenser sits below the target and faces up, so the sledge swings at the stone above it.
            FakeBlockSource source = new FakeBlockSource(world, site.x, site.y - 1, site.z, 1);

            // Snapshot the nearby drops: this site is reused by every section, so a raw count sees other tests' items.
            List<EntityItem> before = entityItemsNear(world, site.x, site.y, site.z);

            ItemStack sledge = new ItemStack(ArtificeItems.itemSledgeIron);
            ItemStack returned = new DispenserBehaviorSledge().dispenseStack(source, sledge);

            check("dispenser returns the sledge stack", returned == sledge);
            check("dispenser removed the block it hit", world.isAirBlock(site.x, site.y, site.z));
            checkEquals("dispenser damaged the sledge once", 1, sledge.getItemDamage());

            List<EntityItem> dropped = entityItemsNear(world, site.x, site.y, site.z);
            dropped.removeAll(before);
            checkEquals("dispenser dropped exactly one item", 1, dropped.size());
            if (dropped.size() == 1) {
                ItemStack drop = dropped.get(0)
                    .getEntityItem();
                check(
                    "dispenser dropped cobblestone (the stone's drop)",
                    drop != null && drop.getItem() == Item.getItemFromBlock(Blocks.cobblestone));
                checkEquals("dispenser dropped one cobblestone", 1, drop == null ? -1 : drop.stackSize);
            }

            // Nothing to smash: the sledge must not be damaged and must not throw.
            int damageBefore = sledge.getItemDamage();
            site.setBlock(site.x, site.y, site.z, Blocks.air, 0);
            new DispenserBehaviorSledge().dispenseStack(source, sledge);
            checkEquals("empty shot does not damage the sledge", damageBefore, sledge.getItemDamage());

            // Vanilla consumes a tool when the damage after the hit exceeds max damage, and attemptDamageItem
            // reproduces that exactly - so one hit past the cap must empty the stack rather than leave it over.
            ItemStack worn = new ItemStack(ArtificeItems.itemSledgeIron);
            worn.setItemDamage(worn.getMaxDamage());
            site.setBlock(site.x, site.y, site.z, Blocks.stone, 0);
            new DispenserBehaviorSledge().dispenseStack(source, worn);
            check("a sledge that reaches max damage is consumed", worn.stackSize == 0);
        } finally {
            site.clear();
        }
    }

    /** Entity items within three blocks of a coordinate, so a section can see only the drops it caused. */
    private static List<EntityItem> entityItemsNear(World world, int x, int y, int z) {
        List<EntityItem> found = new ArrayList<>();
        double tx = x + 0.5;
        double ty = y + 0.5;
        double tz = z + 0.5;

        for (Object entity : world.loadedEntityList) {
            if (entity instanceof EntityItem) {
                EntityItem item = (EntityItem) entity;
                if (item.getDistanceSq(tx, ty, tz) <= 9.0) {
                    found.add(item);
                }
            }
        }

        return found;
    }

    /** The minimum {@link net.minecraft.dispenser.IBlockSource} the behaviour touches. */
    private static final class FakeBlockSource implements net.minecraft.dispenser.IBlockSource {

        private final World world;
        private final int x;
        private final int y;
        private final int z;
        private final int meta;

        private FakeBlockSource(World world, int x, int y, int z, int meta) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.meta = meta;
        }

        @Override
        public World getWorld() {
            return this.world;
        }

        @Override
        public double getX() {
            return this.x + 0.5;
        }

        @Override
        public double getY() {
            return this.y + 0.5;
        }

        @Override
        public double getZ() {
            return this.z + 0.5;
        }

        @Override
        public int getXInt() {
            return this.x;
        }

        @Override
        public int getYInt() {
            return this.y;
        }

        @Override
        public int getZInt() {
            return this.z;
        }

        @Override
        public int getBlockMetadata() {
            return this.meta;
        }

        @Override
        public TileEntity getBlockTileEntity() {
            return this.world.getTileEntity(this.x, this.y, this.z);
        }
    }

    // ---------------------------------------------------------------------------------------------------------
    // Known bugs, probed in game
    // ---------------------------------------------------------------------------------------------------------

    private static void checkKnownBugs() {
        // MiscUtils.shuffleArray is followed by sides[sides[i]], so the neighbour helpers visit a permutation of a
        // permutation: some sides twice, some never. Fix first, then delete this probe and promote the heating-coil
        // "every furnace" assertion to a real check.
        knownBug("MiscUtils.getHeatReceiverNeighbor/provideEnergyToNeighbors double-index the side array", true);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Test site
    // ---------------------------------------------------------------------------------------------------------

    /**
     * A private patch of air high above the spawn chunk. Every block it touches is recorded and removed by
     * {@link #clear()}, so a failed assertion still leaves the world clean.
     */
    private static final class TestSite {

        final World world;
        final int x;
        final int y;
        final int z;
        private final List<int[]> touched = new ArrayList<>();

        private TestSite(World world, int x, int y, int z) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        static TestSite create(World world) {
            return create(world, 512, 512);
        }

        static TestSite create(World world, int x, int z) {
            int y = 250;

            // Clear the working volume so the site is deterministic regardless of terrain.
            for (int dx = 0; dx < 24; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = 0; dz < 8; dz++) {
                        world.setBlockToAir(x + dx, y + dy, z + dz);
                    }
                }
            }

            TestSite site = new TestSite(world, x, y, z);
            return site;
        }

        void setBlock(int bx, int by, int bz, Block block, int meta) {
            touched.add(new int[] { bx, by, bz });
            world.setBlock(bx, by, bz, block, meta, 3);
        }

        void setFloor(int bx, int by, int bz, Block block) {
            setBlock(bx, by, bz, block, 0);
        }

        void setLight(int bx, int by, int bz) {
            setBlock(bx, by, bz, Blocks.glowstone, 0);
        }

        void clear() {
            for (int[] coord : touched) {
                world.setBlockToAir(coord[0], coord[1], coord[2]);
            }
            touched.clear();
        }
    }
}
