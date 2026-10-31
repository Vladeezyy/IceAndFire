"""1.21.2 - 26.1 override signatures: the declaration gets the new parameters and the super call inside it passes them.

python3 tools/port/port_signatures.py      (repo root; common/src/main/java)
"""
import pathlib
import re

P = r'(?:@\w+(?:\.\w+)* )?(?:final )?'
# name: (declaration regex with groups for the kept parameter names, new parameter list, super-call rewrite)
RULES = [
    ('doHurtTarget', r'doHurtTarget\(' + P + r'Entity (\w+)\)', lambda m: 'doHurtTarget(ServerLevel level, Entity ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.doHurtTarget\((\w+)\)', r'super.doHurtTarget(level, \1)', b)),
    ('customServerAiStep', r'customServerAiStep\(\)', lambda m: 'customServerAiStep(ServerLevel level)',
     lambda b, m: b.replace('super.customServerAiStep()', 'super.customServerAiStep(level)')),
    ('isInvulnerableTo', r'isInvulnerableTo\(' + P + r'DamageSource (\w+)\)', lambda m: 'isInvulnerableTo(ServerLevel level, DamageSource ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.isInvulnerableTo\((\w+)\)', r'super.isInvulnerableTo(level, \1)', b)),
    ('causeFallDamage', r'causeFallDamage\(' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'DamageSource (\w+)\)', lambda m: 'causeFallDamage(double ' + m.group(1) + ', float ' + m.group(2) + ', DamageSource ' + m.group(3) + ')', None),
    ('calculateFallDamage', r'calculateFallDamage\(' + P + r'float (\w+), ' + P + r'float (\w+)\)', lambda m: 'calculateFallDamage(double ' + m.group(1) + ', float ' + m.group(2) + ')', None),
    ('getBaseExperienceReward', r'getBaseExperienceReward\(\)', lambda m: 'getBaseExperienceReward(ServerLevel level)',
     lambda b, m: b.replace('super.getBaseExperienceReward()', 'super.getBaseExperienceReward(level)')),
    ('interact', r'InteractionResult interact\(' + P + r'Player (\w+), ' + P + r'InteractionHand (\w+)\)', lambda m: 'InteractionResult interact(Player ' + m.group(1) + ', InteractionHand ' + m.group(2) + ', Vec3 location)',
     lambda b, m: re.sub(r'super\.interact\((\w+), (\w+)\)', r'super.interact(\1, \2, location)', b)),
    ('applyEffectTick', r'applyEffectTick\(' + P + r'LivingEntity (\w+), ' + P + r'int (\w+)\)', lambda m: 'applyEffectTick(ServerLevel serverLevel, LivingEntity ' + m.group(1) + ', int ' + m.group(2) + ')',
     lambda b, m: re.sub(r'super\.applyEffectTick\((\w+), (\w+)\)', r'super.applyEffectTick(serverLevel, \1, \2)', b)),
    ('startRiding', r'boolean startRiding\(' + P + r'Entity (\w+), ' + P + r'boolean (\w+)\)', lambda m: 'boolean startRiding(Entity ' + m.group(1) + ', boolean ' + m.group(2) + ', boolean sendEventAndTriggers)',
     lambda b, m: re.sub(r'super\.startRiding\((\w+), (\w+)\)', r'super.startRiding(\1, \2, sendEventAndTriggers)', b)),
    ('destroy', r'void destroy\(' + P + r'DamageSource (\w+)\)', lambda m: 'void destroy(ServerLevel level, DamageSource ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.destroy\((\w+)\)', r'super.destroy(level, \1)', b)),
    ('getAnalogOutputSignal', r'getAnalogOutputSignal\(' + P + r'BlockState (\w+), ' + P + r'Level (\w+), ' + P + r'BlockPos (\w+)\)', lambda m: 'getAnalogOutputSignal(BlockState ' + m.group(1) + ', Level ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Direction direction)', None),
    ('fallOn', r'fallOn\(' + P + r'Level (\w+), ' + P + r'BlockState (\w+), ' + P + r'BlockPos (\w+), ' + P + r'Entity (\w+), ' + P + r'float (\w+)\)', lambda m: 'fallOn(Level ' + m.group(1) + ', BlockState ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Entity ' + m.group(4) + ', double ' + m.group(5) + ')', None),
    ('entityInside', r'entityInside\(' + P + r'BlockState (\w+), ' + P + r'Level (\w+), ' + P + r'BlockPos (\w+), ' + P + r'Entity (\w+)\)', lambda m: 'entityInside(BlockState ' + m.group(1) + ', Level ' + m.group(2) + ', BlockPos ' + m.group(3) + ', Entity ' + m.group(4) + ', InsideBlockEffectApplier effectApplier, boolean isPrecise)',
     lambda b, m: re.sub(r'super\.entityInside\((\w+), (\w+), (\w+), (\w+)\)', r'super.entityInside(\1, \2, \3, \4, effectApplier, isPrecise)', b)),
    ('pickUpItem', r'void pickUpItem\(' + P + r'ItemEntity (\w+)\)', lambda m: 'void pickUpItem(ServerLevel level, ItemEntity ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.pickUpItem\((\w+)\)', r'super.pickUpItem(level, \1)', b)),
    # Ice and Fire
    ('hurt', r'boolean hurt\(' + P + r'DamageSource (\w+), ' + P + r'float (\w+)\)', lambda m: 'boolean hurtServer(ServerLevel level, DamageSource ' + m.group(1) + ', float ' + m.group(2) + ')',
     lambda b, m: re.sub(r'super\.hurt\(', 'super.hurtServer(level, ', b)),
    ('killedEntity', r'boolean killedEntity\(' + P + r'ServerLevel (\w+), ' + P + r'LivingEntity (\w+)\)', lambda m: 'boolean killedEntity(ServerLevel ' + m.group(1) + ', LivingEntity ' + m.group(2) + ', DamageSource damageSource)',
     lambda b, m: re.sub(r'super\.killedEntity\((\w+), (\w+)\)', r'super.killedEntity(\1, \2, damageSource)', b)),
    ('kill', r'void kill\(\)', lambda m: 'void kill(ServerLevel level)', lambda b, m: b.replace('super.kill()', 'super.kill(level)')),
    ('dropFromLootTable', r'void dropFromLootTable\(' + P + r'DamageSource (\w+), ' + P + r'boolean (\w+)\)', lambda m: 'void dropFromLootTable(ServerLevel level, DamageSource ' + m.group(1) + ', boolean ' + m.group(2) + ')',
     lambda b, m: re.sub(r'super\.dropFromLootTable\((\w+), (\w+)\)', r'super.dropFromLootTable(level, \1, \2)', b)),
    ('dropEquipment', r'void dropEquipment\(\)', lambda m: 'void dropEquipment(ServerLevel level)', lambda b, m: b.replace('super.dropEquipment()', 'super.dropEquipment(level)')),
    ('shouldDropLoot', r'boolean shouldDropLoot\(\)', lambda m: 'boolean shouldDropLoot(ServerLevel level)', lambda b, m: b.replace('super.shouldDropLoot()', 'super.shouldDropLoot(level)')),
    ('equipItemIfPossible', r'ItemStack equipItemIfPossible\(' + P + r'ItemStack (\w+)\)', lambda m: 'ItemStack equipItemIfPossible(ServerLevel level, ItemStack ' + m.group(1) + ')',
     lambda b, m: re.sub(r'super\.equipItemIfPossible\((\w+)\)', r'super.equipItemIfPossible(level, \1)', b)),
    ('getHomeRadius', r'float getHomeRadius\(\)', lambda m: 'int getHomeRadius()', None),
    ('changeDimension', r'Entity changeDimension\(' + P + r'TeleportTransition (\w+)\)', lambda m: 'Entity teleport(TeleportTransition ' + m.group(1) + ')',
     lambda b, m: b.replace('super.changeDimension(', 'super.teleport(')),
    ('inventoryTick', r'void inventoryTick\(' + P + r'ItemStack (\w+), ' + P + r'Level (\w+), ' + P + r'Entity (\w+), ' + P + r'int (\w+), ' + P + r'boolean (\w+)\)',
     lambda m: 'void inventoryTick(ItemStack ' + m.group(1) + ', ServerLevel ' + m.group(2) + ', Entity ' + m.group(3) + ', @Nullable EquipmentSlot slot)',
     lambda b, m: re.sub(r'super\.inventoryTick\((\w+), (\w+), (\w+), (\w+), (\w+)\)', r'super.inventoryTick(\1, \2, \3, slot)', re.sub(r'\b' + m.group(5) + r'\b', '(slot == EquipmentSlot.MAINHAND)', b))),
    ('onCraftedBy', r'void onCraftedBy\(' + P + r'ItemStack (\w+), ' + P + r'Level (\w+), ' + P + r'Player (\w+)\)', lambda m: 'void onCraftedBy(ItemStack ' + m.group(1) + ', Player ' + m.group(3) + ')',
     lambda b, m: re.sub(r'super\.onCraftedBy\((\w+), (\w+), (\w+)\)', r'super.onCraftedBy(\1, \3)', re.sub(r'(?<![\w.])' + m.group(2) + r'\b', m.group(3) + '.level()', b))),
    ('propagatesSkylightDown', r'boolean propagatesSkylightDown\(' + P + r'BlockState (\w+), ' + P + r'BlockGetter \w+, ' + P + r'BlockPos \w+\)', lambda m: 'boolean propagatesSkylightDown(BlockState ' + m.group(1) + ')', None),
    ('onAboveBubbleCol', r'void onAboveBubbleCol\(' + P + r'boolean (\w+)\)', lambda m: 'void onAboveBubbleColumn(boolean ' + m.group(1) + ', BlockPos bubblePos)',
     lambda b, m: re.sub(r'super\.onAboveBubbleCol\((\w+)\)', r'super.onAboveBubbleColumn(\1, bubblePos)', b)),
    ('canChangeDimensions', r'canChangeDimensions\(', lambda m: 'canTeleport(', lambda b, m: b.replace('super.canChangeDimensions(', 'super.canTeleport(')),
]
IMPORTS = {'EquipmentSlot': 'net.minecraft.world.entity.EquipmentSlot', 'Nullable': 'javax.annotation.Nullable', 'BlockPos': 'net.minecraft.core.BlockPos', 'DamageSource': 'net.minecraft.world.damagesource.DamageSource', 'ServerLevel': 'net.minecraft.server.level.ServerLevel', 'Vec3': 'net.minecraft.world.phys.Vec3', 'Direction': 'net.minecraft.core.Direction',
           'InsideBlockEffectApplier': 'net.minecraft.world.entity.InsideBlockEffectApplier'}


def body_end(text, start):
    depth, i = 1, start
    while depth and i < len(text):
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


changed = 0
for path in pathlib.Path('common/src/main/java').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    new = text
    for name, decl, header, body_fix in RULES:
        out, pos = '', 0
        for m in re.finditer(r'(?<=\s)' + decl + r'(\s*(?:throws [\w, ]+)?\s*)\{', new):
            end = body_end(new, m.end())
            body = new[m.end():end]
            if body_fix:
                body = body_fix(body, m)
            out += new[pos:m.start()] + header(m) + m.group(m.lastindex) + '{' + body
            pos = end
        new = out + new[pos:]
    new = re.sub(r'\.isDay\(\)', '.isBrightOutside()', new)
    new = re.sub(r'\.isNight\(\)', '.isDarkOutside()', new)
    new = re.sub(r'\.canChangeDimensions\(', '.canTeleport(', new)
    if new != text:
        for simple, fqn in IMPORTS.items():
            if re.search(r'\b' + simple + r'\b', new) and 'import ' + fqn + ';' not in new and not re.search(r'^import [\w.]*\.' + simple + ';', new, re.M):
                new = re.sub(r'^(package [\w.]+;\n)', r'\1import ' + fqn + ';\n', new, count=1, flags=re.M)
        path.write_text(new, encoding='utf-8')
        changed += 1
print(changed, 'files changed')
