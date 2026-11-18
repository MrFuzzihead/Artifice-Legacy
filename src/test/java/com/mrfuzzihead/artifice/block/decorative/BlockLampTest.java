package com.mrfuzzihead.artifice.block.decorative;

import org.junit.Assert;
import org.junit.Test;

/**
 * The lamp's metadata is its light level, and its tick is only ever worth scheduling when that number would change.
 * {@link BlockLamp#targetMetadata(int, int)} is that number, and it is the reason §5.20's guard can exist.
 */
public class BlockLampTest {

    @Test
    public void theStrongerOfDirectAndIndirectPowerWins() {
        // Both signals are real: a redstone block beside the lamp feeds it weakly, a repeater or dust line feeds it
        // indirectly. The lamp shows the brighter of the two, which is what the pre-fix two-branch code computed.
        // Signature is (indirect, direct).
        Assert.assertEquals(15, BlockLamp.targetMetadata(0, 15));
        Assert.assertEquals(15, BlockLamp.targetMetadata(15, 0));
        Assert.assertEquals(9, BlockLamp.targetMetadata(9, 4));
        Assert.assertEquals(9, BlockLamp.targetMetadata(4, 9));
        Assert.assertEquals(7, BlockLamp.targetMetadata(7, 7));
    }

    @Test
    public void noPowerMeansNoLight() {
        Assert.assertEquals(0, BlockLamp.targetMetadata(0, 0));
    }

    @Test
    public void targetIsAlwaysOneOfTheInputs() {
        // Not a light-level clamp: the inputs come from the world and are already 0..15. What matters is that the
        // lamp never invents a value that came from neither signal, because that would make it re-tick forever.
        for (int direct = 0; direct <= 15; direct++) {
            for (int indirect = 0; indirect <= 15; indirect++) {
                int target = BlockLamp.targetMetadata(direct, indirect);
                Assert.assertTrue(
                    "target " + target + " from " + indirect + "/" + direct,
                    target == direct || target == indirect);
                Assert.assertTrue("target in range: " + target, target >= 0 && target <= 15);
            }
        }
    }

    @Test
    public void anUnchangedTargetProducesAnUnchangedMetadata() {
        // The guard in onNeighborBlockChange is `target == metadata -> return`. It may only skip the tick when the
        // lamp would write the same value, which holds exactly when target is idempotent under itself.
        for (int power = 0; power <= 15; power++) {
            Assert.assertEquals(power, BlockLamp.targetMetadata(power, power));
        }
    }
}
