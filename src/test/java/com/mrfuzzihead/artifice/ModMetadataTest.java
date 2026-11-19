package com.mrfuzzihead.artifice;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import cpw.mods.fml.common.Mod;

/**
 * Pins {@code src/main/resources/mcmod.info} to the code and the build it describes.
 *
 * <p>
 * The file had drifted in the two ways nothing was watching: its description was still the template's
 * {@code "TODO: Update me"}, and its dependency lists were empty while the {@code @Mod} annotation declared CoFHCore
 * required. Neither is a crash, which is exactly why it survived - it is only visible to whoever opens the mod list.
 * These assertions read the source file (the built copy has the tokens substituted, so it cannot check them) and fail
 * on the next drift instead.
 * </p>
 *
 * <p>
 * The tokens are part of the assertion on purpose. The GTNH convention plugin substitutes {@code ${modId}},
 * {@code ${modName}}, {@code ${modVersion}} and {@code ${minecraftVersion}} in this file; hardcoding any of them here
 * would freeze the value at whatever it was when someone typed it, which is how a jar ends up claiming a name or a
 * version its code does not. {@code RegistrationInvariantsTest} pins the other half - the mod constant is
 * {@code Tags.VERSION}, the same token {@code ${modVersion}} expands to - so the two cannot diverge.
 * </p>
 */
public class ModMetadataTest {

    private static final File MC_MOD_INFO = new File("src/main/resources/mcmod.info");

    /**
     * The mods {@code @Mod(dependencies = ...)} declares as {@code required-after}, i.e. the ones a pack must have.
     */
    private static List<String> requiredAfter(String dependencies) {
        List<String> required = new ArrayList<>();
        for (String entry : dependencies.split(";")) {
            String spec = entry.trim();
            if (spec.startsWith("required-after:")) {
                required.add(
                    spec.substring("required-after:".length())
                        .split("@")[0]);
            }
        }
        Collections.sort(required);
        return required;
    }

    /** The mods the same annotation only orders after: present or not, the mod loads. */
    private static List<String> after(String dependencies) {
        List<String> optional = new ArrayList<>();
        for (String entry : dependencies.split(";")) {
            String spec = entry.trim();
            if (spec.startsWith("after:")) {
                optional.add(
                    spec.substring("after:".length())
                        .split("@")[0]);
            }
        }
        Collections.sort(optional);
        return optional;
    }

    private static String read() throws IOException {
        Assert.assertTrue("missing " + MC_MOD_INFO.getPath(), MC_MOD_INFO.isFile());
        return new String(Files.readAllBytes(MC_MOD_INFO.toPath()), StandardCharsets.UTF_8);
    }

    /** The quoted value of a top-level string field. The file is small and its shape is fixed. */
    private static String stringField(String json, String name) {
        int key = json.indexOf("\"" + name + "\":");
        Assert.assertTrue("mcmod.info has no \"" + name + "\" field", key >= 0);
        int open = json.indexOf('"', key + name.length() + 3);
        int close = json.indexOf('"', open + 1);
        Assert.assertTrue("\"" + name + "\" is not a closed string in mcmod.info", open >= 0 && close > open);
        return json.substring(open + 1, close);
    }

    /** The quoted entries of a top-level array field, in file order. */
    private static List<String> arrayField(String json, String name) {
        int key = json.indexOf("\"" + name + "\":");
        Assert.assertTrue("mcmod.info has no \"" + name + "\" field", key >= 0);
        int open = json.indexOf('[', key);
        int close = json.indexOf(']', open);
        Assert.assertTrue("\"" + name + "\" is not a closed array in mcmod.info", open >= 0 && close > open);

        List<String> entries = new ArrayList<>();
        Matcher matcher = Pattern.compile("\"([^\"]*)\"")
            .matcher(json.substring(open + 1, close));
        while (matcher.find()) {
            entries.add(matcher.group(1));
        }
        return entries;
    }

    @Test
    public void fileUsesTheVersionTwoFormatAndTheGradleTokens() throws IOException {
        String json = read();

        Assert.assertTrue(
            "mcmod.info should use the modListVersion 2 layout",
            json.contains("\"modListVersion\": 2") && json.contains("\"modList\":"));

        // Each of these is substituted by the GTNH convention plugin. A literal here is a value that cannot be kept
        // in step with gradle.properties, so assert the token rather than the value it expands to.
        Assert.assertEquals("${modId}", stringField(json, "modid"));
        Assert.assertEquals("${modName}", stringField(json, "name"));
        Assert.assertEquals("${modVersion}", stringField(json, "version"));
        Assert.assertEquals("${minecraftVersion}", stringField(json, "mcversion"));
    }

    @Test
    public void descriptionIsNotAPlaceholder() throws IOException {
        String description = stringField(read(), "description");

        Assert.assertFalse(
            "mcmod.info still carries the template description: " + description,
            description.toUpperCase()
                .contains("TODO"));
        Assert.assertTrue(
            "mcmod.info description is too short to say anything: " + description,
            description.length() >= 60);
        // It is displayed in the mod list, so it must not be a class name or a file path.
        Assert.assertFalse("description should not name a package", description.contains("com.mrfuzzihead"));
    }

    @Test
    public void requiredModsMatchTheAnnotation() throws IOException {
        Mod mod = ArtificeCore.class.getAnnotation(Mod.class);
        Assert.assertNotNull("ArtificeCore is missing its @Mod annotation", mod);

        List<String> required = requiredAfter(mod.dependencies());
        Assert.assertFalse("the @Mod annotation declares nothing required - check the parse", required.isEmpty());
        Assert.assertEquals(
            "mcmod.info requiredMods disagrees with @Mod(dependencies = ...)",
            required,
            arrayField(read(), "requiredMods"));
    }

    @Test
    public void optionalIntegrationsAreNotListedAsRequirements() throws IOException {
        String json = read();
        List<String> declared = arrayField(json, "requiredMods");

        Mod mod = ArtificeCore.class.getAnnotation(Mod.class);
        List<String> optional = after(mod.dependencies());
        Assert
            .assertFalse("the @Mod annotation declares no optional integrations - check the parse", optional.isEmpty());

        // mcmod.info's dependency fields are display-only in FML 1.7.10 (CODE_REVIEW 9.13), but a bare modid in a
        // requiredMods list reads as "required" to every tool that does look at it. The annotation is the authority on
        // what is optional, so nothing it only orders after may appear here.
        for (String modid : optional) {
            Assert.assertFalse(
                "optional integration " + modid + " must not be a required mod in mcmod.info",
                declared.contains(modid));
        }
    }

    @Test
    public void creditsAndUrlAreFilledIn() throws IOException {
        String json = read();

        Assert.assertFalse(
            "mcmod.info has no credits",
            stringField(json, "credits").trim()
                .isEmpty());
        Assert.assertEquals(
            "the url field should point at this repository",
            "https://github.com/MrFuzzihead/Artifice-Legacy",
            stringField(json, "url"));
        Assert.assertEquals(
            "the update check should point at this repository's releases",
            "https://github.com/MrFuzzihead/Artifice-Legacy/releases/latest",
            stringField(json, "updateUrl"));
    }
}
