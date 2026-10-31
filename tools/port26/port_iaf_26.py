"""1.21.1 -> 26.1.2: Ice and Fire's own mechanical rewrites, after tools/port26/port_26.py (Rats' rules).

    python3 tools/port26/port_iaf_26.py      (repo root)

Global renames that are safe whatever the receiver; the receiver-dependent ones are in fix_lines.py (by error).
Running it again changes nothing.
"""
import pathlib
import re

EFFECTS = {
    'MOVEMENT_SPEED': 'SPEED', 'MOVEMENT_SLOWDOWN': 'SLOWNESS', 'DIG_SPEED': 'HASTE', 'DIG_SLOWDOWN': 'MINING_FATIGUE',
    'DAMAGE_BOOST': 'STRENGTH', 'HEAL': 'INSTANT_HEALTH', 'HARM': 'INSTANT_DAMAGE', 'JUMP': 'JUMP_BOOST',
    'CONFUSION': 'NAUSEA', 'DAMAGE_RESISTANCE': 'RESISTANCE',
}

RENAMES = [
    (r'\.isClientSide\b(?!\()', '.isClientSide()'),
    (r'\bisControlledByLocalInstance\(\)', 'isLocalInstanceAuthoritative()'),
    (r'\b(?:this\.)?(?:level\(\)|level|world|worldIn|getLevel\(\)|entity\.level\(\)|[\w.]+\.level\(\))\.getProfiler\(\)', 'net.minecraft.util.profiling.Profiler.get()'),
    (r'\.hasImpulse\b', '.needsSync'),
    (r'\bLivingEntity\.getSlotForHand\(([\w.]+(?:\(\))?)\)', r'\1.asEquipmentSlot()'),
    (r'\btryCheckInsideBlocks\(\)', 'applyEffectsFromBlocks()'),
    (r'\bMobEffects\.(' + '|'.join(EFFECTS) + r')\b', lambda m: 'MobEffects.' + EFFECTS[m.group(1)]),
]

IMPORTS = {
    'ValueInput': 'net.minecraft.world.level.storage.ValueInput',
    'ValueOutput': 'net.minecraft.world.level.storage.ValueOutput',
    'TooltipDisplay': 'net.minecraft.world.item.component.TooltipDisplay',
    'Consumer': 'java.util.function.Consumer',
    'UUIDUtil': 'net.minecraft.core.UUIDUtil',
}

changed = 0
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        new = text
        for pattern, repl in RENAMES:
            new = re.sub(pattern, repl, new)
        code = re.sub(r'^import [^\n]*\n', '', re.sub(r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"', '', new, flags=re.S), flags=re.M)
        for name, fqn in IMPORTS.items():
            if path.stem == name:
                continue
            if re.search(r'(?<![\w.])' + name + r'\b', code) and not re.search(r'^import [\w.]*\b' + name + ';', new, re.M):
                new = re.sub(r'^(package [\w.]+;\n)', r'\1import ' + fqn + ';\n', new, count=1, flags=re.M)
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
