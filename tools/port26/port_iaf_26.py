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
    (r'\.isSolidRender\([^()]*(?:\([^()]*\))?, [^()]*\)', '.isSolidRender()'),
    (r'\n[ \t]*DebugPackets\.sendPathFindingPacket\([^;]*\);', ''),
    (r'import net\.minecraft\.network\.protocol\.game\.DebugPackets;\n', ''),
    (r'\bMinecraft\.getInstance\(\)\.getTimer\(\)', 'Minecraft.getInstance().getDeltaTracker()'),
    (r'(lookupOrThrow\(Registries\.ENCHANTMENT\))\.getTag\(', r'\1.get('),
    (r'ResourceKey::location\b', 'ResourceKey::identifier'),
    (r'\.getLevelData\(\)\.getGameRules\(\)', '.getGameRules()'),
    (r'EnumProperty\.create\(("\w+"), Direction\.Plane\.HORIZONTAL\)', r'EnumProperty.create(\1, Direction.class, Direction.Plane.HORIZONTAL)'),
    # client: 26.1 render types (RenderTypes; entityCutout is no-cull now, entityCutoutCull the old entityCutout)
    (r'(?<![\w.])RenderType\.entityCutout\(', 'RenderTypes.entityCutoutCull('),
    (r'(?<![\w.])RenderType\.entityCutoutNoCull\(', 'RenderTypes.entityCutout('),
    (r'(?<![\w.])RenderType\.entityCutoutNoCullZOffset\(', 'RenderTypes.entityCutoutZOffset('),
    (r'(?<![\w.])RenderType\.entityNoOutline\(([^()]*(?:\([^()]*\))*)\)', r'RenderTypes.entityTranslucent(\1, false)'),
    (r'(?<![\w.])RenderType\.(entityTranslucent|eyes|lightning|energySwirl|armorCutoutNoCull|entitySolid|lines)\(', r'RenderTypes.\1('),
    # block render layers come from the textures in 26.1 (no ItemBlockRenderTypes)
    (r'\n[ \t]*dev\.vladeezyy\.iceandfire\.platform\.ClientPlatform\.INSTANCE\.setRenderLayer\([^;]*\);', ''),
    (r'\.registryOrThrow\(', '.lookupOrThrow('),
    (r'\.registry\((Registries\.\w+)\)\.(?:orElseThrow|get)\(\)', r'.lookupOrThrow(\1)'),
    (r'(\.lookupOrThrow\([^()]*\))\.getHolderOrThrow\(', r'\1.getOrThrow('),
    (r'(\.lookupOrThrow\([^()]*\))\.getHolder\(', r'\1.get('),
    (r'new InteractionResult(?:Holder<>)?\((InteractionResult\.\w+), [^()]*(?:\([^()]*\))?\)', r'\1'),
    (r'\.noCollission\(\)', '.noCollision()'),
    (r'\bSpreadingSnowyDirtBlock\b', 'SpreadingSnowyBlock'),
    (r'\bDirectionProperty\.create\(', 'EnumProperty.create('),
    (r'\bDirectionProperty\b', 'EnumProperty<Direction>'),
    (r'import net\.minecraft\.world\.level\.block\.state\.properties\.EnumProperty<Direction>;', 'import net.minecraft.world.level.block.state.properties.EnumProperty;'),
    (r'\bGameRules\.RULE_DOMOBLOOT\b', 'GameRules.MOB_DROPS'), (r'\bGameRules\.RULE_MOBGRIEFING\b', 'GameRules.MOB_GRIEFING'),
    (r'\bGameRules\.RULE_DOENTITYDROPS\b', 'GameRules.ENTITY_DROPS'), (r'\bGameRules\.RULE_DOMOBSPAWNING\b', 'GameRules.SPAWN_MOBS'),
    (r'\.getGameRules\(\)\.getBoolean\(', '.getGameRules().get('),
    (r'\bPathType\.DANGER_FIRE\b', 'PathType.FIRE_IN_NEIGHBOR'), (r'\bPathType\.DAMAGE_FIRE\b', 'PathType.FIRE'),
    (r'\bChunkPos\.asLong\(', 'ChunkPos.pack('),
    (r'\bnew ChunkPos\(((?:[\w.]+\(\))?[\w.]*(?:pos|Pos|pos\(\))\w*(?:\(\))?)\)', r'ChunkPos.containing(\1)'),
    (r'\bDirection\.getNearest\(', 'Direction.getApproximateNearest('),
    (r'\bIngredient\.CODEC_NONEMPTY\b', 'Ingredient.CODEC'),
    (r'\.getParamOrNull\(', '.getOptionalParameter('),
    (r'\bnew TemptGoal\(([^;]*?), Ingredient\.of\(([\w.]+)\)', r'new TemptGoal(\1, stack -> stack.is(\2)'),
    (r'\n[ \t]*this\.noCulling = (?:true|false);', ''),
    (r'\bNbtUtils\.createUUID\(([^()]*(?:\([^()]*\))?)\)', r'new net.minecraft.nbt.IntArrayTag(UUIDUtil.uuidToIntArray(\1))'),
    (r'\bNbtUtils\.loadUUID\(([^()]*)\)', r'UUIDUtil.uuidFromIntArray(((net.minecraft.nbt.IntArrayTag) \1).getAsIntArray())'),
    (r'\.putUUID\(("[^"]*"|[\w.]+), ', r'.store(\1, UUIDUtil.CODEC, '),
    (r'\.getUUID\(("[^"]*"|[\w.]+)\)', r'.read(\1, UUIDUtil.CODEC).orElse(null)'),
    (r'\.hasUUID\(("[^"]*"|[\w.]+)\)', r'.read(\1, UUIDUtil.CODEC).isPresent()'),
    (r'\.getInventory\(\)\.selected\b', '.getInventory().getSelectedSlot()'),
    (r'(BuiltInRegistries\.\w+)\.holders\(\)', r'\1.listElements()'),
    (r'\bRenderSystem\.(?:enableBlend|disableBlend|defaultBlendFunc|enableDepthTest|disableDepthTest)\(\);\n', ''),
]

IMPORTS = {
    'ValueInput': 'net.minecraft.world.level.storage.ValueInput',
    'ValueOutput': 'net.minecraft.world.level.storage.ValueOutput',
    'TooltipDisplay': 'net.minecraft.world.item.component.TooltipDisplay',
    'Consumer': 'java.util.function.Consumer',
    'UUIDUtil': 'net.minecraft.core.UUIDUtil',
    'EntitySpawnReason': 'net.minecraft.world.entity.EntitySpawnReason',
    'ItemStackWithSlot': 'net.minecraft.world.ItemStackWithSlot',
    'RenderTypes': 'net.minecraft.client.renderer.rendertype.RenderTypes',
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
