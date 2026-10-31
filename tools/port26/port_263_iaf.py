"""26.2 -> 26.3: Ice and Fire's mechanical rewrites (after scripts/port_263_input.py and port_26.py <26.3 sources>).

    python3 tools/port26/port_263_iaf.py      (repo root; running it again changes nothing)

- PoseStack.mulPose(quaternion) -> rotate (mulPose takes matrices only); EnderMan -> Enderman;
- BlockState.blocksMotion() -> is(BlockTags.BLOCKS_MOTION);
- the swing is LivingEntity's SwingState: swing(hand) -> swing(hand, SwingAnimation.DEFAULT, true) (sends it as 1.21.1
  did), attackAnim -> getSwingAnimation(0), swinging -> isSwinging(), swingingArm -> the current swing's hand;
  the troll weapon's swingTime writes (never called: onUpdate has no caller, onEntitySwing the wrong signature) go.
"""
import pathlib
import re

RENAMES = [
    (r'\.mulPose\((?=(?:Axis\.|new Quaternionf|com\.mojang\.math\.Axis\.|[\w.]*[Qq]uat|[\w.]*rotation\(\)|[\w.]*orientation))', '.rotate('),
    (r'\bEnderMan\b', 'Enderman'),
    (r'\.blocksMotion\(\)', '.is(net.minecraft.tags.BlockTags.BLOCKS_MOTION)'),
    (r'(\b[\w.()]+)\.swing\((InteractionHand\.\w+|hand|handIn|\w*[Hh]and)\)', r'\1.swing(\2, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true)'),
    (r'\b(\w+)\.attackAnim\b', r'\1.getSwingAnimation(0)'),
    (r'\b(\w+)\.swingingArm\b', r'(\1.getCurrentSwing() != null ? \1.getCurrentSwing().hand() : InteractionHand.MAIN_HAND)'),
    (r'\b(\w+)\.swinging\b(?!\()', r'\1.isSwinging()'),
    (r'\n[ \t]*player\.swingTime(?: = -1|--);', ''),
]
changed = 0
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        new = text
        for pattern, repl in RENAMES:
            new = re.sub(pattern, repl, new)
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')

# --- second pass: blocks, tools, renderers ---
SECOND = [
    # 26.3 blocks have no codec(): the CODEC fields made with simpleCodec and the codec() overrides go (block/ only)
    ('block', r'\n[ \t]*public static final MapCodec<\w+> CODEC = simpleCodec\([^;]*\);', ''),
    ('block', r'\n[ \t]*@Override\n[ \t]*(?:protected|public) (?:@NotNull )?MapCodec<\? extends \w+> codec\(\) \{\n[ \t]*return CODEC;\n[ \t]*\}\n', '\n'),
    # DirtPathBlock is PathBlock(base block, properties)
    ('block', r'\bextends DirtPathBlock\b', 'extends net.minecraft.world.level.block.PathBlock'),
    ('block', r'import net\.minecraft\.world\.level\.block\.DirtPathBlock;\n', ''),
    # PushReaction names
    ('', r'\bPushReaction\.DESTROY\b', 'PushReaction.POPPED'), ('', r'\bPushReaction\.BLOCK\b', 'PushReaction.IMMOVEABLE'),
    ('', r'\bPushReaction\.NORMAL\b', 'PushReaction.PUSH_PULL'), ('', r'\bPushReaction\.PUSH_ONLY\b', 'PushReaction.PUSH'),
    # EntityRenderer.shouldRender takes the partial tick
    ('client', r'public boolean shouldRender\(([^)]*double camZ)\)', r'public boolean shouldRender(\1, float partialTicks)'),
    ('client', r'super\.shouldRender\((\w+), (\w+), (\w+), (\w+), (\w+)\)', r'super.shouldRender(\1, \2, \3, \4, \5, partialTicks)'),
    # AxeItem/ShovelItem/HoeItem are gone: items with Properties.axe/shovel/hoe (they set the block transformer)
    ('item', r'extends (Axe|Shovel|Hoe)Item\b', 'extends Item'),
    ('item', r'super\(IafToolMaterials\.vanilla\(toolmaterial\), ([^,]+), ([^,]+), dev\.vladeezyy\.iceandfire\.platform\.Registrar\.itemProps\(\)\);',
     None),
]
TOOL = {'ItemModAxe.java': 'axe', 'ItemModShovel.java': 'shovel', 'ItemModHoe.java': 'hoe'}
changed = 0
for path in pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire').rglob('*.java'):
    text = path.read_text(encoding='utf-8')
    new = text
    rel = path.relative_to('common/src/main/java/com/github/alexthe666/iceandfire').as_posix()
    for area, pattern, repl in SECOND:
        if area and not rel.startswith(area):
            continue
        if repl is None:
            kind = TOOL.get(path.name)
            if kind:
                new = re.sub(pattern, lambda m: 'super(IafToolMaterials.properties(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().%s(IafToolMaterials.vanilla(toolmaterial), %s, %s), toolmaterial));' % (kind, m.group(1), m.group(2)), new)
            continue
        new = re.sub(pattern, repl, new)
    if path.name == 'BlockCharedPath.java':
        new = new.replace('        super(\n            dev.vladeezyy.iceandfire.platform.Registrar.blockProps()', '        super(net.minecraft.world.level.block.Blocks.DIRT,\n            dev.vladeezyy.iceandfire.platform.Registrar.blockProps()')
    if new != text:
        path.write_text(new, encoding='utf-8')
        changed += 1
print(changed, 'files changed (second pass)')
