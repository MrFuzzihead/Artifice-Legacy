package com.mrfuzzihead.artifice.compat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.mrfuzzihead.artifice.block.world.BlockOre;

/**
 * Pins the Angelica config rewrite (CODE_REVIEW Appendix E.5).
 *
 * <p>
 * The file is somebody else's: Angelica owns it, and a pack update will regenerate it with the legacy class name. So
 * the properties that matter are not "the string is replaced" but "nothing else in the file moved" - the byte-for-byte
 * round trip, the untouched neighbours in the whitelist list, and the fact that a second run is a no-op.
 * </p>
 */
public class AngelicaWhitelistTest {

    private static final Charset BYTES = Charset.forName("ISO-8859-1");

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder();

    @Test
    public void replacesTheLegacyClassAndLeavesEverythingElseAlone() throws IOException {
        String before = "# Angelica modules\n" + "blockCrackFix=true\n"
            + "blockCrackFixRenderPassWhitelist <\n"
            + "    gregtech.common.blocks.BlockOres\n"
            + "    "
            + AngelicaWhitelist.LEGACY_ENTRY
            + "\n"
            + "    gtPlusPlus.core.block.base.BlockBaseOre\n"
            + " >\n"
            + "otherSetting=7\n";
        File file = write(before);

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.PATCHED, result.outcome);
        Assert.assertEquals(1, result.replaced);
        Assert.assertTrue(result.unknown.isEmpty());
        Assert.assertTrue(result.changed());
        Assert.assertEquals(before.replace(AngelicaWhitelist.LEGACY_ENTRY, BlockOre.class.getName()), read(file));
    }

    @Test
    public void theNewNameIsTheClassThatActuallyExists() {
        // Read from the class, not retyped, so a package move cannot leave a stale literal behind.
        Assert.assertEquals("com.mrfuzzihead.artifice.block.world.BlockOre", AngelicaWhitelist.currentEntry());
        Assert.assertEquals(BlockOre.class.getName(), AngelicaWhitelist.currentEntry());
        Assert.assertNotEquals(AngelicaWhitelist.LEGACY_ENTRY, AngelicaWhitelist.currentEntry());
    }

    @Test
    public void aSecondRunChangesNothing() throws IOException {
        File file = write("blockCrackFixRenderPassWhitelist <\n    " + AngelicaWhitelist.LEGACY_ENTRY + "\n >\n");

        Assert.assertEquals(AngelicaWhitelist.Outcome.PATCHED, AngelicaWhitelist.patch(temp.getRoot()).outcome);
        byte[] afterFirst = Files.readAllBytes(file.toPath());

        AngelicaWhitelist.Result second = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.ALREADY_CURRENT, second.outcome);
        Assert.assertEquals(0, second.replaced);
        Assert.assertFalse(second.changed());
        Assert.assertArrayEquals(afterFirst, Files.readAllBytes(file.toPath()));
    }

    @Test
    public void aFileWithoutOurEntryIsNotRewritten() throws IOException {
        String before = "blockCrackFixRenderPassWhitelist <\n    gregtech.common.blocks.BlockOres\n >\n";
        File file = write(before);

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.NOT_LISTED, result.outcome);
        Assert.assertEquals(before, read(file));
    }

    @Test
    public void aMissingFileIsNotCreated() {
        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.NO_FILE, result.outcome);
        Assert.assertFalse(new File(temp.getRoot(), AngelicaWhitelist.CONFIG_FILE).exists());
    }

    @Test
    public void aDirectoryWhereTheFileShouldBeIsNotAFile() throws IOException {
        Assert.assertTrue(new File(temp.getRoot(), AngelicaWhitelist.CONFIG_FILE).mkdir());

        Assert.assertEquals(AngelicaWhitelist.Outcome.NO_FILE, AngelicaWhitelist.patch(temp.getRoot()).outcome);
    }

    @Test
    public void everyOccurrenceIsReplaced() throws IOException {
        File file = write(
            AngelicaWhitelist.LEGACY_ENTRY + "\n"
                + AngelicaWhitelist.LEGACY_ENTRY
                + "\n"
                + AngelicaWhitelist.LEGACY_ENTRY
                + "\n");

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.PATCHED, result.outcome);
        Assert.assertEquals(3, result.replaced);
        Assert.assertEquals(
            BlockOre.class.getName() + "\n" + BlockOre.class.getName() + "\n" + BlockOre.class.getName() + "\n",
            read(file));
    }

    @Test
    public void lineEndingsSurvive() throws IOException {
        String before = "a=1\r\nwhitelist <\r\n    " + AngelicaWhitelist.LEGACY_ENTRY + "\r\n >\r\n";
        File file = write(before);

        AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(before.replace(AngelicaWhitelist.LEGACY_ENTRY, BlockOre.class.getName()), read(file));
        Assert.assertFalse("no LF may be promoted to CRLF", read(file).contains("\n\r"));
    }

    @Test
    public void bytesThatAreNotAsciiArePreserved() throws IOException {
        // Latin-1 0xE9 and a NUL: both must come back out unchanged, which is why the file is read as ISO-8859-1.
        byte[] before = { '#', ' ', (byte) 0xE9, 0, '\n', 'x', '=' };
        byte[] entry = (AngelicaWhitelist.LEGACY_ENTRY + "\n").getBytes(BYTES);
        byte[] withEntry = new byte[before.length + entry.length];
        System.arraycopy(before, 0, withEntry, 0, before.length);
        System.arraycopy(entry, 0, withEntry, before.length, entry.length);
        File file = new File(temp.getRoot(), AngelicaWhitelist.CONFIG_FILE);
        Files.write(file.toPath(), withEntry);

        AngelicaWhitelist.patch(temp.getRoot());

        byte[] expected = (new String(before, BYTES) + BlockOre.class.getName() + "\n").getBytes(BYTES);
        Assert.assertArrayEquals(expected, Files.readAllBytes(file.toPath()));
    }

    @Test
    public void everyClassInTheOldPackageIsMovedNotJustTheOreBlock() throws IOException {
        // Any Artifice class Angelica might name, including ones added to its defaults after this was written.
        String before = AngelicaWhitelist.LEGACY_ENTRY + "\n"
            + "shukaro.artifice.block.world.BlockRock\n"
            + "shukaro.artifice.block.decorative.BlockLamp\n";
        File file = write(before);

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.PATCHED, result.outcome);
        Assert.assertEquals(3, result.replaced);
        Assert.assertTrue("all three classes exist here: " + result.unknown, result.unknown.isEmpty());
        Assert.assertFalse(read(file).contains(AngelicaWhitelist.LEGACY_PREFIX));
        Assert.assertTrue(read(file).contains("com.mrfuzzihead.artifice.block.decorative.BlockLamp"));
    }

    @Test
    public void aNameThatDoesNotExistHereIsStillRewrittenButReported() throws IOException {
        String before = AngelicaWhitelist.LEGACY_ENTRY + "\n" + "shukaro.artifice.render.SomeOtherClass\n";
        File file = write(before);

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(AngelicaWhitelist.Outcome.PATCHED, result.outcome);
        Assert.assertEquals(2, result.replaced);
        // The rewrite is unconditional; the diagnostic is what tells us Angelica's list changed.
        Assert
            .assertEquals(Collections.singletonList("com.mrfuzzihead.artifice.render.SomeOtherClass"), result.unknown);
        Assert
            .assertEquals(BlockOre.class.getName() + "\ncom.mrfuzzihead.artifice.render.SomeOtherClass\n", read(file));
    }

    @Test
    public void anotherModFromTheSameAuthorIsNotTouched() throws IOException {
        String before = "shukaro.nodalmechanics.common.TileEntityNode\n" + AngelicaWhitelist.LEGACY_ENTRY + "\n";
        File file = write(before);

        AngelicaWhitelist.Result result = AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(1, result.replaced);
        Assert.assertTrue(read(file).contains("shukaro.nodalmechanics.common.TileEntityNode"));
    }

    @Test
    public void theOldPackageDoesNotSurvive() throws IOException {
        File file = write(AngelicaWhitelist.LEGACY_ENTRY + "\n");

        AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertFalse(read(file).contains(AngelicaWhitelist.LEGACY_PREFIX));
        Assert.assertTrue(
            "the replacement prefix must be this mod's package",
            AngelicaWhitelist.CURRENT_PREFIX.startsWith("com.mrfuzzihead.artifice."));
    }

    @Test
    public void theTemporaryFileIsNotLeftBehind() throws IOException {
        write(AngelicaWhitelist.LEGACY_ENTRY + "\n");

        AngelicaWhitelist.patch(temp.getRoot());

        Assert.assertEquals(
            1,
            temp.getRoot()
                .listFiles().length);
    }

    // -------------------------------------------------------------------------------------------------------------

    private File write(String text) throws IOException {
        File file = new File(temp.getRoot(), AngelicaWhitelist.CONFIG_FILE);
        Files.write(file.toPath(), text.getBytes(BYTES));
        return file;
    }

    private static String read(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()), BYTES);
    }
}
