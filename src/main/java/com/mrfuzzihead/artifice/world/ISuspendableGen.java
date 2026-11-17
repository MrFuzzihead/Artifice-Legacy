package shukaro.artifice.world;

import java.util.Set;
import shukaro.artifice.util.ChunkCoord;

public interface ISuspendableGen {
   Set<ChunkCoord> getChunksToGen();

   boolean doGeneration(int var1, int var2);
}
