"""Second pass: find modid / registry-name (`artifice:...`) references in a pack.

The package rename (`shukaro.artifice` -> `com.mrfuzzihead.artifice`) only matters
to code that links classes by name.  A *registry* rename (`artifice:foo` -> ...)
matters to everything that types a string, which is a much bigger surface, so
classify the hits by shape rather than just counting them.
"""
import io
import os
import re
import sys
import zipfile

SKIP_EXT = {
    '.png', '.jpg', '.jpeg', '.gif', '.ogg', '.wav', '.mp3', '.frag', '.obj',
    '.mtl', '.ttf', '.otf', '.zip', '.kma', '.mcmeta', '.ico', '.bmp', '.tga',
    '.dds', '.exr', '.hdr', '.pdn', '.xcf', '.ttc', '.woff', '.woff2',
}

# registry id (lowercase, as Minecraft namespaces them)
REGISTRY = re.compile(rb'[Aa]rtifice:[A-Za-z0-9_./-]+')
# Forge dependency annotations
DEP = re.compile(rb'(?:required-)?(?:after|before)[:=][^"\']{0,80}')
# everything else that just says "Artifice"
WORD = re.compile(rb'Artifice')


def classify(snippet):
    if re.search(rb'artifice:[a-z0-9_]', snippet):
        return 'registry'
    if re.search(rb'(after|before)[:=]', snippet):
        return 'dependency'
    return 'word'


def scan_zip(path, jar, out, depth=0):
    try:
        zf = zipfile.ZipFile(path)
    except Exception:                                           # noqa: BLE001
        return
    with zf:
        for info in zf.infolist():
            if info.is_dir() or info.file_size > 60 * 1024 * 1024:
                continue
            name = info.filename
            if os.path.splitext(name)[1].lower() in SKIP_EXT:
                continue
            try:
                blob = zf.read(info)
            except Exception:                                   # noqa: BLE001
                continue
            if name.lower().endswith(('.jar', '.zip')):
                if depth < 2 and b'rtifice' in blob:
                    scan_zip(io.BytesIO(blob), f'{jar}!{name}', out, depth + 1)
                continue
            if b'rtifice' not in blob:
                continue
            for m in REGISTRY.finditer(blob):
                out.append(('registry', jar, name, m.group(0).decode('latin-1')))
            for m in re.finditer(rb'.[\x00-\xff]{0,40}?[Aa]rtifice[\x00-\xff]{0,40}', blob):
                snip = m.group(0)
                if REGISTRY.search(snip) or WORD.search(snip) is None:
                    continue
                out.append((classify(snip), jar, name,
                            re.sub(rb'[^\x20-\x7e]+', b' ', snip).decode('latin-1').strip()))


def main(root, skip_substr):
    out = []
    for dp, _, fs in os.walk(root):
        for f in sorted(fs):
            if not f.lower().endswith('.jar'):
                continue
            if skip_substr and skip_substr.lower() in f.lower():
                continue
            scan_zip(os.path.join(dp, f), f, out)
    return out


if __name__ == '__main__':
    res = main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else None)
    seen = set()
    for kind, jar, entry, val in res:
        k = (kind, jar, val)
        if k in seen:
            continue
        seen.add(k)
        print(f'{kind}\t{jar}\t{entry}\t{val[:150]}')
    print(f'\n{len(seen)} distinct', file=sys.stderr)
