# Artifice-Legacy

[![](https://img.shields.io/badge/Minecraft-1.7.10-62a34a)](https://minecraft.wiki/w/Java_Edition_1.7.10)
[![](https://img.shields.io/badge/Forge-10.13.4.1614-1e2b4f)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html)
[![](https://img.shields.io/badge/CoFHCore-required-b07219)](https://github.com/GTNewHorizons/CoFHCore)
[![](https://github.com/MrFuzzihead/Artifice-Legacy/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/MrFuzzihead/Artifice-Legacy/actions/workflows/build-and-test.yml)
[![](https://github.com/MrFuzzihead/Artifice-Legacy/actions/workflows/game-test.yml/badge.svg)](https://github.com/MrFuzzihead/Artifice-Legacy/actions/workflows/game-test.yml)

Rocks, frames, machines and redstone for Minecraft 1.7.10: basalt, marble and limestone worldgen, blast walls,
scaffolds, coloured lamps and torches, wireless redstone, a nuclear battery, a heating coil, a salvage sledge, an
area-of-effect sickle and three enchantments.

Originally written by Shukaro. This fork is the decompiled 1.1.5R source, moved to the `com.mrfuzzihead.artifice`
package, cleaned up and kept building against the GTNH toolchain. Registry names are **frozen** — the mod id is still
`Artifice` and all 155 block and item names are unchanged — so a world from the original mod keeps loading.

## What it adds

| Kind | Contents |
|---|---|
| Rocks | Basalt, marble and seven limestone colours, each in plain, cobblestone, brick, paver, antipaver and chiseled styles, with slabs, double slabs and stairs |
| Frames | Frame, detector, blast wall and glass wall (light/dark) in four tiers — Basic, Reinforced, Industrial, Advanced — plus a scaffold |
| Machines | Nuclear battery (RF buffer) and heating coil (RF to furnace heat), with rates in the config |
| Redstone | Attuned redstone transmitter and receiver (frequency-based wireless redstone), and a logical redstone block whose four faces can each be set to positive, negative, output or disabled, in digital or analog mode |
| Worldgen | Rock layers, clusters and cave systems for all nine rock types; ore variants of every vanilla and common mod ore, plus sulfur, niter, ender ore, uranium, glow sand, charred log, tephra and oil lakes |
| Tools | Sledge (five materials) — breaks a block and drops its *crafting ingredients* instead, with a per-material loss chance; sickle (five materials) — harvests plants in a radius |
| Decoration | Steel block, 16 lamp colours in normal and inverted forms, 16 coloured torches, and five flowers (bluebell, orchid, iris, lotus, lotus lily) |
| Items | Steel, nuggets, coins, dyes, box upgrades, a craft kit and four fluid buckets |
| Enchantments | Invisible, Soulstealing, Resistance |

The four fluids are oil, fuel, bitumen and creosote. A bucket is only registered for a fluid Artifice created itself: if
BuildCraft or Railcraft is installed, their oil, fuel and creosote win and our subtypes stay inert rather than competing
for the same fluid.

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.7.10 |
| Minecraft Forge | 10.13.4.1614 |
| [CoFHCore](https://github.com/GTNewHorizons/CoFHCore) | required — the buckets and the RF machinery extend CoFH classes |
| BuildCraft, Railcraft, Forestry, MFR | optional integrations, see below |
| Thaumcraft | optional, load order only - declared in the `@Mod` annotation, no integration code |
| ForgeMultipart, CarpentersBlocks, Applied Energistics 2, Chisel, Angelica | optional integrations, see below |

## Compatibility

Every integration is gated on the other mod being present; none of them is a load-order requirement.

| Mod | What the integration does |
|---|---|
| BuildCraft | Registers rock, frame and glass walls as facades |
| ForgeMultipart | Registers the same blocks as microblock materials |
| Forestry | Adds bluebell, orchid, iris and lotus to the bee flower manager |
| MFR | Registers the flowers as harvestable, and uranium, ender ore and sulfur with the laser drill - each behind its own config key |
| Applied Energistics 2 | Whitelists the tile entities for spatial storage |
| CarpentersBlocks | Adds the rock and frame blocks to the cover blacklist |
| Chisel | Registers the carving groups. Chisel 2's own Artifice table has never worked (it asks for names that do not exist), so this mod supplies them |
| Angelica | Rewrites the `shukaro.artifice` class names in `config/angelica-modules.cfg` to this package at startup |

## Building

Everything is handled by the `com.gtnewhorizons.gtnhconvention` plugin. The shipped jar is Java 8 bytecode (modern
syntax via Jabel), so Gradle itself wants a JDK 17 or newer and the `run*` tasks run the game on a JDK 8 - which is
exactly what `.github/workflows/game-test.yml` provisions.

```sh
./gradlew build              # compile, unit tests, format check, coverage floor
./gradlew test               # unit tests only
./gradlew jacocoTestReport   # HTML coverage -> build/reports/jacoco/test/html
./gradlew runClient          # dev client
./gradlew runServer          # dev server
```

### Tests

| Command | What it runs |
|---|---|
| `./gradlew test` | 177 headless JUnit 4 tests in 23 classes (JUnit 4.13.2 + Mockito 4.11.0, Java 8 launcher) |
| `./gradlew check` | the above plus Spotless, Checkstyle, the coverage floor (5% line / 3% branch) and the gameTest compile |
| `./gradlew gameTestServer` | boots a real dedicated server, runs 1,847 in-game assertions, then exits with the result |
| `./gradlew runServer -PsmokeTest` | boots a real server, prints the smoke-test marker and exits |

The in-game mod lives in `src/gameTest/java` and is never shipped. It covers what cannot be tested headless: the real
block and item registries (including the frozen name list), tile entities, worldgen determinism against a pinned seed,
the crafting and smelting recipes, the ore dictionary fallbacks, and the compatibility hand-offs.

Every test that guards a bug fix was verified to fail against the code before the fix; `CODE_REVIEW.md` records which,
and where a first attempt at a test passed for the wrong reason.

### Releasing

The version is the git tag the jar was built from. `ArtificeCore.modVersion` is `Tags.VERSION` and `mcmod.info` quotes
the same token, so both are substituted from the same value at build time — there is no number to edit anywhere:

```sh
git tag -a 1.1.6 -m "1.1.6"
git push origin 1.1.6
```

`release-tags.yml` runs on any tag and attaches the built jar to a GitHub release. An untagged build reports a
describe string instead (`<shortsha>-<branch>.<n>+<shortsha>`), which is right for a dev jar and wrong for anything a
pack compares, so tag before you publish. The old `1.7.10R` prefix is gone with the literal that carried it: the tag is
the whole version.

## Configuration

`config/Artifice.cfg`, generated on first launch, 149 keys in twelve categories. The ones worth knowing:

- **Resource / Plant / Layer / Cluster / Cave Generation** — what generates, how big, how often, per rock type.
- **Compatibility > Convert Other Mods' Ores** (default on) — whether a blob may rewrite another mod's ore block into
  the Artifice variant for its host rock. Vanilla ores and Artifice's own always convert. Turning it off makes worldgen
  more conservative but is inconsistent across a chunk boundary that is generated mid-change.
- **Recipes** — one toggle per feature, so a pack can drop anything it does not want.
- **Enchantments** — the three ids are allocated at 70/71/72 in a fixed order because an existing save refers to an
  enchantment by number; do not change the start id in a world you care about.
- **Machines** — battery and coil rates. The nuclear battery is crafted empty and only keeps a charge it is given.

## Upgrading from Artifice

- **Registry names and the mod id are unchanged**, so a save loads as-is. The package moved from `shukaro.artifice` to
  `com.mrfuzzihead.artifice`, which only matters to mods that name our classes in strings or reflections.
- **FuzziTweaks 1.2.0 must be dropped in the same update.** Its `ArtificeEnchantsMixin` hard-links the old package and
  crashes startup; the fix it carried is in this mod now.
- **Angelica** bakes one of our old class names into its config default. The mod corrects the config itself on first
  launch, so no manual edit is needed.
- `CODE_REVIEW.md` Appendix E is the full downstream audit: what reads our class names, what reads our registry names,
  and what a save actually stores.

## Contributing

Bug reports and pull requests are welcome. Please read `CODE_REVIEW.md` before structural changes: it records the
defect catalogue, the decisions behind the current behaviour, the performance work, and the test-coverage analysis,
including the items that were deliberately left alone and why.

## License

MIT — see [LICENSE](LICENSE).

## Credit

Artifice was created by Shukaro. This fork is maintained by MrFuzzihead for the GTNH pack; the codebase review and test
harness are documented in `CODE_REVIEW.md`.
