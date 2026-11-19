package com.mrfuzzihead.artifice.compat;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;

import com.mrfuzzihead.artifice.ArtificeCore;
import com.mrfuzzihead.artifice.block.world.BlockOre;

/**
 * Moves any {@code shukaro.artifice.} class name in Angelica's config to this mod's package (CODE_REVIEW Appendix E.5).
 *
 * <p>
 * Angelica's {@code blockCrackFixRenderPassWhitelist} is a list of <em>class-name strings</em> with a default baked
 * into its own jar, and that default still says {@code shukaro.artifice.block.world.BlockOre}. After the package
 * rename the entry no longer matches any block, so Artifice ores silently drop off the whitelist and z-fight when
 * {@code blockCrackFix} is on. There is no API to add to the list, so the config file is the only place to fix it.
 * </p>
 *
 * <p>
 * The rewrite is the package move itself - every {@code shukaro.artifice.} becomes {@code com.mrfuzzihead.artifice.} -
 * rather than a single hard-coded class name. It is not a guess: it is the same one-way rename §3 performed, so it
 * fixes any Artifice class name Angelica carries now or adds later, and a name that resolves to nothing is a no-op
 * for Angelica either way, since it matches these strings against {@code block.getClass().getName()}. What the
 * single-name version bought was the <em>diagnostic</em> - knowing when the file names something unexpected - so that
 * survives: each rewritten name is looked up as a class, and any that this mod does not actually have is logged. The
 * trailing dot matters: {@code shukaro.nodalmechanics} (same author, different mod) is untouched.
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
 * byte-for-byte.
 * </p>
 */
public final class AngelicaWhitelist {

    /** The config file Angelica writes; the default lives in its jar, which is why a fresh instance regresses. */
    public static final String CONFIG_FILE = "angelica-modules.cfg";

    /** The package prefix this mod used to live under, with the dot so it cannot match {@code shukaro.artifice2}. */
    public static final String LEGACY_PREFIX = "shukaro.artifice.";

    /** The class name Angelica's default list carries, kept for the log line and the tests. */
    public static final String LEGACY_ENTRY = LEGACY_PREFIX + "block.world.BlockOre";

    /** The replacement, read from the mod's own package so a move cannot leave a stale literal here. */
    public static final String CURRENT_PREFIX = ArtificeCore.class.getPackage()
        .getName() + ".";

    /** A byte-preserving charset: one byte maps to one char, so a round trip cannot corrupt the rest of the file. */
    private static final Charset BYTES = Charset.forName("ISO-8859-1");

    /** How many unresolved names the log line names before it stops counting them out. */
    private static final int LOGGED_UNKNOWN = 5;

    private AngelicaWhitelist() {}

    /** What {@link #patch(File)} did. */
    public enum Outcome {
        /** No {@code angelica-modules.cfg} in the config directory: Angelica is not installed. */
        NO_FILE,
        /** The file already names this mod's package and nothing was written. */
        ALREADY_CURRENT,
        /** Legacy references were rewritten and the file was written. */
        PATCHED,
        /** The file exists but names nothing from this mod, so there was nothing to replace. */
        NOT_LISTED,
        /** Reading or writing failed; the file is untouched. */
        FAILED
    }

    /** The result of one patch attempt. */
    public static final class Result {

        public final Outcome outcome;
        /** How many legacy package references were replaced. */
        public final int replaced;
        /** Rewritten class names that do not exist in this mod, in first-seen order; usually empty. */
        public final List<String> unknown;
        /** The file that was examined, for the log line. */
        public final File file;
        /** The failure, when {@link #outcome} is {@link Outcome#FAILED}; otherwise null. */
        public final String error;

        Result(Outcome outcome, int replaced, List<String> unknown, File file, String error) {
            this.outcome = outcome;
            this.replaced = replaced;
            this.unknown = Collections.unmodifiableList(unknown);
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
                        "Angelica: moved {} {} reference(s) in {} to {}",
                        this.replaced,
                        LEGACY_PREFIX,
                        this.file.getName(),
                        CURRENT_PREFIX);
                    break;
                case ALREADY_CURRENT:
                    logger.debug("Angelica: {} already names {}", this.file.getName(), CURRENT_PREFIX);
                    break;
                case NOT_LISTED:
                    logger.debug("Angelica: {} names nothing from this mod, nothing to do", this.file.getName());
                    break;
                case NO_FILE:
                    logger.debug("Angelica: no {} in the config directory", CONFIG_FILE);
                    break;
                default:
                    logger.warn(
                        "Angelica: could not update {} - Artifice ores stay off the render-pass whitelist ({})",
                        this.file,
                        this.error);
                    break;
            }

            if (!this.unknown.isEmpty()) {
                logger.warn(
                    "Angelica: {} names {} class(es) this mod does not have after the rewrite: {}{};"
                        + " check {} by hand",
                    this.file.getName(),
                    this.unknown.size(),
                    this.unknown.subList(0, Math.min(LOGGED_UNKNOWN, this.unknown.size())),
                    this.unknown.size() > LOGGED_UNKNOWN ? " and more" : "",
                    this.file);
            }
        }

        @Override
        public String toString() {
            return this.outcome + (this.replaced > 0 ? " x" + this.replaced : "")
                + (this.unknown.isEmpty() ? "" : " +" + this.unknown.size() + " unknown");
        }
    }

    /** @return the class name Angelica's list should carry, read from the class itself so it cannot go stale. */
    public static String currentEntry() {
        return BlockOre.class.getName();
    }

    /**
     * Rewrites {@code configDir/angelica-modules.cfg} if it carries the legacy package.
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
            return new Result(Outcome.NO_FILE, 0, Collections.<String>emptyList(), file, null);
        }

        String text;
        try {
            text = new String(Files.readAllBytes(file.toPath()), BYTES);
        } catch (IOException | RuntimeException e) {
            return new Result(Outcome.FAILED, 0, Collections.<String>emptyList(), file, describe(e));
        }

        int replaced = count(text, LEGACY_PREFIX);
        if (replaced == 0) {
            Outcome outcome = text.contains(CURRENT_PREFIX) ? Outcome.ALREADY_CURRENT : Outcome.NOT_LISTED;
            return new Result(outcome, 0, Collections.<String>emptyList(), file, null);
        }

        // Before the rewrite, while the old names are still there to walk.
        List<String> unknown = unknownClasses(text);
        String patched = text.replace(LEGACY_PREFIX, CURRENT_PREFIX);
        try {
            write(file, patched);
        } catch (IOException | RuntimeException e) {
            return new Result(Outcome.FAILED, 0, Collections.<String>emptyList(), file, describe(e));
        }

        return new Result(Outcome.PATCHED, replaced, unknown, file, null);
    }

    /**
     * Walks every legacy reference and reports the ones that would name a class this mod does not have - which is how
     * we learn that Angelica's default list changed, without refusing to fix the names we do understand.
     */
    private static List<String> unknownClasses(String text) {
        List<String> unknown = new ArrayList<>();
        int at = text.indexOf(LEGACY_PREFIX);
        while (at >= 0) {
            int end = at;
            while (end < text.length() && isNameChar(text.charAt(end))) {
                end++;
            }
            String rewritten = CURRENT_PREFIX + text.substring(at + LEGACY_PREFIX.length(), end);
            if (!unknown.contains(rewritten) && !classExists(rewritten)) {
                unknown.add(rewritten);
            }
            at = text.indexOf(LEGACY_PREFIX, end);
        }
        return unknown;
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '.' || c == '_' || c == '$';
    }

    /**
     * Resolves without initialising: this asks whether the class is in the jar, not whether it can be used yet, and
     * loading a block class before its mod's {@code preInit} would be a bad idea.
     */
    private static boolean classExists(String name) {
        try {
            Class.forName(name, false, AngelicaWhitelist.class.getClassLoader());
            return true;
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }
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
