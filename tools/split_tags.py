#!/usr/bin/env python3
"""Tag classes of the datagen that are also the game code's tag constants -> constants class in common (same name)
+ the data provider in the NeoForge module (<Name>Provider, constants by static import). One-off. Run from the root."""
import re
import subprocess
from pathlib import Path

PKG = "com/github/alexthe666/iceandfire/datagen"
COMMON = Path("common/src/main/java") / PKG
NEO = Path("neoforge/src/main/java") / PKG
FILES = ["tags/IafBlockTags", "tags/IafItemTags", "tags/IafEntityTags", "tags/BannerPatternTagGenerator", "tags/POITagGenerator", "IafBiomeTagGenerator"]
FIELD = re.compile(r"^    public static (?:final )?TagKey<[\w.<>?]+> \w+ = .*?;\n", re.M | re.S)
HELPER = re.compile(r"^    private static TagKey<[\w.<>?]+> \w+\([^)]*\) \{\n.*?^    \}\n", re.M | re.S)


def body_end(src, start):
    depth = 0
    for i in range(src.index("{", start), len(src)):
        depth += src[i] == "{"
        depth -= src[i] == "}"
        if depth == 0:
            return i
    raise ValueError


for f in FILES:
    path = COMMON / f"{f}.java"
    src = path.read_text()
    name = path.stem
    pkg = re.search(r"^package ([\w.]+);", src, re.M).group(1)
    fields = FIELD.findall(src)
    helpers = HELPER.findall(src)
    imports = [l for l in re.findall(r"^import .*;$", src, re.M) if "neoforged" not in l and "data." not in l]
    # the provider: the original class renamed, constants imported
    neo = NEO / f"{f}Provider.java"
    neo.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["git", "mv", str(path), str(neo)], check=True)
    prov = neo.read_text()
    for x in fields + helpers:
        prov = prov.replace(x, "")
    prov = re.sub(rf"\b{name}\b", f"{name}Provider", prov)
    prov = re.sub(r"^(package [\w.]+;\n)", rf"\1\nimport static {pkg}.{name}.*;\n", prov, count=1, flags=re.M)
    neo.write_text(prov)
    # the constants
    body = "".join(fields) + ("\n" if helpers else "") + "\n".join(helpers)
    cls = f"package {pkg};\n\n" + "\n".join(imports) + f"\n\n/** The tags of the data provider {name}Provider (NeoForge module), for the game code of both loaders */\npublic final class {name} {{\n    private {name}() {{\n    }}\n\n{body}}}\n"
    path.write_text(cls)
    print(name, len(fields), "constants", len(helpers), "helpers")
