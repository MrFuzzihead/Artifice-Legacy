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
| 1 | Land the verification harness (PR #2): `./gradlew check` + `./gradlew gameTestServer` | infra | **now** |
| 2 | `ArtificeCore.missingMappings` uses `event.get()`; after the mod-id change it must use `event.getAll()` or legacy worlds silently lose every block | bug (blocker for §4) | **P0** |
| 3 | Registry names are `Artifice:tile.artifice.basalt` / `Artifice:item.artifice.box`. Normalise them while the mod-id is being changed, not later | migration | **P0** |
| 4 | `ArtificeEnchants` can leave `enchantmentSoulstealing` / `enchantmentResistance` / `enchantmentInvisible` null; `ArtificeEventHandler.onDeath` and `ArtificeCreativeTab` then NPE on every kill / tab open in a large pack | bug | **P0** |
| 5 | `DispenserBehaviorSledge` calls `getIngredientsForBlock` twice, the second time after `setBlockToAir` → guaranteed NPE when a dispenser fires a sledge | bug | **P1** |
| 6 | `MiscUtils` neighbour helpers double-index the side array (`sides[sides[i]]`), so 4 of 6 sides are usually skipped. Energy distribution and furnace heating are lossy | bug | **P1** |
| 7 | `MiscUtils.getAdjoinedTileEntities` uses the method arguments instead of the current coordinate, so the flood fill is depth 1 | bug | **P1** |
| 8 | `NameMetaPair.hashCode()` ignores the `-1` wildcard that `equals()` honours → wildcard lookups miss in hash collections; `WorldGenVolcano`'s replace set never matches | bug | **P1** |
| 9 | `ArtificeTickHandler` never dequeues suspendable generators (their `getChunksToGen()` key set never shrinks) → a permanent per-5-tick scan that grows for the life of the save | leak/perf | **P1** |
| 10 | The whole volcano feature (`WorldGenVolcano`, `VolcanoHelper`, `blockTephra`, two config keys) is unreachable dead code | dead code | **P1** |
| 11 | `ArtificeCreativeTab` overrides client-only `CreativeTabs` methods without `@SideOnly(CLIENT)` → `NoSuchMethodError` on a dedicated server | bug | P2 |
| 12 | `>=`/`>` off-by-one bounds checks in 6 `getIcon` implementations | bug | P2 |
| 13 | `BlockFrameScaffold.checkStay` bails out on the first scaffold neighbour instead of continuing | bug | P2 |
| 14 | `BlockLogicRedstone.canConnectRedstone` uses a hand-rolled side table that does not match `isProvidingWeakPower`'s `OPPOSITES` mapping | bug | P2 |
| 15 | `BlockOre` NPEs when an ore-dictionary entry it expects is absent (very likely in a GTNH pack) | bug | P2 |
| 16 | Ore smelting recipes are registered in `init` but the ore-dictionary entries they match on only appear in `postInit`, so they never register | bug | P2 |
| 17 | The frequency-probability formula is wrong at both ends: `0` means "once per chunk", `99` is 100× rarer than `100`, and `>1000` saturates (§7.2) | bug | P2 |
| 18 | `RFperFurnaceHeat` is an unvalidated integer divisor; `0` throws `ArithmeticException` in the heating coil (§5.21) | bug | P2 |
| 19 | `/calc` is registered but its evaluator is commented out; it prints `1+1 = ` | broken feature | P3 |
| 20 | ~900 lines of duplicated worldgen blob code (Cluster/Sulfur/Desert/Cave) | refactor | P3 |
| 21 | `ItemSledge` scans the whole crafting table on every block break | perf | P3 |
| 22 | `Tracking` rescans every loaded tile entity of every world, per attuned block, per tick | perf | P3 |

Severity key: **P0** = data loss or blocks a planned change, **P1** = crashes / lost functionality / unbounded growth, **P2** = wrong behaviour, **P3** = cleanup.

### 0.1 Pack follow-ups

These come from the downstream audit of the real pack (`FuzzCraft-Legacy`, 233 mod jars) in **Appendix E**. They
are not codebase defects, but they decide whether the migration boots at all.

| # | Item | Kind | Priority |
|---|---|---|---|
| P1 | **FuzziTweaks 1.2.0 hard-links `shukaro.artifice` from a Mixin.** Its `ArtificeEnchantsMixin` targets `shukaro.artifice.ArtificeEnchants` and reads `ArtificeConfig` / `ArtificeCore` fields. The target is gated on the *mod id* `Artifice`, which the rename does not change, so the mixin is still queued and then fails → **startup crash**. Must be dropped in the same release (§3.2, Appendix E.3) | pack | **P0** |
| P2 | Port FuzziTweaks' enchant-id allocation in-tree **first**, preserving ids 70/71/72, or dropping the mixin silently re-collapses the three enchantments onto one id (§5.3) | code | **P0** |
| P3 | `config/angelica-modules.cfg:51` — one string (`shukaro.artifice.block.world.BlockOre` → `com.mrfuzzihead.…`) | pack | P3 |
| P4 | Chisel's Artifice carving table never worked (it looks up `Artifice:artifice.marble`; the real name is `Artifice:tile.artifice.marble`). Optional repair, not migration work (§4.2.1) | pack / upstream | P3 |

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
| **ForgeMultipart / BuildCraft / MFR / Forestry / Carpenters / EE3 interop** | All of these are ItemStack-, string- or reflection-based and reference *their own* classes, not ours. Unaffected. | None |
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
   occurrences across 414 region files (E.5). The hash contains the **mod id, the registry name and the feature
   name**, so §4's registry rename is not merely cosmetic — see §4.2.1.

---

## 4. Migration (b): mod id `Artifice` → `artifice`

This is the risky one, because the mod id is stamped into **every block and item registry name** in the world.

### 4.1 What FML actually keys on the mod id

From the 1.7.10 FML sources (`GameData.addPrefix`, `Loader.fireMissingMappingEvent`,
`FMLPreInitializationEvent`), a mod id is used for:

| Mechanism | Code | Persisted? | Affected by the rename? |
|---|---|---|---|
| Block/item registry domain | `GameData.addPrefix()` → `modId + ":" + name` | **yes** — chunk NBT + every `ItemStack` NBT | **yes** — every block, every item |
| Config file name | `new File(configDir, activeContainer.getModId() + ".cfg")` | yes (on disk) | **yes** — `config/Artifice.cfg` is orphaned |
| `@Instance` injection | `@Mod.Instance("Artifice")` | no | must match the new id or injection fails |
| `Loader.isModLoaded` / `required-after:` | exact string match | in other mods' jars | **yes** — other mods' dependency strings break |
| IMC address | `FMLInterModComms.sendMessage("Artifice", …)` | no | **yes** — silently dropped |
| `getModContainer("Artifice")` | exact string match | no | yes, if anyone calls it |
| Network channel | `NetworkRegistry.newChannel("Artifice", …)` | no | **no** — the channel name is arbitrary. Keep or change freely, but change both sides together |
| Creative tab label | `new ArtificeCreativeTab("Artifice")` | no | no |
| Enchantment names | `setName("artifice.invisible")` | no (ids are persisted) | no |
| Tile-entity ids | `"tileArtificeAttuned"` | yes | **no** — not namespaced |
| Ore dictionary / fluids | `"oreMarble"`, `"oil"` | yes | no |
| CoFH worldgen feature names | `"Artifice: … Cycles"` | hash in chunk NBT | no (and do not change them) |

### 4.2 The registry-name problem, in full

Because every block/item is registered **from its own constructor** with `this.getUnlocalizedName()` as the
name, the current registry names carry the `tile.`/`item.` prefix *and* the `artifice.` prefix:

```
Block: Artifice:tile.artifice.basalt
Item:  Artifice:item.artifice.sledge.wood
Slab:  Artifice:tile.artifice.basalt.slab
Ore:   Artifice:tile.artifice.orecoal
Fluid: Artifice:tile.artifice.fluid.oil
Bucket:Artifice:item.bucket
```

A raw `Artifice` → `artifice` swap therefore produces `artifice:tile.artifice.basalt`. That is *correct* but
ugly, and it means you would have to do a second, equally risky rename later. **Do both in one release.**

Recommended target names:

```
artifice:basalt            artifice:basalt.slab        artifice:basalt.stairs.cobblestone
artifice:marble            artifice:ore_coal           artifice:sledge.wood
artifice:frame             artifice:lamp.white         artifice:glasswall
```

Appendix A has the full current-name → proposed-name table, derived from the source.

Two hard constraints when you do this:

1. **Blocks and their `ItemBlock`s must keep the same name.** `GameData.verifyItemBlockName` throws
   `IllegalStateException` if `iBlockRegistry.getNameForObject(block)` and `iItemRegistry.getNameForObject(itemBlock)`
   differ. `registerBlock(block, itemclass, name)` registers both under `name`, so passing one normalised name is
   enough — do not normalise the block and item names separately.
2. **Do not pass a name that already has a prefix.** `GameData.registerBlock` logs
   `bigWarning("Illegal extra prefix …")` when the name already contains `:` (and then keeps the old prefix).
   Keep passing bare names; `addPrefix` adds the domain.

### 4.2.1 What these names cost in a real pack

Two measured effects, both from `FuzzCraft-Legacy` (Appendix E).

**(a) Third-party tables hard-code the ugly form — and some are already broken.** Chisel's compat table
(`team/chisel/compat/Compatibility.java`) does:

```java
addSupport("Artifice", "artifice.marble",      "marble",      0, 99);
addSupport("Artifice", "artifice.marble.slab", "marble_slab", 0, 99);
for (String color : ROCK_COLORS)
    addSupport("Artifice", "artifice.limestone." + color, "limestone", 0, 99);
```

`addSupport(modid, registryName, group, meta, order)` resolves through `GameRegistry.findBlock(modid, registryName)`,
which is an exact `modId + ":" + name` lookup. Artifice registers `Artifice:tile.artifice.marble`, so
`findBlock("Artifice", "artifice.marble")` returns null and every one of those calls returns early. The
integration has therefore **never worked** — confirmed against the AE2-generated `items.csv`, which contains 383
`Artifice:` entries and not one plain `Artifice:marble`. Normalising the registry names in §4 will not fix this
(the lookup still misses); see E.4 for the three-line repair.

The lesson for the rename: `tile.`-prefixed registry names are unusual, third-party tables that were written
against them get them wrong, and nobody notices because failure is silent. Normalising is a **compat improvement**,
not just tidying.

**(b) The names are hashed into every chunk.** CoFHCore's retrogen stores a per-chunk hash list in the save as
`Artifice:<registryName> <featureName>`, e.g. `Artifice: tile.artifice.marble@0 Clusters`. The mod id, the
registry name **and** the feature name are all in the string, so renaming any of the three makes CoFH treat the
feature as new for every existing chunk and re-run it — duplicate ore clusters and lakes in already-generated
terrain. Renaming the package does not touch it; renaming the registry names does. Measured on the real save:
11 distinct strings, 3,358,850 occurrences over 414 region files (E.5).

### 4.3 The migration mechanism (this is the part that must be right)

`FMLMissingMappingsEvent` is the only supported hook, and there are three traps:

**(a) `event.get()` will return an empty list after the rename.**

`Loader.fireMissingMappingEvent` builds the event as
`missingMappings.put(m.name.substring(0, m.name.indexOf(':')), m)` — the key is the **domain from the saved
name** — and `FMLMissingMappingsEvent.get()` returns `missing.get(activeContainer.getModId())`. After the rename,
the container id is `artifice` while the saved names say `Artifice:`, so `get()` returns nothing. Only
`getAll()` sees them. The current handler (`ArtificeCore.java:138`) uses `get()`.

**(b) Each mapping must be remapped to the *same kind* of object**, and the target must already be registered:

```java
// sketch - ArtificeCore#missingMappings, after the rename
private static final String LEGACY_DOMAIN = "Artifice:";
private static final String DOMAIN = modID + ":";   // "artifice:"

@EventHandler
public void missingMappings(FMLMissingMappingsEvent event) {
    for (MissingMapping m : event.getAll()) {      // getAll(), not get()
        try {
            if (m.type == Type.BLOCK) {
                Block target = (Block) Block.blockRegistry.getObject(remapName(m.name));
                if (target != null) m.remap(target); else m.warn();
            } else {
                Item target = (Item) Item.itemRegistry.getObject(remapName(m.name));
                if (target != null) m.remap(target); else m.warn();
            }
        } catch (RuntimeException e) {
            m.warn();   // remap() throws if the target is unregistered or the type is wrong
        }
    }
}

/** Maps a legacy name to the new one: both the domain swap and the path normalisation. */
private static String remapName(String legacyName) {
    if (!legacyName.startsWith(LEGACY_DOMAIN)) {
        return legacyName.replace("dobule", "double");   // keep the existing typo fix working for legacy blocks too
    }
    String path = legacyName.substring(LEGACY_DOMAIN.length());
    if (path.startsWith("tile.artifice.")) path = path.substring("tile.artifice.".length());
    else if (path.startsWith("item.artifice.")) path = path.substring("item.artifice.".length());
    else if (path.startsWith("item.")) path = path.substring("item.".length());
    return DOMAIN + path;
}
```

Notes:

* `remap()` registers an *alias* in `GameData.iBlockRegistry` / `iItemRegistry`
  (`Registry.addAlias(oldName, newName)`), so `ItemStack.loadItemStackFromNBT` resolves old names for the rest of
  that session. When a chunk or an inventory is written again it writes the new name, so the alias load
  disappears naturally.
* `MissingMapping.remap(Block)` throws `IllegalArgumentException` for an `ITEM` mapping and for an unregistered
  target, and `NullPointerException` for a null target. Hence the try/catch. Mapping an ItemBlock via
  `Item.getItemFromBlock(target)` is not needed if you look the item up in `Item.itemRegistry` by the same name.
* The current `dobule` → `double` fix must be folded into the same `remapName` so that a legacy world with both
  problems gets both fixed in one pass.
* `event.getAll()` also hands you *other mods'* missing mappings. Filter on `startsWith(LEGACY_DOMAIN)` (or handle
  `m.name` defensively) so you never touch another mod's data.

**(c) The ID has to move with the name.** `processIdRematches` re-registers the target at `remap.id` and throws
`IllegalStateException` if the new id differs. That is automatic with `remap()`; do not try to fix names by
editing registries yourself.

### 4.4 Config migration

`config/Artifice.cfg` becomes `config/artifice.cfg` and every setting silently reverts to default. Two of those
settings are **not cosmetic**:

* `Enchantments/Starting ID for EID range` (default 70). If the file regenerates and the id moves, every existing
  enchanted item keeps a numeric id that may now point at a different enchantment or at none.
* `Compatibility/Marble List` and `Basalt List` drive `ArtificeRegistry` chains, which affect **recipes**.

Add a one-time rename at the start of `preInit`, before any `Configuration` is constructed:

```java
File legacy = new File(evt.getModConfigurationDirectory(), "Artifice.cfg");
File current = evt.getSuggestedConfigurationFile();
if (legacy.isFile() && !current.isFile()) {
    //noinspection ResultOfMethodCallIgnored
    legacy.renameTo(current);     // fall back to copy+delete if renameTo fails across filesystems
}
```

(Use `evt.getModConfigurationDirectory()` — `suggestedConfigurationFile` points at the *new* name.)

### 4.5 Enchantment ids

Ids are persisted as NBT shorts on items, and default to the 70..255 range from `enchantmentStartID`. Nothing
about the rename changes them, but the *config regeneration* in 4.4 would. Keep 4.4, and consider documenting the
recommended range in the config comment.

The ids are **observable in the pack**, and they are not a free knob. `config/fuzziids/enchantments.csv` (a dump by
FuzziIDs, not a pin file — see E.6) records this pack's actual allocation:

```
70,enchantment.artifice.invisible,Artifice,shukaro.artifice.enchant.EnchantmentInvisible
71,enchantment.artifice.soulstealing,Artifice,shukaro.artifice.enchant.EnchantmentSoulstealing
72,enchantment.artifice.resistance,Artifice,shukaro.artifice.enchant.EnchantmentResistance
```

Those are the ids already on every enchanted item in existing worlds. Changing the registration *order*, the
*start* id or the collision strategy remaps them — a `Resistance` book becomes an `Invisible` book with no
warning, because 1.7.10 stores the number, not the name. The risk is not theoretical here: it is exactly what
FuzziTweaks currently patches (`@Overwrite initEnchants()`), so the in-tree fix in §5.3 has to reproduce 70/71/72
before that mixin can be deleted.

Also note §5.3: if the range is exhausted the enchantments stay `null` and the mod NPEs. A large pack is exactly
where both problems meet.

### 4.6 Network channel

`CommonProxy` creates `NetworkRegistry.newChannel("Artifice", …)`. The name is arbitrary and is not persisted,
so the rename does not require touching it. If you do rename it, old and new clients cannot talk — which matters
only for a mixed-version server, and FML's `required-after` handshake normally prevents that. Recommendation:
change it to `artifice` for tidiness, in the same release, and note it in the changelog.

### 4.7 IMC and `Loader.isModLoaded`

`FMLInterModComms` delivery is keyed by mod id. A mod that sends `sendMessage("Artifice", "register-marble", …)`
will be told "Mod Artifice not found" and the message is dropped. Two options:

* **Option A (recommended, simplest):** accept the break, and document it. The IMC API is small and the
  pack-facing entry point that matters (`ArtificeRegistry` via reflection) is rarer than `sendMessage`.
* **Option B (compatibility shim):** ship a second, tiny `@Mod(modid = "Artifice", name = "Artifice (legacy id)")`
  container in the same jar for one release. FML allows multiple `@Mod` classes per jar. It gets an empty
  `preInit` and its own `processIMCRequests` that forwards `event.getMessages()` into `IMC.processIMC`.
  `Loader.isModLoaded("Artifice")` and `required-after:Artifice` then keep working. Cost: a second mod-list entry,
  a second `mcmod.info`-equivalent entry, and a deprecation window you have to remember to close.

`Loader.isModLoaded` / `required-after` breakage is the one you cannot fix from inside a single container, so if
your pack (or a popular addon) depends on it, Option B is the only way to ship the rename without a hard break.

### 4.8 Feature names, fluids and other "looks like a mod id but isn't"

* **CoFH `WorldHandler` feature names** (`getFeatureName()` in all nine `WorldGen*` classes) are hashed into a
  per-chunk `genHash`. They are display strings prefixed `"Artifice: "`. Do **not** rename them as part of a
  cosmetic cleanup; a rename is a worldgen event.
* **`BlockFluid*`** pass `"Artifice".toLowerCase()` to CoFH's `BlockFluidCoFHBase`, which builds the *block name*
  `artifice.fluid.oil`. Lower-cased already → the registry name stays `Artifice:tile.artifice.fluid.oil` today and
  becomes `artifice:artifice.fluid.oil` unless normalised. Include the fluid blocks in the normalisation.
* **`TextureHandler`** hardcodes `"Artifice".toLowerCase() + ":"` for the resource domain. The resource domain is
  *not* the mod id and does not need to match it — leave it pointing at `assets/artifice`, or switch it to
  `ArtificeCore.modID.toLowerCase()` for clarity. Do not change the asset folder as part of the rename; that would
  break resource packs.

### 4.9 Step-by-step release plan

**Release N (current mod id, no functional change beyond the harness):**

1. Merge PR #2. Tag a release so you have a "last known good `Artifice` id" build.
2. Add the `getAll()`-based `missingMappings` handler *now*, while the id is still `Artifice`. It is a no-op for
   same-id worlds (nothing missing) but it makes the next step a pure config change, and you can test the legacy
   mapping path by hand-editing a chunk name.
3. Add the config-file rename *now*, keyed on the same constant. Also a no-op today.

**Release N+1 (the rename):**

4. `modId = artifice`, `modGroup` unchanged, `@Mod(modid = "artifice")`, `@Instance("artifice")`,
   `ArtificeCore.modID = "artifice"`.
5. Register every block/item with a bare, normalised name (`"basalt"`, not `"tile.artifice.basalt"`). This is a
   mechanical change in ~30 constructors; the `getUnlocalizedName()` calls in `ArtificeBlocks` for the slabs must
   be replaced with explicit names at the same time.
6. Verify:
   * `./gradlew gameTestServer` — it asserts `name.startsWith(ArtificeCore.modID + ":")` and uniqueness for every
     block and item, so a missed registration or a collision fails the suite.
   * Load a pre-rename world and confirm: no "missing block" prompt, inventories intact, tile entities intact,
     `fml-server-latest.log` shows the remaps (`FMLLog.fine("The Block %s is being remapped to %s.")`), and a
     re-save round-trips without warnings.
   * Check `config/artifice.cfg` contains the *old values* (i.e. 4.4 worked) — especially the enchantment start id.
7. Optional: the legacy `Artifice` shim container (Option B) for one release, if the pack scan finds
   `required-after:Artifice` or `Loader.isModLoaded("Artifice")`.

**Release N+2:** drop the shim, drop the alias path only when you are comfortable that no world in the pack still
holds `Artifice:` names (alias removal is safe once every chunk and every `ItemStack` has been re-saved at least
once — practically: after one full play cycle, not one login).

### 4.10 What the existing test suite already covers

* `RegistrationInvariantsTest.modAnnotationAndConstantsAgree` fails if `@Mod(modid)`, `ArtificeCore.modID` and
  `@Instance` drift apart — so step 4 cannot half-happen.
* `ServerGameTests.checkRegistries` asserts every block and item name starts with `ArtificeCore.modID + ":"`, is
  unique, and that every block has an `ItemBlock`. It follows the rename automatically and will catch a missed
  registration or a `verifyItemBlockName` mismatch on the next run.
* There is intentionally **no** test asserting the literal `"Artifice"`, so the rename does not require touching
  the suite.
* Missing from the harness (add when step 5 lands): a test that takes a fabricated `Artifice:tile.artifice.basalt`
  NBT name and asserts the alias resolves to `artifice:basalt`. `FMLMissingMappingsEvent` is not constructible
  without FML internals, so this is best done as a game-test assertion that calls the same `remapName` helper the
  handler uses — extract it to a package-visible static method for that.

---

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

Acceptance test: `NameMetaPairTest.wildcardPairIsFindableInAHashSet` (`@Ignore`d today).

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
worlds already contain (§4.5). **Acceptance test:** the game-test mod asserts the three ids are 70/71/72 under the
stock config, so a reorder fails CI instead of a save.

This exact logic currently ships as a third-party Mixin (`FuzziTweaks`,
`ArtificeEnchantsMixin#fuzziTweaks$findFreeEnchantID`) because it was the only way to patch a released mod. Moving
it in-tree is what lets that mixin be deleted — which the namespace migration requires anyway (§0.1, E.3).

### 5.4 `MiscUtils` neighbour helpers visit a permuted subset of the six sides — **P1**

`util/MiscUtils.java:63-76` and `:85-98`.

```java
Integer[] sides = {0,1,2,3,4,5};
shuffleArray(sides);
Integer[] arr$ = sides;
for (int i$ = 0; i$ < len$; i$++) {
    int i = arr$[i$];
    ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[sides[i]];   // sides[sides[i$]] !
```

This is a decompiler artefact of `for (int i : sides)`: the index is applied twice. The loop still runs six times,
but it visits a permutation of a permutation — some sides twice, some never. Consequences:

* `TileEntityHeatingCoil` and `TileEntityNuclearBattery` push energy into a random, incomplete subset of
  neighbours (energy is silently lost when a receiver is never visited).
* `MiscUtils.getHeatReceiverNeighbor` can fail to find an adjacent furnace at all, so the heating coil sometimes
  does nothing.

Fix:

```java
for (int i : sides) {
    ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[i];
    ...
}
```

Acceptance test: the game-test "every furnace around a coil burns" assertion, currently a `knownBug` probe.

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

### 5.8 `BlockLogicRedstone.canConnectRedstone` uses the wrong side table — **P2**

`block/functional/BlockLogicRedstone.java:150-168` maps `side → sides[…]` with a hand-written table
(`-1→1, 0→2, 1→5, 2→3, 3→4`), while `isProvidingWeakPower` (`:133`) uses `ForgeDirection.OPPOSITES[side]`
(`0→1, 1→0, 2→3, 3→2, 4→5, 5→4`). The two disagree, so the block can render a redstone connection on a face it
does not actually read or emit on. Use `ForgeDirection.OPPOSITES[side]` in both.

### 5.9 `BlockOre` dereferences a possibly-absent ore-dictionary entry — **P2**

`block/world/BlockOre.java:264, 282, 308, 312` use `ItemHelper.getOre(this.name).getItem()`. For
`oreCopper/oreTin/oreSilver/oreLead/oreNickel/oreUranium`, if the pack registers no such ore (or registers it
*after* Artifice in the load order — which is common), `ItemHelper.getOre` returns `null` → NPE on block break,
silk touch, or `createStackedBlock`. GTNH packs usually have all of these, but a "no IC2/ores" pack will crash.

Fix: null-check and fall back to `Item.getItemFromBlock(this)` / the block's own drops.

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
in the lang file are unused.

### 5.12 Off-by-one bounds checks in `getIcon` — **P2**

Six places use `>` where `>=` is required, so the maximum valid index falls through to an array access one past
the end:

| File | Line | Expression | Consequence |
|---|---|---|---|
| `block/world/BlockFlora.java` | 38 | `meta > flora.length` | `meta == 5` → AIOOBE (5 is not a valid meta anyway, but it comes from NBT) |
| `block/decorative/BlockLamp.java` | 105 | `meta > 15` | `meta == 16` → AIOOBE on a corrupt save |
| `block/frame/BlockFrameBlastWall.java` | 52 | `meta > tiers.length` | `meta == 4` → AIOOBE |
| `block/frame/BlockFrameGlassWall.java` | 61 | `meta > tiers.length` | same |
| `block/frame/BlockFrameScaffold.java` | 73 | `meta > tiers.length` | same |
| `item/ItemDye.java` | 31 | `meta > icons.length` | `meta == 4` → AIOOBE |

`BlockFrameScaffold.getIcon(IBlockAccess, …)` (`:83`) is worse: it indexes `sideIcons[access.getBlockMetadata(…)]`
with no check at all. `ItemUpgrade`/`ItemCoin`/`ItemNugget`/`ItemSteel` have no check either. A single helper
(`ArrayUtils.getOrDefault(array, meta, fallback)`) would remove the whole class of bug.

### 5.13 `BlockRock` registers the paver texture as the antipaver texture — **P2 (visual)**

`block/world/BlockRock.java:79-80`:

```java
TextureHandler.registerConnectedTexture(reg, this, 4, "antipaver", this.name);
this.icons[4] = TextureHandler.getConnectedTexture(this, 3, 0).icon;   // 3 == paver, not 4
```

`BlockRockSlab` does it correctly (`:114-121`), so the antipaver *block* face uses the paver icon while the
antipaver *slab* uses the antipaver icon. One-character fix.

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

### 5.15 `ItemSledge` scoops recipes by matching the *unlocalised* name — **P2**

`item/ItemSledge.java:172-186`: a recipe is considered "the recipe for this drop" when
`recipe.getRecipeOutput().getUnlocalizedName().equals(input.getUnlocalizedName())`. Vanilla block items share
unlocalised names per block (`tile.stone` covers every stone variant), and any two mods can collide. The result is
that a sledge can "unsmelt" the wrong recipe — e.g. breaking a `stone` variant may use the recipe for a different
variant. Worse, the method returns the **first** match in the recipe list, so load order decides the output.

Prefer `ItemStack.isItemEqual` + `OreDictionary`-aware comparison, and skip recipes whose output has subtypes
unless the damage matches (the code already half-does this).

### 5.16 `BlockFlora.updateTick` never calls `super` — **P2**

`block/world/BlockFlora.java:57-66` overrides `updateTick` and does the day/night swap but never calls
`super.updateTick`, which is where `BlockBush` drops the flower when it can no longer stay (soil removed, light
gone). Because `setTickRandomly(true)` is set, the block ticks, but the "drop when unsupported" path only runs via
`onNeighborBlockChange`. Mostly harmless, but it means a flower floating on a removed dirt block can persist until
a neighbour update.

Also `getIcon` uses `>` (see 5.12) and `getSubBlocks` excludes the closed lotus deliberately.

### 5.17 `BoxCraftingHandler` NPEs on an empty box — **P2**

`recipe/BoxCraftingHandler.java:41`: `NBTTagCompound tag = slot.getTagCompound();` with no null check. An empty
box (crafted, or emptied by right-click) has no NBT; using it in any crafting recipe that fires
`ItemCraftedEvent` while it sits in the grid throws. Guard with `if (tag == null) continue;`.

`ItemBox.onItemRightClick` has the same pattern (`item/ItemBox.java:87`) but is guarded by `if (tag != null)`.

### 5.18 `RecipeBox` / `RecipeUpgrade` return a null `getRecipeOutput()` — **P2**

`recipe/RecipeBox.java:144` and `recipe/RecipeUpgrade.java:170` return `this.output`, which is only set by
`matches()`. Vanilla and NEI call `getRecipeOutput()` on recipes that were not matched (recipe lists, tooltips,
the NEI recipe handler). `ItemSledge.getIngredientsForItemStack` also calls `getRecipeOutput()` on every recipe in
the list (see `:181`) — with a `RecipeBox` in the list that returns null, the null check saves it, but NEI's
handlers do not always check.

Fix: keep a separate `defaultOutput` field (or return `ArtificeItems.itemBox`'s empty stack) and never return
`null`.

### 5.19 `BlockFrameDetector` keeps rescheduling itself — **P2 (lag)**

`block/frame/BlockFrameDetector.java:60-87`. Every neighbour change schedules a 2-tick update; `updateTick`
cycles metadata 0→1→2 and calls `world.notifyBlocksOfNeighborChange(...)` on every step; states 0 and 1 reschedule.
So any block placed next to a detector makes it pulse and notify its neighbours every 2 ticks, forever, until it
happens to stop at meta 2. With a redstone dust line adjacent, each notification re-triggers `onNeighborBlockChange`
on the dust and on the detector, which re-schedules. This is the classic 1.7.10 neighbour-update storm; wrap the
notification in a metadata-changed check and only notify on an edge (0→1, 1→2), not on every tick.

### 5.20 `BlockLamp.onNeighborBlockChange` schedules unconditionally — **P2 (lag)**

`block/decorative/BlockLamp.java:136-140` schedules a 2-tick update for *any* neighbour change, including the
`setBlockMetadataWithNotify` the lamp itself performs. Combine with 5.19 and a lamp wall becomes a permanent
ticking machine. Check whether the computed power actually differs before scheduling.

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

Also: the battery has **no charging path** — `updateEntity` only ever extracts. The only energy it will ever hold
is the `charge` NBT stamped on the crafted item (§5.27). Worth deciding whether that is intended.

### 5.22 `ArtificeRegistry` logs through a possibly-null logger — **P2**

`ArtificeRegistry.java:28, 86, 161, 173, 184` call `ArtificeCore.logger.info(...)` unconditionally. `logger` is
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

### 5.24 Config is loaded twice per side — **P3**

`ArtificeCore.preInit` calls `ArtificeConfig.initClient(evt)` *and* `initCommon(evt)`, each of which constructs
its own `Configuration` over the same file and calls `load()`+`save()`. It works (both see the same file), but it
is four file writes per launch, and `initClient` runs on the server where the `Client` category is meaningless.
Fold the client keys into `initCommon` (or guard `initClient` with `FMLCommonSetupEvent`/side check).

### 5.25 `ArtificeEnchants` cannot distinguish "disabled" from "failed" — **P3**

`ArtificeEnchants` only logs a warning when it cannot allocate an id. Combine with 5.3 and the config flags are
misleading: `enchantmentSoulstealingEnable == true` does not imply `enchantmentSoulstealing != null`. Either
expose a `isSoulstealingAvailable()` helper and use it everywhere, or (better) fail the registration and clear
the config flag.

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

### 5.27 The nuclear battery recipe ships a full battery — **P3 (balance)**

`recipe/ArtificeRecipes.java:209-222` sets `"charge" = nuclearBatteryCapacity` on the recipe output, and
`BlockNuclearBattery.onBlockPlacedBy` adopts it. So one craft = 1,000,000,000 RF from nothing (there is no way to
recharge, so this is the *only* source). If that is intended as "a battery comes pre-charged", fine, but then the
capacity default and the recipe cost should be revisited; if not, drop the NBT from the recipe output.

### 5.28 `BlockFrameGlassWall` / `BlockFrameScaffold` `getIcon(IBlockAccess)` mismatch — **P3**

`BlockFrameScaffold.java:89` returns `sideIcons[meta]` with the raw metadata (no bound check, see 5.12) while the
`getIcon(int,int)` overload uses the masked value. Same idea in `BlockFrameGlassWall:61/68`.

### 5.29 `ItemBlockSlabArtifice` tooltips never match — **P3**

`block/decorative/ItemBlockSlabArtifice.java:85` builds the key from `stack.getUnlocalizedName()` (a
*localisation* key like `tile.artifice.basalt.slab.cobble`) while `ArtificeRegistry` keys tooltips by
`(Item, damage)` registry identity. The lookup can never hit. Use `new NameMetaPair(stack.getItem(), damage)`, as
`ItemBlockArtifice` does.

Also, `getHalfSlab`/`getDoubleSlab` (`:26-88`) resolve the slab by substring-matching the unlocalised name
(`contains("gray")` after `contains("lightgray")`, …). It works today but breaks the moment a name changes, and it
is the kind of code a registry-name normalisation (§4.2) will silently break. Replace with a static map from
half-slab → double-slab built in `ArtificeBlocks`.

### 5.30 `FrameRenderer.getRenderId()` returns 0 — **P3**

`render/FrameRenderer.java:62` returns `0` while `ClientProxy` registers it under
`ArtificeConfig.frameRenderID`. `RenderingRegistry` uses the id passed to `registerBlockHandler`, so rendering
works, but `ISimpleBlockRenderingHandler#getRenderId` is part of the contract and is used by some tools/NEI
plugins. Return `ArtificeConfig.frameRenderID`.

### 5.31 `OreRenderer` uses a mutable static as render state — **P3**

`render/OreRenderer.java:82` reads `ClientProxy.renderPass`, which `BlockOre.canRenderInPass` writes as a side
effect. It works because the render pass is single-threaded, but it makes the renderer non-reentrant and is the
reason `BlockOre.getRenderBlockPass()` returns `1` for both passes. Prefer passing the pass through the
`RenderBlocks` instance or using two render ids.

### 5.32 `BlockEnderOre.getItemDropped` and friends ignore metadata — **P3**

`block/world/BlockEnderOre.java` has no metadata subtypes, yet `getDamageValue`/`damageDropped`/`getDrops` are
defined as if it did. Harmless, but it is copy-paste from `BlockSulfur`, and it hides the fact that the glowing
variant drops the same item (which is correct).

### 5.33 `WorldGenCluster`/`WorldGenCave` mutate the generator instance per biome — **P3 (thread safety)**

`world/WorldGenCluster.java:95-112` and `world/WorldGenCave.java:109-126` assign `this.block` while choosing a
limestone variant from the biome. `IFeatureGenerator`s are shared across chunk-generation threads in 1.7.10
(CoFH's `WorldHandler.generateWorld` may be called from the chunk populator), so two chunks generating
concurrently can swap each other's stone type mid-blob. Use a local variable and pass it to `ClusterGen`.

### 5.34 `ArtificeBlocks` registers slabs with `getUnlocalizedName()` — **P3, but see §4**

`ArtificeBlocks.java:194, 210`: `blockBasalt.getUnlocalizedName() + ".slab"` embeds `tile.` twice in the registry
name (`Artifice:tile.artifice.basalt.slab`). It also couples the registry name to the *localisation* key. Give
slabs an explicit registry name.

### 5.35 The `/calc` command is registered but non-functional — **P3**

`command/CommandCalc.java`: the `DoubleEvaluator` field and `evaluate` call are commented out
(`:44-46`), so `/calc 1+1` prints `1+1 = ` and nothing else. `canCommandSenderUseCommand` returns `true`
unconditionally (no permission check). Either restore the evaluator (it needs the `fathzer:javaluator`
dependency) or delete the command — currently it is an untested, broken command exposed to every player.

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

---

## 6. Performance

| Location | Issue | Fix |
|---|---|---|
| `event/Tracking.java:16-38` | `getHighestFrequencyPower` scans **every loaded tile entity of every world** for every attuned block, every tick (`TileEntityAttuned.updateEntity`). With N attuned blocks and M tile entities this is O(N·M) per tick. | Maintain a `Map<String, Set<TileEntityAttuned>>` (or a per-frequency max-power cache invalidated on change) in `Tracking`. |
| `event/ArtificeTickHandler.java:50-70` | Unbounded growth (see 5.6) plus an O(deque) scan every 5 ticks that never shrinks. | Track completion; remove on empty. |
| `item/ItemSledge.java:179-186` | Every block break scans the entire `CraftingManager` recipe list (thousands of entries in a GTNH pack) and compares unlocalised names. | Pre-index `Map<Item, List<IRecipe>>` on `FMLPostInitializationEvent`, or cache per `(item, meta)`. |
| `block/frame/BlockFrameScaffold.java:205-225` | `isRooted` walks from `y-1` to 0 (up to 256 `getBlock` calls) on every neighbour change; `checkStay` then calls `getRadiusMatches` over a `(2r+1)³` cube (up to 33³ ≈ 36k blocks) **per candidate**, and `BlockCoord.isConnected` is a `LinkedList`-based flood fill with `contains()` (O(n²)). Placing a scaffold tower is O(height · r³ · n²). | Walk the support chain once with a visited set; cap the radius search; replace the `LinkedList` flood fill with an `ArrayDeque` + `HashSet`. |
| `block/world/BlockOre.java:142-204` (`sparkle`) | Called from `randomDisplayTick` on the client for every glowing ore in view; spawns up to 6 particles per call. Two of the six `world.getBlock` calls happen even when the first check fails. | Fine for a handful of ores; avoid `setTickRandomly` + `randomDisplayTick` double duty. |
| `world/WorldGenLayer.java:36-90` | Scans the full 16×16×height band and does two `NameMetaPair` hash lookups per block. At the default basalt layer (0–10) that is ~2.8k blocks/chunk; if a user configures a tall layer it is 65k. | Skip blocks that are air first, and use a single `getBlock`+`getBlockMetadata` pair (already done) but early-out on `isAirBlock` before building the pair. |
| `util/MiscUtils.java:63-98` | Called per coil/battery per tick; builds and shuffles a 6-element `Integer[]` each call (two allocations + boxing). | Use a static `int[]` rotated by a random start index instead of shuffle+boxing. |
| `compat/*` | Each compat class catches `Throwable` and prints a stack trace. Fine at load, but `EE3`/`Forestry`/`Carpenters` use reflection per call. | Only relevant if those methods are hot; they are load-time only. |
| `render/RenderBlocksInverted.java` | 2,830 lines duplicating `RenderBlocks` just to render the inverted faces of frames. | Only on the client render path; leave alone, but see §7. |

---

## 7. Refactors

### 7.1 One blob generator instead of four (≈700 lines removed)

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

### 7.3 Registry names as constants

Every `registerBlock`/`registerItem` call is made from a constructor with `getUnlocalizedName()` as the name.
Introduce a `Names` class with `public static final String BASALT = "basalt";` … and use it in both the
`setBlockName`/`registerBlock` pair. This is the prerequisite for §4.2 and makes `GameRegistry.findBlock` usable
for the first time.

### 7.4 `ArtificeBlocks.initBlocks` is one 150-line method

Split into `initRocks`, `initOres`, `initFrames`, `initDecorative`, `initFluids`, `initFunctional`, each with a
static field group. The current method constructs ~120 blocks and registers ~40 of them in one go, which makes the
registration order (and therefore the §4.3 block/item name coupling) very hard to reason about.

### 7.5 The tooltip registry is 226 registration calls

`ArtificeTooltips` (752 lines) is a wall of `ArtificeRegistry.registerTooltip(new ItemStack(...), ...)`. A
`registerTooltips(Block/Item, String... lines)` helper keyed by the same metadata tables would shrink it by
roughly 60% and make it impossible to forget a subtype. Note `ItemSledge`/`ItemSickle` look up tooltips with
`getItemDamage() == 0` while `ItemBlockArtifice` uses the real damage — a helper would standardise this.

### 7.6 Unchecked collections

`ArtificeTickHandler` (`TMap<Integer, ArrayDeque<ISuspendableGen>>` used with raw casts), `WorldGenSulfur/Desert`
(`new ArrayList()` raw), `compat/Forestry` (`List<ItemStack>` unchecked cast from reflection), `MiscUtils`
(`Integer[]` shuffles), `ArtificeRegistry` (`ArrayList<Object>` input lists). Most of these are decompiler
artefacts and are safe to parameterise.

### 7.7 Magic numbers that deserve names

* `BlockOre.sparkle` hardcodes ore indices 5 (redstone) and 13 (ender). `RegistrationInvariantsTest` now pins the
  table order, but the code should reference named constants.
* `TileFurnaceWrapper.getMaxHeat()` returns `1600` (a literal).
* `ArtificeEventHandler` uses `reslvl * 7 / 100.0F`.
* `BlockFrameScaffold` uses `0.022222223F` and `0.125F`.
* `ArtificeConfig` uses `-40` / `+5` for the lamp tint (`BlockLamp.getRenderColor`).

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
| `ComparableItemStack` / `ComparableItemStackNBT` | Used only by `RecipeBox.matches`. | Keep (it is on the recipe path) but add tests — `ComparableItemStackNBT.isStackEqual` casts without an `instanceof` and will `ClassCastException` on a mixed list. |
| `ArtificeCore.modChannel`, `modVersion`, `ArtificeCore.modName` | `modChannel` unused; `modVersion` duplicates the `@Mod(version=…)` literal; `modName` duplicates `name=`. | Make the annotation reference the constants (annotation values must be compile-time constants, which they are) so there is one source of truth. |
| `CommandCalc` evaluator | See §5.35. | Delete the command or restore the dependency. |
| `BlockRock.isReplaceableOreGen` | Never called by anything in this mod (it is an old Forge hook). | Verify against the 1.7.10 `Block` API; delete if unreferenced. |
| `BlockOre.tickRate` | Returns 30 but `setTickRandomly` uses the random-tick rate, not `tickRate`. | Either use `scheduleBlockUpdate` or drop the override. |
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

### 9.2 Ore smelting recipes never register

`ArtificeRecipes.registerOreRecipes` (`:842`) runs in `init`, but the ore-dictionary entries for `blockOres` only
appear in `postInit` (`ArtificeBlocks.registerOreVariants`). `ItemHelper.getOreName(stack)` therefore returns
`"Unknown"` for every one of the 15×9 ore blocks and no smelting recipe is added. Move `registerOreVariants()` to
`preInit` (after `initBlocks`), which also makes the ore dictionary complete before other mods' `init` runs — a
compat improvement in itself.

### 9.3 `BlockFrame` hardness scales with metadata — including invalid metadata

`BlockFrame.getBlockHardness` returns `meta + 5`. `BlockRock`/`BlockRockSlab` accept metadata up to 7 (and
`BlockRock.getIcon` clamps at 6), so a block placed with corrupt metadata 7 has hardness 12. Harmless, but a
hardness table indexed by tier would be clearer.

### 9.4 `BlockFrameBlastWall` hardness double-dips

`BlockFrameBlastWall.getBlockHardness` (`:80`) returns `super.getBlockHardness(...) + 5`, and the superclass
already returns `meta + 5` → `meta + 10`. Probably intentional (blast walls should be harder) but it is invisible
in the config; document it or move it into the tier table.

### 9.5 `BlockFlora` and `BlockLotus` day/night logic uses `getWorldTime()`

`WorldGenFlowers` writes the open/closed lotus metadata based on `world.getWorldTime() > 12000L`, and
`BlockLotus.updateTick` flips it on light level. On a world with a fixed time (e.g. a "permanent day" dimension)
the generation state is arbitrary. Cosmetic.

### 9.6 `ItemBox` stores numeric item ids

`ItemBox` NBT uses `id` = `Item.getIdFromItem(item)` and `meta`. Numeric ids are **not stable across mod changes**
in 1.7.10 (they depend on registration order), so a box in a chest can silently turn into a different item after a
pack update. Storing the registry *name* (`"artifice:sledge.wood"`) is the robust fix, and it dovetails with §4.2.
The same applies to `DispenserBehaviorBox` and `RecipeBox`.

### 9.7 Fluids

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
marble/limestone integration has silently done nothing in every pack that has both mods (§4.2.1(a), E.4). Nobody
reported it because the failure is a null `findBlock` and an early return.

Two takeaways:

* Normalising the names in §4 is a **compat improvement**, not cosmetics.
* If Chisel support is wanted before then, it is cheaper to register the carving groups from our side, where the
  real names are known, than to wait for another repo. Sketched in E.4.

---

## 10. Test strategy and coverage analysis

### 10.1 What is covered now

* **Unit (77 tests, headless):** pure value types and algorithms — `BlockCoord` (89/188 lines), `ChunkCoord`
  (19/24), `NameMetaPair` (18/34), `MiscUtils` (7/69), `HeatStorage` (20/20), `MinecraftColors` (43/43),
  `FormatCodes` (26/26), `RomanUtil`, `XSRandom`, `EnumUpgrades`, `TileEntityLogic` (19/57), the packet codec
  (15/15), plus the metadata-table invariants and the `@Mod`/`@Instance` agreement.
* **In-game (1,442 assertions):** registry completeness and namespacing, item-block presence, ore-dictionary
  membership, metadata round-trips for every rock/tier/slab/lamp/torch/flora subtype, scaffold support, the logic
  block truth table, the attuned frequency link, heating-coil → six-furnace transfer, nuclear-battery
  charge round-trip, drops, recipe presence, tooltips, config surface.

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
slab `getHalfSlab` map (§5.29) and the `remapName` helper (§4.3) would each add pure testable surface.

### 10.3 Next tests to add (in priority order)

1. `remapName` (both the domain swap and the `tile.`/`item.` normalisation) — pure, and it is the thing that
   protects worlds during §4.
2. `MiscUtils` neighbour iteration, once fixed — assert all six directions are visited exactly once.
3. The §7.2 frequency helper, once extracted.
4. A game-test assertion that a crafted nuclear battery's NBT matches the configured capacity (pins §5.27 either
   way).
5. A game-test assertion that `BlockFrameScaffold` supports a 3-tall tower (pins §5.7).
6. A game-test assertion that breaking an `oreIron` block yields an iron-ore item and drops XP (§5.9).

---

## Appendix A — Registry names currently emitted

Derived from each class's `setBlockName`/`setUnlocalizedName` plus FML's `modId + ":"` prefixing. `Artifice:` is
the domain today; the proposed column applies §4.2.

| Kind | Current registry name | Proposed |
|---|---|---|
| Rock | `Artifice:tile.artifice.basalt` | `artifice:basalt` |
| Rock (limestone) | `Artifice:tile.artifice.limestone.gray` | `artifice:limestone.gray` |
| Slab | `Artifice:tile.artifice.basalt.slab` | `artifice:basalt.slab` |
| Double slab | `Artifice:tile.artifice.basalt.doubleslab` | `artifice:basalt.doubleslab` |
| Stairs | `Artifice:tile.artifice.basalt.stairs.cobblestone` | `artifice:basalt.stairs.cobblestone` |
| Frame | `Artifice:tile.artifice.frame` | `artifice:frame` |
| Detector | `Artifice:tile.artifice.detector` | `artifice:detector` |
| Blast wall | `Artifice:tile.artifice.reinforced` | `artifice:reinforced` |
| Glass wall | `Artifice:tile.artifice.glasswall` / `…glasswalldark` | `artifice:glasswall` / `…glasswalldark` |
| Scaffold | `Artifice:tile.artifice.scaffold` | `artifice:scaffold` |
| Steel block | `Artifice:tile.artifice.steel` | `artifice:steel_block` |
| Lamp | `Artifice:tile.artifice.lamp.white` … (16 + 16 inverted) | `artifice:lamp.white` … |
| Coloured torch | `Artifice:tile.artifice.coloredtorch0` … `15` | `artifice:torch.colored0` … |
| Flora | `Artifice:tile.artifice.flora` | `artifice:flora` |
| Lotus | `Artifice:tile.artifice.flora.lily` | `artifice:lotus` |
| Ore | `Artifice:tile.artifice.orecoal` … (15, plus `.glowing` for redstone/ender) | `artifice:ore.coal` … |
| Sulfur | `Artifice:tile.artifice.sulfur` | `artifice:sulfur` |
| Niter | `Artifice:tile.artifice.niter` | `artifice:niter` |
| Ender ore | `Artifice:tile.artifice.enderore` (+ `.glowing`) | `artifice:enderore` |
| Uranium | `Artifice:tile.artifice.uranium` | `artifice:uranium` |
| Glow sand | `Artifice:tile.artifice.glowsand` | `artifice:glowsand` |
| Charred log | `Artifice:tile.artifice.charredlog` | `artifice:charredlog` |
| Tephra | `Artifice:tile.artifice.tephra` | `artifice:tephra` |
| Attuned | `Artifice:tile.artifice.attunedredstone.transmitter` / `.receiver` | `artifice:attuned.transmitter` / `.receiver` |
| Logic | `Artifice:tile.artifice.logicredstone` | `artifice:logicredstone` |
| Nuclear battery | `Artifice:tile.artifice.nuclearbattery` | `artifice:nuclearbattery` |
| Heating coil | `Artifice:tile.artifice.heatingcoil` | `artifice:heatingcoil` |
| Fluid block | `Artifice:tile.artifice.fluid.oil` / `fuel` / `bitumen` / `creosote` | `artifice:fluid.oil` … |
| Sledge | `Artifice:item.artifice.sledge.wood` … | `artifice:sledge.wood` … |
| Sickle | `Artifice:item.artifice.sickle.wood` … | `artifice:sickle.wood` … |
| Steel item | `Artifice:item.artifice.steel` | `artifice:steel` |
| Resource | `Artifice:item.artifice.resource` | `artifice:resource` |
| Nugget / coin / dye / upgrade | `Artifice:item.artifice.{nugget,coin,dye,upgrade}` | `artifice:{nugget,coin,dye,upgrade}` |
| Box | `Artifice:item.artifice.box` | `artifice:box` |
| Craft kit | `Artifice:item.artifice.craftkit` | `artifice:craftkit` |
| Bucket | `Artifice:item.bucket` | `artifice:bucket` |

Pair-count sanity check: the game test asserts uniqueness across **both** registries and that every block has an
`ItemBlock` under the same name.

## Appendix B — Config surface

File: `config/Artifice.cfg` (becomes `config/artifice.cfg`; see §4.4).

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

* **No hits** → both renames are safe from a pack standpoint; ship §4 with only the world-migration code.
* **Hits only for `shukaro`** → §3 needs the compatibility subclass (or a note in the changelog).
* **Hits for `Artifice` as a dependency/IMC address** → use Option B in §4.7 (the shim container), or accept the
  break and coordinate the dependent mod's update in the same pack revision.

**This scan has been run** against `FuzzCraft-Legacy` (233 jars, 574 MB) and the results are in Appendix E. The
shell snippets above are the quick version; `tools/scan_legacy_refs.py` and `tools/scan_registry_refs.py` in this
repo are the version that was actually used (they walk the constant pool of every class, skip binary assets,
recurse into embedded jars, and separate *type* references from *string* references). Re-run them before any
release that touches names.

## Appendix D — File reference index

| Finding | File:line |
|---|---|
| §4.3 legacy mapping hook | `ArtificeCore.java:136-149` |
| §4.1 mod id constants | `ArtificeCore.java:44-64` |
| §4.2 registry-name source | every `registerBlock(..., this.getUnlocalizedName())`; slabs at `ArtificeBlocks.java:194-214` |
| §5.1 hash/equals | `util/NameMetaPair.java:78, 105` |
| §5.2 sledge dispenser | `item/DispenserBehaviorSledge.java:25-29` |
| §5.3 enchant nulls | `ArtificeEnchants.java:15-55`; `event/ArtificeEventHandler.java:24, 46`; `gui/ArtificeCreativeTab.java:39+` |
| §5.3 enchant allocation (ported fix) | `ArtificeEnchants.java:15-55`; reference implementation: `fuzzitweaks-1.2.0.jar!.../ArtificeEnchantsMixin#fuzziTweaks$findFreeEnchantID` |
| §E.3 FuzziTweaks mixin | `fuzzitweaks-1.2.0.jar!com/mrfuzzihead/fuzzitweaks/mixins/late/artifice/ArtificeEnchantsMixin.class`, `mixins/Mixins.class`, `mixins/TargetMods.class`, `mixins.fuzzitweaks.late.json` |
| §E.4 Chisel table | `chisel-2.18.1-GTNH.jar!team/chisel/compat/Compatibility.class` (`init`, `addSupport(String,String,String,int,int)`); API: `com/cricketcraft/chisel/api/carving/Carving.addVariation` |
| §E.5 Angelica default | `angelica-2.2.46.jar!com/gtnewhorizons/angelica/config/AngelicaConfig.class`; live value at `config/angelica-modules.cfg:51` |
| §E.7 config keys | `config/ProjectE/custom_emc.cfg:22`, `config/RTG/rtg.cfg:435,443`, `config/mystcraft/symbols.cfg:242`, `config/AppliedEnergistics2/recipes/items.csv`, `config/UniDict.cfg.BACKUP:169` |
| §5.4 neighbour double index | `util/MiscUtils.java:69, 91` |
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
| §5.36 sneak | `event/Tracking.java:14`; `event/ArtificeClientTickHandler.java`; `net/handlers/SneakMessageHandler.java` |
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

Two passes, both scripts are in `tools/`:

1. **`scan_legacy_refs.py`** — raw byte scan of every non-binary entry for `shukaro`. Matches are classified:
   `Lshukaro/artifice/...` / `shukaro/artifice/...` in a class file is a **type reference** in the constant pool —
   a hard link that fails at load. Everything else is a **string** — soft, and only as good as the code that
   consumes it (reflection, a config key, a class-name list).
2. **`scan_registry_refs.py`** — every `artifice:`-shaped token and every `Artifice` word, classified as
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
allocation logic has to exist in-tree first (§5.3) and keep ids 70/71/72 (§4.5).

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
lists both class names.

### E.6 Not a consumer (and how to tell)

| Hit | Why it is not us |
|---|---|
| `NodalMechanics-1.3.4-GTNH` — 57 hits | `shukaro/nodalmechanics/**`, Shukaro's other mod. Same author, different package. |
| `gtnh-web-map` — 1 hit | `renderdata/WarpTheory-texture.txt` → `shukaro.warptheory.block.BlockVanish`. Also `renderdata/1.6.2/Artifice-*.txt`, a 1.6.2 leftover in `deleted.txt`. |
| `Thaumcraft`, `salisarcana`, `tc4tweak` | Thaumcraft's own research category `ARTIFICE`, nothing to do with us. |
| `buildcraft-7.2.1` | a changelog line. |
| `coretweaks/cache/jarDiscoverer.cache` | a cached jar listing, regenerated. |
| `config/fuzziids/*.csv` | **output**, not configuration. FuzziIDs only writes these (`IdDumpManager` → `CsvWriter`; there is no reader class), so the `shukaro.artifice.enchant.*` strings in `enchantments.csv` are a report and are regenerated. It is still the best record of the pack's live enchantment ids (70/71/72, §4.5). |

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
exactly the surface that makes §4's registry rename expensive: each one has to be edited or re-keyed, on top of
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
the worldgen hash" warning in §3.3 and §4.2.1(b).

Tile entities are the good news: `ArtificeTiles` registers plain ids (`tileArtificeAttuned`,
`tileArtificeLogic`, `tileArtificeRadioBattery`, `tileArtificeHeatingCoil`), and 1.7.10 stores the registered id,
not the class name. Chunk data therefore survives the package rename untouched, and a future id change would at
least be recoverable by registering both ids.

### E.9 Verdict

| Migration | Safe in this pack? |
|---|---|
| **Package** `shukaro.artifice` → `com.mrfuzzihead.artifice` | **Yes, after two edits** — FuzziTweaks must lose its mixin (§5.3 first) and one Angelica config line changes. Worlds, configs and every other mod are unaffected. |
| **Registry names / mod id** (§4) | **No, not in one step** — the CoFH hash strings and the config surface in E.7 both say so. That is why §4 already proposes a release of its own, with `missingMappings` and a legacy-name layer. |
| **Enchantment ids** | Unchanged by either rename, but *only* if the in-tree fix reproduces 70/71/72. |

Order of work, which is what `refactor/1.1.6` exists for:

1. §5.3 in-tree enchant fix, plus a game-test assertion on ids 70/71/72.
2. FuzziTweaks: drop the mixin, require the new Artifice.
3. `config/angelica-modules.cfg:51`, and optionally an Angelica PR listing both class names.
4. Optional: `ChiselCompat` (§E.4, option 1).
5. §4 (mod id + registry names) only as a separate release with the `missingMappings` machinery — not as part of
   the package cleanup.
