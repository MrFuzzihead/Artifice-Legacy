package com.mrfuzzihead.artifice.gui;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;

import org.junit.Assert;
import org.junit.Test;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Covers the side annotations on {@link ArtificeCreativeTab} (CODE_REVIEW 5.11).
 *
 * <p>
 * Every method Artifice overrides on {@link CreativeTabs} is {@code @SideOnly(Side.CLIENT)} there, and FML's side
 * transformer removes those members from the server jar. An override without the annotation is therefore not an
 * override on a dedicated server, and its {@code super} call throws {@code NoSuchMethodError} the first time any
 * server-side code walks the tab list.
 * </p>
 *
 * <p>
 * This runs in a plain JVM, where the transformer does not apply, so the annotation is directly observable. The
 * in-game counterpart ({@code ServerGameTests.checkCreativeTabSideOnly}) asserts the other half: on the dedicated
 * server the methods are gone, which only holds while the annotations are present.
 * </p>
 */
public class ArtificeCreativeTabTest {

    private static Method declared(String name) {
        for (Method method : ArtificeCreativeTab.class.getDeclaredMethods()) {
            if (method.getName()
                .equals(name)) {
                return method;
            }
        }

        return null;
    }

    /** Every method our class declares that the superclass also declares with a client-only annotation. */
    private static List<String> overriddenClientOnlyMethods() {
        List<String> names = new ArrayList<>();

        for (Method superMethod : CreativeTabs.class.getDeclaredMethods()) {
            SideOnly sideOnly = superMethod.getAnnotation(SideOnly.class);
            if (sideOnly == null || sideOnly.value() != Side.CLIENT) {
                continue;
            }

            for (Method ourMethod : ArtificeCreativeTab.class.getDeclaredMethods()) {
                if (ourMethod.getName()
                    .equals(superMethod.getName())
                    && ourMethod.getParameterTypes().length == superMethod.getParameterTypes().length) {
                    names.add(ourMethod.getName());
                }
            }
        }

        return names;
    }

    @Test
    public void everyClientOnlyOverrideIsAnnotated() {
        List<String> overrides = overriddenClientOnlyMethods();
        Assert.assertFalse(
            "expected ArtificeCreativeTab to override at least one client-only CreativeTabs method",
            overrides.isEmpty());

        for (String name : overrides) {
            SideOnly annotation = declared(name).getAnnotation(SideOnly.class);
            Assert
                .assertNotNull(name + " overrides a client-only CreativeTabs member but is not annotated", annotation);
            Assert.assertEquals(
                name + " must be client-only, like the member it overrides",
                Side.CLIENT,
                annotation.value());
        }
    }

    @Test
    public void theTabIconsAreClientOnly() {
        for (String name : new String[] { "getTabIconItem", "getTranslatedTabLabel", "displayAllReleventItems" }) {
            Method method = declared(name);
            Assert.assertNotNull("ArtificeCreativeTab must still override " + name, method);

            SideOnly annotation = method.getAnnotation(SideOnly.class);
            Assert.assertNotNull(name + " must be @SideOnly(CLIENT)", annotation);
            Assert.assertEquals(Side.CLIENT, annotation.value());
        }
    }

    @Test
    public void theSuperclassMembersItOverridesAreClientOnly() {
        // If vanilla ever makes one of these common, this test should be revisited rather than silently drifting.
        for (String name : new String[] { "getTabIconItem", "getTranslatedTabLabel", "displayAllReleventItems" }) {
            Method superMethod = null;
            for (Method method : CreativeTabs.class.getDeclaredMethods()) {
                if (method.getName()
                    .equals(name)) {
                    superMethod = method;
                    break;
                }
            }

            Assert.assertNotNull("CreativeTabs no longer declares " + name, superMethod);
            Assert.assertNotNull(
                "CreativeTabs." + name + " is no longer client-only - revisit the override annotation",
                superMethod.getAnnotation(SideOnly.class));
        }
    }
}
