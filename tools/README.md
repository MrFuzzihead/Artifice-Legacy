# Pack audit tools

Two throwaway-but-reusable scanners behind **Appendix E** of `CODE_REVIEW.md`. They answer the two
questions that decide whether a rename is safe *in a given pack*:

1. does anything link the old **package** by name (class references, Mixin targets, `Class.forName`)?
2. does anything key on the old **mod id** or **registry names** (configs, scripts, hard-coded lookups)?

## Usage

```bash
python tools/scan_legacy_refs.py   <pack>/minecraft/mods      # "who links shukaro.artifice?"
python tools/scan_registry_refs.py <pack>/minecraft/mods [jar-name-substring-to-skip]
```

Output is tab-separated, one hit per line: `kind<TAB>jar!entry<TAB>value`.

`scan_legacy_refs.py` kinds:

* `type` — a `shukaro/artifice/...` constant-pool entry. **Hard link.** The consumer will throw
  `NoClassDefFoundError` / `InvalidMixinException` after the rename.
* `text` — the literal string `shukaro` somewhere else. Soft: reflection, a config key, a class-name list.
  Read the snippet before deciding.
* `error` — the jar could not be opened.

`scan_registry_refs.py` kinds:

* `registry` — an `artifice:<name>` token.
* `dependency` — a `after:` / `before:` Forge dependency string.
* `word` — any other `Artifice`, e.g. prose, a config key, a class name.

## What they do and do not see

Both walk every entry of every jar, skip binary assets (png/ogg/obj/…), and recurse into embedded jars
(jar-in-jar, shaded libraries). For `.class` files this is a raw byte scan of the constant pool, which is a
superset of "this class references that name" — it catches string literals too, which is what you want for
`Class.forName` and Mixin target names.

They cannot see a name assembled at runtime from fragments (`"shu" + "karo"`), and they cannot tell you what a
matching *string* is used for. Anything reported as `text` / `word` needs to be disassembled
(`javap -p -c -constants`) before it can be called safe or unsafe — that is how Appendix E.3–E.5 were classified.

Also check, because these are not jars:

```bash
grep -ril 'shukaro\|artifice:' <pack>/minecraft/config <pack>/minecraft/scripts
```

and remember that world saves store *ids*, not names, except for CoFH's retrogen hash list — see E.8 for the
chunk-level scan.
