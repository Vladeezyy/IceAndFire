#!/usr/bin/env python3
"""NeoForge-only code of the port -> loader-neutral common code (idempotent; run from the repository root).

    python3 -I tools/port_split.py

Registration (DeferredRegister / DeferredHolder / DeferredItem / DeferredBlock -> platform Registrar / RegistryEntry /
ItemEntry / BlockEntry, with the same calls), config (ModConfigSpec -> platform ConfigSpec, the same builder calls),
packet handlers (IPayloadContext -> platform PacketContext), item storages (IItemHandler -> platform ItemHandler).
"""
import re
from pathlib import Path

ROOT = Path("common/src")
P = "dev.vladeezyy.iceandfire.platform"
IMPORTS = {
    "net.neoforged.neoforge.registries.DeferredRegister": f"{P}.Registrar",
    "net.neoforged.neoforge.registries.DeferredHolder": f"{P}.RegistryEntry",
    "net.neoforged.neoforge.registries.DeferredItem": f"{P}.ItemEntry",
    "net.neoforged.neoforge.registries.DeferredBlock": f"{P}.BlockEntry",
    "net.neoforged.neoforge.common.ModConfigSpec": f"{P}.ConfigSpec",
    "net.neoforged.neoforge.network.handling.IPayloadContext": f"{P}.PacketContext",
    "net.neoforged.neoforge.items.IItemHandler": f"{P}.transfer.ItemHandler",
    "net.neoforged.neoforge.items.IItemHandlerModifiable": f"{P}.transfer.ModifiableItemHandler",
    "net.neoforged.neoforge.items.ItemStackHandler": f"{P}.transfer.ItemStackHandler",
    "net.neoforged.neoforge.items.wrapper.InvWrapper": f"{P}.transfer.InvWrapper",
    "net.neoforged.neoforge.items.wrapper.SidedInvWrapper": f"{P}.transfer.SidedInvWrapper",
    "net.neoforged.neoforge.items.wrapper.CombinedInvWrapper": f"{P}.transfer.CombinedInvWrapper",
}
WORDS = [
    (r"\bDeferredRegister\.Items\b", "Registrar.Items"), (r"\bDeferredRegister\.Blocks\b", "Registrar.Blocks"),
    (r"\bDeferredRegister\b", "Registrar"), (r"\bDeferredHolder\b", "RegistryEntry"),
    (r"\bDeferredItem\b", "ItemEntry"), (r"\bDeferredBlock\b", "BlockEntry"),
    (r"\bModConfigSpec\b", "ConfigSpec"), (r"\bIPayloadContext\b", "PacketContext"),
    (r"\bIItemHandlerModifiable\b", "ModifiableItemHandler"), (r"\bIItemHandler\b", "ItemHandler"),
]


def rewrite(src):
    def imp(m):
        name = m.group(1)
        return f"import {IMPORTS[name]};\n" if name in IMPORTS else m.group(0)
    src = re.sub(r"import ([\w.]+);\n", imp, src)
    for a, b in WORDS:
        src = re.sub(a, b, src)
    # packet side: NeoForge's reception side -> the packet flow
    src = src.replace("context.flow().getReceptionSide() == LogicalSide.CLIENT", "context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND")
    src = src.replace("context.flow().getReceptionSide() == LogicalSide.SERVER", "context.flow() == net.minecraft.network.protocol.PacketFlow.SERVERBOUND")
    # NeoForge's Lazy -> Guava's memoizing supplier (Minecraft ships Guava)
    src = re.sub(r"\bLazy<", "java.util.function.Supplier<", src)
    src = src.replace("Lazy.of(", "com.google.common.base.Suppliers.memoize(")
    src = re.sub(r"import net\.neoforged\.neoforge\.common\.util\.Lazy;\n", "", src)
    return src


changed = 0
for path in sorted(ROOT.rglob("*.java")):
    if "dev/vladeezyy/iceandfire/platform" in path.as_posix():
        continue
    s = path.read_text()
    n = rewrite(s)
    if n != s:
        path.write_text(n)
        changed += 1
print(changed, "files rewritten")
