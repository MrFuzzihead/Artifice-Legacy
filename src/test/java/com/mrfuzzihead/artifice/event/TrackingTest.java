package com.mrfuzzihead.artifice.event;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import net.minecraft.world.World;

import org.junit.Assert;
import org.junit.Test;

import com.mrfuzzihead.artifice.tile.TileEntityAttuned;

/**
 * The frequency index that replaced the all-worlds tile-entity scan (CODE_REVIEW §6).
 *
 * <p>
 * These are the properties the scan had and the index must keep: the answer is the highest power on the frequency
 * across every loaded world, and it is read from the tile's live field rather than cached, so a lever moving takes
 * effect on the next query. The "only receivers count" rule needs a real world and a real redstone block, so it is
 * covered in-game instead ({@code attuned frequency link} in {@code ServerGameTests}).
 * </p>
 *
 * <p>
 * The rest are the properties the index adds: a frequency only sees its own tiles, and entries leave the index when
 * their tile is invalidated, when the world unloads, or - if both of those were missed - when a query trips over
 * them.
 * </p>
 */
public class TrackingTest {

    private static TileEntityAttuned attuned(String frequency, int power) {
        TileEntityAttuned tea = new TileEntityAttuned();
        tea.setFrequency(frequency);
        tea.power = power;
        Tracking.track(tea);
        return tea;
    }

    @Test
    public void theStrongestReceiverWins() {
        attuned("max-of-three", 3);
        attuned("max-of-three", 11);
        attuned("max-of-three", 7);

        Assert.assertEquals(11, Tracking.getHighestFrequencyPower("max-of-three"));
    }

    @Test
    public void otherFrequenciesAreInvisible() {
        attuned("frequency-a", 15);
        attuned("frequency-b", 4);

        Assert.assertEquals(15, Tracking.getHighestFrequencyPower("frequency-a"));
        Assert.assertEquals(4, Tracking.getHighestFrequencyPower("frequency-b"));
        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("frequency-never-used"));
    }

    @Test
    public void anUnknownOrNullFrequencyIsZeroRatherThanAnError() {
        attuned("", 12);

        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("nothing-here"));
        Assert.assertEquals(0, Tracking.getHighestFrequencyPower(null));
    }

    @Test
    public void powerIsReadLiveEvenWithNothingFileUnderTheFrequency() {
        TileEntityAttuned tea = attuned("live-power", 0);
        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("live-power"));

        // This is the property a cached maximum would lose: a receiver's power is a redstone input, so the same
        // tile entity has to report a different value the moment the input changes.
        tea.power = 9;
        Assert.assertEquals(9, Tracking.getHighestFrequencyPower("live-power"));

        tea.power = 2;
        Assert.assertEquals(2, Tracking.getHighestFrequencyPower("live-power"));
    }

    @Test
    public void aFallingMaximumDoesNotLeaveTheOldValueBehind() {
        TileEntityAttuned strong = attuned("falling-max", 15);
        attuned("falling-max", 5);
        Assert.assertEquals(15, Tracking.getHighestFrequencyPower("falling-max"));

        strong.power = 1;
        Assert.assertEquals(5, Tracking.getHighestFrequencyPower("falling-max"));
    }

    @Test
    public void anInvalidatedTileLeavesTheIndex() {
        TileEntityAttuned tea = attuned("invalidated", 15);
        Assert.assertEquals(1, Tracking.tracked("invalidated"));

        tea.invalidate();

        Assert.assertEquals(0, Tracking.tracked("invalidated"));
        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("invalidated"));
    }

    @Test
    public void aQuerySweepsATileThatWentInvalidWithoutUntracking() {
        TileEntityAttuned tea = attuned("stale-entry", 15);

        // What a missed invalidate() looks like: the tile is dead but the index still lists it. (The real invalidate
        // untracks; re-filing the dead tile reproduces the case where a third-party path removed it without notice.)
        tea.invalidate();
        Tracking.track(tea);

        Assert.assertEquals(1, Tracking.tracked("stale-entry"));

        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("stale-entry"));
        Assert.assertEquals(0, Tracking.tracked("stale-entry"));
    }

    @Test
    public void aWorldUnloadForgetsOnlyThatWorld() {
        World unloaded = mock(World.class);
        World kept = mock(World.class);

        TileEntityAttuned gone = attuned("world-unload", 15);
        gone.setWorldObj(unloaded);
        TileEntityAttuned stays = attuned("world-unload", 4);
        stays.setWorldObj(kept);

        Tracking.forget(unloaded);

        Assert.assertEquals(1, Tracking.tracked("world-unload"));
        Assert.assertEquals(4, Tracking.getHighestFrequencyPower("world-unload"));
    }

    @Test
    public void changingTheFrequencyMovesTheTileInsteadOfDuplicatingIt() {
        TileEntityAttuned tea = attuned("before", 8);

        tea.setFrequency("after");

        Assert.assertEquals(0, Tracking.tracked("before"));
        Assert.assertEquals(1, Tracking.tracked("after"));
        Assert.assertEquals(0, Tracking.getHighestFrequencyPower("before"));
        Assert.assertEquals(8, Tracking.getHighestFrequencyPower("after"));
    }

    @Test
    public void settingTheSameFrequencyTwiceIsANoOp() {
        TileEntityAttuned tea = attuned("idempotent", 5);

        tea.setFrequency("idempotent");
        tea.setFrequency("idempotent");

        Assert.assertEquals(1, Tracking.tracked("idempotent"));
    }

    @Test
    public void anEmptyOrNullFrequencyIsStoredAsAnEmptyString() {
        TileEntityAttuned tea = new TileEntityAttuned();
        Assert.assertEquals("", tea.getFrequency());

        tea.setFrequency(null);
        Assert.assertEquals("", tea.getFrequency());

        tea.setFrequency("named");
        Assert.assertEquals("named", tea.getFrequency());
    }

    @Test
    public void updateFrequencyOnlyTouchesItsOwnFrequency() {
        // updateFrequency() re-runs the tiles on a frequency after a neighbour change. It is called from
        // onNeighborBlockChange, i.e. on the redstone path, which is why it must not be a world scan any more - but
        // it must still reach every tile on that frequency and no other.
        TileEntityAttuned mine = tracked("redstone-path");
        TileEntityAttuned other = tracked("unrelated");

        Tracking.updateFrequency("redstone-path");

        verify(mine, times(1)).updateEntity();
        verify(other, never()).updateEntity();
    }

    @Test
    public void updateFrequencyToleratesFrequenciesThatDoNotExist() {
        tracked("something");

        Tracking.updateFrequency("absent");
        Tracking.updateFrequency("");
        Tracking.updateFrequency(null);
    }

    /** A spy, so the call can be counted, filed under a frequency and attached to a world. */
    private static TileEntityAttuned tracked(String frequency) {
        TileEntityAttuned tea = spy(new TileEntityAttuned());
        doNothing().when(tea)
            .updateEntity();
        tea.setWorldObj(mock(World.class));
        tea.setFrequency(frequency);
        Tracking.track(tea);
        return tea;
    }
}
