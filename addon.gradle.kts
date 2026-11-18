// Extra build configuration for the GTNH/RFG build.
//
// Everything the verification harness needs lives here so that build.gradle.kts stays the stock GTNH convention
// file (which is what makes `autoUpdateBuildScript` safe to leave enabled):
//
//   * JaCoCo instrumentation of the unit-test run, a report task and a coverage regression floor.
//   * The `gameTest` source set: a separate, never-shipped test-only `@Mod` that boots with a real game.
//   * `./gradlew gameTestServer`, which boots a dedicated server, runs that mod and exits with its result.
//   * `./gradlew runServer -PsmokeTest`, which only boots the game and exits, for CI smoke coverage.
//
// The layout follows MrFuzzihead/PeripheralsPlusPlus (CODE_REVIEW.md section on the verification harness).

import java.math.BigDecimal

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.jvm.toolchain.JavaToolchainService
import org.gradle.process.CommandLineArgumentProvider
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport

// ---------------------------------------------------------------------------------------------------------
// Unit tests + coverage
// ---------------------------------------------------------------------------------------------------------

apply(plugin = "jacoco")

project.extensions.configure(JacocoPluginExtension::class.java) {
    toolVersion = "0.8.16"
}

tasks.withType<Test>().configureEach {
    // Gradle's own JaCoCo Test extension holds a legacy Configuration that Gradle 9's configuration cache cannot
    // serialize. The GTNH build enables the configuration cache globally, so the agent is attached through a
    // CommandLineArgumentProvider instead (below); that is a task input rather than configuration-time state.
    extensions.configure(JacocoTaskExtension::class.java) {
        isEnabled = false
    }
    // The plugin's own agent provider holds a legacy Configuration, which Gradle 9's configuration cache cannot
    // serialize. Drop it and use the provider registered below instead.
    jvmArgumentProviders.clear()
    // The mod ships Java 8 bytecode, so run the suite on the same JVM generation the mod supports. Mockito 4's
    // inline mock maker is also happiest there. Gradle provisions the toolchain if it is not installed.
    javaLauncher.set(
        project.extensions.getByType(JavaToolchainService::class.java).launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        }
    )
    // The suite runs on Java 8's default heap, which is not enough for the scaffold equivalence test: it drives the
    // algorithm the refactor replaced (a (2r+1)^3 cube scan plus a flood fill per candidate) over thousands of random
    // configurations, and that algorithm allocates a BlockCoord per block it looks at.
    maxHeapSize = "1536m"
    testLogging {
        events("failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// Tests need the compileOnly dependencies too: several production classes implement compileOnly or
// devOnlyNonPublishable supertypes (cofh.api.energy, cofh.lib, cofh.core), so a test cannot even name the class
// without them on its classpath.
val mainSourceSet = project.extensions.getByType(SourceSetContainer::class.java).getByName("main")
configurations.named("testCompileClasspath") {
    extendsFrom(configurations.named("compileOnly").get())
}
configurations.named("testRuntimeClasspath") {
    extendsFrom(configurations.named("compileOnly").get())
}

// JaCoCo agent for the unit-test run. `build/jacoco/test.exec` is what jacocoTestReport and
// jacocoTestCoverageVerification read.
val unitTestJacoco = objects.newInstance(JacocoAgentArgumentProvider::class.java)
unitTestJacoco.agent.from(configurations.named("jacocoAgent"))
unitTestJacoco.destFile.set(project.layout.buildDirectory.file("jacoco/test.exec").map { it.asFile.absolutePath })
unitTestJacoco.includes.set("com.mrfuzzihead.artifice.*")

tasks.withType<Test>().configureEach {
    jvmArgumentProviders.add(unitTestJacoco)
}

fun jacocoClassDirs() = project.files(mainSourceSet.output.classesDirs).asFileTree.matching {
    // The generated version holder and the client-only renderers are not meaningfully unit-testable headless;
    // measuring them would only dilute the number. The in-game test covers the client-side registration wiring.
    exclude(
        "com/mrfuzzihead/artifice/Tags.class",
        "com/mrfuzzihead/artifice/render/**",
        "com/mrfuzzihead/artifice/net/ClientProxy.class",
        "com/mrfuzzihead/artifice/event/ArtificeClient*.class"
    )
}

tasks.named<JacocoReport>("jacocoTestReport") {
    group = "verification"
    description = "Generates HTML and XML JaCoCo coverage reports from the unit test run"
    dependsOn(tasks.named("test"))

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
    executionData.setFrom(project.files(project.layout.buildDirectory.file("jacoco/test.exec")))
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    group = "verification"
    description = "Fails the build when unit test coverage drops below the configured floor"
    dependsOn(tasks.named("test"))

    classDirectories.setFrom(jacocoClassDirs())
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
    executionData.setFrom(project.files(project.layout.buildDirectory.file("jacoco/test.exec")))

    violationRules {
        rule {
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                // A regression guard at the current baseline, not a target: 332/6082 lines (5.46%) and
                // 91/2896 branches (3.14%) as of the harness landing. Ratchet this up as tests are added; never
                // lower it to make a change pass. CODE_REVIEW.md section 10 has the coverage analysis.
                minimum = BigDecimal("0.05")
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = BigDecimal("0.03")
            }
        }
    }
}

// `check` runs the tests and enforces the floor, so CI catches coverage regressions. Both the GTNH reusable
// build-and-test workflow and a plain `./gradlew build` go through `check`.
tasks.named("check").configure {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}

// ---------------------------------------------------------------------------------------------------------
// In-game test mod (never shipped)
// ---------------------------------------------------------------------------------------------------------

val gameTestSourceSet = project.extensions.getByType(SourceSetContainer::class.java).create("gameTest")

configurations.getByName(gameTestSourceSet.compileClasspathConfigurationName)
    .extendsFrom(configurations.getByName("compileClasspath"))
configurations.getByName(gameTestSourceSet.runtimeClasspathConfigurationName)
    .extendsFrom(configurations.getByName("runtimeClasspath"))
gameTestSourceSet.compileClasspath += mainSourceSet.output
gameTestSourceSet.compileClasspath += mainSourceSet.compileClasspath
gameTestSourceSet.runtimeClasspath += mainSourceSet.output
gameTestSourceSet.runtimeClasspath += mainSourceSet.runtimeClasspath
// The game-test mod is plain Java 8. Jabel's compiler plugin is loaded by javac through ServiceLoader when it
// sits on the compile classpath, and it in turn needs a ByteBuddy agent that is not present here.
gameTestSourceSet.compileClasspath = gameTestSourceSet.compileClasspath
    .filter { !it.name.contains("jabel") }

val isGameTestRun = project.hasProperty("gameTest") ||
    gradle.startParameter.taskNames.any { it.contains("gameTestServer") }
val isSmokeTestRun = project.hasProperty("smokeTest")

if (isGameTestRun || isSmokeTestRun) {
    tasks.withType<JavaExec>().configureEach {
        if (name.startsWith("runServer") || name.startsWith("runClient")) {
            classpath = classpath + gameTestSourceSet.output
            // A reused dev world may reference blocks from mods that are no longer installed; the game test is
            // not about that, so answer FML's "continue anyway?" prompt up front instead of blocking on stdin.
            systemProperty("fml.queryResult", "confirm")
        }
    }
}

if (isGameTestRun) {
    tasks.withType<JavaExec>().configureEach {
        if (name.startsWith("runServer") || name.startsWith("runClient")) {
            systemProperty("artifice.gameTest", "true")
        }
    }
}

if (isSmokeTestRun) {
    tasks.withType<JavaExec>().configureEach {
        if (name.startsWith("runServer") || name.startsWith("runClient")) {
            systemProperty("artifice.smokeTest", "true")
        }
    }
}

tasks.named("check").configure {
    dependsOn(gameTestSourceSet.classesTaskName)
}

tasks.register("gameTestServer") {
    group = "verification"
    description = "Boots a dedicated server, runs the in-game game test mod, and exits with its result"
    if (isGameTestRun && tasks.names.contains("runServer")) dependsOn(tasks.named("runServer"))
}

// ---------------------------------------------------------------------------------------------------------
// In-game coverage report
// ---------------------------------------------------------------------------------------------------------
// A gameTestServer run can also write build/jacoco/gameTest.exec (see the agent below). That data must NOT be
// merged into jacocoTestReport: a Forge 1.7.10 *dev* launch round-trips every class through ASM, so the class ids
// in the exec file do not match the class files on disk. The GTNH RFB boot layer dumps the post-transform bytes
// under <run dir>/RFB_CLASS_DUMP/<loader>/, which is what JaCoCo actually saw, so that is what this report reads.
// It is skipped when no dump exists (an empty execution-data collection makes JaCoCo skip the task).
val gameTestExec = project.layout.buildDirectory.file("jacoco/gameTest.exec")

tasks.register<JacocoReport>("jacocoGameTestReport") {
    group = "verification"
    description = "Reports coverage from the in-game game test run, using the post-transform classes FML loaded"
    executionData.setFrom(project.files(gameTestExec).filter { it.exists() })

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(project.layout.buildDirectory.file("reports/jacoco/gameTest/jacocoGameTestReport.xml"))
        html.outputLocation.set(project.layout.buildDirectory.dir("reports/jacoco/gameTest/html"))
    }

    classDirectories.setFrom(
        project.files(
            project.file("${findProperty("runServerWorkingDirectory") ?: "run/server"}/RFB_CLASS_DUMP/Launch")
        ).asFileTree.matching {
            include("com/mrfuzzihead/artifice/**")
            exclude("com/mrfuzzihead/artifice/gametest/**")
        }
    )
    sourceDirectories.setFrom(mainSourceSet.allJava.srcDirs)
}

/**
 * Attaches the JaCoCo agent to a forked game.
 *
 * Both the unit-test `Test` task and the game `JavaExec` run tasks need the agent as an explicit `-javaagent`
 * argument, because Gradle's own JaCoCo extension cannot be used here (see above). The agent jar is declared as a
 * `@Classpath` input so Gradle resolves it with the task instead of at configuration time (configuration-cache
 * safe).
 */
abstract class JacocoAgentArgumentProvider : CommandLineArgumentProvider {

    @get:Classpath
    abstract val agent: ConfigurableFileCollection

    @get:Input
    abstract val destFile: Property<String>

    @get:Input
    abstract val includes: Property<String>

    override fun asArguments(): Iterable<String> {
        val jar = extractAgent()
        return listOf(
            "-javaagent:" + jar.absolutePath +
                "=destfile=" + destFile.get() +
                ",append=false,dumponexit=true" +
                ",includes=" + includes.get()
        )
    }

    /**
     * The `jacocoAgent` configuration resolves to the JaCoCo *distribution* jar, which has no `Premain-Class` of
     * its own: the real agent is `jacocoagent.jar` inside it. Gradle's own JaCoCo support unpacks it the same way.
     */
    private fun extractAgent(): java.io.File {
        val distribution = agent.files.single()
        val target = java.io.File(destFile.get()).parentFile.resolve("jacocoagent.jar")
        java.util.zip.ZipFile(distribution).use { zip ->
            val entry = zip.getEntry("jacocoagent.jar") ?: return distribution
            target.parentFile?.mkdirs()
            zip.getInputStream(entry).use { input -> target.outputStream().use { output -> input.copyTo(output) } }
        }
        return target
    }
}

if (isGameTestRun) {
    val jacocoProvider = objects.newInstance(JacocoAgentArgumentProvider::class.java)
    jacocoProvider.agent.from(configurations.named("jacocoAgent"))
    jacocoProvider.destFile.set(
        gameTestExec.map { it.asFile.absolutePath }
    )
    jacocoProvider.includes.set("com.mrfuzzihead.artifice.*")

    tasks.withType<JavaExec>().configureEach {
        if (name.startsWith("runServer") || name.startsWith("runClient")) {
            jvmArgumentProviders.add(jacocoProvider)
        }
    }

    tasks.named("gameTestServer") {
        finalizedBy(tasks.named("jacocoGameTestReport"))
    }
}

