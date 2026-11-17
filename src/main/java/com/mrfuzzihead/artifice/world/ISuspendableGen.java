package com.mrfuzzihead.artifice.world;

import java.util.Set;

import com.mrfuzzihead.artifice.util.ChunkCoord;

public interface ISuspendableGen {

    Set<ChunkCoord> getChunksToGen();

    boolean doGeneration(int var1, int var2);
}
