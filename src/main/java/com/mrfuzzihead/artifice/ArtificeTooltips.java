package com.mrfuzzihead.artifice;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import com.mrfuzzihead.artifice.recipe.EnumUpgrades;
import com.mrfuzzihead.artifice.util.FormatCodes;
import com.mrfuzzihead.artifice.util.RomanUtil;

public class ArtificeTooltips {

    public static final String commentCode = FormatCodes.DarkGrey.code + FormatCodes.Italic.code;

    public static String localizeTooltip(String name, Boolean flavor) {
        return flavor ? commentCode + StatCollector.translateToLocal("tooltip.artifice." + name + ".flavor")
            : StatCollector.translateToLocal("tooltip.artifice." + name);
    }

    public static void initTooltips() {
        initBlockTooltips();
        initItemTooltips();
    }

    /**
     * Registers several lines for one stack, in the order they should appear.
     *
     * <p>
     * The registry appends, so this is what the wall of individual calls did - but it says once per stack that these
     * lines belong together, instead of repeating the ItemStack construction per line (CODE_REVIEW §7.5).
     * </p>
     */
    private static void tooltips(ItemStack stack, String... lines) {
        for (String line : lines) {
            ArtificeRegistry.registerTooltip(stack, line);
        }
    }

    private static void initItemTooltips() {
        tooltips(new ItemStack(ArtificeItems.itemResource, 1, 0), localizeTooltip("sulfur", false));
        tooltips(new ItemStack(ArtificeItems.itemResource, 1, 1), localizeTooltip("niter", false));
        tooltips(new ItemStack(ArtificeItems.itemResource, 1, 2), localizeTooltip("enderdust", true));
        tooltips(
            new ItemStack(ArtificeItems.itemDye, 1, 0),
            localizeTooltip("dye.blue", false),
            localizeTooltip("dye.blue", true));
        tooltips(
            new ItemStack(ArtificeItems.itemDye, 1, 1),
            localizeTooltip("dye.black", false),
            localizeTooltip("dye.black", true));
        tooltips(
            new ItemStack(ArtificeItems.itemDye, 1, 2),
            localizeTooltip("dye.brown", false),
            localizeTooltip("dye.brown", true));
        tooltips(
            new ItemStack(ArtificeItems.itemDye, 1, 3),
            localizeTooltip("dye.white", false),
            localizeTooltip("dye.white", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSledgeWood, 1, 0),
            localizeTooltip("sledge", false),
            FormatCodes.Italic.code + localizeTooltip("sledge.loss", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 50%",
            localizeTooltip("sledge", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSledgeStone, 1, 0),
            localizeTooltip("sledge", false),
            FormatCodes.Italic.code + localizeTooltip("sledge.loss", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 40%",
            localizeTooltip("sledge", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSledgeIron, 1, 0),
            localizeTooltip("sledge", false),
            FormatCodes.Italic.code + localizeTooltip("sledge.loss", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 30%",
            localizeTooltip("sledge", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSledgeGold, 1, 0),
            localizeTooltip("sledge", false),
            FormatCodes.Italic.code + localizeTooltip("sledge.loss", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 25%",
            localizeTooltip("sledge", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSledgeDiamond, 1, 0),
            localizeTooltip("sledge", false),
            FormatCodes.Italic.code + localizeTooltip("sledge.loss", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 20%",
            localizeTooltip("sledge", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSickleWood, 1, 0),
            localizeTooltip("sickle", false),
            FormatCodes.Italic.code + localizeTooltip("sickle.radius", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 1",
            localizeTooltip("sickle.wood", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSickleStone, 1, 0),
            localizeTooltip("sickle", false),
            FormatCodes.Italic.code + localizeTooltip("sickle.radius", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 2",
            localizeTooltip("sickle.stone", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSickleIron, 1, 0),
            localizeTooltip("sickle", false),
            FormatCodes.Italic.code + localizeTooltip("sickle.radius", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 3",
            localizeTooltip("sickle.iron", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSickleGold, 1, 0),
            localizeTooltip("sickle", false),
            FormatCodes.Italic.code + localizeTooltip("sickle.radius", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 4",
            localizeTooltip("sickle.gold", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSickleDiamond, 1, 0),
            localizeTooltip("sickle", false),
            FormatCodes.Italic.code + localizeTooltip("sickle.radius", false)
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + " 5",
            localizeTooltip("sickle.diamond", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSteelIngot, 1, 0),
            localizeTooltip("steel.ingot", false),
            localizeTooltip("steel.ingot", true));
        tooltips(
            new ItemStack(ArtificeItems.itemSteelIngot, 1, 1),
            localizeTooltip("steel.dust", false),
            localizeTooltip("steel.dust", true));
        tooltips(new ItemStack(ArtificeItems.itemNugget, 1, 0), localizeTooltip("nugget.copper", true));
        tooltips(new ItemStack(ArtificeItems.itemNugget, 1, 1), localizeTooltip("nugget.silver", true));
        tooltips(new ItemStack(ArtificeItems.itemNugget, 1, 2), localizeTooltip("nugget.platinum", true));
        tooltips(
            new ItemStack(ArtificeItems.itemCoin, 1, 0),
            localizeTooltip("coin.copper", false),
            localizeTooltip("coin.copper", true));
        tooltips(
            new ItemStack(ArtificeItems.itemCoin, 1, 1),
            localizeTooltip("coin.silver", false),
            localizeTooltip("coin.silver", true));
        tooltips(
            new ItemStack(ArtificeItems.itemCoin, 1, 2),
            localizeTooltip("coin.gold", false),
            localizeTooltip("coin.gold", true));
        tooltips(
            new ItemStack(ArtificeItems.itemCoin, 1, 3),
            localizeTooltip("coin.platinum", false),
            localizeTooltip("coin.platinum", true));
        tooltips(
            new ItemStack(ArtificeItems.itemBox, 1, 0),
            localizeTooltip("box", false),
            localizeTooltip("box", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 0),
            localizeTooltip("upgrade.type.toolweapon", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.efficiency.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.SharpeningKitS.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.sharpness.getMaxLevel()))
                + ")",
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.sharpness.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.SharpeningKitE.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.efficiency.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.sharpener", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 1),
            localizeTooltip("upgrade.type.universal", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.unbreaking.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.Reinforcement.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.unbreaking.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.reinforcement", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 2),
            localizeTooltip("upgrade.type.bow", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.punch.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.ReinforcedLimbs.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.punch.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.reinforcedlimbs", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 3),
            localizeTooltip("upgrade.type.bow", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.power.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.PlaitedString.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.power.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.plaited", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 4),
            localizeTooltip("upgrade.type.weapon", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.knockback.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.Counterweight.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.knockback.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.counterweight", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 5),
            localizeTooltip("upgrade.type.armor", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.thorns.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.ArmorSpikes.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.thorns.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.spikes", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 6),
            localizeTooltip("upgrade.type.armor", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.protection.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.LaminatedPadding.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.protection.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.lamination", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 7),
            localizeTooltip("upgrade.type.armor", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.projectileProtection.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.QuiltedCover.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.projectileProtection.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.cover", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 8),
            localizeTooltip("upgrade.type.boot", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.featherFalling.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.ElasticSoles.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.featherFalling.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.soles", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 9),
            localizeTooltip("upgrade.type.armor", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.fireProtection.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.Firedamp.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.fireProtection.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.firedamp", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 10),
            localizeTooltip("upgrade.type.armor", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.blastProtection.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.ElasticLayering.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.blastProtection.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.layering", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 11),
            localizeTooltip("upgrade.type.helmet", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.respiration.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.ScubaTank.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.respiration.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.tank", true));
        tooltips(
            new ItemStack(ArtificeItems.itemUpgrade, 1, 12),
            localizeTooltip("upgrade.type.helmet", false),
            FormatCodes.Yellow.code + StatCollector.translateToLocal(Enchantment.aquaAffinity.getName())
                + " ("
                + (ArtificeConfig.limitUpgrades ? RomanUtil.convertToRoman(EnumUpgrades.DiveKit.maxLevel)
                    : RomanUtil.convertToRoman(Enchantment.aquaAffinity.getMaxLevel()))
                + ")",
            localizeTooltip("upgrade.divekit", true));
        tooltips(
            new ItemStack(ArtificeItems.itemBucket, 1, 0),
            localizeTooltip("flammable", false),
            localizeTooltip("bucket.oil", true));
        tooltips(
            new ItemStack(ArtificeItems.itemBucket, 1, 1),
            localizeTooltip("explosive", false),
            localizeTooltip("bucket.fuel", true));
        tooltips(
            new ItemStack(ArtificeItems.itemBucket, 1, 2),
            localizeTooltip("flammable", false),
            localizeTooltip("bucket.creosote", true));
        tooltips(
            new ItemStack(ArtificeItems.itemBucket, 1, 3),
            localizeTooltip("flammable", false),
            localizeTooltip("bucket.bitumen", true));
        tooltips(
            new ItemStack(ArtificeItems.itemCraftKit, 1, 0),
            localizeTooltip("craftkit", false),
            localizeTooltip("craftkit", true));
        tooltips(new ItemStack(ArtificeItems.itemResource, 1, 3), localizeTooltip("radioactive", false));
    }

    private static void initBlockTooltips() {
        tooltips(new ItemStack(ArtificeBlocks.blockSulfur, 1, 1), localizeTooltip("sulfur", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNiter, 1, 1), localizeTooltip("niter", false));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFlora, 1, 3),
            localizeTooltip("lotus", false),
            localizeTooltip("lotus.close", false),
            localizeTooltip("lotus", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockLotus, 1, 0),
            localizeTooltip("lotus.close", false),
            localizeTooltip("lily", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFlora, 1, 0),
            localizeTooltip("bluebell", false),
            localizeTooltip("bluebell", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFlora, 1, 1),
            localizeTooltip("orchid", false),
            localizeTooltip("orchid", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFlora, 1, 2),
            localizeTooltip("iris", false),
            localizeTooltip("iris", true));
        tooltips(new ItemStack(ArtificeBlocks.blockBasalt, 1, 3), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarble, 1, 3), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockBasalt, 1, 4), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarble, 1, 4), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockBasaltSlab, 1, 2), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarbleSlab, 1, 2), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockBasaltSlab, 1, 3), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarbleSlab, 1, 3), localizeTooltip("connected", false));
        tooltips(new ItemStack(ArtificeBlocks.blockBasalt, 1, 0), localizeTooltip("volcanic", false));
        tooltips(new ItemStack(ArtificeBlocks.blockBasalt, 1, 1), localizeTooltip("volcanic", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarble, 1, 0), localizeTooltip("metamorphic", false));
        tooltips(new ItemStack(ArtificeBlocks.blockMarble, 1, 1), localizeTooltip("metamorphic", false));

        for (int i = 0; i < ArtificeBlocks.blockLimestones.length; i++) {
            tooltips(new ItemStack(ArtificeBlocks.blockLimestones[i], 1, 0), localizeTooltip("sedimentary", false));
            tooltips(new ItemStack(ArtificeBlocks.blockLimestones[i], 1, 1), localizeTooltip("sedimentary", false));
            tooltips(new ItemStack(ArtificeBlocks.blockLimestones[i], 1, 3), localizeTooltip("connected", false));
            tooltips(new ItemStack(ArtificeBlocks.blockLimestones[i], 1, 4), localizeTooltip("connected", false));
            tooltips(new ItemStack(ArtificeBlocks.blockLimestoneSlabs[i], 1, 2), localizeTooltip("connected", false));
            tooltips(new ItemStack(ArtificeBlocks.blockLimestoneSlabs[i], 1, 3), localizeTooltip("connected", false));
        }

        tooltips(
            new ItemStack(ArtificeBlocks.blockDetector, 1, 0),
            localizeTooltip("detector", false),
            localizeTooltip("detector", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFrame, 1, 0),
            localizeTooltip("frame.basic", false),
            localizeTooltip("frame.basic", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFrame, 1, 1),
            localizeTooltip("frame.reinforced", false),
            localizeTooltip("frame.reinforced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFrame, 1, 2),
            localizeTooltip("frame.industrial", false),
            localizeTooltip("frame.industrial", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockFrame, 1, 3),
            localizeTooltip("frame.advanced", false),
            localizeTooltip("frame.advanced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWall, 1, 0),
            localizeTooltip("glasswall.basic", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(0),
            localizeTooltip("glasswall.basic", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWall, 1, 1),
            localizeTooltip("glasswall.reinforced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(1),
            localizeTooltip("glasswall.reinforced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWall, 1, 2),
            localizeTooltip("glasswall.industrial", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(2),
            localizeTooltip("glasswall.industrial", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWall, 1, 3),
            localizeTooltip("glasswall.advanced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(3),
            localizeTooltip("glasswall.advanced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWallDark, 1, 0),
            localizeTooltip("glasswall.basic", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(0),
            localizeTooltip("glasswall.basic", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWallDark, 1, 1),
            localizeTooltip("glasswall.reinforced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(1),
            localizeTooltip("glasswall.reinforced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWallDark, 1, 2),
            localizeTooltip("glasswall.industrial", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(2),
            localizeTooltip("glasswall.industrial", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockGlassWallDark, 1, 3),
            localizeTooltip("glasswall.advanced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockGlassWall.getResistance(3),
            localizeTooltip("glasswall.advanced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockReinforced, 1, 0),
            localizeTooltip("blastwall.basic", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockReinforced.getResistance(0),
            localizeTooltip("blastwall.basic", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockReinforced, 1, 1),
            localizeTooltip("blastwall.reinforced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockReinforced.getResistance(1),
            localizeTooltip("blastwall.reinforced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockReinforced, 1, 2),
            localizeTooltip("blastwall.industrial", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockReinforced.getResistance(2),
            localizeTooltip("blastwall.industrial", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockReinforced, 1, 3),
            localizeTooltip("blastwall.advanced", false),
            FormatCodes.Italic.code + localizeTooltip("resistance", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + (int) ArtificeBlocks.blockReinforced.getResistance(3),
            localizeTooltip("blastwall.advanced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockScaffold, 1, 0),
            localizeTooltip("scaffold.basic", false),
            FormatCodes.Italic.code + localizeTooltip("overhang", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + ArtificeBlocks.blockScaffold.getOverhang(0),
            localizeTooltip("scaffold.basic", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockScaffold, 1, 1),
            localizeTooltip("scaffold.reinforced", false),
            FormatCodes.Italic.code + localizeTooltip("overhang", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + ArtificeBlocks.blockScaffold.getOverhang(1),
            localizeTooltip("scaffold.reinforced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockScaffold, 1, 2),
            localizeTooltip("scaffold.industrial", false),
            FormatCodes.Italic.code + localizeTooltip("overhang", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + ArtificeBlocks.blockScaffold.getOverhang(2),
            localizeTooltip("scaffold.industrial", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockScaffold, 1, 3),
            localizeTooltip("scaffold.advanced", false),
            FormatCodes.Italic.code + localizeTooltip("overhang", false)
                + " "
                + FormatCodes.Reset.code
                + FormatCodes.Aqua.code
                + ArtificeBlocks.blockScaffold.getOverhang(3),
            localizeTooltip("scaffold.advanced", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockSteel, 1, 0),
            localizeTooltip("steel.ingot", false),
            localizeTooltip("steel.ingot", true));

        for (int i = 0; i < 16; i++) {
            tooltips(
                new ItemStack(ArtificeBlocks.blockLamps[i], 1, 0),
                localizeTooltip("lamp", false),
                localizeTooltip("lamp", true));
            tooltips(
                new ItemStack(ArtificeBlocks.blockLampsInverted[i], 1, 0),
                localizeTooltip("lamp", false),
                localizeTooltip("lamp", true));
        }

        tooltips(new ItemStack(ArtificeBlocks.blockGlowSand, 1, 0), localizeTooltip("glowsand", true));
        tooltips(new ItemStack(ArtificeBlocks.blockGlowSand, 1, 1), localizeTooltip("glowsand", true));

        for (int i = 0; i < 16; i++) {
            tooltips(new ItemStack(ArtificeBlocks.blockColoredTorches[i], 1, 0), localizeTooltip("coloredtorch", true));
        }

        tooltips(new ItemStack(ArtificeBlocks.blockCharredLog, 1, 0), localizeTooltip("charredlog", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockAttunedRedstoneTransmitter, 1, 0),
            localizeTooltip("attunedredstone", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockAttunedRedstoneReceiver, 1, 0),
            localizeTooltip("attunedredstone", true));
        tooltips(new ItemStack(ArtificeBlocks.blockLogicRedstone, 1, 0), localizeTooltip("digital", false));
        tooltips(new ItemStack(ArtificeBlocks.blockLogicRedstone, 1, 1), localizeTooltip("analog", false));
        tooltips(new ItemStack(ArtificeBlocks.blockLogicRedstone, 1, 0), localizeTooltip("logicredstone", true));
        tooltips(new ItemStack(ArtificeBlocks.blockLogicRedstone, 1, 1), localizeTooltip("logicredstone", true));
        tooltips(new ItemStack(ArtificeBlocks.blockUranium, 1, 0), localizeTooltip("radioactive", false));
        tooltips(new ItemStack(ArtificeBlocks.blockUranium, 1, 1), localizeTooltip("radioactive", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 0), localizeTooltip("battery.full", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 1), localizeTooltip("battery.partial", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 2), localizeTooltip("battery.drained", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 3), localizeTooltip("battery.empty", false));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 0), localizeTooltip("battery", true));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 1), localizeTooltip("battery", true));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 2), localizeTooltip("battery", true));
        tooltips(new ItemStack(ArtificeBlocks.blockNuclearBattery, 1, 3), localizeTooltip("battery", true));
        tooltips(
            new ItemStack(ArtificeBlocks.blockHeatingCoil, 1, 0),
            localizeTooltip("heatingcoil", false),
            localizeTooltip("heatingcoil", true));
    }
}
