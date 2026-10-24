#!/usr/bin/env python3
"""NeoForge hooks of the game code -> Platform / IafBus (idempotent; run from the repository root).

    python3 -I tools/port_hooks.py
"""
import re
from pathlib import Path

JAVA = Path("common/src/main/java")
P = "dev.vladeezyy.iceandfire.platform.Platform.INSTANCE"
RULES = [
    (r"(?:net\.neoforged\.neoforge\.common\.)?NeoForge\.EVENT_BUS\.post\((new (?:GenericGriefEvent|DragonFireEvent|DragonFireDamageWorldEvent)\(.*?\))\)\.isCanceled\(\)",
     r"dev.vladeezyy.iceandfire.bus.IafBus.post(\1)"),
    (r"(?:net\.neoforged\.neoforge\.event\.)?EventHooks\.canEntityGrief\(", P + ".canEntityGrief("),
    (r"(?:net\.neoforged\.neoforge\.event\.)?EventHooks\.onPlayerDestroyItem\(", P + ".onPlayerDestroyItem("),
    (r"(?:net\.neoforged\.neoforge\.event\.)?EventHooks\.onProjectileImpact\(", P + ".onProjectileImpact("),
    (r"(?:net\.neoforged\.neoforge\.event\.)?EventHooks\.getExperienceDrop\(", P + ".experienceDrop("),
    (r"(\w+(?:\.\w+\(\))*)\.getItem\(\)\.canPerformAction\(([^,()]+(?:\(\))?), ItemAbilities\.SHIELD_BLOCK\)", P + r".canShieldBlock(\2)"),
    (r"PacketDistributor\.sendToPlayersTrackingEntityAndSelf\(", P + ".sendToTrackingAndSelf("),
    (r"FMLEnvironment\.dist == Dist\.CLIENT", P + ".isClient()"),
    (r"import net\.neoforged\.neoforge\.(?:common\.NeoForge|event\.EventHooks|common\.ItemAbilities|network\.PacketDistributor"
     r"|capabilities\.Capabilities|fml\.loading\.FMLEnvironment);\n", ""),
    (r"import net\.neoforged\.fml\.loading\.FMLEnvironment;\n", ""),
]

changed = 0
for path in JAVA.rglob("*.java"):
    src = path.read_text()
    new = src
    for pattern, repl in RULES:
        new = re.sub(pattern, repl, new)
    if new != src:
        path.write_text(new)
        changed += 1
print(changed, "files changed")
