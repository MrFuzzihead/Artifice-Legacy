package shukaro.artifice.util;

import java.util.Random;

public class XSRandom extends Random {
   private long seed;

   public XSRandom(long seed) {
      this.seed = seed;
   }

   @Override
   protected int next(int nbits) {
      long x = this.seed;
      x ^= x << 21;
      x ^= x >>> 35;
      x ^= x << 4;
      this.seed = x;
      x &= (1L << nbits) - 1L;
      return (int)x;
   }
}
