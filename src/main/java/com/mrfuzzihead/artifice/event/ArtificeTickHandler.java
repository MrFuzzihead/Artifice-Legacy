package shukaro.artifice.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import java.util.ArrayDeque;
import java.util.Iterator;
import net.minecraft.world.World;
import shukaro.artifice.util.ChunkCoord;
import shukaro.artifice.world.IDelayedGen;
import shukaro.artifice.world.ISuspendableGen;

public class ArtificeTickHandler {
   public static TMap<Integer, ArrayDeque<ISuspendableGen>> suspendedGenerators = new THashMap();
   public static TMap<Integer, ArrayDeque<IDelayedGen>> delayedGenerators = new THashMap();

   public static void queueGeneration(int dimID, ISuspendableGen gen) {
      if (gen.getChunksToGen().size() != 0) {
         if (!suspendedGenerators.containsKey(dimID)) {
            suspendedGenerators.put(dimID, new ArrayDeque());
         }

         ((ArrayDeque)suspendedGenerators.get(dimID)).add(gen);
      }
   }

   public static void queueGeneration(int dimID, IDelayedGen gen) {
      if (!delayedGenerators.containsKey(dimID)) {
         delayedGenerators.put(dimID, new ArrayDeque());
      }

      ((ArrayDeque)delayedGenerators.get(dimID)).add(gen);
   }

   @SubscribeEvent
   public void worldTickEnd(WorldTickEvent event) {
      if (event.phase == Phase.END && event.side == Side.SERVER) {
         World world = event.world;
         int dim = world.provider.dimensionId;
         ArrayDeque<ISuspendableGen> suspendedGens = (ArrayDeque<ISuspendableGen>)suspendedGenerators.get(dim);
         if (world.getTotalWorldTime() % 5L == 0L && suspendedGens != null && suspendedGens.size() > 0) {
            boolean done = false;

            for (ISuspendableGen gen : suspendedGens) {
               Iterator i$ = gen.getChunksToGen().iterator();
               if (i$.hasNext()) {
                  ChunkCoord c = (ChunkCoord)i$.next();
                  gen.doGeneration(c.chunkX, c.chunkZ);
                  if (gen.getChunksToGen().size() == 0) {
                     ((ArrayDeque)suspendedGenerators.get(dim)).remove(gen);
                  }

                  done = true;
               }

               if (done) {
                  break;
               }
            }
         }

         ArrayDeque<IDelayedGen> delayedGens = (ArrayDeque<IDelayedGen>)delayedGenerators.get(dim);
         if (world.getTotalWorldTime() % 10L == 0L && delayedGens != null && delayedGens.size() > 0) {
            Iterator i$x = delayedGens.iterator();
            if (i$x.hasNext()) {
               IDelayedGen gen = (IDelayedGen)i$x.next();
               gen.doGeneration();
               ((ArrayDeque)delayedGenerators.get(dim)).remove(gen);
            }
         }
      }
   }
}
