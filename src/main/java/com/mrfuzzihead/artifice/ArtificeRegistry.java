package com.mrfuzzihead.artifice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import com.mrfuzzihead.artifice.util.NameMetaPair;

public abstract class ArtificeRegistry {

    private static final List<Integer> dimensionBlacklist = new ArrayList<>();
    private static final Set<NameMetaPair> stoneTypes = new HashSet<>();
    private static final List<String> worldTypeBlacklist = new ArrayList<>();
    private static final Map<NameMetaPair, List<String>> tooltipMap = new HashMap<>();
    private static final List<ItemStack> marbleTypes = new ArrayList<>();
    private static final List<ItemStack> basaltTypes = new ArrayList<>();

    /**
     * This class is a public entry point: other mods may call {@code registerStoneType} and friends from their own
     * {@code preInit}, which can run before Artifice assigns {@code ArtificeCore.logger}. Logging through it
     * unconditionally was a NullPointerException waiting for exactly that load order (CODE_REVIEW 5.22).
     */
    private static void logInfo(String message) {
        if (ArtificeCore.logger != null) {
            ArtificeCore.logger.info(message);
        }
    }

    public static void registerMarbleType(ItemStack stack) {
        logInfo("Registering marble type " + stack.toString());
        if (marbleTypes.isEmpty()) {
            marbleTypes.add(stack);
        } else if (!marbleTypes.contains(stack)) {
            marbleTypes.add(stack);
            if (!ArtificeConfig.stoneCycleRecipes) {
                return;
            }

            if (marbleTypes.size() < 3) {
                IRecipe inner = new ShapelessOreRecipe(marbleTypes.get(1), new Object[] { marbleTypes.get(0) });
                IRecipe wrap = new ShapelessOreRecipe(marbleTypes.get(0), new Object[] { marbleTypes.get(1) });
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(inner);
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(wrap);
            } else {
                IRecipe inner = new ShapelessOreRecipe(
                    marbleTypes.get(marbleTypes.size() - 1),
                    new Object[] { marbleTypes.get(marbleTypes.size() - 2) });
                IRecipe wrap = new ShapelessOreRecipe(
                    marbleTypes.get(0),
                    new Object[] { marbleTypes.get(marbleTypes.size() - 1) });

                for (Object o : CraftingManager.getInstance()
                    .getRecipeList()
                    .toArray()) {
                    if (o instanceof ShapelessOreRecipe) {
                        ItemStack output = ((ShapelessOreRecipe) o).getRecipeOutput();
                        if (output.isItemEqual(marbleTypes.get(0))) {
                            ArrayList<Object> input = ((ShapelessOreRecipe) o).getInput();
                            if (input.size() <= 1 && input.get(0) instanceof ItemStack
                                && ((ItemStack) input.get(0)).isItemEqual(marbleTypes.get(marbleTypes.size() - 2))) {
                                CraftingManager.getInstance()
                                    .getRecipeList()
                                    .remove(o);
                            }
                        }
                    }
                }

                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(inner);
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(wrap);
            }
        }
    }

    public static List<ItemStack> getMarbleTypes() {
        return marbleTypes;
    }

    public static void registerBasaltType(ItemStack stack) {
        logInfo("Registering basalt type " + stack.toString());
        if (basaltTypes.isEmpty()) {
            basaltTypes.add(stack);
        } else if (!basaltTypes.contains(stack)) {
            basaltTypes.add(stack);
            if (!ArtificeConfig.stoneCycleRecipes) {
                return;
            }

            if (basaltTypes.size() < 3) {
                IRecipe inner = new ShapelessOreRecipe(basaltTypes.get(1), new Object[] { basaltTypes.get(0) });
                IRecipe wrap = new ShapelessOreRecipe(basaltTypes.get(0), new Object[] { basaltTypes.get(1) });
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(inner);
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(wrap);
            } else {
                IRecipe inner = new ShapelessOreRecipe(
                    basaltTypes.get(basaltTypes.size() - 1),
                    new Object[] { basaltTypes.get(basaltTypes.size() - 2) });
                IRecipe wrap = new ShapelessOreRecipe(
                    basaltTypes.get(0),
                    new Object[] { basaltTypes.get(basaltTypes.size() - 1) });

                for (Object o : CraftingManager.getInstance()
                    .getRecipeList()
                    .toArray()) {
                    if (o instanceof ShapelessOreRecipe) {
                        ItemStack output = ((ShapelessOreRecipe) o).getRecipeOutput();
                        if (output.isItemEqual(basaltTypes.get(0))) {
                            ArrayList<Object> input = ((ShapelessOreRecipe) o).getInput();
                            if (input.size() <= 1 && input.get(0) instanceof ItemStack
                                && ((ItemStack) input.get(0)).isItemEqual(basaltTypes.get(basaltTypes.size() - 2))) {
                                CraftingManager.getInstance()
                                    .getRecipeList()
                                    .remove(o);
                            }
                        }
                    }
                }

                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(inner);
                CraftingManager.getInstance()
                    .getRecipeList()
                    .add(wrap);
            }
        }
    }

    public static List<ItemStack> getBasaltTypes() {
        return basaltTypes;
    }

    public static void registerTooltip(ItemStack stack, String line) {
        NameMetaPair pair = new NameMetaPair(stack.getItem(), stack.getItemDamage());
        if (tooltipMap.get(pair) == null) {
            List<String> temp = new ArrayList<>();
            temp.add(line);
            tooltipMap.put(pair, temp);
        } else {
            tooltipMap.get(pair)
                .add(line);
        }
    }

    /**
     * Appends the tooltip lines registered for a stack, honouring the two tooltip config switches.
     *
     * <p>
     * Five item classes had their own copy of this loop, and they had drifted: four honoured
     * {@code ArtificeConfig.flavorText} and the box did not (CODE_REVIEW §7.5). One implementation, with the box's
     * choice passed in explicitly.
     * </p>
     */
    public static void appendTooltips(ItemStack stack, List<String> infoList) {
        appendTooltips(stack, infoList, ArtificeConfig.flavorText);
    }

    /** @param allowFlavour whether the italic "flavour" lines are shown at all */
    public static void appendTooltips(ItemStack stack, List<String> infoList, boolean allowFlavour) {
        if (!ArtificeConfig.tooltips) {
            return;
        }
        appendLines(tooltipMap.get(new NameMetaPair(stack.getItem(), stack.getItemDamage())), infoList, allowFlavour);
    }

    /**
     * For items with a single metadata value (tools, boxes, buckets): matches whatever damage the stack carries,
     * which is what the sledge and sickle were approximating by looking up damage 0.
     */
    public static void appendTooltips(Item item, List<String> infoList) {
        if (!ArtificeConfig.tooltips) {
            return;
        }
        appendLines(tooltipMap.get(new NameMetaPair(item)), infoList, ArtificeConfig.flavorText);
    }

    private static void appendLines(List<String> lines, List<String> infoList, boolean allowFlavour) {
        if (lines == null) {
            return;
        }
        for (String line : lines) {
            if (allowFlavour || !line.startsWith(ArtificeTooltips.commentCode)) {
                infoList.add(line);
            }
        }
    }

    public static Map<NameMetaPair, List<String>> getTooltipMap() {
        return tooltipMap;
    }

    public static void registerDimensionBlacklist(int dimID) {
        if (!dimensionBlacklist.contains(dimID)) {
            logInfo("Registering " + dimID + " to dimension blacklist");
            dimensionBlacklist.add(dimID);
        }
    }

    public static List<Integer> getDimensionBlacklist() {
        return dimensionBlacklist;
    }

    public static void registerStoneType(Block stone, int meta) {
        NameMetaPair pair = new NameMetaPair(stone, meta);
        if (!stoneTypes.contains(pair)) {
            logInfo("Registering stone type " + pair.toString());
            stoneTypes.add(pair);
        }
    }

    public static Set<NameMetaPair> getStoneTypes() {
        return stoneTypes;
    }

    public static void registerWorldTypeBlacklist(String type) {
        if (!worldTypeBlacklist.contains(type)) {
            logInfo("Registering " + type + " to world type blacklist");
            worldTypeBlacklist.add(type);
        }
    }

    public static List<String> getWorldTypeBlacklist() {
        return worldTypeBlacklist;
    }
}
