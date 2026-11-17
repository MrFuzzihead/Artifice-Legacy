"""Scan every jar in a 1.7.10 pack for references to a legacy package namespace.

1.7.10 jars carry no compile-time linkage metadata we can trust at runtime (no
module graph), but the class-file constant pool keeps every referenced type as a
UTF-8 string, so a raw byte search over the entry is a superset of "this jar
mentions shukaro/artifice somewhere".  Skipping the huge binary assets and
recurse into embedded jars (jar-in-jar, shaded libs).
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

NEEDLES = [b'shukaro', b'Shukaro', b'SHUKARO']

# A constant pool reference looks like  shukaro/artifice/foo/Bar  or  Lshukaro/artifice/foo/Bar;
CLASS_REF = re.compile(rb'(?:L)?shukaro/artifice/[A-Za-z0-9_$/]+')


def interesting(entry_name):
    ext = os.path.splitext(entry_name)[1].lower()
    return ext not in SKIP_EXT


def scan_bytes(blob, label, out):
    low = blob
    if not any(n in low for n in NEEDLES):
        return
    hits = set()
    for m in CLASS_REF.finditer(low):
        hits.add(m.group(0).decode('latin-1').lstrip('L'))
    if hits:
        for h in sorted(hits):
            out.append((label, 'type', h))
    else:
        # Not a type reference: record the surrounding text so we can judge it.
        for n in NEEDLES:
            i = low.find(n)
            while i != -1:
                snippet = low[max(0, i - 60):i + 90]
                snippet = re.sub(rb'[^\x20-\x7e]+', b' ', snippet).decode('latin-1').strip()
                out.append((label, 'text', snippet))
                i = low.find(n, i + 1)
                if len(out) > 4000:
                    return


def scan_zip(path, out, depth=0):
    try:
        zf = zipfile.ZipFile(path)
    except Exception as exc:                                    # noqa: BLE001
        out.append((os.path.basename(str(path)), 'error', f'{exc}'))
        return
    with zf:
        for info in zf.infolist():
            if info.is_dir() or info.file_size > 60 * 1024 * 1024:
                continue
            name = info.filename
            if not interesting(name):
                continue
            try:
                blob = zf.read(info)
            except Exception:                                   # noqa: BLE001
                continue
            label = f'{os.path.basename(str(path))}!{name}'
            if name.lower().endswith(('.jar', '.zip')):
                if depth < 2 and any(n in blob for n in NEEDLES):
                    scan_zip(io.BytesIO(blob), out, depth + 1)
                continue
            scan_bytes(blob, label, out)


def main(root):
    out = []
    jars = sorted(
        os.path.join(dp, f)
        for dp, _, fs in os.walk(root)
        for f in fs
        if f.lower().endswith('.jar')
    )
    print(f'scanning {len(jars)} jars under {root}', file=sys.stderr)
    for p in jars:
        scan_zip(p, out)
    return out


if __name__ == '__main__':
    results = main(sys.argv[1])
    seen = set()
    for label, kind, val in results:
        key = (label, val)
        if key in seen:
            continue
        seen.add(key)
        print(f'{kind}\t{label}\t{val}')
    print(f'\n{len(seen)} distinct hits', file=sys.stderr)
