package com.mrfuzzihead.artifice.compat;

import net.minecraft.block.Block;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.compat.chisel.CarvingTable;

import cpw.mods.fml.common.registry.GameRegistry;
import team.chisel.carving.Carving;

/**
 * Registers this mod's rocks in Chisel's carving groups (CODE_REVIEW §9.9 / Appendix E.4).
 *
 * <p>
 * Chisel 2 ships an Artifice table of its own, and it has never worked: it asks the registry for
 * {@code Artifice:artifice.marble}, but this mod registers {@code Artifice:tile.artifice.marble}, so
 * {@code findBlock} returns null and all eight calls return early. Nobody reported it because the failure is a null
 * lookup and an early return - the chisel simply behaved as if Artifice marble did not exist.
 * </p>
 *
 * <p>
 * The fix belongs here rather than in Chisel's repository: this side knows the real names, and it can also add basalt
 * and the slabs, which Chisel never did. {@link CarvingTable} holds the names, groups and metadata;
 * {@code CarvingTableTest} pins them headlessly and {@code ServerGameTests.checkChiselCarvingNames} asserts in game
 * that every name in the table resolves to a registered block.
 * </p>
 *
 * <p>
 * The Chisel API is a {@code compileOnly} dependency and this class is only loaded when Chisel is present, so a pack
 * without Chisel never links against it. {@code addVariation} creates the group if it does not exist yet, so the
 * groups below do not have to be registered first, and an entry whose block fails to resolve is logged rather than
 * skipped silently - that silence is the original bug.
 * </p>
 */
public final class ChiselCompat implements ICompat {

    public static final String MOD_ID = "chisel";

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        int added = 0;
        int missing = 0;

        for (CarvingTable.Entry entry : CarvingTable.entries()) {
            Block block = GameRegistry.findBlock(ArtificeCore.modID, entry.registryName);
            if (block == null) {
                missing++;
                ArtificeCore.logger.warn(
                    "Chisel: {} does not resolve to a registered block, skipping the {} carving group",
                    entry.qualifiedName(),
                    entry.group);
                continue;
            }
            Carving.chisel.addVariation(entry.group, block, entry.meta, entry.order);
            added++;
        }

        if (missing > 0) {
            ArtificeCore.logger.warn(
                "Chisel: registered {} carving variations in {} groups, {} entries did not resolve",
                added,
                CarvingTable.groups()
                    .size(),
                missing);
        } else {
            ArtificeCore.logger.info(
                "Chisel: registered {} carving variations in {} groups",
                added,
                CarvingTable.groups()
                    .size());
        }
    }
}
