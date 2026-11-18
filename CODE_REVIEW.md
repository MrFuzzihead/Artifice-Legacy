# Artifice-Legacy — Codebase Review

| | |
|---|---|
| **Repository** | `MrFuzzihead/Artifice-Legacy` |
| **Reviewed commit** | `616bb9e` (Initial decompilation build: namespace refactor, spotless, cofhCore dep) |
| **Scope** | 145 Java files / ~17,600 lines, 1.7.10 Forge, GTNH RetroFuturaGradle buildchain, CoFHCore required |
| **Companion PRs** | #2 `feature/test-harness` — JUnit/Mockito/JaCoCo + in-game game-test mod. #3 `docs/code-review` — this document. Both merged into `main` at `762f04e`. |
| **Follow-up branch** | `refactor/1.1.6` — the release branch for the work below, including the downstream audit in **Appendix E** |

---

## 0. TL;DR — the action list

| # | Item | Kind | Priority |
|---|---|---|---|
| 1 | Land the verification harness (PR #2): `./gradlew check` + `./gradlew gameTestServer` | infra | **done** |
| 2 | `ArtificeEnchants` can leave `enchantmentSoulstealing` / `enchantmentResistance` / `enchantmentInvisible` null; `ArtificeEventHandler.onDeath` and `ArtificeCreativeTab` then NPE on every kill / tab open in a large pack | bug | ~~P0~~ done |
| 3 | `DispenserBehaviorSledge` calls `getIngredientsForBlock` twice, the second time after `setBlockToAir` → guaranteed NPE when a dispenser fires a sledge | bug | ~~P1~~ done |
| 4 | ~~`MiscUtils` neighbour helpers double-index the side array~~ - **retracted**: the double index is a permutation either way, so nothing was ever skipped (§5.4). Cleaned up for readability only | not a defect | ~~P1~~ |
| 5 | `MiscUtils.getAdjoinedTileEntities` uses the method arguments instead of the current coordinate, so the flood fill is depth 1 | bug | ~~P1~~ done |
| 6 | `NameMetaPair.hashCode()` ignores the `-1` wildcard that `equals()` honours → wildcard lookups miss in hash collections; `WorldGenVolcano`'s replace set never matches | bug | ~~P1~~ done |
| 7 | `ArtificeTickHandler` never dequeues suspendable generators (their `getChunksToGen()` key set never shrinks) → a permanent per-5-tick scan that grows for the life of the save | leak/perf | ~~P1~~ done |
| 8 | The whole volcano feature (`WorldGenVolcano`, `VolcanoHelper`, `blockTephra`, two config keys) is unreachable dead code | dead code | **P1** (open - needs a keep-or-delete decision) |
| 9 | `ArtificeCreativeTab` overrides client-only `CreativeTabs` methods without `@SideOnly(CLIENT)` → `NoSuchMethodError` on a dedicated server | bug | ~~P2~~ done |
| 10 | `>=`/`>` off-by-one bounds checks in 6 `getIcon` implementations | bug | ~~P2~~ done |
| 11 | `BlockFrameScaffold.checkStay` bails out on the first scaffold neighbour instead of continuing | bug | ~~P2~~ done |
| 12 | `BlockLogicRedstone.canConnectRedstone` uses a hand-rolled side table that does not match `isProvidingWeakPower`'s `OPPOSITES` mapping | bug | ~~P2~~ done |
| 13 | `BlockOre` NPEs when an ore-dictionary entry it expects is absent (very likely in a GTNH pack) | bug | ~~P2~~ done |
| 14 | Ore smelting recipes are registered in `init` but the ore-dictionary entries they match on only appear in `postInit`, so they never register | bug | ~~P2~~ done |
| 15 | The frequency-probability formula is wrong at both ends: `0` means "once per chunk", `99` is 100× rarer than `100`, and `>1000` saturates (§7.2) | bug | ~~P2~~ done |
| 16 | `RFperFurnaceHeat` is an unvalidated integer divisor; `0` throws `ArithmeticException` in the heating coil (§5.21) | bug | ~~P2~~ done |
| 17 | `/calc` is registered but its evaluator is commented out; it prints `1+1 = ` | broken feature | ~~P3~~ done (deleted) |
| 18 | ~900 lines of duplicated worldgen blob code (Cluster/Sulfur/Desert/Cave) | refactor | ~~P3~~ done (§7.1) |
| 19 | `ItemSledge` scans the whole crafting table on every block break | perf | ~~P3~~ done |
| 20 | `Tracking` rescans every loaded tile entity of every world, per attuned block, per tick | perf | ~~P3~~ done |
| 21 | `WorldGenLayer` read every block twice and shuffled air through the lookup (§6) | perf | ~~P3~~ done |
| 22 | `MiscUtils`' neighbour helpers allocated a boxed array and took a lock per call (§6.2) | perf | ~~P3~~ done |
| 23 | `ComparableItemStackNBT.isStackEqual` casts without a check, and `isItemEqual` dereferences a null item (§8) | bug | ~~P2~~ done |
| 24 | Mod id, name and version each existed twice, plus an unused `modChannel` (§8) | cleanup | ~~P3~~ done |
| 25 | Raw `ArrayList`/`ArrayDeque` casts throughout worldgen and the tick handler (§7.6) | cleanup | ~~P3~~ done |
| 26 | Six unnamed literals on hot or hard-to-read paths (§7.7) | cleanup | ~~P3~~ done |
| 27 | Frame hardness was `meta + 5`, so corrupt metadata invented a hardness; the blast wall's extra 5 was unexplained (§9.3, §9.4) | cleanup | ~~P3~~ done |
| 28 | Registry names were frozen by §4 but only in prose; nothing failed if one changed | infra | ~~P2~~ done |
| 29 | `initBlocks` was one 144-line method (§7.4) | refactor | ~~P3~~ done |
| 30 | 45 registry-name literals scattered across 30 classes (§7.3) | refactor | ~~P3~~ done |
| 31 | 226 tooltip registrations and five copies of the tooltip lookup (§7.5) | refactor | ~~P3~~ done |
| 32 | Scaffold support scanned a 33³ cube and ran a flood fill per candidate (§6.3) | perf | ~~P3~~ done |
| 33 | Four ~100-line worldgen blob generators with two drifted differences (§7.1) | refactor | ~~P3~~ done |

Severity key: **P0** = data loss or blocks a planned change, **P1** = crashes / lost functionality / unbounded growth, **P2** = wrong behaviour, **P3** = cleanup.

### 0.1 Pack follow-ups

These come from the downstream audit of the real pack (`FuzzCraft-Legacy`, 233 mod jars) in **Appendix E**. They
are not codebase defects, but they decide whether the migration boots at all.

| # | Item | Kind | Priority |
|---|---|---|---|
| P1 | **FuzziTweaks 1.2.0 hard-links `shukaro.artifice` from a Mixin.** Its `ArtificeEnchantsMixin` targets `shukaro.artifice.ArtificeEnchants` and reads `ArtificeConfig` / `ArtificeCore` fields. The target is gated on the *mod id* `Artifice`, which the rename does not change, so the mixin is still queued and then fails → **startup crash**. Must be dropped in the same release (§3.2, Appendix E.3) | pack | **P0** |
| P2 | Port FuzziTweaks' enchant-id allocation in-tree **first**, preserving ids 70/71/72, or dropping the mixin silently re-collapses the three enchantments onto one id (§5.3) | code | **P0** |
| P3 | `config/angelica-modules.cfg:51` — one string (`shukaro.artifice.block.world.BlockOre` → `com.mrfuzzihead.…`) | pack | **done from our side** (§9.12) |
| P4 | Chisel's Artifice carving table never worked (it looks up `Artifice:artifice.marble`; the real name is `Artifice:tile.artifice.marble`). Optional repair, not migration work (E.4) | pack / upstream | **done from our side** (§9.9) |

P1 and P2 are **done and merged**, in two halves that have to be released together: the in-tree allocation and the
70/71/72 assertion are in this repo (§5.3), and the mixin, its config key and FuzziTweaks' `shukaro.artifice` compile
dependency are gone on `refactor/drop-artifice-mixin` there. Cutting 1.1.6 is what releases them as one event. P3 and
P4 no longer need a pack-side edit or an upstream PR: the Chisel groups are registered from our side (§9.9) and the
Angelica whitelist is rewritten at startup (§9.12).

---

## 1. Method

1. Read every file in `src/main/java`, plus the generated `mcmod.info` and the lang files.
2. Cross-checked the runtime contract against the actual 1.7.10 FML sources
   (`GameRegistry`, `GameData`, `Loader`, `FMLMissingMappingsEvent`, `FMLPreInitializationEvent`) and the
   decompiled Minecraft sources in `build/rfg/minecraft-src`, so every claim about *what FML does with a name* is
   taken from the code that will run, not from memory.
3. Built and ran the mod: `./gradlew check` (unit suite) and `./gradlew gameTestServer` (1,442 in-game assertions
   against a real dedicated server). Several findings below are proven by those runs rather than inferred.
4. Diffed the current tree against `migrate/artificeDecompiled` to separate decompiler artefacts from real logic.

Where a finding is a known-bug marker in the test suite it is called out, so the fix has an acceptance test
already.

---

## 2. Baseline: what the harness gives you

Landed in PR #2, additive only (no production file touched):

```bash
./gradlew check              # 77 JUnit tests + coverage floor (5% line / 3% branch)
./gradlew gameTestServer     # boots a dedicated server, runs 1,442 in-game checks, exits 0/1
./gradlew runServer -PsmokeTest   # boots, prints ARTIFICE_SMOKE_TEST_OK, exits 0
./gradlew jacocoTestReport   # build/reports/jacoco/test/html
./gradlew jacocoGameTestReport   # in-game coverage, only when run/server/RFB_CLASS_DUMP exists
```

`jacocoGameTestReport` reads the post-transform classes that the GTNH RFB boot layer dumps under
`run/server/RFB_CLASS_DUMP/Launch`. Without that dump it produces an empty report (the unit-test report is
unaffected), because a 1.7.10 *dev* launch rewrites class bytes through ASM and the JaCoCo class ids no longer
match the files on disk — so the two exec files must not be merged.

Current measured coverage: **332 / 6,082 lines (5.46%)**, **91 / 2,896 branches (3.14%)**. That is the honest
headless ceiling for this codebase until the refactors in §7 land — nearly everything else needs a live
`World`, a bootstrapped registry or a GL context. The in-game suite is where those live.

Two tests are `@Ignore`d and are the acceptance tests for §5.1 and §5.10.

---

## 3. Migration (a): package `shukaro.artifice` → `com.mrfuzzihead.artifice`

### 3.1 What is already done, and what it does *not* affect

The package rename is complete: `grep -r shukaro src/` returns nothing, and the 145 files under
`src/main/java/com/mrfuzzihead/artifice` all reference the new package. The `migrate/artificeDecompiled/shukaro/...`
tree is the untouched decompiler output and is git-ignored, so it will not leak into the jar.

Crucially, a **package** rename is *not* a **registry** rename. Every persistent identifier in this mod is either

* a registry name (`"Artifice:tile.artifice.basalt"`) — derived from the *mod id*, not the package, or
* a tile-entity id (`"tileArtificeAttuned"`) — a plain, un-namespaced string, or
* an NBT key (`"frequency"`, `"charge"`, `"sides"`, `"Heat"`, `"id"`, `"meta"`),
* an ore-dictionary string (`"oreMarble"`, `"dustSulfur"`, …),
* a fluid name (`"oil"`, `"fuel"`, `"creosote"`, `"bitumen"`),
* an enchantment **numeric id** (70..255 range from config) and a lang key (`enchantment.artifice.invisible`),
* a CoFH worldgen feature name (`"Artifice: Basalt@0 Layers"`, …).

None of those contain `shukaro`. So **existing worlds, existing configs and existing enchantments survive the
package rename untouched.** That is the good news, and it is worth stating explicitly because it is the question
that matters: no world migration is needed for (a).

### 3.2 The real impact: binary compatibility for other mods

| Impact | Detail | Severity |
|---|---|---|
| **Compile-time API break** | `shukaro.artifice.ArtificeRegistry` is a public, documented-by-usage entry point (`registerMarbleType`, `registerBasaltType`, `registerStoneType`, `registerDimensionBlacklist`, `registerWorldTypeBlacklist`, `registerTooltip`). Any addon that compiled against `shukaro.artifice.*` gets `NoClassDefFoundError` at load, not a graceful skip. | High if such addons exist |
| **AE2 spatial whitelist** | `compat/AE2.java` sends four `whitelist-spatial` IMC messages whose *values are class names* (`com.mrfuzzihead.artifice.tile.TileEntityAttuned`, …). AE2 resolves them by class name. Old entries persisted from a previous version (`shukaro.artifice.tile.…`) will fail to resolve; AE2 logs and skips them. Re-sent on every load with the new names, so new worlds are correct, but a pre-existing AE2 config may keep stale `shukaro` strings forever. | Low, cosmetic |
| **Reflection from other mods** | Nothing in this repo reflects into `shukaro.*`. The pack scan (Appendix E) found exactly one consumer, and it is not reflection: **FuzziTweaks 1.2.0** links the old package directly from a Mixin (`@Mixin(value = shukaro.artifice.ArtificeEnchants.class)`, `@Overwrite initEnchants()`, plus `ArtificeConfig` / `ArtificeCore` field refs). It crashes on startup against the renamed build. | **Critical — must be fixed with the release** |
| **Angelica** | `blockCrackFixRenderPassWhitelist` defaults to a list of block **class names**, including `shukaro.artifice.block.world.BlockOre`. Resolved by string, so the entry silently stops matching after the rename. Cosmetic (z-fighting on ores) and fixable in config, but the *default* lives in Angelica's jar, so a fresh instance regresses. | Low, and it recurs on reinstall |
| **ForgeMultipart / BuildCraft / MFR / Forestry / Carpenters interop** | All of these are ItemStack-, string- or reflection-based and reference *their own* classes, not ours. Unaffected. (The EE3 compat has since been deleted; see §9.10.) | None |
| **Tile entities** | `ArtificeTiles` registers `"tileArtificeAttuned"` etc. Tile-entity ids are **not** namespaced and are **not** class names in 1.7.10, so chunk data loads unchanged. Verified against the pack's own save: 414 region files, zero `shukaro` strings (§E.5). | None |
| **`mcmod.info` / `authorList`** | Already lists `MrFuzzihead` and `Shukaro`. Fine. | None |

### 3.3 Recommendation

1. **Do nothing to existing worlds.** There is nothing to migrate.
2. **Treat `ArtificeRegistry` as an API.** Right now it lives in the root package and is `public abstract` with
   static helpers. If anything in your pack calls it, move it to `com.mrfuzzihead.artifice.api.ArtificeRegistry`
   *and* keep a deprecated `shukaro.artifice.ArtificeRegistry` subclass for one release. The pack scan says
   **nothing calls it** — leave it where it is and stop calling it "API" (Appendix E).
3. **The pack scan is done** (Appendix C commands, results in Appendix E). Among 233 jars, four mention
   `shukaro`, and only one is about this mod: FuzziTweaks 1.2.0, which is a **hard break** and is covered in
   §0.1 / E.3. NodalMechanics (`shukaro.nodalmechanics`) and `gtnh-web-map` (`shukaro.warptheory`) are the same
   author in different packages and are irrelevant.
4. **Do not rename the CoFH worldgen feature names.** They are hashed into every chunk (`WorldHandler.genHash`)
   to drive retrogen. Measured on the real save: 11 distinct `Artifice:<registryName> <feature>` strings, 3.36M
   occurrences across 414 region files (E.8). The hash contains the **mod id, the registry name and the feature
   name**, which is the main reason the rename is off the table (§4).

---

## 4. Migration (b): mod id `Artifice` → `artifice` — **rejected**

The mod id and the registry names stay as they are. Recorded here so it does not get re-proposed.

The case against was measured on the real pack (Appendix E):

* **CoFH retrogen hashes.** Every chunk stores `Artifice:<registryName> <featureName>` for each feature CoFH ran —
  3,358,850 occurrences in 11 distinct forms in one test world (E.8). Changing the mod id or a registry name makes
  CoFH treat that feature as new for every chunk and re-run it over existing terrain: duplicate ore clusters and
  lakes in already-generated worlds.
* **Configs key on `modid:name`.** AE2 (facade list + `Facades.cfg`), ProjectE, RTG, Mystcraft and UniDict all name
  Artifice stacks that way (E.7). Each would need re-keying, or a legacy alias layer, or both.
* **Nothing needs it.** No jar in the pack depends on the mod id — no `required-after:Artifice`, no IMC address, no
  network channel, no mod-container lookup (E.2). The only two consumers are Chisel, whose table never resolved a
  name and which we now drive from our side (§9.9), and FuzziTweaks, which is ours and is being fixed anyway (E.3).
* **The upside was cosmetic** — `artifice:basalt` instead of `artifice:tile.artifice.basalt` — against a release
  that would have to carry `missingMappings` remaps for every block and item, a config migration, and a worldgen
  retrogen story.

Accepted consequence: Chisel's marble/limestone carving stays inert until its table is fixed or we register the
carving groups ourselves (E.4, option 1). Appendix A is now a record of what is emitted, not a rename proposal.

## 5. Defect catalogue

Line numbers are from `src/main/java/com/mrfuzzihead/artifice/` at commit `616bb9e`.

### 5.1 `NameMetaPair.equals`/`hashCode` contract violation — **P1**

`util/NameMetaPair.java:78` (`hashCode`) vs `:105` (`equals`).

`equals` treats metadata `-1` as "any metadata", but `hashCode` is `name.hashCode() | meta`. For `meta == -1`
every bit is set, so a wildcard key hashes to `-1` and lands in a different bucket than the concrete key it should
match. Any `HashSet`/`HashMap` lookup with the wildcard form silently misses.

This is not theoretical: `WorldGenVolcano` builds `replaced` from `new NameMetaPair(biome.topBlock)` (wildcard
metadata) and then queries it with concrete `(block, meta)` pairs, so the set never matches and the volcano's
surface-replacement logic is dead even if the feature were enabled.

Fix:

```java
@Override
public int hashCode() {
    return this.name.hashCode();   // metadata is a wildcard-or-equal, so it must not participate
}
```

Acceptance test: `NameMetaPairTest.wildcardPairIsFindableInAHashSet`, which passes now - a `-1` key and a concrete
key hash alike and match each other in a `HashSet`, while two concrete keys with different metadata still do not.
`WorldGenVolcano`'s `replaced` set therefore matches again, which matters if the volcano is ever wired up (§8).

**Status: fixed on `refactor/1.1.6`.**

### 5.2 `DispenserBehaviorSledge` NPEs on every dispense — **P1**

`item/DispenserBehaviorSledge.java:25-29`:

```java
if (ItemSledge.getIngredientsForBlock(world, target.x, target.y, target.z, stack) != null) {
    world.setBlockToAir(target.x, target.y, target.z);
    stack.setItemDamage(stack.getItemDamage() + 1);
    for (ItemStack drop : ItemSledge.getIngredientsForBlock(...)) {   // now returns null: the block is air
```

`getIngredientsForBlock` starts with `if (world.isAirBlock(x, y, z)) return null;`, so the second call always
returns `null` and the for-each throws. Cache the first result in a local. Also: `stack.setItemDamage(+1)` bypasses
`damageItem` (no Unbreaking, no creative guard, can exceed `maxDamage`).

**Status: fixed on `refactor/1.1.6`, and the NPE was hiding a second crash.** Reading the ingredients once is the
whole fix for the NPE; damage now goes through `attemptDamageItem(1, rand)` and clears the stack when it exceeds
`maxDamage`, which is what vanilla `damageItem` does without a player to attribute the break to.

With the NPE out of the way the drop loop ran for the first time, and threw **`NoSuchMethodError:
EntityItem.setVelocity(DDD)V` on a dedicated server**: `Entity.setVelocity` is `@SideOnly(Side.CLIENT)` and is
stripped from the server jar by Forge's side transformer. The motion is now written to `motionX/motionY/motionZ`
directly. This whole path had never executed on a server, which is the sort of thing the in-game harness exists to
find - and did, on the first run.

Game test `checkSledgeDispenser`: fires the behaviour at a real stone block, asserts the block is gone, exactly one
cobblestone item entity appears near the target (snapshot diff, because the test site is shared), the sledge takes
exactly one damage, an empty shot damages nothing, and a sledge at `maxDamage` is consumed. Against the old code the
group reports `threw java.lang.NullPointerException`.

### 5.3 Enchantment registration can leave nulls that are dereferenced unconditionally — **P0 in large packs**

`ArtificeEnchants.java:15-55` tries ids from `enchantmentStartID` (default 70) to 255 and leaves the field `null`
if every id is taken. Every consumer dereferences it without a null check:

* `event/ArtificeEventHandler.java:24` — `ArtificeEnchants.enchantmentSoulstealing.effectId` → **NPE on every
  player kill** for every player, server-wide.
* `event/ArtificeEventHandler.java:46` — `enchantmentResistance.effectId` → NPE on every magic/wither hit.
* `event/ArtificeClientEventHandler.java:71` — visible armour render path.
* `gui/ArtificeCreativeTab.java:45+` — opening the Artifice tab.

A GTNH-sized pack is exactly where the 70..255 window runs out (many mods use the same region). Fix: check the
field for null at each use site, or fail loudly once at registration and disable the feature (not just log a
warning). `ArtificeCreativeTab` is already only guarded by `ArtificeConfig.*Enable`, which is a *different*
condition from "registration succeeded".

**There is a second, worse failure mode, and this pack hits it.** The loop's *only* collision test is the
`IllegalArgumentException` that vanilla's `Enchantment(int, int, EnumEnchantmentType)` constructor throws on a
duplicate id — it catches that and retries the next id. Mods that replace the enchantment store (EndlessIDs, which
is in this pack, and similar conflict detectors) relocate duplicates instead of throwing, so the constructor stops
being a collision signal. Each of the three loops then succeeds on its **first** try and all three enchantments
land on id 70. The fields stay non-null, so the NPEs above do not fire; instead the three effects silently become
one, and anything enchanted afterwards gets the wrong effect.

So the fix is not only null guards — the allocation must stop relying on the constructor:

```java
public static void initEnchants() {
    final boolean[] used = new boolean[Enchantment.enchantmentsList.length];
    if (ArtificeConfig.enchantmentInvisibleEnable) {
        int id = findFreeEnchantID(ArtificeConfig.enchantmentStartID, used);
        if (id < 0) {
            ArtificeCore.logger.warn("No available enchantment IDs for invisible enchant!");
        } else {
            used[id] = true;
            enchantmentInvisible = new EnchantmentInvisible(id, ArtificeConfig.enchantmentInvisibleWeight);
            ArtificeCore.logger.info("Registered invisible enchant to ID " + id);
        }
    }
    // soulstealing, then resistance - same shape, same order
}

/** First id at or above {@code start} that is neither registered nor claimed earlier in this pass. */
private static int findFreeEnchantID(int start, boolean[] used) {
    int id = Math.max(0, start);
    while (id < used.length && (Enchantment.enchantmentsList[id] != null || used[id])) id++;
    return id < used.length ? id : -1;
}
```

`used.length` rather than a literal 256, because EndlessIDs grows the array. Keep the order invisible ->
soulstealing -> resistance and the start id, and this reproduces ids 70/71/72 exactly — which is what this pack's
worlds already contain (E.6). **Acceptance test:** the game-test mod asserts the three ids are 70/71/72 under the
stock config, so a reorder fails CI instead of a save.

This exact logic currently ships as a third-party Mixin (`FuzziTweaks`,
`ArtificeEnchantsMixin#fuzziTweaks$findFreeEnchantID`) because it was the only way to patch a released mod. Moving
it in-tree is what lets that mixin be deleted — which the namespace migration requires anyway (§0.1, E.3).

**Status: fixed on `refactor/1.1.6`.** `ArtificeEnchants.initEnchants` now allocates the way the mixin did — a
verified-free id per enchantment, `Enchantment.enchantmentsList.length` read at run time, same order and start id,
plus a `try`/`catch` that logs at error and leaves the field null instead of dying — and the four call sites ask
`ArtificeEnchants.hasInvisible()` / `hasSoulstealing()` / `hasResistance()` (config flag *and* registration
succeeded) instead of the config flag alone. The game test adds ids 70/71/72, "each enchantment occupies its own
`enchantmentsList` slot" and "no two share an id". FuzziTweaks' side is branch `refactor/drop-artifice-mixin`.

### 5.4 `MiscUtils` neighbour helpers index the side array twice — **retracted, not a defect**

`util/MiscUtils.java:63-76` and `:85-98`.

```java
Integer[] sides = {0,1,2,3,4,5};
shuffleArray(sides);
Integer[] arr$ = sides;
for (int i$ = 0; i$ < len$; i$++) {
    int i = arr$[i$];
    ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[sides[i]];   // sides[sides[i$]] !
```

This is a decompiler artefact of `for (int i : sides)` - the index is applied twice - but **the original analysis
was wrong**: it claimed the loop visits "a permutation of a permutation, some sides twice, some never". It does not.
`sides` is a bijection of `0..5`, so `{sides[sides[j]] : j}` is the image of a bijection over the complete set, which
is the complete set. Every side is visited exactly once; only the *order* differs, and the order was randomised on
purpose. There is no energy loss and no unreachable furnace.

Verified rather than argued: on the pre-fix code, both new tests below still pass (a `provideEnergyToNeighbors`
sweep over six receivers gets six distinct sides, and a lone furnace above a coil still gets heat on 200 consecutive
draws), and the in-game "coil heats the furnace in each direction" check passes on all six sides. A defect that no
test can distinguish from correct behaviour is not a defect.

Kept anyway, as readability: the `arr$`/`len$` decompiler scaffolding is gone and both helpers are a plain
`for (int side : sides)`, which removes the thing that made this look wrong in the first place.

Tests added while checking it, worth keeping as properties even though they do not pin a bug:
`MiscUtilsTest.provideEnergyToNeighborsVisitsEverySideExactlyOnce`, `.getHeatReceiverNeighborFindsAReceiverOnAnySide`,
and the in-game per-direction furnace check in `checkHeatingCoil`.

### 5.5 `MiscUtils.getAdjoinedTileEntities` flood fill is depth 1 — **P1**

`util/MiscUtils.java:103-134`. Inside the `while (!teList.isEmpty())` loop the neighbours are looked up with the
**method parameters** `x/y/z`, not the current `te.xCoord/yCoord/zCoord`:

```java
te = teList.remove(0);
...
if (world.blockExists(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ)) {   // should be te.xCoord + …
```

After the first expansion every neighbour of the origin is already in `done`, so nothing new is ever queued: the
"adjoined" set is the origin plus its immediate same-class neighbours. The method is currently unused (see §8), so
this is latent — but it is a trap for whoever wires it up.

**Status: fixed on `refactor/1.1.6`.** The expansion now uses the tile it just popped (`te.xCoord/yCoord/zCoord`),
and `MiscUtilsTest.getAdjoinedTileEntitiesWalksTheWholeConnectedRun` pins it with a 1x1x4 run of furnaces: the old
code returned two of the four, the new code returns all four. The method is still unused (§8) - fixing it did not
give it a caller.

### 5.6 `ArtificeTickHandler` never dequeues suspendable generators — **P1 (unbounded growth)**

`event/ArtificeTickHandler.java:50-70` removes a generator when `gen.getChunksToGen().size() == 0`.
`WorldGenCluster/Sulfur/Desert.C?Gen#getChunksToGen()` returns `toGen.keySet()`, and `toGen` entries are added
(`put`) but **never removed** — only the `BlockCoord` lists inside them are drained. So the size never reaches
zero, the generator stays in the deque forever, and on every 5th world tick the whole deque is scanned and
`doGeneration` is invoked for the first entry (which immediately finds nothing and returns). The deque grows with
every cluster/sulfur/niter blob ever generated, for the lifetime of the save.

Fix: track completion explicitly (`boolean isDone()` or return `true`/`false` from `doGeneration` meaning "no more
work"), or remove the chunk key when its list empties:

```java
if (list.isEmpty()) { it.remove(); }   // inside doGeneration
```

Also `doGeneration` returns `true` unconditionally, so the return value is currently meaningless.

**Status: fixed on `refactor/1.1.6`.** The "am I finished?" decision is now one shared helper,
`util/PendingChunks.hasWork(map)`: it drops the chunks whose pending list is empty and returns whether anything is
left. All four generators (Cluster, Sulfur, Desert/Niter, Cave) return that from both of their `return true;`
statements - the normal exit *and* the "neighbouring chunk is not loaded, come back later" exit - and each ensures
the current chunk has a queue entry before touching it, so a drained-then-revisited chunk cannot NPE.

`ArtificeTickHandler` no longer removes a generator from inside an iterator over that generator's own chunk set. The
sweep is a static, unit-testable pair of methods (`tickSuspendableGenerators`, `tickDelayedGenerators`): peek the
head, generate one chunk, dequeue when it reports done, and drop a generator whose chunk set is empty *without*
calling into it. A delayed generator is polled before it runs, so a generator that throws is not retried forever.

Tests: `PendingChunksTest` pins the pruning (drained chunk stops counting and leaves the map, live chunks survive,
null lists, idempotence, the key set actually shrinking) - four of its seven tests fail against a `hasWork` that only
checks `isEmpty()`. `ArtificeTickHandlerTest` pins the queue behaviour with fake generators: done leaves the queue,
suspended stays, one chunk per sweep, an empty generator is dropped without being run, order is head-first, delayed
generators run one per sweep.

### 5.7 `BlockFrameScaffold.checkStay` gives up on the first scaffold neighbour — **P2**

`block/frame/BlockFrameScaffold.java:180-202`:

```java
for (ForgeDirection d : sides) {
    BlockCoord t = c.copy().offset(d.ordinal());
    if (t.getBlock(world) instanceof BlockFrameScaffold) {
        if (t.blockEquals(world, this, meta)) {
            ... if rooted -> return true
        }
        return false;            // <-- aborts the loop even though other neighbours may be rooted
    }
}
```

The first scaffold neighbour that is not itself rooted ends the search. A support check that should walk a chain
of scaffolds therefore fails, and `onNeighborBlockChange` drops the block. Should be `continue`.

Related, same class `:151-160` (`onBlockClicked`): `while (world.getBlock(x, y, z).equals(this)) y++;` has no world
height bound; it relies on `getBlock` above y=255 returning something non-null. Add a `y < 256` guard.

`isRooted` (`:205-225`) walks from `y-1` down to bedrock on every neighbour change — for a tall scaffold tower
that is O(256) per placement and per neighbour update. See §6.

**Status: fixed on `refactor/1.1.6`** — `return false` is now `continue`, and the climb in `onBlockClicked` is
bounded at `y < 256`.

The analysis was right about the mechanism but wrong about how often it bites, and the test proved it: an
**anchored neighbour of the same tier** was already rescued by the radius search that runs inside the same
iteration (`c.getRadiusMatches(...)` scans a cube of `overhang` around the block, so a direct neighbour is always
in it). The observable case is a scaffold neighbour of a **different tier**: `t.blockEquals(...)` is false, so the
radius search never runs, and the `return false` ends the search before the loop reaches the anchored side. Two
scaffolds of different metals side by side, one of them floating, is enough to drop a block that had a good anchor.

The in-game test places exactly that: stone, a same-tier anchored scaffold to the SOUTH, the floating block,
then an other-tier scaffold to the NORTH - which is the placement that notifies the floating block. Old code: the
block is gone (`got BlockAir`). New code: it stays, `canBlockStay` agrees, and removing the anchor's ground still
drops it.

### 5.8 `BlockLogicRedstone.canConnectRedstone` uses the wrong side table — **P2**

`block/functional/BlockLogicRedstone.java:150-168` maps `side → sides[…]` with a hand-written table
(`-1→1, 0→2, 1→5, 2→3, 3→4`), while `isProvidingWeakPower` (`:133`) uses `ForgeDirection.OPPOSITES[side]`
(`0→1, 1→0, 2→3, 3→2, 4→5, 5→4`). The two disagree, so the block can render a redstone connection on a face it
does not actually read or emit on. Use `ForgeDirection.OPPOSITES[side]` in both.

**Status: fixed on `refactor/1.1.6`.** `canConnectRedstone` now reads `sides[OPPOSITES[side]]`, the same expression
`isProvidingWeakPower` uses, so the face that renders a connection is the face that emits. The unknown-direction
call (`side == -1`, which vanilla and several wire mods use to ask "do you connect at all?") returns true when any
face is configured instead of silently answering for the UP face, and an out-of-range side no longer indexes off
the end of the array.

The in-game test sets one POSITIVE and one OUTPUT face and asserts, for all six directions, that
`canConnectRedstone(side)` agrees with `sides[OPPOSITES[side]]` - plus the specific UP/NORTH cases and the `-1`
query. Against the old table, 3 of those fail (DOWN and UP both report no connection, and the "connects upward"
assertion fails).

### 5.9 `BlockOre` dereferences a possibly-absent ore-dictionary entry — **P2**

`block/world/BlockOre.java:264, 282, 308, 312` use `ItemHelper.getOre(this.name).getItem()`. For
`oreCopper/oreTin/oreSilver/oreLead/oreNickel/oreUranium`, if the pack registers no such ore (or registers it
*after* Artifice in the load order — which is common), `ItemHelper.getOre` returns `null` → NPE on block break,
silk touch, or `createStackedBlock`. GTNH packs usually have all of these, but a "no IC2/ores" pack will crash.

Fix: null-check and fall back to `Item.getItemFromBlock(this)` / the block's own drops.

**Status: fixed on `refactor/1.1.6`.** The fallbacks live in `util/OreDictFallback` (`item`, `blockStack`, `meta`) and
all four paths in `BlockOre` go through them: `getItemDropped`, `damageDropped`, `getExpDrop` and
`createStackedBlock`. With no dictionary entry the ore drops as itself, which is recoverable, rather than throwing.

Pinned where it can be: `OreDictionary.getOres(...)` hands out an immutable list in this Forge version, so an ore
cannot be unregistered in-game to drive the null path through the block. The in-game test drives the helpers with a
null entry (the exact state the old code dereferenced) for all 15 ore types, and separately asserts that
`BlockOre#getItemDropped` really does route through the entry for a registered ore, so the wiring is covered even
though the null branch is pinned one level in.

### 5.10 `TileEntityLogic.readFromNBT` indexes the sides array unchecked — **P2**

`tile/TileEntityLogic.java:100-104`:

```java
int[] sidesInt = tag.getIntArray("sides");
for (int i = 0; i < 6; i++) this.sides[i] = SideState.values()[sidesInt[i]];
```

`getIntArray` returns an empty array for a missing key, and `SideState.values()[…]` will also throw on a value ≥ 4.
A tile entity written by an older build, by a world-editing tool, or after a future rename of the key throws
`ArrayIndexOutOfBoundsException` during chunk load. Fix: length-check, clamp, default to `DISABLED`.

Acceptance test: `TileEntityLogicNbtTest.readToleratesAMissingSidesTag` (`@Ignore`d today).

**Status: fixed on `refactor/1.1.6`.** The read is length-checked and range-checked per slot: a missing key, a short
array, an empty array and an out-of-range ordinal all fall back to `DISABLED`, which is what a fresh tile entity
has. Four tests: missing, short, out-of-range (including `-1`, `99` and `Integer.MAX_VALUE`) and empty.

### 5.11 `ArtificeCreativeTab` overrides client-only methods without `@SideOnly` — **P2**

`gui/ArtificeCreativeTab.java:21` and `:35`. Vanilla `CreativeTabs#getTabIconItem`,
`#displayAllReleventItems`, `#getTabLabel`, `#getIconItemStack`, `#getCreativeTab` are all
`@SideOnly(Side.CLIENT)`. FML's `SideTransformer` **removes** those members on a dedicated server, so
`ArtificeCreativeTab`'s overrides are no longer overrides — they are new methods — and
`super.displayAllReleventItems(...)` throws `NoSuchMethodError`. Proven by the game test on a real server:

```
java.lang.NoSuchMethodError: net.minecraft.creativetab.CreativeTabs.displayAllReleventItems(Ljava/util/List;)V
    at com.mrfuzzihead.artifice.gui.ArtificeCreativeTab.displayAllReleventItems(ArtificeCreativeTab.java:36)
```

Today nothing calls it server-side, so it is latent; any server-side mod (NEI-like inventory tools, `/give` tab
browsers, recipe dumpers) that touches the tab list will hit it. Fix: annotate the overrides (and the field, or
the whole class) `@SideOnly(Side.CLIENT)`, and guard `getTabIconItem`'s callers.

Note also that `getTranslatedTabLabel()` returns the raw label, so the tab is not localised — `itemGroup.*` keys
in the lang file are unused. (There are no `itemGroup.*` keys in this mod's lang files, so returning the raw label
displays correctly; wiring up translation would need those keys first, so it is left alone.)

**Status: fixed on `refactor/1.1.6`.** `getTabIconItem`, `getTranslatedTabLabel` and `displayAllReleventItems` now
carry `@SideOnly(Side.CLIENT)`, matching the members they override.

The old game-test probe was a **false negative** and is replaced. It checked `CreativeTabs.getDeclaredMethods()` for
`displayAllReleventItems` and reported "still present", which looked like the premise was wrong. It is not: on the
dedicated server run, `CreativeTabs` does not declare the method, and with the annotations in place neither does
`ArtificeCreativeTab` — the transformer strips both. So the assertion is now *absence*: on a server, an
unannotated override survives into the jar as a new method and shows up in `getDeclaredMethods()`, and an annotated
one does not. The annotations themselves are asserted in a plain-JVM test, where stripping does not apply:
`ArtificeCreativeTabTest` reflects over every `ArtificeCreativeTab` method that matches a client-only `CreativeTabs`
member and requires the same annotation (2 of its 3 tests fail without the annotations).

### 5.12 Off-by-one bounds checks in `getIcon` — **P2**

Six places use `>` where `>=` is required, so the maximum valid index falls through to an array access one past
the end:

| File | Line | Expression | Consequence |
|---|---|---|---|
| `block/world/BlockFlora.java` | 38 | `meta > flora.length` | `meta == 5` → AIOOBE (5 is not a valid meta anyway, but it comes from NBT) |
| `block/decorative/BlockLamp.java` | 105 | `meta > 15` | nothing - `getIcon` ignores metadata entirely, so the clamp is dead code (see below) |
| `block/frame/BlockFrameBlastWall.java` | 52 | `meta > tiers.length` | `meta == 4` → AIOOBE |
| `block/frame/BlockFrameGlassWall.java` | 61 | `meta > tiers.length` | same |
| `block/frame/BlockFrameScaffold.java` | 73 | `meta > tiers.length` | same |
| `item/ItemDye.java` | 31 | `meta > icons.length` | `meta == 4` → AIOOBE |

`BlockFrameScaffold.getIcon(IBlockAccess, …)` (`:83`) is worse: it indexes `sideIcons[access.getBlockMetadata(…)]`
with no check at all. `ItemUpgrade`/`ItemCoin`/`ItemNugget`/`ItemSteel` have no check either. A single helper
(`ArrayUtils.getOrDefault(array, meta, fallback)`) would remove the whole class of bug.

**Status: fixed on `refactor/1.1.6`** with `util/ArrayUtils.getOrFirst(array, index)` (clamp to the first entry)
and `getOrDefault(array, index, fallback)`. Sites: `BlockFlora.getIcon`, `BlockFrameScaffold.getIcon` (both forms -
the `IBlockAccess` one now clamps the side-icons lookup too), `BlockFrameBlastWall`/`BlockFrameGlassWall`'s
`IBlockAccess` forms (they double-clamped; now they just delegate), `ItemDye`, `ItemUpgrade`, `ItemCoin`,
`ItemNugget`. `ArrayUtils` is null-safe and empty-safe, so a table that failed to register icons returns `null`
instead of throwing.

One correction to the table: `BlockLamp` was listed as an AIOOBE, but its `getIcon(int, int)` returns `this.icon`
and ignores metadata, so the `meta > 15` clamp it performs is dead code rather than a bug. Not changed - the clamp
is harmless and its `IBlockAccess` form just forwards. `ItemSteel` was also listed as unchecked; it branches on
`meta == 0` and never indexes, so it is fine.

Tests: `ArrayUtilsTest` (8 cases: in range, one past the end, negative, `Integer.MAX_VALUE`, null array, empty array,
null fallback, and a loop over every index of a table).

**Follow-up (round 8): the one table that was missed.** `BlockFrameBase` - the superclass of `BlockFrame` - indexed
`this.icons[meta]` in **both** `getIcon` overloads with no check at all, and its table has one entry per tier. That is
not hypothetical: the in-game suite places a frame at metadata 7 on purpose (to check the hardness clamp), so a frame
carrying 4..15 from a save, a world edit or another mod's tool crashed the client renderer. It is the only unchecked
icon table in the mod, and the harness cannot call it - a dedicated server cannot load `IIcon` or `IIconRegister` at
all (see §10.3).

The fix is a subscript helper rather than more copies of the clamp: `ArrayUtils.clampIndex(index, length)` returns the
index when it is a valid subscript and `0` otherwise, `getOrFirst` is now expressed in terms of it, and every table
that took its index straight from world metadata goes through it (`BlockFrameBase` both forms, `BlockOre`'s rock
proxy - four sites, `BlockNuclearBattery`, `ItemBlockFlora`, `ItemResource`). The point is that the subscript is
correct *by construction*, since the array access itself is the one thing no test in this repo can execute.
`ArrayUtilsTest` pins `clampIndex` over -8..23 and at both `Integer` extremes, including the 4..15 range the frame
table is exposed to.

### 5.13 `BlockRock` registers the paver texture as the antipaver texture — **P2 (visual)**

`block/world/BlockRock.java:79-80`:

```java
TextureHandler.registerConnectedTexture(reg, this, 4, "antipaver", this.name);
this.icons[4] = TextureHandler.getConnectedTexture(this, 3, 0).icon;   // 3 == paver, not 4
```

`BlockRockSlab` does it correctly (`:114-121`), so the antipaver *block* face uses the paver icon while the
antipaver *slab* uses the antipaver icon. One-character fix.

**Status: fixed on `refactor/1.1.6`** (the index is 4). Not covered by a test: icons only exist client-side, and the
in-game harness runs a dedicated server. Recorded here as a read-the-code fix.

The second half of this item - `BlockRockSlab` only registering the CTM pair when a rock with that name exists, so
`icons[2]`/`icons[3]` can stay null - is untouched.

Also `BlockRockSlab.registerBlockIcons` (`:110-124`) only registers the paver/antipaver CTM **if** a matching rock
is found by `unlocalizedName.contains(name)`; otherwise `icons[2]`/`icons[3]` stay null and `getIcon` NPEs later.
Register unconditionally.

### 5.14 `ItemSickle` area-of-effect bugs — **P2**

`item/ItemSickle.java`:

* `:138` `for (int i = -radius; i < radius; i++)` — an asymmetric window (excludes `+radius`), so the sickle
  harvests an off-centre square. Should be `<=`.
* `:126` `stack.damageItem(count / 4, player)` — integer division; harvesting 1–3 leaves costs **zero** durability.
* `:161` `stack.attemptDamageItem(count / 2, itemRand)` — same, plus `attemptDamageItem` already handles the
  break; the explicit `player.destroyCurrentEquippedItem()` afterwards is redundant.
* The leaf branch harvests each leaf with `canHarvestBlock` but then `setBlockToAir`s unconditionally, so a leaf
  type that refuses harvesting is still deleted (no drop). That is probably intended for a sickle, but it means
  leaves from mods that drop saplings on decay (instead of break) lose the drop.
* `:143-160` (the plant branch) calls `harvestBlock` and `setBlockToAir` on both sides; `harvestBlock` is
  internally guarded by `isRemote`, but `setBlockToAir` is not, so the client runs a redundant block change on
  every harvest. Harmless, but it produces client-side block-update churn in large fields.

**Status: fixed on `refactor/1.1.6`.** The window is inclusive on both axes, both damage calls are
`Math.max(1, count / n)` (a harvest always costs something), and the plant branch only changes blocks when
`!world.isRemote`. The leaf branch is otherwise unchanged: deleting every leaf in range is the sickle's job, and the
`harvestBlock`-then-`setBlockToAir` pair is what makes that work for leaves that refuse harvesting.

Game test: a 3×3 tallgrass field with a wood sickle (radius 1) harvests exactly the centre plus the four orthogonal
neighbours and leaves the four corners, and costs 2 durability. Old code harvests 3, leaves 6, and costs 1.

### 5.15 `ItemSledge` scoops recipes by matching the *unlocalised* name — **P2**

`item/ItemSledge.java:172-186`: a recipe is considered "the recipe for this drop" when
`recipe.getRecipeOutput().getUnlocalizedName().equals(input.getUnlocalizedName())`. Vanilla block items share
unlocalised names per block (`tile.stone` covers every stone variant), and any two mods can collide. The result is
that a sledge can "unsmelt" the wrong recipe — e.g. breaking a `stone` variant may use the recipe for a different
variant. Worse, the method returns the **first** match in the recipe list, so load order decides the output.

Prefer `ItemStack.isItemEqual` + `OreDictionary`-aware comparison, and skip recipes whose output has subtypes
unless the damage matches (the code already half-does this).

**Status: fixed on `refactor/1.1.6`** — the loop now matches on `output.isItemEqual(input)` instead of the
unlocalised name, skips recipes with a null output (which `RecipeBox` used to be, §5.18), and no longer relies on
`getHasSubtypes` to fake a damage comparison.

Honest about the testing: a name collision needs a mod-provided recipe to demonstrate, so the in-game test guards
the path that has to keep working instead - a crafting table unsmelts into exactly four planks, and stone still
yields its own cobblestone rather than some recipe's result. The stricter comparison is the fix; the test is the
leash.

### 5.16 `BlockFlora.updateTick` never calls `super` — **P2**

`block/world/BlockFlora.java:57-66` overrides `updateTick` and does the day/night swap but never calls
`super.updateTick`, which is where `BlockBush` drops the flower when it can no longer stay (soil removed, light
gone). Because `setTickRandomly(true)` is set, the block ticks, but the "drop when unsupported" path only runs via
`onNeighborBlockChange`. Mostly harmless, but it means a flower floating on a removed dirt block can persist until
a neighbour update.

Also `getIcon` uses `>` (see 5.12) and `getSubBlocks` excludes the closed lotus deliberately.

**Status: fixed on `refactor/1.1.6`** — the override now calls `super.updateTick` after the day/night swap.

Game test: a flower whose soil was removed with **no** neighbour notification (flags `0`, so nothing else can remove
it) survives until `updateTick` runs, and is gone afterwards. Without the super call it survives the tick too - which
is what the test showed against the old code for all three flammable subtypes.

### 5.17 `BoxCraftingHandler` NPEs on an empty box — **P2**

`recipe/BoxCraftingHandler.java:41`: `NBTTagCompound tag = slot.getTagCompound();` with no null check. An empty
box (crafted, or emptied by right-click) has no NBT; using it in any crafting recipe that fires
`ItemCraftedEvent` while it sits in the grid throws. Guard with `if (tag == null) continue;`.

`ItemBox.onItemRightClick` has the same pattern (`item/ItemBox.java:87`) but is guarded by `if (tag != null)`.

**Status: fixed on `refactor/1.1.6`** — a null tag and a stored item id that no longer resolves both skip the slot.

**Fixing it uncovered a worse bug in the same method: the box's contents were being destroyed.** The non-sneak path
only ever produced an output stack when the contents overflowed a max stack (`if (thing.getMaxStackSize() < size)`),
and the sneaking path computed `num * size - numPer`, which is zero for a single box. So crafting with a filled box -
the normal way to empty one - consumed the box and gave back **nothing** at all. It now hands over
`boxes * damage` items in max-stack chunks, using the same shape as the right-click path in `ItemBox`, which was the
working reference all along. That is a data-loss fix that was hiding behind the NPE.

### 5.18 `RecipeBox` / `RecipeUpgrade` return a null `getRecipeOutput()` — **P2**

`recipe/RecipeBox.java:144` and `recipe/RecipeUpgrade.java:170` return `this.output`, which is only set by
`matches()`. Vanilla and NEI call `getRecipeOutput()` on recipes that were not matched (recipe lists, tooltips,
the NEI recipe handler). `ItemSledge.getIngredientsForItemStack` also calls `getRecipeOutput()` on every recipe in
the list (see `:181`) — with a `RecipeBox` in the list that returns null, the null check saves it, but NEI's
handlers do not always check.

Fix: keep a separate `defaultOutput` field (or return `ArtificeItems.itemBox`'s empty stack) and never return
`null`.

**Status: fixed on `refactor/1.1.6`.** Both classes carry a `defaultOutput` (an empty box, an empty upgrade) and
`getRecipeOutput`/`getCraftingResult` fall back to it, so a caller that never matched gets a real stack instead of
null. The in-game test walks `CraftingManager` for both recipe types and asserts a non-null output with a non-null
item - against the old code the group reports `threw java.lang.NullPointerException` (on the empty-box case, which
runs first).

### 5.19 `BlockFrameDetector` keeps rescheduling itself — **P2 (lag)**

`block/frame/BlockFrameDetector.java:60-87`. Every neighbour change schedules a 2-tick update; `updateTick`
cycles metadata 0→1→2 and calls `world.notifyBlocksOfNeighborChange(...)` on every step; states 0 and 1 reschedule.
So any block placed next to a detector makes it pulse and notify its neighbours every 2 ticks, forever, until it
happens to stop at meta 2. With a redstone dust line adjacent, each notification re-triggers `onNeighborBlockChange`
on the dust and on the detector, which re-schedules. This is the classic 1.7.10 neighbour-update storm; wrap the
notification in a metadata-changed check and only notify on an edge (0→1, 1→2), not on every tick.

**Status: fixed on `refactor/1.1.6`, and the fix needed one more thing than "notify on an edge".** Only notifying on
the two power edges is not sufficient on its own: a neighbour that reacts to the falling edge notifies back, and if it
did so after we were idle again we would simply re-arm. The pulse therefore keeps three states - `META_IDLE`,
`META_ACTIVE` (the only one that emits power) and `META_TAIL` - and the tail is the point. The falling edge is written
while we are active, so the proactive metadata write (flag 1) tells the neighbours, and any echo they send back finds
us in `META_TAIL` and is dropped; the tail then returns to idle with a **silent** write (`FLAGS_SILENT`, flag 2 -
clients only, no block update), so nothing is left to re-arm. The explicit
`notifyBlocksOfNeighborChange` call is gone: the flag already notifies, and calling both meant three notifications per
pulse, one of them arriving with the power already off.

Two smaller hardenings came with it: `onNeighborBlockChange` compares the neighbour by identity (`neighbor == this`
instead of `neighbor.equals(this)`, which NPEs on a null neighbour) and refuses to arm while a pulse is in flight; and
corrupt metadata (3-15 from an older save) now heals to idle instead of falling through to the "else" branch.

Tested at both levels. `BlockFrameDetectorTest` pins the three-state table, the two power edges, the heal path and
that one pulse is three steps with one powered step. The in-game `detector pulse` test
(`ServerGameTests:checkDetectorPulse`) puts a `NeighborProbe` - a test-only block that echoes every neighbour change
back, which is exactly what dust does - next to the detector, drives the cycle by hand and counts the world's pending
scheduled updates: **32 transitions on the old code, 3 now**, ending idle with nothing scheduled.

### 5.20 `BlockLamp.onNeighborBlockChange` schedules unconditionally — **P2 (lag)**

`block/decorative/BlockLamp.java:136-140` schedules a 2-tick update for *any* neighbour change, including the
`setBlockMetadataWithNotify` the lamp itself performs. Combine with 5.19 and a lamp wall becomes a permanent
ticking machine. Check whether the computed power actually differs before scheduling.

**Status: fixed on `refactor/1.1.6`.** `targetMetadata(int indirect, int direct)` is now one helper - the old code
spelled the same two queries out in two branches, which is why there was no way to ask "would this change anything?" -
and `onNeighborBlockChange` returns early when the target already equals the metadata, and when the neighbour is the
lamp itself. `updateTick` is unchanged in effect (write only on a difference) but now reads through the helper, and the
literal `2` became `tickRate`. The inverted lamps get the same path, since only the light mapping differs.

Tested by `BlockLampTest` (the helper's max-of-two semantics, exhaustively over 0-15 × 0-15) and by the in-game
`lamp stability` test, which asserts the two things that were wrong: a lamp whose power cannot change schedules
nothing, and a lamp that does change runs **one** tick and leaves nothing scheduled. Against the old code the first
assertion fails immediately (one pending update where there should be none) and the second reports two ticks.

### 5.21 Machine config values are unvalidated and one is an integer divisor — **P2**

`tile/TileEntityNuclearBattery.java:75-78`:

```java
float ratio = (float) this.charge.getEnergyStored() / this.charge.getMaxEnergyStored();
return (int) Math.ceil(ArtificeConfig.nuclearBatteryRate * ratio);
```

None of the machine numbers in `ArtificeConfig` are validated, and one of them is used as an **integer divisor**:

`tile/TileFurnaceWrapper.java:44` — `received / ArtificeConfig.RFperFurnaceHeat`. Setting
`Machines/Amount of RF each unit of furnace burn time costs` to `0` makes every heating-coil tick that finds a
furnace throw `ArithmeticException: / by zero`. A negative value inverts the furnace's burn time. Likewise
`nuclearBatteryCapacity <= 0` makes the ratio `0/0` (`NaN`) or negative and silently disables the battery, and
`nuclearBatteryRate <= 0` makes it a no-op.

Clamp every machine value when the config is read (`Math.max(1, …)` for the divisors/rates, `Math.max(0, …)` for
`coilLoss`), and log the clamp once so the user knows their config was adjusted.

**Status: fixed on `refactor/1.1.6`, and the config surface is now testable.** `ArtificeConfig.initCommon` was split
into a file-loading wrapper plus `readCommon(Configuration)`, which is package-private and takes an already-loaded
config - so the whole 149-key surface can be read in a plain JVM. Machine values are clamped where they are read:
`RFperFurnaceHeat >= 1` (the divisor), `nuclearBatteryCapacity >= 1`, and `nuclearBatteryRate`, `heatingCoilRate`
and `coilLoss` `>= 0`. Height ranges get the same treatment, because they feed `rand.nextInt(max - min + 1)`: every
`rockLayers`/`rockClusters`/`rockCaves` min/max pair and the ender/uranium pair get `max = Math.max(min, max)`, so an
inverted range cannot throw out of worldgen.

No log line for the clamp itself: the value in the file is what the user sees, and warning on every load for a value
they may have set deliberately is noise. `ArtificeConfigTest` (5 cases) reads a hostile configuration (0, negatives,
inverted ranges) and asserts the invariants, reads a default one and asserts the documented defaults, and reads twice
to prove the clamps do not accumulate. Against unclamped code, the divisor test fails with 0.

The battery's missing charging path (§5.27) is untouched - see there.

Also: the battery has **no charging path** — `updateEntity` only ever extracts. The only energy it will ever hold
is the `charge` NBT stamped on the crafted item (§5.27). Worth deciding whether that is intended.

### 5.22 `ArtificeRegistry` logs through a possibly-null logger — **P2**

`ArtificeRegistry.java:28, 86, 161, 173, 184` call `ArtificeCore.logger.info(...)` unconditionally.

**Status: fixed on `refactor/1.1.6`** — a private `logInfo` helper null-checks the logger once, and the five call
sites go through it. Not tested: it needs another mod to call in before Artifice's `preInit`, which is a load-order
scenario the harness cannot produce. The change is a one-line guard, read and reviewed rather than executed. `logger` is
assigned in `ArtificeCore.preInit`, but `ArtificeRegistry` is a public entry point that other mods may call from
*their* `preInit`, which may run before Artifice's. Also `ArtificeConfig.setStoneList` calls it during Artifice's
own preInit (after the assignment, so that path is fine). Guard with a null check or assign the logger in a static
initializer.

### 5.23 Config: the marble/basalt list separator does not match its comment — **P3**

`ArtificeConfig.java:516-517` splits on `";"`, but the config comment (`:282`) says
`"Comma-seperated list of Name:Meta pairs"`. Anyone following the comment gets a parse failure and a warning.
Fix the comment (and consider `Name@Meta` to match the IMC format used by `compat/IMC`).

Related: `setDimBlacklist` (`:487-499`) splits `""` into `[""]`, fails `Integer.parseInt("")` and logs
`"Failed to load the dimension blacklist"` on **every fresh install**. Skip empty input.

**Status: fixed on `refactor/1.1.6`.** Both comments now describe the parser (`Name;Meta`, comma separated) and name
the example format; `parseDimensionList` and `parseStonePairs` are pure helpers that skip blanks and tolerate
whitespace. The warning is only logged when a configured entry actually failed to parse, so a fresh install is
silent. `ArtificeConfigTest` covers empty, blank, null, junk, doubled commas and a pair with no separator.

### 5.24 Config is loaded twice per side — **P3**

`ArtificeCore.preInit` calls `ArtificeConfig.initClient(evt)` *and* `initCommon(evt)`, each of which constructs
its own `Configuration` over the same file and calls `load()`+`save()`. It works (both see the same file), but it
is four file writes per launch, and `initClient` runs on the server where the `Client` category is meaningless.
Fold the client keys into `initCommon` (or guard `initClient` with `FMLCommonSetupEvent`/side check).

**Status: fixed on `refactor/1.1.6`** — the three `Client` keys are read by `readCommon`, so there is one
`Configuration` and one save per side. `initClient` is an empty deprecated stub for binary compatibility with
anything that called it. `ArtificeConfigTest.clientKeysAreReadByTheCommonPass` asserts both the default and that the
key is honoured.

### 5.25 `ArtificeEnchants` cannot distinguish "disabled" from "failed" — **P3**

`ArtificeEnchants` only logs a warning when it cannot allocate an id. Combine with 5.3 and the config flags are
misleading: `enchantmentSoulstealingEnable == true` does not imply `enchantmentSoulstealing != null`. Either
expose a `isSoulstealingAvailable()` helper and use it everywhere, or (better) fail the registration and clear
the config flag.

**Status: fixed on `refactor/1.1.6`** via the first option: `hasInvisible()`/`hasSoulstealing()`/`hasResistance()`
answer "config flag *and* registration succeeded", and every use site asks them (§5.3's fix). The config flag is
left untouched on purpose - rewriting a user's config file to disable an enchantment they asked for is a surprise,
and the helper makes the flag's meaning unambiguous without it.

### 5.26 `EnchantmentResistance` armour damage picks a random piece — **P3**

`event/ArtificeEventHandler.java:58-64`:

```java
int toDamage;
do { toDamage = rand.nextInt(4) + 1; } while (getEquipmentInSlot(toDamage) == null);
getEquipmentInSlot(toDamage).damageItem(1, entityLiving);
```

It can pick an armour slot that does **not** carry the resistance enchantment, and it damages one piece per hit
regardless of the number of enchanted pieces. It also only terminates because some piece exists; if the
enchantment is on a slot the loop skips, it damages the wrong armour. Iterate the four slots and damage the ones
that actually have the enchantment.

**Status: fixed on `refactor/1.1.6`** — the loop walks slots 1..4 and damages every piece that carries the
enchantment, so a hit wears the armour that is actually absorbing it.

Game test: diamond helmet with resistance I plus a plain diamond chestplate, 20 magic hits. New code: 20 on the
helmet, 0 on the chestplate. Old code: 16 and 4 - the random pick hit an unenchanted piece four times.

### 5.27 The nuclear battery recipe ships a full battery — **P3 (balance), open**

`recipe/ArtificeRecipes.java:209-222` sets `"charge" = nuclearBatteryCapacity` on the recipe output, and
`BlockNuclearBattery.onBlockPlacedBy` adopts it. So one craft = 1,000,000,000 RF from nothing (there is no way to
recharge, so this is the *only* source). If that is intended as "a battery comes pre-charged", fine, but then the
capacity default and the recipe cost should be revisited; if not, drop the NBT from the recipe output.

**Status: open, waiting on a decision.** The recipe still stamps the full capacity. Whichever way it goes, the in-game
battery test already reads the crafted stack's NBT, so the change is two lines with a test in place.

### 5.28 `BlockFrameGlassWall` / `BlockFrameScaffold` `getIcon(IBlockAccess)` mismatch — **P3**

`BlockFrameScaffold.java:89` returns `sideIcons[meta]` with the raw metadata (no bound check, see 5.12) while the
`getIcon(int,int)` overload uses the masked value. Same idea in `BlockFrameGlassWall:61/68`.

**Status: fixed on `refactor/1.1.6`** as part of §5.12: both `IBlockAccess` overloads now delegate to the
`(int, int)` form, which clamps through `ArrayUtils.getOrFirst`.

### 5.29 `ItemBlockSlabArtifice` tooltips never match — **P3**

`block/decorative/ItemBlockSlabArtifice.java:85` builds the key from `stack.getUnlocalizedName()` (a
*localisation* key like `tile.artifice.basalt.slab.cobble`) while `ArtificeRegistry` keys tooltips by
`(Item, damage)` registry identity. The lookup can never hit. Use `new NameMetaPair(stack.getItem(), damage)`, as
`ItemBlockArtifice` does.

**Status: the tooltip half is fixed on `refactor/1.1.6`** — the lookup uses
`new NameMetaPair(stack.getItem(), damage)` like the other item blocks. Game test: the four paver/antipaver slab
metas resolve under their registry identity, and the unlocalised-name key the item block used to build is asserted
to be a different key (i.e. the old lookup could not have matched).

The `getHalfSlab`/`getDoubleSlab` half is **not** done. It is a refactor of working code and belongs with §7.3
(registry names as constants) rather than as a drive-by change here.

Also, `getHalfSlab`/`getDoubleSlab` (`:26-88`) resolve the slab by substring-matching the unlocalised name
(`contains("gray")` after `contains("lightgray")`, …). It works today but breaks the moment a name changes, and it
is the kind of code a registry-name change would silently break. Replace with a static map from
half-slab → double-slab built in `ArtificeBlocks`.

### 5.30 `FrameRenderer.getRenderId()` returns 0 — **P3**

`render/FrameRenderer.java:62` returns `0` while `ClientProxy` registers it under
`ArtificeConfig.frameRenderID`. `RenderingRegistry` uses the id passed to `registerBlockHandler`, so rendering
works, but `ISimpleBlockRenderingHandler#getRenderId` is part of the contract and is used by some tools/NEI
plugins. Return `ArtificeConfig.frameRenderID`.

**Status: fixed on `refactor/1.1.6`.** Not tested: the renderer is client-only and the harness is a dedicated
server.

### 5.31 `OreRenderer` uses a mutable static as render state — **P3**

**Status: not changed.** It is a client-render refactor with no server-side observable and no test that can hold
it honest; the review's own framing is "prefer", and doing it blind risks the ore rendering for no verified gain.

`render/OreRenderer.java:82` reads `ClientProxy.renderPass`, which `BlockOre.canRenderInPass` writes as a side
effect. It works because the render pass is single-threaded, but it makes the renderer non-reentrant and is the
reason `BlockOre.getRenderBlockPass()` returns `1` for both passes. Prefer passing the pass through the
`RenderBlocks` instance or using two render ids.

### 5.32 `BlockEnderOre.getItemDropped` and friends ignore metadata — **P3**

**Status: not changed - and the row's reasoning was wrong.** `damageDropped` is load-bearing. Forge's
`Block#getDrops` builds each drop as `new ItemStack(getItemDropped(meta), 1, damageDropped(meta))`, so without it
ender ore drops `itemResource` metadata **0**, i.e. sulfur dust. Now asserted in game for both the normal and the
glowing block through the real `getDrops` path, which is also what makes the row's "harmless, remove them" advice
verifiable rather than argued. `getDamageValue` is left alone for the same reason: it is a live hook whose only
effect is on corrupt metadata, and there is no gain in changing that.

`block/world/BlockEnderOre.java` has no metadata subtypes, yet `getDamageValue`/`damageDropped`/`getDrops` are
defined as if it did. Harmless, but it is copy-paste from `BlockSulfur`, and it hides the fact that the glowing
variant drops the same item (which is correct).

### 5.33 `WorldGenCluster`/`WorldGenCave` mutate the generator instance per biome — **P3 (thread safety)**

**Status: not changed.** The concern is real but conditional: 1.7.10 chunk generation runs on the server thread
unless a mod offloads it, and the fix means threading the chosen block through `ClusterGen`/`CaveGen` - a worldgen
edit with no test that can catch a mistake. It is better done as part of the §7.1 dedupe, where the two generators
become one and the mutation disappears by construction.

`world/WorldGenCluster.java:95-112` and `world/WorldGenCave.java:109-126` assign `this.block` while choosing a
limestone variant from the biome. `IFeatureGenerator`s are shared across chunk-generation threads in 1.7.10
(CoFH's `WorldHandler.generateWorld` may be called from the chunk populator), so two chunks generating
concurrently can swap each other's stone type mid-blob. Use a local variable and pass it to `ClusterGen`.

### 5.34 `ArtificeBlocks` registers slabs with `getUnlocalizedName()` — **P3**

`ArtificeBlocks.java:194, 210`: `blockBasalt.getUnlocalizedName() + ".slab"` embeds `tile.` twice in the registry
name (`Artifice:tile.artifice.basalt.slab`). It also couples the registry name to the *localisation* key. Give
slabs an explicit registry name.

**Status: not doing this.** It is a registry-name change, and §4 decided that registry names are frozen: every
`Artifice:tile.artifice.*` string is inside the CoFH retrogen hashes (E.8) and in pack configs (E.7). The coupling
to the unlocalisation key is ugly, but it is also *stable*, which is the property that matters here. Recorded as a
deliberate no.

### 5.35 The `/calc` command is registered but non-functional — **P3**

`command/CommandCalc.java`: the `DoubleEvaluator` field and `evaluate` call are commented out
(`:44-46`), so `/calc 1+1` prints `1+1 = ` and nothing else. `canCommandSenderUseCommand` returns `true`
unconditionally (no permission check). Either restore the evaluator (it needs the `fathzer:javaluator`
dependency) or delete the command — currently it is an untested, broken command exposed to every player.

**Status: deleted on `refactor/1.1.6`** — `CommandCalc`, its registration, and its three lang keys are gone. It
printed `1+1 = ` and nothing else, and every player could run it. Restoring it is a two-line diff plus the
dependency if anyone wants it back; a broken command is worse than no command.

### 5.36 The sneak-tracking protocol is fragile — **P3**

`Tracking.sneaks` is a static `List<Integer>` of entity ids used on **both** sides:

* `ArtificeClientTickHandler` adds/removes the local player's id each tick and sends a packet.
* `SneakMessageHandler` toggles the *same* id in the server's copy.
* `BoxCraftingHandler` (server) reads it to decide "shift-click" behaviour.

Problems: (a) ids are never removed on logout/disconnect, so a stale id can collide with a new player's id and
silently enable shift-behaviour; (b) the client already updated its own copy before the packet round-trip, so the
server and client can disagree for a tick; (c) `ArrayList` plus `contains` on a hot path. Prefer the vanilla
`EntityPlayer#isSneaking()` on the server (it is synced every tick in 1.7.10 — `EntityPlayerSP`/`EntityPlayerMP`
both track it) and delete the packet entirely.

**Status: the decision path is fixed on `refactor/1.1.6`; the protocol is still there.** `BoxCraftingHandler` now
asks `player.isSneaking()`, which removes (a) and (b) from the only place that decides behaviour. The game test sets
`player.setSneaking(true)` instead of adding an id to `Tracking.sneaks`.

**Status: deleted (round 8).** The follow-up is done: `ArtificePacketSneak`, `ArtificePacket`,
`ArtificeMessageToMessageCodec`, `SneakMessageHandler`, `PacketDispatcher`, `ArtificeClientTickHandler` and
`Tracking.sneaks` are gone, along with the mod's only network channel - `CommonProxy.init` now registers just the GUI
handler. Nothing else subscribed: the channel was created for this packet and no other class touched it, which is
what a grep of `artificeChannel` showed before deleting. The one client-only tick handler was the only thing that
could observe a key press, so removing it removes the (a) stale-id and (b) client/server disagreement problems
outright rather than mitigating them.

---

## 6. Performance

Status key: **done** = fixed; **open** = still there; **left** = deliberately not changed.

| Location | Issue | Fix | Status |
|---|---|---|---|
| `event/Tracking.java` | `getHighestFrequencyPower` scanned **every loaded tile entity of every world** for every attuned block, every tick (`TileEntityAttuned.updateEntity`). With N attuned blocks and M tile entities this was O(N·M) per tick. `BlockAttunedRedstone.onNeighborBlockChange` paid the same price on every redstone update. | Maintain a `Map<String, Set<TileEntityAttuned>>` in `Tracking`. | **done** |
| `event/ArtificeTickHandler.java:50-70` | Unbounded growth (see 5.6) plus an O(deque) scan every 5 ticks that never shrinks. | Track completion; remove on empty. | **done** (5.6) |
| `item/ItemSledge.java` | Every block break scanned the entire `CraftingManager` recipe list (thousands of entries in a GTNH pack). | Index shaped recipes by output item. | **done** |
| `world/WorldGenLayer.java` | Built the same `NameMetaPair` twice per block (four world reads, two registry lookups on a hit) and did it for air too. | Read the block once, skip air. | **done** |
| `util/MiscUtils.java:63-98` | Called per coil/battery per tick; built and shuffled a 6-element `Integer[]` each call (two allocations, six boxes) through `Math.random()`, which is one shared `Random` behind a lock. | Static `int[]` rotated by a random start. | **done, differently - see below** |
| `block/frame/BlockFrameScaffold.java` | `isRooted` walks from `y-1` to 0 on every neighbour change; `checkStay` then called `getRadiusMatches` over a `(2r+1)³` cube (up to 33³ ≈ 36k blocks) **per candidate**, and `BlockCoord.isConnected` is a `LinkedList`-based flood fill with `contains()` (O(n²)). Placing a scaffold tower was O(height · r³ · n²). | One flood fill with a visited set. | **done** |
| `block/world/BlockOre.java:142-204` (`sparkle`) | Called from `randomDisplayTick` on the client for every glowing ore in view; spawns up to 6 particles per call. Two of the six `world.getBlock` calls happen even when the first check fails. | Fine for a handful of ores; avoid `setTickRandomly` + `randomDisplayTick` double duty. | left (client-side) |
| `compat/*` | Each compat class catches `Throwable` and prints a stack trace. Fine at load: the remaining classes are load-time only, and the two that used reflection now compile against the real API (§9.10). | Nothing. | left |
| `render/RenderBlocksInverted.java` | 2,830 lines duplicating `RenderBlocks` just to render the inverted faces of frames. | Only on the client render path, but see §7. | left (client-side) |

### 6.1 Tracking: an index of tiles, not a cache of power

The attuned transmitter asks its question once per tick, and the answer used to be a walk of
`world.loadedTileEntityList` for every world. The tile entities now file themselves under their frequency as they
`validate()` and `invalidate()`, so a query sees only the blocks that share its frequency. Two details decided the
shape:

* **It indexes tile entities, not values.** Receiver power is a `getBlockPowerInput` read, i.e. a redstone input that
  can change at any moment. A cached maximum would go stale on the next lever pull; the query still reads each
  tile's live `power` field, exactly as the scan did, and still checks that the block there is really the receiver.
* **`frequency` had to become private.** A direct field write leaves the tile filed under its old key and silently
  stops coupling. The gametest did exactly that, and had to be converted to `setFrequency()`; external code that
  asserts on the field will now fail to compile rather than misbehave at runtime.

Entries leave by three mechanisms: `invalidate()`, a `WorldEvent.Unload` hook (a world that is unloaded never ticks
again, so its tiles never reach `invalidate()`), and a sweep of invalid entries during a query. Only the third is
load-bearing for correctness; the others keep the lists small.

### 6.3 The scaffold walk: one flood fill, and the safety net that shaped it

`checkStay` asked "is there an anchored block of my kind within my tier's overhang, reachable from here?" by scanning
the whole `(2r+1)³` cube and then running a flood fill **per candidate** in it, each flood fill using
`LinkedList.contains` and allocating a `BlockCoord` per neighbour. At tier 3 that is 35,937 block reads plus millions
of list comparisons for a question with one answer. It is now a single flood fill over the connected run: visit each
block once, test the anchor distance as you go.

The safety net came first and earned its keep. `BlockFrameScaffoldSupportTest` carries the old algorithm transcribed
as a reference and compares it with the shipped code over 900 random configurations plus curated cases. Two of my
attempts at the flood fill were wrong in ways I would not have reasoned out:

* **bounding the traversal by the overhang breaks a ring** - an anchor within reach can be connected only through
  blocks outside it, which is why the old code bounded the distance check and not the search;
* **the walk may only start if one of the five sides is the same tier, but once started it covers the whole
  component** - including through the block above. The old code's two halves disagreed about that, and the test says
  so.

That second point is the behaviour recorded in §9.11. `BlockCoord.isConnected` and `getRadiusMatches` have no callers
left (§8).

### 6.2 MiscUtils: why not the static rotation

The review suggested a static `int[]` rotated by a random start, which would have removed the last allocation. It
is not a fair order for the question being asked. Both callers hand something valuable to the first side they find -
the heating coil's entire tick of heat, the battery's entire tick of energy - so "which side wins" has to be
uniform. With candidates on two sides, a rotation from a uniform start visits one of them first in **4,037 of 6,000
draws** instead of 3,000: a coil between two furnaces would have fed one of them two thirds of the time and let the
other starve.

What shipped is a primitive `int[]` and a Fisher-Yates shuffle against a private `Random`: no boxing, no lock, one
six-element array, against six `world.getTileEntity` calls. `getHeatReceiverNeighborIsFairBetweenTwoAdjacentReceivers`
pins the property and fails on the rotation.

---

## 7. Refactors

### 7.1 One blob generator instead of four - **done**

`WorldGenCluster.ClusterGen` (`:139-215`), `WorldGenSulfur.SulfurGen` (`:145-230`) and
`WorldGenDesert.NiterGen` (`:140-225`) are near-identical: same `TMap<ChunkCoord, ArrayList<BlockCoord>>`, same
threshold walk, same `setBlock`. `WorldGenCave.CaveGen` is a variant. Extract:

```java
abstract class BlobGen implements ISuspendableGen {
    protected abstract boolean canReplace(World w, BlockCoord c);
    protected abstract void place(World w, BlockCoord c);
}
```

and delete the three copies. This also fixes the shared `toGen` bug (5.6) once.

**Status: done.** `world/BlobGen` holds the walk - per-chunk candidate queue, expand the neighbours of the current
position, pick a candidate at random, replace it, repeat until the blob is the configured size - and each feature is a
fifteen-line subclass saying which blocks it may replace (`canGen`), what a replacement looks like (`place`), and
whether its budget survives a suspension.

    Cluster  255 -> 137    Sulfur 206 -> 117    Desert 197 -> 108    Cave 265 -> 146    (+ BlobGen 168)

447 lines gone. The two differences the four copies had drifted into are now explicit arguments rather than duplicated
loops, and both are worth knowing because neither was chosen: sulfur and niter replace whatever their candidate check
approved, while cluster and cave replace stone types and convert another mod's ore into their own variant of it; and
caves keep the generation budget in a field, so a suspended generator stops at `size` blocks in total, while the other
three keep it in a local, so each resume gets a fresh budget.

The proof that this changed nothing is `checkWorldGenFingerprints` (§10.1): a hash of the 3x3 chunk area each
generator produces for a fixed seed over a prepared band. All four are unchanged.

### 7.2 One frequency-probability helper instead of eight copies

The expression

```java
int total;
if (this.frequency >= 100) {
    total = rand.nextInt(100) < Math.abs(this.frequency % (int) Math.pow(10.0, (int) Math.log10(this.frequency)))
        ? this.frequency / 100 + 1
        : this.frequency / 100;
} else {
    total = rand.nextInt(100) < this.frequency ? 0 : 1;
}
```

appears verbatim in **eight** generators (`WorldGenLake`, `Volcano`, `Flowers`, `Lily`, `Sulfur`, `Desert`,
`Cluster`, `Cave`). Besides the duplication it is subtly broken:

* **`frequency = 0` means "once per chunk", not "off".** The `else` branch is
  `total = rand.nextInt(100) < 0 ? 0 : 1`, which always yields `1`. A user who sets 0 to disable a feature gets it
  at the maximum guaranteed rate. (The `*Gen` booleans are the real off switch, so this only bites someone who
  zeroes the frequency.)
* **Below 100 the semantics invert.** `99` yields one blob 1% of the time, `100` yields one 100% of the time.
  A user who sets 99 gets a feature 100× rarer than 100, which is the opposite of what the config comment says.
* **Above 1,000 the remainder test saturates.** The divisor is `10^floor(log10(f))`, but the roll is
  `rand.nextInt(100)` (0..99). For `frequency = 1500`, `1500 % 1000 == 500`, `rand.nextInt(100) < 500` is always
  true, so the generator yields **16** blobs per chunk instead of 15.5. `1900` yields 20 instead of 19.5. The
  `Math.pow`/`Math.log10` round trip is also a floating-point trap and entirely unnecessary: using `frequency % 100`
  against the same 0..99 roll gives the intended proportional fraction at every magnitude.

Replace with a single documented helper:

```java
/** Expected blobs per chunk: frequency is a percentage, 100 = one per chunk. */
static int rollsThisChunk(Random rand, int frequency) {
    if (frequency <= 0) return 0;
    int whole = frequency / 100;
    int remainder = frequency % 100;
    return whole + (rand.nextInt(100) < remainder ? 1 : 0);
}
```

Bonus: this becomes a pure, unit-testable function — exactly the kind of thing the new JUnit suite should own.

**Status: done on `refactor/1.1.6`.** `world/GenFrequency.rollsThisChunk(rand, frequency)` is now the only copy;
the eight generators call it. `GenFrequencyTest` pins the contract: 0 and negatives place nothing, 100 is exactly
one per chunk, below 100 is the chance of one (1%, 25%, 50%, 99%), above 100 scales linearly (150 → 1.5,
1550 → 15.5), 1500 is exactly 15 and 1900 exactly 19 (the saturation the review described), the whole part is
always present and never exceeded, and the function is seed-deterministic.

One correction to the numbers above: `1500 % 1000 == 500` made the old extra-roll branch always true, so 1500
produced 16 - and the "intended" 15.5 does not apply to it either, because `1500 / 100` is exactly 15. The
half-blob examples are 1550 and 150. The semantics change below 100 is real and deliberate: the config key is
"Percentage … Frequency", and the defaults are 100 (150 for ender ore), so no default changes behaviour.

### 7.3 Registry names as constants - **done**

Every `registerBlock`/`registerItem` call was made from a constructor with a literal name. Forty-five literals across
thirty classes are now in `util/Names`: one constant per fixed name, and a builder per family (`rock`, `slab`,
`stairs`, `ore`, `lamp`, `torch`, `tool`) for the ones composed from parts.

**Status: done.** No name changed - the 155-entry frozen list from the previous batch is the proof, and it is what
made the sweep safe to do at all. Two names are deliberately not centralised: the bucket keeps its bare vanilla-style
name, and the fluid blocks take theirs from CoFH's base class, which is handed the namespace.

`NamesTest` pins the builders (case folding for ores, the `tile.` strip in stairs, the inverted lamp prefix) and that
no two constants collide - except the steel block and the steel item, which share `artifice.steel` across two separate
registries and always have.

### 7.4 `ArtificeBlocks.initBlocks` is one 150-line method - **done**

Split into `initFlora`, `initRocks`, `initOres`, `initFluids`, `initDecorative`, `registerRockBlocks`, `initFrames`,
`initFunctional` - the groups it always had, called in the order it always ran them. The method constructed ~120
blocks and registered ~40 of them in one go, with slab registration and four ore-dictionary batches interleaved
between the construction lines.

**Status: done.** Pure extraction: the concatenated statement sequence is byte-identical to the original 125
statements, verified mechanically rather than by eye, so no block id moved - which matters, because block ids are
assigned in registration order and a world's id table only stays valid if that order does. `initBlocks` says so out
loud now, and two groups carry the reason they sit where they do: `initFluids` defers to a fluid another mod may have
registered, and `registerRockBlocks` has to run after the rocks and before the frames.

### 7.5 The tooltip registry is 226 registration calls - **done**

`ArtificeTooltips` (752 lines) was a wall of `ArtificeRegistry.registerTooltip(new ItemStack(...), ...)` - 226 calls,
rebuilding the `ItemStack` for every line of every stack.

**Status: done.** Consecutive lines for the same stack go through one `tooltips(stack, line, ...)` call: 226 calls
become 116 and the file loses 176 lines. The collapse was mechanical and checked - a parser grouped only calls that
were adjacent, shared a textually identical stack, and were separated by nothing but whitespace - so order and content
are unchanged by construction.

The lookup was five copies of the same loop, and they had drifted: four honoured `ArtificeConfig.flavorText` and the
box did not. `ArtificeRegistry.appendTooltips` does it once, with the box passing `allowFlavour = false` explicitly, so
the difference is a decision rather than an omission. The sledge and sickle looked tooltips up at damage 0, which was
harmless but said the wrong thing; they now use a wildcard item lookup that would survive a tool gaining metadata.

The in-game tooltip test pins the shape of the two multi-line stacks (a dye: name then flavour; a sledge: name, loss
chance, flavour) and that the flavour line carries the comment code the renderer filters on.

### 7.6 Unchecked collections - **done**

`ArtificeTickHandler` and the four suspendable generators (`WorldGenCluster`, `WorldGenSulfur`, `WorldGenDesert`,
`WorldGenCave`) held `TMap<ChunkCoord, ArrayList<BlockCoord>>` and then cast the value out of it:
`((ArrayList) this.toGen.get(chunk)).add(t)`. The cast asserted what the declaration already said, and would have
turned a declaration change into a `ClassCastException` at worldgen time. `ItemSledge`'s ore-dictionary ingredient
list was checked as `instanceof ArrayList`; it is a `List` of candidates where the first stands for the slot.

All of them are parameterised now, with no behaviour change. What is left of §7.6 is `ArtificeRegistry`'s
`ArrayList<Object>` input lists, which come from `ShapelessOreRecipe.getInput()` - a raw `ArrayList` in 1.7.10, so
there is nothing to parameterise at the source. Most of these are decompiler
artefacts and are safe to parameterise.

### 7.7 Magic numbers that deserve names - **done**

* `BlockOre.INDEX_REDSTONE` / `INDEX_ENDER` - the two ore-table indices with a particle effect. The table order was
  already pinned by `RegistrationInvariantsTest`; the test now names the constants too.
* `TileFurnaceWrapper.MAX_HEAT` - 1600 **RF**, which at the default 8 RF per burn tick is 200 burn ticks, i.e. one
  item, and the same budget a piece of coal carries. Worth saying out loud that the cap is in RF and not in ticks: a
  pack that changes the exchange rate changes how much smelting the cap buys.
* `TileEntityHeatingCoil.ENERGY_BUFFER` - the RF buffer, which shares the value with the heat cap by coincidence.
* `ArtificeEventHandler.RESISTANCE_PER_LEVEL_PERCENT` - the 7 in `reslvl * 7 / 100.0F`.
* `BlockLamp.TINT_DARKENED` / `TINT_INVERTED` - the two render-tint offsets, used by both the fixed and the
  per-block colour paths.
* `BlockFrameScaffold.CLIMB_EDGE` / `COLLISION_INSET` - naming these turned up a detail worth recording: the
  collision box is inset by **0.135** from the block edge, not 0.125, because `Block`'s own bounds are already
  inset by 0.01. The constant is additional to the bounds, which is not obvious from either number alone. The new
  in-game assertion compares against `getBlockBoundsMinX()` for exactly that reason.

Tests: the furnace wrapper's cap and its refusal to overfill (in-game, on a real furnace), and the scaffold's
collision box against the block's own bounds.

---

## 8. Dead code and broken features

| Item | Evidence | Recommendation |
|---|---|---|
| **Volcano worldgen** | `WorldGenVolcano` (44 lines), `VolcanoGen` (200 lines), `VolcanoHelper` (93 lines), `blockTephra`, `ArtificeConfig.volcanoGen/volcanoFrequency`, and two lang entries exist, but `ArtificeWorld.initWorldGen()` never registers `WorldGenVolcano`. | Either register it (`WorldHandler.addFeature(new WorldGenVolcano())` when `volcanoGen`) and fix §5.1 so it works, or delete the 350 lines. **It currently does nothing.** |
| `VolcanoGen.genTephra`, `genCaldera` (empty body), `coverFeature`, `VolcanoHelper.get2dSINDistributedCoord` | Unreferenced. | Delete with the feature, or wire into `VolcanoGen`. |
| `IDelayedGen` + `ArtificeTickHandler.delayedGenerators` | Only implemented by `WorldGenVolcano.VolcanoGen` → entire delayed-gen path is dead. | Delete with the volcano. |
| `HeatStorage` | Implements `IHeatProvider`/`IHeatReceiver` and has tests, but **no tile entity uses it** (both machines use CoFH `EnergyStorage`). | Keep if it is intended as an API; otherwise delete. Its tests are the only reason it is covered. |
| `XSRandom` | Never instantiated. | Delete, or use it in worldgen for determinism. |
| `MiscUtils.getAdjoinedTileEntities` | No callers (it is the buggy depth-1 flood fill from §5.5). | Delete, or fix and use it for multiblock coils. |
| `BlockCoord.isConnected` / `getRadiusMatches` | The scaffold support walk used both until §6.3 replaced it with one flood fill; nothing else calls them. | Delete, or keep as utility API - they are public and the support test still transcribes them. |
| `ComparableItemStack` / `ComparableItemStackNBT` | Used only by `RecipeBox.matches`. | **done**: the unchecked cast and a null-item dereference both answer `false` now, and the in-game "box stack comparison" test covers the pair (it cannot be headless - the harness has no bootstrapped item registry). |
| `ArtificeCore.modChannel`, `modVersion`, `ArtificeCore.modName` | `modChannel` unused; `modVersion` duplicates the `@Mod(version=…)` literal; `modName` duplicates `name=`. | **done**: the annotation and `@Instance` read the constants, `modChannel` is deleted, and `RegistrationInvariantsTest` pins the version against the annotation. The network channel keeps its literal on purpose - it is the wire protocol's name and must not follow a future id change. |
| `CommandCalc` evaluator | See §5.35. | Delete the command or restore the dependency. |
| `BlockRock.isReplaceableOreGen` | **Not dead code - this row was wrong.** `WorldGenMinable.generate` calls `isReplaceableOreGen` on the block it is about to replace, passing the block it intends to replace. | Keep, and it is now documented in place and pinned by the in-game drop test. Deleting it would have stopped other mods' ore generators from putting veins through basalt or marble. |
| `BlockOre.tickRate` | Returns 30 but `setTickRandomly` uses the random-tick rate, not `tickRate`. | **done**: dropped. Verified first - nothing in 1.7.10 calls `Block.tickRate`; `World` and `WorldServer` never reference it. |
| `FrameRenderer.getRenderId()` | Returns 0 (see §5.30). | Fix. |
| `FormatCodes.RandomChar` / `Strike` / `Underlined` | Only `Italic`, `Aqua`, `Yellow`, `Reset`, `DarkGrey` are used. | Harmless; keep as a complete table. |
| `ArtificeBlocks.blockDummy` | Registered nowhere (intentionally — it is a render proxy for `OreRenderer`). | Add a comment; the game test deliberately excludes it. |

---

## 9. Notices, compat observations and feature ideas

### 9.1 Ore-variant generation rewrites other mods' ores

`ArtificeBlocks.initOreSet` (`:264`) builds `oreSet` from **every** stack registered under each `oreNames` entry —
including other mods' ores. `WorldGenCluster`/`WorldGenLayer` then *replace* any block in `oreSet` with Artifice's
own ore block variant (`BlockOre.getOre` → `setBlock`). So generating an Artifice basalt cluster through a
Thermal Expansion copper vein converts that vein to `artifice:tile.artifice.orecopper@basalt`.

That is presumably the intended "ore variants in our rocks" feature, but it means Artifice **mutates worldgen
owned by other mods**, and it happens for any ore name Artifice knows, with no config gate and no logging. Two
concrete risks: (a) a pack that counts ores (vein miner, ore-finder tools) sees different blocks; (b) it can
cascade with CoFH's retrogen. Strongly consider a config flag (`convertOtherModOres`, default on) and a per-ore
log at debug level.

**Status: open, waiting on a decision.** `initOreSet` still collects every mod's stacks for each `oreNames` entry, and
`WorldGenLayer`/`WorldGenCluster` still replace them. It is the one remaining item that changes what the world
generator does to another mod's terrain, which is why it is a decision and not a patch.

### 9.2 Ore smelting recipes never register — **fixed**

`ArtificeRecipes.registerOreRecipes` (`:842`) runs in `init`, but the ore-dictionary entries for `blockOres` only
appear in `postInit` (`ArtificeBlocks.registerOreVariants`). `ItemHelper.getOreName(stack)` therefore returns
`"Unknown"` for every one of the 15×9 ore blocks and no smelting recipe is added. Move `registerOreVariants()` to
`preInit` (after `initBlocks`), which also makes the ore dictionary complete before other mods' `init` runs — a
compat improvement in itself.

**Status: fixed on `refactor/1.1.6`** — exactly that move: `registerOreVariants()` is called from `preInit` right
after `ArtificeBlocks.initBlocks()` and removed from `postInit`. `initOreSet()` (which collects *other* mods' ores)
stays in `postInit`, where it belongs.

The in-game test checks all 8 vanilla-mapped ores × 9 rock variants against the smelting registry (72 assertions).
Against the old ordering it reports "the ore smelting recipes registered at all (1 ores)" - one ore was getting a
recipe from a different code path, which is why the miss was easy to overlook.

### 9.3 `BlockFrame` hardness scales with metadata — including invalid metadata

`BlockFrame.getBlockHardness` returns `meta + 5`. `BlockRock`/`BlockRockSlab` accept metadata up to 7 (and
`BlockRock.getIcon` clamps at 6), so a block placed with corrupt metadata 7 has hardness 12. Harmless, but a
hardness table indexed by tier would be clearer.

**Status: done.** Hardness is a four-entry table in tier order, clamped to the first tier for metadata that is not a
tier - the same treatment §5.12 gave icons - so corrupt metadata 7 now behaves like Basic (5.0) instead of inventing
12. Asserted in-game for every tier, and for metadata 7 both through the world and directly.

### 9.4 `BlockFrameBlastWall` hardness double-dips

`BlockFrameBlastWall.getBlockHardness` (`:80`) returns `super.getBlockHardness(...) + 5`, and the superclass
already returns `meta + 5` → `meta + 10`. Probably intentional (blast walls should be harder) but it is invisible
in the config; document it or move it into the tier table.

**Status: done.** Kept and named: `BlockFrameBlastWall.BLAST_BONUS` is 5, so a blast wall is five harder than the
frame of its tier. Asserted in-game alongside the frame table.

### 9.5 `BlockFlora` and `BlockLotus` day/night logic uses `getWorldTime()`

`WorldGenFlowers` writes the open/closed lotus metadata based on `world.getWorldTime() > 12000L`, and
`BlockLotus.updateTick` flips it on light level. On a world with a fixed time (e.g. a "permanent day" dimension)
the generation state is arbitrary. Cosmetic.

**Status: open (cosmetic).** `WorldGenFlowers` still branches on `world.getWorldTime() > 12000L`.

### 9.6 `ItemBox` stores numeric item ids

`ItemBox` NBT uses `id` = `Item.getIdFromItem(item)` and `meta`. Numeric ids are **not stable across mod changes**
in 1.7.10 (they depend on registration order), so a box in a chest can silently turn into a different item after a
pack update. Storing the registry *name* is the robust fix, and it is the only form that survives a future
rename.
The same applies to `DispenserBehaviorBox` and `RecipeBox`.

**Status: fixed on `refactor/1.1.6`.** `util/BoxContents` owns the format: `RecipeBox` writes `name` *and* the
legacy `id`/`meta`, and every reader (`ItemBox` right-click and tooltip, `BoxCraftingHandler`,
`DispenserBehaviorBox`) prefers `name` with the numeric id as a fallback, so boxes written by older versions keep
working.

Game test: the name is written; a stale numeric id loses to the name; an id-only box still resolves; a box naming an
item the pack does not have resolves to null and is left alone instead of throwing; and a name-carrying box yields
its contents through the crafting handler.

### 9.7 Fluids — **open, waiting on a decision**

`ArtificeFluids` registers `oil`/`fuel`/`creosote`/`bitumen` only if they are not already registered, and
`ArtificeBlocks` then reuses the existing block if the fluid already has one. Good. But `ArtificeItems` still
registers four CoFH buckets and four `FluidContainerRegistry` entries **unconditionally**, so in a pack with
BuildCraft the player gets two oil buckets with different item names. Consider skipping the bucket registration for
fluids Artifice did not create.

### 9.8 Feature ideas that fit the existing code

* **Attuned redstone is O(N·M) and has no range limit.** A proper signal bus (per-frequency power cache, and
  optionally a dimension whitelist) would make it usable at scale.
* **The heating coil is the only furnace-heating block.** A "heat pipe" using the existing `IHeatProvider` /
  `IHeatReceiver` interfaces (and `HeatStorage`, currently unused) would be a natural extension — the interfaces
  already exist.
* **`WorldGenLayer`/`WorldGenCluster` already implement `ISuspendableGen`-style chunking.** Unifying them (§7.1)
  makes retro-generation cheap to add.
* **The creative tab is not localised** (§5.11) and the lang file already has the keys.

### 9.9 Consumers of the odd registry names: Chisel (and the general lesson)

`tile.`-prefixed registry names are unusual, and third-party tables written against them get them wrong. Chisel's
carving table is the worked example: it asks for `Artifice:artifice.marble`, which has never existed, so its
marble/limestone integration has silently done nothing in every pack that has both mods (E.4). Nobody
reported it because the failure is a null `findBlock` and an early return.

Two takeaways:

* If the names are ever normalised, do it as a compat change with a release of its own, not as drive-by tidying.
* If Chisel support is wanted before then, it is cheaper to register the carving groups from our side, where the
  real names are known, than to wait for another repo. Sketched in E.4.

**Status: fixed here.** `compat/chisel/CarvingTable` is the table - registry names built from `Names`, groups, metadata
and Chisel's original `order` of 99 - and `compat/ChiselCompat` hands it to `Carving.chisel.addVariation` behind
`Loader.isModLoaded("chisel")`, compiled against the Chisel API as `compileOnly`. Two deliberate departures from
Chisel's table: every carvable form of a rock is registered (metadata 0-5, and 0-3 for slabs), because a group is
looked up by block *and* metadata, so meta 0 alone would leave a player holding marble bricks with a chisel that does
nothing; and basalt and the limestone slabs are included, which Chisel never knew about.

The names are the whole bug, so they are pinned twice: `CarvingTableTest` asserts them as literals - including the
`tile.`-less form that never resolved - and `ServerGameTests.checkChiselCarvingNames` resolves every entry against the
real registry and checks the resolved set is exactly the rocks and rock slabs. An entry that fails to resolve is now a
warning in the log naming the block, rather than the silent early return that hid this for years.

---

### 9.10 Reflection in production code

Audited after the migration work, because reflection is the one thing a namespace change can break silently and
because it is the only Java-level indirection in the mod.

**Nothing in three of the four categories.** No Mixins (`usesMixins = false`, no `mixins.<modid>.json`, no
SpongePowered imports, nothing in `dependencies.gradle`), no ASM or coremod (`coreModClass` empty, no
`IFMLLoadingPlugin`/`IClassTransformer`), no access transformers (`accessTransformersFile` empty, no `*_at.cfg`, no
`META-INF` at all).

**Reflection: one site left, deliberately.**

| Site | Was | Now |
|---|---|---|
| `compat/CarpentersBlocks` | `Class.forName("...FeatureRegistry").getField("coverExceptions")` | `compileOnly` on `CarpentersBlocks:3.7.3-GTNH`, direct `FeatureRegistry.coverExceptions` |
| `compat/Forestry` | `Class.forName("forestry.api.apiculture.FlowerManager").getField("plainFlowers")` | `compileOnly` on `ForestryMC:4.12.1`, direct `FlowerManager.plainFlowers` |
| `item/ItemSledge` | `ObfuscationReflectionHelper.getPrivateValue(ShapedOreRecipe.class, recipe, **3**)` | `recipe.getInput()` - the accessor is public in 1.7.10 Forge |
| `compat/EE3` | `Class.forName("com.pahimar.ee3.init.ModItems")`, two static fields | **deleted** - see below |

The sledge one was the worst of them: a **field index**, not a name, so any Forge field addition or reorder would
have silently changed which field was read. `ShapedOreRecipe#getInput()` has been public since 1.7.10, so there was
never a reason to reflect.

**EE3 was deleted rather than converted.** It added 18 shapeless recipes (minium or philosopher's stone +
cobblestone + a dye → each coloured marble/basalt/limestone variant), i.e. a transmutation shortcut for recolouring
stone. Three things make it dead weight:

* EE3 never left alpha for 1.7.10 - the last builds are 2014-2015 pre-releases, and the 1.7.10 equivalent people
  actually run is **ProjectE**, which this pack has and EE3 it does not. The class could never execute.
* There is no published deobfuscated artifact to compile against, so the only alternatives were reflection or
  vendoring an API stub for an abandoned mod.
* Artifice already does this mod-natively: the marble/basalt cycling chain (`stoneCycleRecipes`, `registerStoneType`
  / `registerMarbleType` / `registerBasaltType`, driven by IMC) is the supported way to recolour these stones.

If the transmutation recipes are ever wanted, they should be reimplemented as a typed **ProjectE** compat
(`curse.maven:ProjectE-226410:2340786` is a known-good coordinate, already used by FuzziTweaks) rather than
resurrected from reflection. Removing the class also dropped `after:EE3` from the `@Mod` dependency string.


The two converted compat classes keep their `try`/`catch (Throwable)` and their `Loader.isModLoaded` gate: the
`compileOnly` reference makes a version mismatch a `NoSuchFieldError` (an `Error`, not an `Exception`), and an
optional integration must never take the game down. Their classes are constructed unconditionally in `preInit` but
only *loaded* when the gate passes, so an absent mod costs nothing.

### 9.11 A scaffold is not supported by the block above it

Found while replacing the scaffold support walk (§6.3). The search only starts if one of the five **sides** is the
same block and tier; the block directly above is not one of them. So an anchored scaffold column cannot be extended
downwards - the new block has an anchored neighbour one block up, and that is not enough.

Preserved by the refactor (it is a behaviour, not an implementation detail) and pinned by
`BlockFrameScaffoldSupportTest.aBlockIsNotSupportedByTheBlockDirectlyAboveIt`. It reads like an oversight rather than a
rule: hanging a scaffold from the one above it is what a player building downwards would expect. Worth deciding either
way - the fix is one more direction in the entry check, and the test would then record the opposite.

### 9.12 Rewriting Angelica's render-pass whitelist — **done**

Angelica's `blockCrackFixRenderPassWhitelist` is a list of class-name strings whose default lives in Angelica's jar
(E.5), and the default still names `shukaro.artifice.block.world.BlockOre`. There is no API to add to it, so a pack
update regenerates the wrong value and Artifice ores drop off the whitelist (z-fighting with `blockCrackFix` on).

`compat/AngelicaWhitelist` rewrites `config/angelica-modules.cfg` in place. Four decisions worth recording:

* **Timing.** It runs from `FMLConstructionEvent`, the earliest hook that still precedes every mod's `preInit`, and
  again from `FMLLoadCompleteEvent` as a catch-all for a first launch where Angelica writes the file after that pass.
  It is deliberately *not* an `ICompat`: those run in `init`, far too late. On a first launch the fix takes effect on
  the next launch; that is logged, not hidden.
* **Nothing else moves.** The file is read and written as ISO-8859-1, one byte per char, so comments, ordering and CRLF
  survive byte-for-byte - asserted by `AngelicaWhitelistTest.lineEndingsSurvive` and
  `bytesThatAreNotAsciiArePreserved`. It is written through a sibling temp file and renamed over the target, so an
  interrupted write cannot leave a truncated config for Angelica to read as "the player removed everything".
* **The whole prefix, not one class name.** Every `shukaro.artifice.` becomes `com.mrfuzzihead.artifice.`. That is not
  a guess - it is the same one-way rename §3 performed - so it fixes any Artifice class Angelica names now or adds
  later, and a name that resolves to nothing is a no-op for Angelica either way, since it compares these strings with
  `block.getClass().getName()`. The trailing dot keeps `shukaro.nodalmechanics` (same author, different mod) out of it.
  What a single-name rewrite would have bought is the *diagnostic*, so that survives separately: each rewritten name is
  resolved as a class (`Class.forName(..., false, ...)`, no initialisation), and any this mod does not have is logged,
  which is how we would notice Angelica's default list changing.
* **Never fatal.** A failure to write somebody else's config logs a warning and the mod loads; `AngelicaWhitelistTest`
  covers the missing file, a directory where the file should be, and the no-op re-run.

Verified end to end by dropping a real `angelica-modules.cfg` (CRLF) naming `BlockOre`, `BlockRock` and
`shukaro.nodalmechanics` into `run/server/config` and running the in-game suite: two references moved, the other mod's
package untouched, every other byte identical, `1795/1795` checks green.

## 10. Test strategy and coverage analysis

### 10.1 What is covered now

* **Unit (192 tests, 24 classes, headless):** pure value types and algorithms — `BlockCoord`, `ChunkCoord`, `NameMetaPair`,
  `MiscUtils`, `HeatStorage`, `MinecraftColors`, `FormatCodes`, `RomanUtil`, `XSRandom`, `EnumUpgrades`,
  `TileEntityLogic`, `ArrayUtils`, `PendingChunks`, `GenFrequency`, the whole common config
  surface (`ArtificeConfig.readCommon` against a default and a hostile `Configuration`), the tick-handler queue
  sweep, the creative-tab side annotations, the lamp and frame-detector state helpers, the attuned frequency index, the
  registry-name builders, the scaffold support predicate against a transcription of the algorithm it replaced, the
  metadata-table invariants, the Chisel carving table (names, groups, metadata forms and order, asserted as literals
  rather than rebuilt), the Angelica whitelist rewrite (byte-for-byte round trip, CRLF and non-ASCII preservation,
  idempotence, absent file), the icon-table subscript clamp (`clampIndex` over -8..23 and both `Integer` extremes),
  and the `@Mod`/`@Instance` agreement.
* **In-game (1,795 assertions):** registry completeness and namespacing, item-block presence, ore-dictionary
  membership and drop fallbacks, metadata round-trips for every rock/tier/slab/lamp/torch/flora subtype, scaffold
  anchoring, sickle area of effect, the logic block truth table and connection mapping, the attuned frequency link,
  heating-coil → six-furnace transfer, nuclear-battery charge round-trip, drops, ore smelting recipes,
  box identity and crafting, the sledge dispenser path, recipes, tooltips, enchantment ids, config surface, and the
  two neighbour-update tests (a lamp that settles, a detector that stops pulsing next to an echoing neighbour), the
  attuned link through real placement (including a transmitter that must not count as its own source), the ore
  layer generator over a real chunk, the box's stack comparison, the furnace wrapper's heat cap, and the scaffold's
  collision box, frame and blast-wall hardness per tier (including the corrupt-metadata clamp), the frozen
  registry-name set, the line order of the multi-line tooltips, the scaffold's tier-0 reach, and a fingerprint of what
  each of the four worldgen generators places for a fixed seed, and the join the Chisel integration always failed:
  every carving-table name resolving to a registered block, and nothing but our rocks and rock slabs in it, the ender
  ore drop through the real `getDrops` path, the attuned frequency index's cost, a seed sweep with a repeat run and a
  vacuity check per generator, and the frequency index emptied by a real `WorldEvent.Unload`.

### 10.2 Why the headless number is low, and what to do about it

The remaining 95% is dominated by classes that cannot be instantiated without a bootstrapped registry
(`GameRegistry`, `Item.itemRegistry`, `OreDictionary`, `CraftingManager` all require FML's `Loader` and the
`GameData` object holders — the harness proved this empirically). The honest split:

| Bucket | Lines | Accessible how |
|---|---|---|
| Pure logic (now covered) | ~700 | JUnit |
| Registry-dependent registration (`ArtificeBlocks`, `ArtificeItems`, `ArtificeRecipes`) | ~1,900 | in-game |
| Blocks/items behaviour | ~2,300 | in-game |
| Worldgen | ~1,100 | in-game (or extract the roll/distance math into pure helpers — §7.2) |
| Client/render | ~3,300 | client run only |

Realistic headless ceiling with the current structure: **~12–15%**. Extracting the frequency helper (§7.2), the
slab `getHalfSlab` map (§5.29) would each add pure testable surface.

### 10.3 What is still untested

Three of the four gaps this section used to list are now closed, and closing them changed what two of them meant.
What remains is one gap, and it is now *evidenced* rather than assumed.

1. **Client render paths - unreachable, and now provably so.** `IIcon` and `IIconRegister` are
   `@SideOnly(Side.CLIENT)`, and FML's `SideTransformer` refuses them on a dedicated server:

   ```
   java.lang.RuntimeException: Attempted to load class net/minecraft/client/renderer/texture/IIconRegister for
       invalid side SERVER   at cpw.mods.fml.common.asm.transformers.SideTransformer.transform(SideTransformer.java:50)
   ```

   So `registerBlockIcons`, every `getIcon`, `RenderBlocksInverted` (2,830 lines), `OreRenderer`, `FrameRenderer`,
   `TextureHandler` and the CTM code cannot be exercised here at all - not "not yet tested", but *not testable in this
   harness*, and a client run is the only route. What could be done was done: every icon table now takes its
   subscript from `ArrayUtils.clampIndex`, which is unit-tested, so the §5.12 bug class is impossible by construction
   even where the access cannot be executed. That is also how the last unchecked table was found (`BlockFrameBase`,
   §5.12 follow-up) - by reading, because nothing else was available.

2. **`Tracking` under load - closed.** `Tracking.visitsOfLastQuery()` counts the tiles a query walks, and the in-game
   `checkFrequencyIndexCost` places 18 attuned tiles across three interleaved frequencies and asserts a query walks
   exactly its own six, that three queries walk each tile exactly once, and that an unused frequency walks nothing.
   The assertion bites: making the query touch every frequency's set (the behaviour the index replaced) fails five
   assertions, with the counts going from 6 to 24.

3. **Worldgen determinism - closed, and the sweep found a vacuous test.** Each of the four generators now runs three
   times: the pinned seed, a second seed, and the pinned seed again over a freshly prepared band. The second seed must
   place something different (or the pinned hash is evidence of nothing) and the third run must reproduce the first
   (or the generator is holding state between runs - the §5.33 risk). There is also a vacuity check: the pinned
   fingerprint must differ from the fingerprint of the *untouched* band.

   That check failed on the first run, for sulfur. The band seeded two 3x3 sulfur clusters, the generator looks for a
   block adjacent to sulfur or lava at a random y, and it found a legal start so rarely that it placed **nothing**:
   `d13198cb0d98c1cf`, the "sulfur deposit fingerprint", was the fingerprint of the filler. The band is now sulfur
   stripes (lava made it vary between runs), the pinned seed is one that actually generates, and the fingerprint was
   re-derived from a verified run. Ten seeds were probed: eight place something, all eight differ.

4. **Multiplayer-only paths - closed, one by deletion and one by posting the real event.** The sneak protocol no longer
   exists (§5.36), so there is no `S31Packet` round trip left to test. `WorldEvent.Unload` is now posted on the real
   `MinecraftForge.EVENT_BUS` with the live `WorldServer` as the last group in the suite, and the index is checked
   through the visit counter, because "frequency emptied" and "frequency holding one stale tile" both answer `0`.
   Removing the `Tracking.forget(event.world)` call fails three assertions. The group runs last on purpose: other
   mods' unload handlers are entitled to tear down their own state, and a throw from one of them is reported as a note
   rather than as an Artifice failure.

---

---

## Appendix A — Registry names currently emitted

Derived from each class's `setBlockName`/`setUnlocalizedName` plus FML's `modId + ":"` prefixing. These are the
names the mod emits today, and the names §4 keeps.

| Kind | Registry name |
|---|---|
| Rock | `Artifice:tile.artifice.basalt` |
| Rock (limestone) | `Artifice:tile.artifice.limestone.gray` |
| Slab | `Artifice:tile.artifice.basalt.slab` |
| Double slab | `Artifice:tile.artifice.basalt.doubleslab` |
| Stairs | `Artifice:tile.artifice.basalt.stairs.cobblestone` |
| Frame | `Artifice:tile.artifice.frame` |
| Detector | `Artifice:tile.artifice.detector` |
| Blast wall | `Artifice:tile.artifice.reinforced` |
| Glass wall | `Artifice:tile.artifice.glasswall` / `…glasswalldark` |
| Scaffold | `Artifice:tile.artifice.scaffold` |
| Steel block | `Artifice:tile.artifice.steel` |
| Lamp | `Artifice:tile.artifice.lamp.white` … (16 + 16 inverted) |
| Coloured torch | `Artifice:tile.artifice.coloredtorch0` … `15` |
| Flora | `Artifice:tile.artifice.flora` |
| Lotus | `Artifice:tile.artifice.flora.lily` |
| Ore | `Artifice:tile.artifice.orecoal` … (15, plus `.glowing` for redstone/ender) |
| Sulfur | `Artifice:tile.artifice.sulfur` |
| Niter | `Artifice:tile.artifice.niter` |
| Ender ore | `Artifice:tile.artifice.enderore` (+ `.glowing`) |
| Uranium | `Artifice:tile.artifice.uranium` |
| Glow sand | `Artifice:tile.artifice.glowsand` |
| Charred log | `Artifice:tile.artifice.charredlog` |
| Tephra | `Artifice:tile.artifice.tephra` |
| Attuned | `Artifice:tile.artifice.attunedredstone.transmitter` / `.receiver` |
| Logic | `Artifice:tile.artifice.logicredstone` |
| Nuclear battery | `Artifice:tile.artifice.nuclearbattery` |
| Heating coil | `Artifice:tile.artifice.heatingcoil` |
| Fluid block | `Artifice:tile.artifice.fluid.oil` / `fuel` / `bitumen` / `creosote` |
| Sledge | `Artifice:item.artifice.sledge.wood` … |
| Sickle | `Artifice:item.artifice.sickle.wood` … |
| Steel item | `Artifice:item.artifice.steel` |
| Resource | `Artifice:item.artifice.resource` |
| Nugget / coin / dye / upgrade | `Artifice:item.artifice.{nugget,coin,dye,upgrade}` |
| Box | `Artifice:item.artifice.box` |
| Craft kit | `Artifice:item.artifice.craftkit` |
| Bucket | `Artifice:item.bucket` |

There are **155** names in total (blocks and items together). The game test asserts uniqueness across both registries,
that every block has an `ItemBlock` under the same name, and - since this appendix is a freeze (§4) - that the set
matches the list in `ServerGameTests.FROZEN_REGISTRY_NAMES` exactly. Adding a block means adding a line there, which
is the point: it makes the decision explicit and keeps this table honest.

## Appendix B — Config surface

File: `config/Artifice.cfg`.

| Category | Keys (count) | Notes |
|---|---|---|
| `Resource Generation` | 13 | Ender ore, uranium, sulfur, niter, oil lakes |
| `Plant Generation` | 4 | Flowers, lotuses |
| `Layer Generation` | 27 | 9 stone types × 3 |
| `Cluster Generation` | 20 | 3 rock groups × size/height/frequency |
| `Cave Generation` | 20 | 3 rock groups × size/height/frequency |
| `Volcano Generation` | 2 | **dead** (§8) |
| `Recipes` | 26 | every feature toggle |
| `Compatibility` | 4 | dimension blacklist, bonemeal, marble/basalt lists |
| `Compat` | 4 | MFR laser registration |
| `Upgrades` | 15 | per-enchantment caps |
| `Enchantments` | 7 | start id + weights |
| `Machines` | 5 | battery/coil rates |
| `Client` | 2 | tooltips, flavour text (loaded on both sides — §5.24) |
| **Total** | **149** | |

## Appendix C — Modpack scan commands

Run from the pack root (find the mods dir, then scan every jar's classes, configs and scripts):

```bash
# 1. Which jars mention the old namespace at all (class references, strings, META-INF)?
for j in mods/*.jar config/*.jar; do
  if unzip -p "$j" 2>/dev/null | grep -qa "shukaro"; then echo "NAMESPACE: $j"; fi
done

# 2. Which jars reference the old mod id as a dependency or IMC address?
for j in mods/*.jar; do
  if unzip -p "$j" 2>/dev/null | grep -qaE "required-after:Artifice|after:Artifice|isModLoaded\(\"Artifice\"\)|sendMessage\(\"Artifice\"|getModContainer\(\"Artifice\"\)"; then
    echo "MODID: $j"
  fi
done

# 3. Also check the pack's own scripts and configs.
grep -rIl --include='*.zs' --include='*.cfg' --include='*.json' --include='*.txt' \
  -e 'shukaro' -e 'required-after:Artifice' -e 'after:Artifice' -e 'isModLoaded("Artifice")' .

# 4. Crude class-level check (needs a zip-aware grep): look for the ArtificeRegistry owner class.
for j in mods/*.jar; do
  unzip -l "$j" 2>/dev/null | grep -q "shukaro/artifice" && echo "CLASSES: $j"
done
```

Interpretation:

* **No hits** → the package rename is safe from a pack standpoint.
* **Hits only for `shukaro`** → §3 needs the compatibility subclass (or a note in the changelog).
* **Hits for `Artifice` as a dependency/IMC address** → the mod id is load-bearing for someone; do not rename it.

**This scan has been run** against `FuzzCraft-Legacy` (233 jars, 574 MB) and the results are in Appendix E. The
version actually used walked the constant pool of every class in every jar, skipped binary assets, recursed
into embedded jars, and separated *type* references from *string* references; the snippets above are the quick
approximation. Re-run it before any release that touches names.

## Appendix D — File reference index

| Finding | File:line |
|---|---|
| §5.1 hash/equals | `util/NameMetaPair.java:78, 105` |
| §5.2 sledge dispenser | `item/DispenserBehaviorSledge.java:25-29` |
| §5.3 enchant nulls | `ArtificeEnchants.java:15-55`; `event/ArtificeEventHandler.java:24, 46`; `gui/ArtificeCreativeTab.java:39+` |
| §5.3 enchant allocation (ported fix) | `ArtificeEnchants.java:15-55`; reference implementation: `fuzzitweaks-1.2.0.jar!.../ArtificeEnchantsMixin#fuzziTweaks$findFreeEnchantID` |
| §E.3 FuzziTweaks mixin | `fuzzitweaks-1.2.0.jar!com/mrfuzzihead/fuzzitweaks/mixins/late/artifice/ArtificeEnchantsMixin.class`, `mixins/Mixins.class`, `mixins/TargetMods.class`, `mixins.fuzzitweaks.late.json` |
| §E.4 Chisel table | `chisel-2.18.1-GTNH.jar!team/chisel/compat/Compatibility.class` (`init`, `addSupport(String,String,String,int,int)`); API: `com/cricketcraft/chisel/api/carving/Carving.addVariation` |
| §E.5 Angelica default | `angelica-2.2.46.jar!com/gtnewhorizons/angelica/config/AngelicaConfig.class`; live value at `config/angelica-modules.cfg:51` |
| §E.7 config keys | `config/ProjectE/custom_emc.cfg:22`, `config/RTG/rtg.cfg:435,443`, `config/mystcraft/symbols.cfg:242`, `config/AppliedEnergistics2/recipes/items.csv`, `config/UniDict.cfg.BACKUP:169` |
| §5.4 neighbour double index (retracted) | `util/MiscUtils.java:69, 91` |
| §5.5 flood fill | `util/MiscUtils.java:103-134` |
| §5.6 generator leak | `event/ArtificeTickHandler.java:44-70`; `WorldGen{Cave,Cluster,Desert,Sulfur}.java` `getChunksToGen` |
| §5.7 scaffold | `block/frame/BlockFrameScaffold.java:127-145, 175-225` |
| §5.8 logic sides | `block/functional/BlockLogicRedstone.java:145, 155-172` |
| §5.9 ore dict NPE | `block/world/BlockOre.java:264, 282, 308, 312` |
| §5.10 tile NBT | `tile/TileEntityLogic.java:100-104` |
| §5.11 side-only tab | `gui/ArtificeCreativeTab.java:21, 35` |
| §5.12 off-by-one | `BlockFlora:38`, `BlockLamp:105`, `BlockFrameBlastWall:52`, `BlockFrameGlassWall:61`, `BlockFrameScaffold:73`, `ItemDye:31` |
| §5.13 rock icons | `block/world/BlockRock.java:80`; `BlockRockSlab.java:110-124` |
| §5.14 sickle | `item/ItemSickle.java:126, 138, 161` |
| §5.15 sledge recipes | `item/ItemSledge.java:177-192` |
| §5.16 flora tick | `block/world/BlockFlora.java:57-66` |
| §5.17 box crafting | `recipe/BoxCraftingHandler.java:41` |
| §5.18 recipe outputs | `recipe/RecipeBox.java:144`; `recipe/RecipeUpgrade.java:160` |
| §5.19 detector | `block/frame/BlockFrameDetector.java:60-87` |
| §5.20 lamp | `block/decorative/BlockLamp.java:136-140` |
| §5.21 machine config | `tile/TileEntityNuclearBattery.java:75-78`; `tile/TileFurnaceWrapper.java:44` |
| §5.22 logger | `ArtificeRegistry.java:28, 86, 161, 173, 184` |
| §5.23 config separator | `ArtificeConfig.java:487-527` |
| §5.24 double config | `ArtificeCore.java:88-89`; `ArtificeConfig.java:134, 468` |
| §5.26 armour | `event/ArtificeEventHandler.java:68-74` |
| §5.27 battery recipe | `recipe/ArtificeRecipes.java:209-222` |
| §5.29 slab tooltips | `block/decorative/ItemBlockSlabArtifice.java:26-88, 85-100` |
| §5.30 render id | `render/FrameRenderer.java:62` |
| §5.31 render pass | `render/OreRenderer.java:82`; `BlockOre.java:369-377` |
| §5.33 per-biome mutation | `world/WorldGenCluster.java:95-112`; `WorldGenCave.java:109-126` |
| §5.35 calc | `command/CommandCalc.java:33-55` |
| §5.36 sneak | deleted - `event/Tracking.java` (the `sneaks` field and the whole `net/` packet path) |
| §6 tracking O(N·M) | `event/Tracking.java:16-38`; `tile/TileEntityAttuned.java:21-31` |
| §7.1 blob duplication | `WorldGenCluster:133-246`, `WorldGenSulfur:112-197`, `WorldGenDesert:103-188`, `WorldGenCave:142-256` |
| §7.2 frequency copies | 8 `WorldGen*` classes, `generateFeature` |
| §8 volcano dead | `world/WorldGenVolcano.java`, `world/VolcanoHelper.java`; `ArtificeWorld.java` (no reference); `ArtificeConfig.java:82, 197` |
| §9.1 ore rewrite | `ArtificeBlocks.java:264-290`; `WorldGenCluster.java:190-235` |
| §9.2 ore smelting | `recipe/ArtificeRecipes.java:838-860`; `ArtificeBlocks.java:278-290`; `ArtificeCore.java:115-131` |

---

## Appendix E — Downstream consumer audit: FuzzCraft-Legacy (2026-11-17)

The §3.3 action item was "run a pack scan before release". This is the result, and it changes the release plan:
the package rename is **not** a no-op for this pack.

### E.1 Scope and method

| Input | Size | Result |
|---|---|---|
| `mods/*.jar` | 233 jars, 574 MB | 4 jars mention `shukaro`, 1 is a hard dependency |
| `coremods/`, `nestedmods/` | empty | — |
| `config/**`, `scripts/*.zs` | 9 script files, ~1,400 config files | 10 config files reference Artifice; 0 scripts |
| `saves/Test World` | 414 region files | 0 `shukaro` strings; 3.36M CoFH hash strings |

Two passes, both raw byte scans over every jar entry (constant pool included, so string literals and Mixin
targets are caught), skipping binary assets and recursing into embedded jars:

1. **Legacy-reference pass** — raw byte scan of every non-binary entry for `shukaro`. Matches are classified:
   `Lshukaro/artifice/...` / `shukaro/artifice/...` in a class file is a **type reference** in the constant pool —
   a hard link that fails at load. Everything else is a **string** — soft, and only as good as the code that
   consumes it (reflection, a config key, a class-name list).
2. **Registry-reference pass** — every `artifice:`-shaped token and every `Artifice` word, classified as
   *registry name*, *Forge dependency string*, or *prose*.

Both skip image/audio assets and recurse into embedded jars. The limitation to keep in mind: a byte scan cannot see
a name assembled at runtime from fragments, and cannot see the *intent* behind a string. Everything below was
resolved by disassembling the class in question.

### E.2 Results

| Jar | Kind | Reference | Verdict |
|---|---|---|---|
| `fuzzitweaks-1.2.0.jar` | **type** | `@Mixin(shukaro.artifice.ArtificeEnchants)`, `@Overwrite initEnchants()`, `ArtificeConfig.*`, `ArtificeCore.logger` | **P0 — startup crash** (E.3) |
| `angelica-2.2.46.jar` | string | `"shukaro.artifice.block.world.BlockOre"` in a config default | P3 — cosmetic (E.5) |
| `NodalMechanics-1.3.4-GTNH.jar` | type | `shukaro/nodalmechanics/**` — same author, different mod | none (E.6) |
| `gtnh-web-map-0.4-beta-1.jar` | string | `shukaro.warptheory.block.BlockVanish` | none (E.6) |

Zero mod jars reference `Artifice` as a Forge dependency, IMC address, network channel or mod-container name. Zero
reference `ArtificeRegistry` or any of its `register*Type` entry points — so §3.3's "treat it as API" question is
answered: **nobody calls it**.

### E.3 FuzziTweaks 1.2.0 — the one hard break

Decompiled from `fuzzitweaks-1.2.0.jar`:

```java
// mixins/TargetMods.class
ARTIFICE = new TargetMods("Artifice");              // gate: the MOD ID

// mixins/Mixins.class
MixinBuilder.addCommonMixins("artifice.ArtificeEnchantsMixin")
            .addRequiredMod(TargetMods.ARTIFICE)
            .setApplyIf(() -> Config.enableArtificeEnchantIdFix);

// mixins/late/artifice/ArtificeEnchantsMixin.class
@Mixin(value = shukaro.artifice.ArtificeEnchants.class, remap = false)
    @Overwrite initEnchants()V
    // body reads shukaro.artifice.ArtificeConfig.enchantmentInvisibleEnable / enchantmentStartID
    // and shukaro.artifice.ArtificeCore.logger

// mixins.fuzzitweaks.late.json
"required": true
```

The gate is the **mod id**, which the package rename deliberately does not change, so the mixin is still queued
against the renamed build and then:

1. Mixin resolves `shukaro.artifice.ArtificeEnchants` → missing → `InvalidMixinException` at load. `required: true`
   makes it fatal.
2. Even if that were tolerated, the `@Overwrite` body links `ArtificeConfig` and `ArtificeCore` by name, so it
   throws `NoSuchFieldError` the first time `initEnchants()` runs.

There is no bridge-class escape: the overwrite touches statics on two other renamed classes, so a deprecated
`shukaro.artifice.ArtificeEnchants` subclass would not be enough. The mixin must be deleted, which means the
allocation logic has to exist in-tree first (§5.3) and keep ids 70/71/72 (E.6).

Change list (FuzziTweaks repo):

- delete `mixins/late/artifice/ArtificeEnchantsMixin.java`;
- drop the `MixinBuilder` entry above and the now-unused `TargetMods.ARTIFICE`;
- drop `Config.enableArtificeEnchantIdFix` (Forge leaves the stale key behind, harmless);
- add `required-after:Artifice@<first version with the in-tree fix>` to `mcmod.info`.

### E.4 Chisel 2.18.1-GTNH — an integration that never worked

`team/chisel/compat/Compatibility.init()`:

```java
addSupport("Artifice", "artifice.marble",      "marble",      0, 99);
addSupport("Artifice", "artifice.marble.slab", "marble_slab", 0, 99);
for (String color : new String[]{"gray","lightgray","brown","tan","reddish","bluish","greenish"})
    addSupport("Artifice", "artifice.limestone." + color, "limestone", 0, 99);
```

and the helper:

```java
public static void addSupport(String modid, String registryName, String group, int meta, int order) {
    if (!Loader.isModLoaded(modid)) return;
    if (GameRegistry.findBlock(modid, registryName) == null) return;      // <-- always null here
    Carving.chisel.addVariation(group, GameRegistry.findBlock(modid, registryName), meta, order);
}
```

`findBlock` is an exact `modId + ":" + name` lookup and Artifice registers `Artifice:tile.artifice.*`, so the
`tile.`-less name never resolves and all eight calls return early. This is not a migration regression — it is
current behaviour, and the reason no one noticed.

The three names, corrected:

```
artifice.marble              -> tile.artifice.marble
artifice.marble.slab         -> tile.artifice.marble.slab
artifice.limestone.<colour>  -> tile.artifice.limestone.<colour>
```

Both sides use the same seven colour names in the same order (`ArtificeBlocks.rockColorNames:101`), and the
trailing `99` is `order`, not a metadata range (it lands in `SimpleCarvingVariation(block, meta, order)`), so there
is no invalid-meta hazard. All seven limestones deliberately share one `"limestone"` group so the colours swap
into each other.

Where to fix it, in order of preference:

1. **Here** — a `ChiselCompat` class called from `postInit` behind `Loader.isModLoaded("chisel")`, calling
   `Carving.chisel.addVariation(...)` directly (`compileOnly` on the Chisel API). This knows the real names and
   can also register basalt, which Chisel never did. It also stops depending on someone else's string table.
2. **Chisel** — the three literals above; correct, but a second repo and a second release.
3. **Nothing** — status quo; the rename costs nothing either way.

**Done: option 1.** `compat/ChiselCompat` runs from the compat list in `init` (not `postInit` - `addVariation` creates a
missing group and Chisel sorts variations by `order`, so our 99-and-up entries land after its own whichever side
registers first), compiled against `com.github.GTNewHorizons:Chisel:2.18.1-GTNH:dev` as `compileOnly`. The names live
in `compat/chisel/CarvingTable`, are pinned as literals by `CarvingTableTest`, and are resolved against the real
registry by `ServerGameTests.checkChiselCarvingNames` - which is the join that was silently null. See §9.9.

### E.5 Angelica 2.2.46 — a class name baked into a config default

```java
// com/gtnewhorizons/angelica/config/AngelicaConfig.class
"blockCrackFixRenderPassWhitelist"  // "Add a block class here if you see flickering (z-fighting) with blockCrackFix"
  defaults: gregtech.common.blocks.BlockOres, gregtech.common.blocks.GTBlockOre,
            shukaro.artifice.block.world.BlockOre,        <-- this
            bartworks.system.material.BWMetaGeneratedOres, gtPlusPlus.core.block.base.BlockBaseOre, ...
```

Matched by class-name string, so after the rename the entry stops matching and Artifice ores lose the render-pass
whitelist (z-fighting when `blockCrackFix` is on). Cosmetic, no crash, no data.

Already materialised in this instance at `config/angelica-modules.cfg:51`. Edit that line:

```
shukaro.artifice.block.world.BlockOre   ->   com.mrfuzzihead.artifice.block.world.BlockOre
```

Because the *default* lives in Angelica's jar, a fresh instance will regress. Worth a one-line upstream PR that
lists both class names - though as of §9.12 it is no longer required for this pack.

**Done from our side instead: `compat/AngelicaWhitelist` rewrites the file at startup** (§9.12). It runs from
`FMLConstructionEvent` - before any mod's `preInit` reads its config - and again at `FMLLoadCompleteEvent`. It moves
every `shukaro.artifice.` class name in the file to `com.mrfuzzihead.artifice.`, reports any that this mod does not
actually have, and preserves every other byte of the file. `AngelicaWhitelistTest` covers the rewrite, the CRLF and non-ASCII round trips, the no-op re-run, and the
absent-file case; the hook was verified end to end against a real config in `run/server/config`. The upstream PR is
still worth making - it is the only fix that survives a fresh install - but it is no longer required for this pack.

### E.6 Not a consumer (and how to tell)

| Hit | Why it is not us |
|---|---|
| `NodalMechanics-1.3.4-GTNH` — 57 hits | `shukaro/nodalmechanics/**`, Shukaro's other mod. Same author, different package. |
| `gtnh-web-map` — 1 hit | `renderdata/WarpTheory-texture.txt` → `shukaro.warptheory.block.BlockVanish`. Also `renderdata/1.6.2/Artifice-*.txt`, a 1.6.2 leftover in `deleted.txt`. |
| `Thaumcraft`, `salisarcana`, `tc4tweak` | Thaumcraft's own research category `ARTIFICE`, nothing to do with us. |
| `buildcraft-7.2.1` | a changelog line. |
| `coretweaks/cache/jarDiscoverer.cache` | a cached jar listing, regenerated. |
| `config/fuzziids/*.csv` | **output**, not configuration. FuzziIDs only writes these (`IdDumpManager` → `CsvWriter`; there is no reader class), so the `shukaro.artifice.enchant.*` strings in `enchantments.csv` are a report and are regenerated. It is still the best record of the pack's live enchantment ids (70/71/72, E.6). |

### E.7 Config surface: what each rename actually breaks

| File | Key form | Package rename | Mod-id / registry rename |
|---|---|---|---|
| `config/AppliedEnergistics2/recipes/items.csv` | `Artifice:tile.artifice.basalt:0` — `modid:registryName:meta`, **generated** | safe | regenerated; old facades are re-derived from the same names |
| `config/AppliedEnergistics2/Facades.cfg` | `tileartificebasalt.1` — unlocalised name, dots stripped | safe | breaks if unlocalised names change |
| `config/ProjectE/custom_emc.cfg:22` | `S:Artifice:tile.artifice.marble` | safe | needs editing |
| `config/RTG/rtg.cfg:435,443` | `Artifice:tile.artifice.basalt` (volcano block) | safe | needs editing |
| `config/mystcraft/symbols.cfg:242` | `modmat_tile.artifice.fluid.bitumen.enabled` | safe | needs editing |
| `config/UniDict.cfg.BACKUP:169` | `Artifice` in a mod-id list | safe | needs editing (cosmetic) |
| `config/fuzzitweaks.cfg:32` | `EnableArtificeEnchantIdFix` | safe | goes away with the mixin |
| `config/angelica-modules.cfg:51` | class name | **breaks (cosmetic)** | safe |

All of these are `modid:registryName` or `modid:unlocalisedName`, and both components survive the package rename —
the mod id is unchanged and every block/item is still registered from `getUnlocalizedName()`. They are, however,
exactly the surface that would make a registry rename expensive: each one has to be edited or re-keyed, on top of
the CoFH hash problem below.

### E.8 World data: what a save actually contains

Every chunk of `saves/Test World` decompressed (414 region files, all dimensions):

| Lookup | Occurrences | Meaning |
|---|---|---|
| `shukaro` | **0** | No class names are persisted. Nothing to migrate, and a package rename cannot corrupt this world. |
| `tileArtifice*` | **0** | No Artifice tile entities happen to be placed in it — and the ids are stable strings anyway. |
| `Artifice:` | **3,358,850** | CoFHCore retrogen hash strings, in `CoFHWorld → Hash → List`. |

The hash list is per chunk and reads `Artifice:<registryName> <featureName>`:

```
Artifice: tile.artifice.sulfur@0 Lava Deposits        (334,748 chunks)
Artifice: tile.artifice.niter@0 Desert Deposits       (334,748)
Artifice: tile.artifice.uranium@0 Clusters            (334,748)
Artifice: tile.artifice.enderore@0 Clusters           (334,748)
Artifice: tile.artifice.basalt@0 Layers               (334,748)
Artifice: tile.artifice.marble@0 Clusters             (334,748)
Artifice: tile.artifice.limestone.gray@0 Clusters     (334,748)
Artifice: tile.artifice.limestone.gray@0 Caves        (334,748)
Artifice: Flowers                                     (334,748)
Artifice: Lilies                                      (334,748)
Artifice: tile.blockOil Lakes                         (11,370)   <- stale, from an older Artifice
```

Eleven distinct strings, with the **mod id, the registry name and the feature name all inside them**. CoFH treats a
changed hash as "this feature is new for this chunk", so renaming any of the three re-runs that feature over
already-generated terrain — duplicate ore clusters, duplicate lakes. This is the concrete form of the "don't rename
the worldgen hash" warning in §3.3 and E.8.

Tile entities are the good news: `ArtificeTiles` registers plain ids (`tileArtificeAttuned`,
`tileArtificeLogic`, `tileArtificeRadioBattery`, `tileArtificeHeatingCoil`), and 1.7.10 stores the registered id,
not the class name. Chunk data therefore survives the package rename untouched, and a future id change would at
least be recoverable by registering both ids.

### E.9 Verdict

| Migration | Safe in this pack? |
|---|---|
| **Package** `shukaro.artifice` → `com.mrfuzzihead.artifice` | **Yes, after two edits** — FuzziTweaks must lose its mixin (§5.3 first) and one Angelica config line changes. Worlds, configs and every other mod are unaffected. |
| **Registry names / mod id** | **Rejected (§4)** — the CoFH hash strings (E.8) and the config surface (E.7) both say no. The names stay `Artifice:tile.artifice.*`. |
| **Enchantment ids** | Unchanged by either rename, but *only* if the in-tree fix reproduces 70/71/72. |

Order of work, which is what `refactor/1.1.6` exists for:

1. §5.3 in-tree enchant fix, plus a game-test assertion on ids 70/71/72.
2. FuzziTweaks: drop the mixin, require the new Artifice.
3. `config/angelica-modules.cfg:51`, and optionally an Angelica PR listing both class names.
4. Optional: `ChiselCompat` (§E.4, option 1).
