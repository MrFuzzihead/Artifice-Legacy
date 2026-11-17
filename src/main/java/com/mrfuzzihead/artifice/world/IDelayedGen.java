package com.mrfuzzihead.artifice.world;

import com.mrfuzzihead.artifice.util.ChunkCoord;

public interface IDelayedGen {

    ChunkCoord getChunk();

    boolean doGeneration();
}
