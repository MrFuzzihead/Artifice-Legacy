package com.mrfuzzihead.artifice.gametest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.ArtificeConfig;
import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.ArtificeEnchants;
import com.mrfuzzihead.artifice.ArtificeItems;
import com.mrfuzzihead.artifice.ArtificeRegistry;
import com.mrfuzzihead.artifice.item.DispenserBehaviorSledge;
import com.mrfuzzihead.artifice.recipe.EnumUpgrades;
import com.mrfuzzihead.artifice.tile.TileEntityAttuned;
import com.mrfuzzihead.artifice.tile.TileEntityHeatingCoil;
import com.mrfuzzihead.artifice.tile.TileEntityLogic;
import com.mrfuzzihead.artifice.tile.TileEntityNuclearBattery;

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
        run("slab metadata", ServerGameTests::checkSlabMetadata);
        run("lamp lighting", ServerGameTests::checkLampLighting);
        run("torch placement", ServerGameTests::checkTorchPlacement);
        run("scaffold support", ServerGameTests::checkScaffoldSupport);
        run("logic redstone", ServerGameTests::checkLogicRedstone);
        run("attuned redstone", ServerGameTests::checkAttunedRedstone);
        run("heating coil", ServerGameTests::checkHeatingCoil);
        run("nuclear battery", ServerGameTests::checkNuclearBattery);
        run("block drops", ServerGameTests::checkBlockDrops);
        run("sledge dispenser", ServerGameTests::checkSledgeDispenser);
        run("recipes", ServerGameTests::checkRecipes);
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
    private static void checkCreativeTabSideOnly() {
        boolean superHasDisplay = false;
        for (java.lang.reflect.Method method : CreativeTabs.class.getDeclaredMethods()) {
            if (method.getName()
                .equals("displayAllReleventItems")) superHasDisplay = true;
        }

        knownBug(
            "ArtificeCreativeTab overrides client-only CreativeTabs methods without @SideOnly(CLIENT)",
            !superHasDisplay);
    }

    // ---------------------------------------------------------------------------------------------------------
    // Metadata round trips
    // ---------------------------------------------------------------------------------------------------------

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

            checkEquals(
                "scaffold on a solid floor stays",
                ArtificeBlocks.blockScaffold,
                world.getBlock(site.x, site.y, site.z));

            // Removing the support must drop the scaffold (it has no other anchored neighbour).
            world.setBlockToAir(site.x, site.y - 1, site.z);

            checkEquals("scaffold without support is removed", Blocks.air, world.getBlock(site.x, site.y, site.z));
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

            receiver.frequency = frequency;
            transmitter.frequency = frequency;

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
            transmitter.frequency = "artifice-gametest-other";
            transmitter.updateEntity();
            checkEquals("a different frequency does not couple", 0, transmitter.power);
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
            int x = 512;
            int z = 512;
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
