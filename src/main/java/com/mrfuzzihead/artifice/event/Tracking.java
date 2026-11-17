package com.mrfuzzihead.artifice.event;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

import com.mrfuzzihead.artifice.ArtificeBlocks;
import com.mrfuzzihead.artifice.tile.TileEntityAttuned;

public class Tracking {

    public static List<Integer> sneaks = new ArrayList<>();

    public static int getHighestFrequencyPower(String frequency) {
        int i = 0;
        if (DimensionManager.getWorlds() == null) {
            return i;
        } else {
            for (World world : DimensionManager.getWorlds()) {
                if (world != null) {
                    for (Object o : world.loadedTileEntityList) {
                        if (o instanceof TileEntityAttuned) {
                            TileEntityAttuned tea = (TileEntityAttuned) o;
                            if (tea.frequency.equals(frequency) && tea.getWorldObj()
                                .getBlock(tea.xCoord, tea.yCoord, tea.zCoord)
                                == ArtificeBlocks.blockAttunedRedstoneReceiver) {
                                i = Math.max(tea.power, i);
                            }
                        }
                    }
                }
            }

            return i;
        }
    }

    public static void updateFrequency(String frequency) {
        if (DimensionManager.getWorlds() != null) {
            for (World world : DimensionManager.getWorlds()) {
                if (world != null) {
                    for (Object o : world.loadedTileEntityList) {
                        if (o instanceof TileEntityAttuned) {
                            TileEntityAttuned tea = (TileEntityAttuned) o;
                            if (tea.frequency.equals(frequency)) {
                                tea.updateEntity();
                            }
                        }
                    }
                }
            }
        }
    }
}
