package shukaro.artifice.world;

import shukaro.artifice.util.ChunkCoord;

public interface IDelayedGen {
   ChunkCoord getChunk();

   boolean doGeneration();
}
