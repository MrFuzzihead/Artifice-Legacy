package com.mrfuzzihead.artifice.compat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.Logger;

import com.mrfuzzihead.artifice.block.world.BlockOre;

/**
 * Rewrites Angelica's render-pass whitelist so it names this mod's ore block instead of the pre-rename one
 * (CODE_REVIEW Appendix E.5).
 *
 * <p>
 * Angelica's {@code blockCrackFixRenderPassWhitelist} is a list of <em>class-name strings</em> with a default baked
 * into its own jar, and that default still says {@code shukaro.artifice.block.world.BlockOre}. After the package
 * rename the entry no longer matches any block, so Artifice ores silently drop off the whitelist and z-fight when
 * {@code blockCrackFix} is on. There is no API to add to the list, so the config file is the only place to fix it.
 * </p>
 *
 * <p>
 * This is deliberately not an {@link ICompat}: those run in {@code init}, which is far too late - Angelica reads its
 * configuration during startup, so the file has to be correct before then. {@code ArtificeCore} calls this from
 * {@code FMLConstructionEvent}, which is the earliest hook that runs before any mod's {@code preInit}, and again from
 * {@code FMLLoadCompleteEvent} as a catch-all for the first launch, where Angelica may write the file after the
 * construction pass.
 * </p>
 *
 * <p>
 * The rewrite is a targeted string replacement, not a parse-and-rewrite: the file is read and written as
 * ISO-8859-1, which maps bytes to chars one-for-one, so comments, ordering, quoting and line endings survive
 * byte-for-byte. Only the one known class name is replaced; any <em>other</em> {@code shukaro.artifice.} reference in
 * the file is reported and left alone, because a class name we did not expect is a reason to look, not to guess.
 * </p>
 */
public final class AngelicaWhitelist {

    /** The config file Angelica writes; the default lives in its jar, which is why a fresh instance regresses. */
    public static final String CONFIG_FILE = "angelica-modules.cfg";

    /** The class name Angelica's default list still carries. */
    public static final String LEGACY_ENTRY = "shukaro.artifice.block.world.BlockOre";

    /** The old package prefix, used only to notice references this class does not know about. */
    public static final String LEGACY_PREFIX = "shukaro.artifice.";

    /** A byte-preserving charset: one byte maps to one char, so a round trip cannot corrupt the rest of the file. */
    private static final Charset BYTES = Charset.forName("ISO-8859-1");

    private AngelicaWhitelist() {}

    /** What {@link #patch(File)} did. */
    public enum Outcome {
        /** No {@code angelica-modules.cfg} in the config directory: Angelica is not installed. */
        NO_FILE,
        /** The file already names this mod's class and nothing was written. */
        ALREADY_CURRENT,
        /** The legacy entry was replaced and the file was written. */
        PATCHED,
        /** The file exists but never mentioned our ore block, so there was nothing to replace. */
        NOT_LISTED,
        /** Reading or writing failed; the file is untouched. */
        FAILED
    }

    /** The result of one patch attempt. */
    public static final class Result {

        public final Outcome outcome;
        /** How many legacy entries were replaced. */
        public final int replaced;
        /** How many {@code shukaro.artifice.} references remain afterwards that this class did not rewrite. */
        public final int residual;
        /** The file that was examined, for the log line. */
        public final File file;
        /** The failure, when {@link #outcome} is {@link Outcome#FAILED}; otherwise null. */
        public final String error;

        Result(Outcome outcome, int replaced, int residual, File file, String error) {
            this.outcome = outcome;
            this.replaced = replaced;
            this.residual = residual;
            this.file = file;
            this.error = error;
        }

        public boolean changed() {
            return this.outcome == Outcome.PATCHED;
        }

        /** Writes one log line; kept here so both call sites report the same way. */
        public void log(Logger logger) {
            switch (this.outcome) {
                case PATCHED:
                    logger.info(
                        "Angelica: rewrote {} legacy class name(s) in {} ({} other shukaro.artifice reference(s) left alone)",
                        this.replaced,
                        this.file.getName(),
                        this.residual);
                    break;
                case ALREADY_CURRENT:
                    logger.debug("Angelica: {} already names {}", this.file.getName(), currentEntry());
                    break;
                case NOT_LISTED:
                    logger.debug("Angelica: {} does not mention {}, nothing to do", this.file.getName(), LEGACY_ENTRY);
                    break;
                case NO_FILE:
                    logger.debug("Angelica: no {} in the config directory", CONFIG_FILE);
                    break;
                default:
                    logger.warn(
                        "Angelica: could not update {} - the ore block stays off the render-pass whitelist ({})",
                        this.file,
                        this.error);
                    break;
            }

            if (this.residual > 0) {
                logger.warn(
                    "Angelica: {} still contains {} shukaro.artifice reference(s) that this mod does not know about;"
                        + " check {} by hand",
                    this.file.getName(),
                    this.residual,
                    this.file);
            }
        }

        @Override
        public String toString() {
            return this.outcome + (this.replaced > 0 ? " x" + this.replaced : "")
                + (this.residual > 0 ? " +" + this.residual + " residual" : "");
        }
    }

    /** @return the class name Angelica's list should carry, read from the class itself so it cannot go stale. */
    public static String currentEntry() {
        return BlockOre.class.getName();
    }

    /**
     * Rewrites {@code configDir/angelica-modules.cfg} if it carries the legacy class name.
     *
     * <p>
     * Never throws: a failure to write another mod's config must not stop this mod from loading.
     * </p>
     *
     * @param configDir the game's {@code config} directory
     */
    public static Result patch(File configDir) {
        File file = new File(configDir, CONFIG_FILE);
        if (!file.isFile()) {
            return new Result(Outcome.NO_FILE, 0, 0, file, null);
        }

        String text;
        try {
            text = new String(Files.readAllBytes(file.toPath()), BYTES);
        } catch (IOException | RuntimeException e) {
            return new Result(Outcome.FAILED, 0, 0, file, describe(e));
        }

        int replaced = count(text, LEGACY_ENTRY);
        if (replaced == 0) {
            Outcome outcome = text.contains(currentEntry()) ? Outcome.ALREADY_CURRENT : Outcome.NOT_LISTED;
            return new Result(outcome, 0, count(text, LEGACY_PREFIX), file, null);
        }

        String patched = text.replace(LEGACY_ENTRY, currentEntry());
        try {
            write(file, patched);
        } catch (IOException | RuntimeException e) {
            return new Result(Outcome.FAILED, 0, 0, file, describe(e));
        }

        return new Result(Outcome.PATCHED, replaced, count(patched, LEGACY_PREFIX), file, null);
    }

    /**
     * Writes through a sibling temp file and renames over the target, so an interrupted write cannot leave a truncated
     * config behind - Angelica would read that as "the player removed everything".
     */
    private static void write(File file, String text) throws IOException {
        File temp = new File(file.getParentFile(), CONFIG_FILE + ".artifice.tmp");
        try {
            Files.write(temp.toPath(), text.getBytes(BYTES));
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (temp.exists() && !temp.delete()) {
                temp.deleteOnExit();
            }
        }
    }

    private static int count(String haystack, String needle) {
        int found = 0;
        int at = haystack.indexOf(needle);
        while (at >= 0) {
            found++;
            at = haystack.indexOf(needle, at + needle.length());
        }
        return found;
    }

    private static String describe(Exception e) {
        String message = e.getMessage();
        return message == null ? e.getClass()
            .getName()
            : e.getClass()
                .getSimpleName() + ": "
                + message;
    }
}
