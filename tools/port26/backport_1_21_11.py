"""26.1.2 -> 1.21.11: mechanical rewrites of the Java sources (run from the repo root; running it again changes nothing).

    python3 tools/port26/backport_1_21_11.py <vanilla 1.21.11 sources dir>

Run it once on the 26.1.2 sources: the render type renames cannot tell 26.1's entityCutout from 1.21.11's, so a
second run turns the culled cutouts into entityCutoutNoCull.

- imports of Minecraft classes that 26.1 moved: back to the package of the same class name, when it is unique in the
  1.21.11 sources;
- GUI: GuiGraphicsExtractor -> GuiGraphics, the extract* hooks -> render* (container backgrounds -> renderBg), text ->
  drawString, item -> renderItem;
- render types: 26.1 entityCutout(.., outline) was 1.21.11 entityCutoutNoCull, entityCutoutCull was entityCutout (calls
  and method references: Model(RenderTypes::entityCutout) culled the flat parts of Citadel models);
- player messages: sendOverlayMessage -> displayClientMessage(.., true); ChunkPos.pack -> asLong, containing -> new.
"""
import pathlib
import re
import sys

SRC = pathlib.Path(sys.argv[1])
index, existing = {}, set()
for path in SRC.rglob('*.java'):
    rel = path.relative_to(SRC).with_suffix('')
    fqn = '.'.join(rel.parts)
    existing.add(fqn)
    index.setdefault(rel.parts[-1], []).append(fqn)


def exists(fqn):
    parts = fqn.split('.')
    return any('.'.join(parts[:i]) in existing for i in range(len(parts), 0, -1))


G = r'(?:@NotNull )?(?:final )?GuiGraphics(?:Extractor)? (\w+)'
RENAMES = [
    (r'\bGuiGraphicsExtractor\b', 'GuiGraphics'),
    (r'public void extractRenderState\(' + G + r', int (\w+), int (\w+), float (\w+)\)', r'public void render(GuiGraphics \1, int \2, int \3, float \4)'),
    (r'protected void extractLabels\(', 'protected void renderLabels('),
    (r'(this|super)\.extractTooltip\(', r'\1.renderTooltip('),
    (r'protected void extractWidgetRenderState\(', 'protected void renderWidget('),
    (r'protected void extractContents\(', 'protected void renderContents('),
    (r'public void extractBackground\(', 'public void renderBackground('),
    (r'(super|this)\.extractBackground\(', r'\1.renderBackground('),
    (r'\.centeredText\(', '.drawCenteredString('),
    (r'(\w|\))\.text\((?=\w+(?:\.font|Font\(\))?|font|this\.font)', r'\1.drawString('),
    (r'\.fakeItem\(', '.renderFakeItem('),
    (r'\.itemDecorations\(', '.renderItemDecorations('),
    (r'(graphics|guiGraphics|ms|matrixStack|g|gfx|stack|poseStack|pGuiGraphics)\.item\(', r'\1.renderItem('),
    # render types
    (r'\bRenderTypes\.entityCutoutZOffset\(', 'RenderTypes.entityCutoutNoCullZOffset('),
    (r'\bRenderTypes\.entityCutout\(', 'RenderTypes.entityCutoutNoCull('),
    (r'\bRenderTypes\.entityCutoutCull\(', 'RenderTypes.entityCutout('),
    (r'\bRenderTypes::entityCutout\b', 'RenderTypes::entityCutoutNoCull'),
    (r'\bRenderTypes::entityCutoutCull\b', 'RenderTypes::entityCutout'),
    # messages and chunk positions
    (r'\.sendOverlayMessage\(((?:[^()]|\((?:[^()]|\([^()]*\))*\))*)\)', r'.displayClientMessage(\1, true)'),
    (r'\bChunkPos\.pack\(', 'ChunkPos.asLong('),
    (r'\bChunkPos\.containing\(', 'new ChunkPos('),
    (r'\bSavedDataStorage\b', 'DimensionDataStorage'),
    (r'\bgetLightCoords\(', 'getLightColor('),
    (r'\.delayedComponent\(([\w.]+), context -> context\.getOrThrow\(([\w.]+)\)\)', r'.component(\1, \2)'),
    (r'\.entityTags\(\)', '.getTags()'),
    (r'\bSpreadingSnowyBlock\b', 'SpreadingSnowyDirtBlock'),
    (r'import net\.minecraft\.client\.model\.animal\.equine\.BabyHorseModel;\n', ''),
    (r'\bnew BabyHorseModel\(', 'new HorseModel('),
    (r'\bPathType\.FIRE_IN_NEIGHBOR\b', 'PathType.DANGER_FIRE'),
    (r'\bPathType\.FIRE\b', 'PathType.DAMAGE_FIRE'),
    (r'\.getOverworldClockTime\(\)', '.getDayTime()'),
    (r'new ItemParticleOption\(ParticleTypes\.ITEM, ((?:this\.)?\w+(?:\(\))?)\.getItem\(\)\)', r'new ItemParticleOption(ParticleTypes.ITEM, \1)'),
]

changed = 0
for base in ['common/src/main/java', 'common/src/selftest/java', 'neoforge/src', 'fabric/src']:
    for path in pathlib.Path(base).rglob('*.java'):
        text = path.read_text(encoding='utf-8')
        new = text
        for pattern, repl in RENAMES:
            new = re.sub(pattern, repl, new)
        # screens and widgets: render (renderers keep extractRenderState, as on 1.21.11)
        if '/client/gui/' in path.as_posix() or 'extends Screen' in new or 'extends AbstractWidget' in new:
            new = re.sub(r'super\.extractRenderState\(', 'super.render(', new)
            new = re.sub(r'(\w+)\.extractRenderState\((\w+), (mouseX|mx|x|i|pMouseX), ', r'\1.render(\2, \3, ', new)
        # a container screen's background hook with the super call the 26.1 port added -> renderBg
        if 'extends AbstractContainerScreen' in new:
            new = re.sub(r'public void renderBackground\(GuiGraphics (\w+), int (\w+), int (\w+), float (\w+)\) \{\n\s*super\.renderBackground\(\1, \2, \3, \4\);',
                         r'protected void renderBg(GuiGraphics \1, float \4, int \2, int \3) {', new)

        def relocate(match):
            static, fqn = match.group(1) or '', match.group(2)
            if not fqn.startswith(('net.minecraft.', 'com.mojang.')) or exists(fqn.rstrip('.*') if fqn.endswith('.*') else fqn):
                return match.group(0)
            parts = fqn.split('.')
            for i, part in enumerate(parts):
                if part[:1].isupper():
                    candidates = [c for c in index.get(part, []) if c.startswith(('net.minecraft.', 'com.mojang.'))]
                    if len(candidates) == 1:
                        return 'import ' + static + '.'.join([candidates[0]] + parts[i + 1:]) + ';'
                    break
            return match.group(0)

        new = re.sub(r'import (static )?([\w.]+(?:\.\*)?);', relocate, new)
        if new != text:
            path.write_text(new, encoding='utf-8')
            changed += 1
print(changed, 'files changed')
