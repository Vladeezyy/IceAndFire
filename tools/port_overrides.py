#!/usr/bin/env python3
"""NeoForge extension methods in the common code (idempotent; run from the repository root after a compile).

    python3 -I tools/port_overrides.py build-compile.log

Structure processors use vanilla processBlock, the piston reaction moves into the block properties; the other methods
that only NeoForge calls lose @Override and get a "NeoForge extension" comment (Fabric calls them from its mixins).
"""
import re
import sys
from pathlib import Path

JAVA = Path("common/src/main/java/com/github/alexthe666/iceandfire")
MARK = "// NeoForge extension method (no vanilla override)"

# processors: process(..., settings, @Nullable StructureTemplate template) -> processBlock(..., settings)
for p in (JAVA / "world/gen/processor").glob("*.java"):
    s = p.read_text()
    s = re.sub(r"public StructureTemplate\.StructureBlockInfo process\((.*?), (?:@Nullable )?StructureTemplate template\)",
               r"public StructureTemplate.StructureBlockInfo processBlock(\1)", s)
    p.write_text(s)

# piston reaction BLOCK -> .pushReaction(PushReaction.BLOCK) in the properties
for name in ("BlockDragonBone", "BlockDragonforgeBricks", "BlockDragonforgeCore", "BlockDragonforgeInput"):
    p = JAVA / f"block/{name}.java"
    s = p.read_text()
    s = re.sub(r"\n    @Override\n    public (?:@NotNull )?PushReaction getPistonPushReaction\([^)]*\) \{\n        return PushReaction\.BLOCK;\n    \}\n", "\n", s)
    if ".pushReaction(PushReaction.BLOCK)" not in s:
        s = re.sub(r"(public " + name + r"\([^)]*\) \{\n\s*super\(\n\s*(?:BlockBehaviour\.)?Properties\n\s*\.of\(\))", r"\1\n                .pushReaction(PushReaction.BLOCK)", s, count=1)
        assert ".pushReaction(PushReaction.BLOCK)" in s, name
    p.write_text(s)

# the other overrides javac reported
log = Path(sys.argv[1]).read_text().splitlines() if len(sys.argv) > 1 else []
spots = {}
for line in log:
    m = re.match(r"\s*(\S+\.java):(\d+): error: method does not override or implement a method from a supertype", line)
    if m:
        spots.setdefault(m.group(1), set()).add(int(m.group(2)))
for path, lines in spots.items():
    if "/world/gen/processor/" in path or any(f"/block/{n}.java" in path for n in ("BlockDragonBone", "BlockDragonforgeBricks", "BlockDragonforgeCore", "BlockDragonforgeInput")):
        continue  # handled above
    p = Path(path)
    src = p.read_text().split("\n")
    for n in sorted(lines, reverse=True):
        if src[n - 1].strip() == "@Override":
            indent = src[n - 1][:len(src[n - 1]) - len(src[n - 1].lstrip())]
            src[n - 1] = indent + MARK
    p.write_text("\n".join(src))
print(sum(len(v) for v in spots.values()), "overrides marked")
