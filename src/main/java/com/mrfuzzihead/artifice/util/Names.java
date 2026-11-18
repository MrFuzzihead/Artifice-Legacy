package com.mrfuzzihead.artifice.util;

import java.util.Locale;

import net.minecraft.block.Block;

/**
 * Every registry name this mod emits, in one place (CODE_REVIEW §7.3).
 *
 * <p>
 * These strings are load-bearing: they are what a world's id table maps to, what a config file names, and what another
 * mod looks up. §4 froze them, and {@code ServerGameTests.checkFrozenRegistryNames} fails if the set changes - so this
 * class is not a place to rename things, it is the place to see them all at once and to stop the same literal being
 * written twice.
 * </p>
 *
 * <p>
 * The literal form is the <em>block or item name</em>, without the {@code tile.}/{@code item.} prefix that
 * {@code Block.getUnlocalizedName()} adds, because that prefix is what {@code GameRegistry.registerBlock} sees.
 * </p>
 */
public final class Names {

    private Names() {}

    /** The prefix every name in this mod carries, before the {@code tile.}/{@code item.} one. */
    public static final String ROOT = "artifice.";

    // Blocks - worldgen and rocks
    public static final String BASALT = "artifice.basalt";
    public static final String MARBLE = "artifice.marble";
    public static final String SULFUR = "artifice.sulfur";
    public static final String NITER = "artifice.niter";
    public static final String ENDER_ORE = "artifice.enderore";
    public static final String ENDER_ORE_GLOWING = "artifice.enderore.glowing";
    public static final String URANIUM = "artifice.uranium";
    public static final String GLOW_SAND = "artifice.glowsand";
    public static final String CHARRED_LOG = "artifice.charredlog";
    public static final String TEPHRA = "artifice.tephra";

    // Blocks - flora
    public static final String FLORA = "artifice.flora";
    public static final String LOTUS = "artifice.flora.lily";

    // Blocks - frames
    public static final String FRAME = "artifice.frame";
    public static final String DETECTOR = "artifice.detector";
    public static final String REINFORCED = "artifice.reinforced";
    public static final String GLASS_WALL = "artifice.glasswall";
    public static final String GLASS_WALL_DARK = "artifice.glasswalldark";
    public static final String SCAFFOLD = "artifice.scaffold";

    // Blocks - functional
    public static final String ATTUNED_TRANSMITTER = "artifice.attunedredstone.transmitter";
    public static final String ATTUNED_RECEIVER = "artifice.attunedredstone.receiver";
    public static final String LOGIC = "artifice.logicredstone";
    public static final String NUCLEAR_BATTERY = "artifice.nuclearbattery";
    public static final String HEATING_COIL = "artifice.heatingcoil";
    public static final String STEEL_BLOCK = "artifice.steel";

    // Items
    public static final String BOX = "artifice.box";
    public static final String COIN = "artifice.coin";
    public static final String CRAFT_KIT = "artifice.craftkit";
    public static final String DYE = "artifice.dye";
    public static final String NUGGET = "artifice.nugget";
    public static final String RESOURCE = "artifice.resource";
    public static final String STEEL_ITEM = "artifice.steel";
    public static final String UPGRADE = "artifice.upgrade";

    /** The bucket keeps the vanilla-style bare name, and FML prefixes it with the mod id. */
    public static final String BUCKET = "bucket";

    /** The fluid blocks take their name from CoFH's base class, which is given this namespace. */
    public static final String FLUID_NAMESPACE = "artifice";

    /** A rock: basalt, marble, or {@code limestone.<colour>}. */
    public static String rock(String name) {
        return ROOT + name;
    }

    /** A rock slab: {@code artifice.basalt.slab} and {@code artifice.basalt.doubleslab}. */
    public static String slab(String rockName, boolean doubled) {
        return ROOT + rockName + (doubled ? ".doubleslab" : ".slab");
    }

    /**
     * A rock's stairs: {@code artifice.basalt.stairs.cobblestone}. The rock half comes from the block's own name, so
     * the two can never disagree.
     */
    public static String stairs(Block rock, String subtype) {
        return rock.getUnlocalizedName()
            .replace("tile.", "") + ".stairs."
            + subtype.toLowerCase(Locale.ENGLISH);
    }

    /**
     * An ore variant, named after its ore-dictionary entry: {@code oreRedstone} becomes {@code artifice.oreredstone}.
     */
    public static String ore(String oreDictName, boolean glowing) {
        return ROOT + oreDictName.toLowerCase(Locale.ENGLISH) + (glowing ? ".glowing" : "");
    }

    /** A lamp: {@code artifice.lamp.white} or {@code artifice.lampinverted.white}. */
    public static String lamp(String colour, boolean inverted) {
        return ROOT + (inverted ? "lampinverted." : "lamp.") + colour.toLowerCase(Locale.ENGLISH);
    }

    /** A coloured torch, indexed by colour ordinal: {@code artifice.coloredtorch0}. */
    public static String torch(int colourIndex) {
        return ROOT + "coloredtorch" + colourIndex;
    }

    /** A sledge or sickle for one tool material: {@code artifice.sledge.iron}. */
    public static String tool(String kind, Object material) {
        return ROOT + kind
            + "."
            + material.toString()
                .toLowerCase(Locale.ENGLISH);
    }
}
