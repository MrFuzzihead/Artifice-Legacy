package com.mrfuzzihead.artifice.gametest;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.mrfuzzihead.artifice.ArtificeCore;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;

/**
 * Test-only mod: a separate, never-shipped {@code @Mod} that asserts Artifice works against a real game.
 *
 * <p>
 * It lives in the {@code gameTest} source set and is only put on the classpath by {@code ./gradlew gameTestServer}
 * (or {@code -PgameTest}), so it never reaches the published jar. Run it with:
 * </p>
 *
 * <pre>
 * mkdir -p run/server &amp;&amp; echo "eula=true" &gt; run/server/eula.txt
 * ./gradlew gameTestServer
 * </pre>
 *
 * <p>
 * Two modes:
 * </p>
 * <ul>
 * <li>{@code artifice.gameTest} - run every assertion in {@link ServerGameTests} and exit with their result.</li>
 * <li>{@code artifice.smokeTest} - boot the game, wait for the overworld, print a marker and exit 0 without
 * asserting anything. This is what {@code -PsmokeTest} sets, so CI can prove the mod loads at all without needing
 * an operator to type {@code stop}.</li>
 * </ul>
 *
 * <p>
 * The assertions run at {@link FMLServerStartedEvent}, where the overworld exists and block mutations are legal.
 * Deliberately no {@code dependencies} entry: the mod id is about to change (see CODE_REVIEW.md section 4), and the
 * assertions only touch the registries at server-start, which is after every mod's preInit regardless of order.
 * </p>
 */
@Mod(modid = GameTestMod.MOD_ID, name = "Artifice Game Tests", version = "1.0", acceptableRemoteVersions = "*")
public class GameTestMod {

    public static final String MOD_ID = "artificegametest";
    static final String OK_MARKER = "ARTIFICE_GAMETEST_OK";
    static final String FAIL_MARKER = "ARTIFICE_GAMETEST_FAILED";
    static final String SMOKE_MARKER = "ARTIFICE_SMOKE_TEST_OK";

    static final String GAME_TEST_PROPERTY = "artifice.gameTest";
    static final String SMOKE_TEST_PROPERTY = "artifice.smokeTest";
    static final String SMOKE_DELAY_PROPERTY = "artifice.smokeTest.delay";

    private boolean ran;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Nothing to do: the assertions only need the registries, and those are read at server-start.
    }

    @Mod.EventHandler
    public void onServerStarted(FMLServerStartedEvent event) {
        if (ran) return;
        ran = true;

        boolean smokeTest = Boolean.getBoolean(SMOKE_TEST_PROPERTY);
        boolean gameTest = Boolean.getBoolean(GAME_TEST_PROPERTY);

        if (!smokeTest && !gameTest) return;

        if (smokeTest && !gameTest) {
            scheduleSmokeExit();
            return;
        }

        int failures;
        int checks;
        try {
            failures = ServerGameTests.runAll();
            checks = ServerGameTests.checkCount();
        } catch (Throwable t) {
            t.printStackTrace();
            System.out.println(FAIL_MARKER + ": " + t);
            exit(1);
            return;
        }

        System.out.println("[ARTIFICE-GAMETEST] " + (checks - failures) + "/" + checks + " checks passed");
        if (failures > 0) {
            System.out.println(FAIL_MARKER + ": " + failures + " assertion(s) failed, see above");
            exit(1);
        } else {
            System.out.println(OK_MARKER);
            exit(0);
        }
    }

    /**
     * Boots, waits for the overworld to exist and then exits 0. Waiting for the world matters: exiting at
     * load-complete races the spawn-chunk generation, and the server never prints {@code Done}.
     */
    private void scheduleSmokeExit() {
        final long delay = Long.getLong(SMOKE_DELAY_PROPERTY, 2000L);
        final WorldServer world = MinecraftServer.getServer()
            .worldServerForDimension(0);

        if (world == null) {
            System.out.println(FAIL_MARKER + ": no overworld after server start");
            exit(1);
            return;
        }

        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(Math.max(0L, delay));
                } catch (InterruptedException e) {
                    Thread.currentThread()
                        .interrupt();
                    return;
                }

                ArtificeCore.logger.info("Artifice smoke test: mod loaded, shutting down");
                System.out.println(SMOKE_MARKER + ": FML load complete, shutting down");
                exit(0);
            }
        }, "Artifice-smoke-test");
        thread.setDaemon(true);
        thread.start();
    }

    private static void exit(int status) {
        FMLCommonHandler.instance()
            .exitJava(status, false);
    }
}
