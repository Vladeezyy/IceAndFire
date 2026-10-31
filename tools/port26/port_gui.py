"""1.21.1 -> 26.1 GUI of the mod (GuiGraphics -> GuiGraphicsExtractor; idempotent): screen and widget hooks
(render -> extractRenderState, renderBg -> extractBackground, renderLabels -> extractLabels, renderWidget ->
extractWidgetRenderState), the 2D pose stack (Matrix3x2fStack), blits with a pipeline, text with an opaque colour
(26.x draws a colour without alpha transparent), items and tooltips.
"""
import pathlib
import re

ROOTS = [pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire/client'), pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire/compat')]
G = r'(?:@NotNull )?(?:final )?GuiGraphicsExtractor (\w+)'


def args_end(text, i):
    depth = 1
    while depth:
        depth += {'(': 1, ')': -1}.get(text[i], 0)
        i += 1
    return i


def split_args(s):
    out, depth, cur = [], 0, ''
    for ch in s:
        if ch == ',' and depth == 0:
            out.append(cur.strip())
            cur = ''
            continue
        depth += {'(': 1, ')': -1, '[': 1, ']': -1}.get(ch, 0)
        cur += ch
    out.append(cur.strip())
    return out


def rewrite_calls(text, name, fn):
    out, pos = '', 0
    for m in re.finditer(r'\.' + name + r'\(', text):
        if m.start() < pos:
            continue
        end = args_end(text, m.end())
        rep = fn(split_args(text[m.end():end - 1]))
        if rep is None:
            continue
        out += text[pos:m.start()] + '.' + rep
        pos = end
    return out + text[pos:]


def blit(a):
    if a and a[0].startswith('RenderPipelines.'):
        return None
    if len(a) == 7:
        tex, x, y, u, v, w, h = a
        return 'blit(RenderPipelines.GUI_TEXTURED, %s, %s, %s, (float) (%s), (float) (%s), %s, %s, 256, 256)' % (tex, x, y, u, v, w, h)
    if len(a) == 9:
        tex, x, y, u, v, w, h, tw, th = a
        return 'blit(RenderPipelines.GUI_TEXTURED, %s, %s, %s, (float) (%s), (float) (%s), %s, %s, %s, %s)' % (tex, x, y, u, v, w, h, tw, th)
    if len(a) == 10:
        # blit(texture, x, y, uWidth?, ...) with a z offset as the 4th argument in 1.21.1: blit(tex, x, y, z, u, v, w, h, tw, th)
        tex, x, y, z, u, v, w, h, tw, th = a
        return 'blit(RenderPipelines.GUI_TEXTURED, %s, %s, %s, (float) (%s), (float) (%s), %s, %s, %s, %s)' % (tex, x, y, u, v, w, h, tw, th)
    return None


def text_call(new_name):
    def fn(a):
        if len(a) >= 5:
            a = list(a)
            if not a[4].startswith('net.minecraft.util.ARGB.opaque('):
                a[4] = 'net.minecraft.util.ARGB.opaque(' + a[4] + ')'
            return new_name + '(' + ', '.join(a) + ')'
        return None
    return fn


for root in ROOTS:
    for path in root.rglob('*.java'):
        text = path.read_text()
        if 'GuiGraphicsExtractor' not in text:
            continue
        new = text
        new = re.sub(r'public void render\(' + G + r', int (\w+), int (\w+), float (\w+)\)', r'public void extractRenderState(GuiGraphicsExtractor \1, int \2, int \3, float \4)', new)
        new = re.sub(r'super\.render\((\w+), (\w+), (\w+), (\w+)\)', r'super.extractRenderState(\1, \2, \3, \4)', new)
        new = re.sub(r'public void renderBackground\(' + G + r', int (\w+), int (\w+), float (\w+)\)', r'public void extractBackground(GuiGraphicsExtractor \1, int \2, int \3, float \4)', new)
        new = re.sub(r'(super|this)\.renderBackground\(', r'\1.extractBackground(', new)
        new = re.sub(r'protected void renderBg\(' + G + r', float (\w+), int (\w+), int (\w+)\)\s*\{',
                     r'public void extractBackground(GuiGraphicsExtractor \1, int \3, int \4, float \2) {\n        super.extractBackground(\1, \3, \4, \2);', new)
        new = re.sub(r'protected void renderLabels\(' + G + r', int (\w+), int (\w+)\)', r'protected void extractLabels(GuiGraphicsExtractor \1, int \2, int \3)', new)
        new = re.sub(r'(this|super)\.renderTooltip\((\w+), (\w+), (\w+)\)', r'\1.extractTooltip(\2, \3, \4)', new)
        new = re.sub(r'(?:public|protected) void renderWidget\(' + G + r', int (\w+), int (\w+), float (\w+)\)', r'protected void extractWidgetRenderState(GuiGraphicsExtractor \1, int \2, int \3, float \4)', new)
        new = re.sub(r'(\w+)\.render\((\w+), (mouseX|mx|x), (mouseY|my|y), (partialTicks|partialTick|partial|pt|delta|f)\)', r'\1.extractRenderState(\2, \3, \4, \5)', new)
        new = new.replace('.pose().pushPose()', '.pose().pushMatrix()').replace('.pose().popPose()', '.pose().popMatrix()')
        new = rewrite_calls(new, r'pose\(\)\.translate', lambda a: 'pose().translate((float) (%s), (float) (%s))' % (a[0], a[1]) if len(a) == 3 else None)
        new = rewrite_calls(new, r'pose\(\)\.scale', lambda a: 'pose().scale((float) (%s), (float) (%s))' % (a[0], a[1]) if len(a) == 3 else None)
        new = re.sub(r'\.pose\(\)\.mulPose\(Axis\.ZP\.rotationDegrees\(([^()]*(?:\([^()]*\))?)\)\)', r'.pose().rotate((float) Math.toRadians(\1))', new)
        new = rewrite_calls(new, 'blit', blit)

        # font.drawInBatch(text, x, y, color, shadow, pose, buffers, mode, bg, light) in a GUI -> graphics.text
        def batch(a, recv):
            if len(a) == 10 and 'pose().last().pose()' in a[5]:
                gfx = a[5].split('.pose()')[0]
                return gfx + '.text(' + recv + ', ' + a[0] + ', (int) (' + a[1] + '), (int) (' + a[2] + '), net.minecraft.util.ARGB.opaque(' + a[3] + '), ' + a[4] + ')'
            return None
        out, pos = '', 0
        for m in re.finditer(r'((?:this\.)?(?:minecraft\.)?(?:font|fontRenderer|fontrenderer|getFont\(\)|getBestiaryFont\(\)))\.drawInBatch\(', new):
            if m.start() < pos:
                continue
            end = args_end(new, m.end())
            rep = batch(split_args(new[m.end():end - 1]), m.group(1))
            if rep is None:
                continue
            out += new[pos:m.start()] + rep
            pos = end
        new = out + new[pos:]
        new = re.sub(r'\n[ \t]*RenderSystem\.setShaderColor\(1(?:\.0F)?, 1(?:\.0F)?, 1(?:\.0F)?, 1(?:\.0F)?\);', '', new)
        if re.search(r'extends (?:Button|AbstractButton)\b', new):
            new = re.sub(r'protected void extractWidgetRenderState\(GuiGraphicsExtractor (\w+), int (\w+), int (\w+), float (\w+)\)', r'protected void extractContents(GuiGraphicsExtractor \1, int \2, int \3, float \4)', new)
        new = rewrite_calls(new, 'drawString', text_call('text'))
        new = rewrite_calls(new, 'drawCenteredString', text_call('centeredText'))
        new = re.sub(r'\.renderItem\(', '.item(', new)
        new = re.sub(r'\.renderFakeItem\(', '.fakeItem(', new)
        new = re.sub(r'\.renderItemDecorations\(', '.itemDecorations(', new)
        new = re.sub(r'(\w+)\.renderTooltip\((\w+(?:\.font)?|font|this\.font|this\.minecraft\.font), ', r'\1.setTooltipForNextFrame(\2, ', new)
        if new != text:
            if 'RenderPipelines.' in new and 'import net.minecraft.client.renderer.RenderPipelines;' not in new:
                new = re.sub(r'^(package [\w.]+;\n)', r'\1import net.minecraft.client.renderer.RenderPipelines;\n', new, count=1, flags=re.M)
            path.write_text(new)
            print('changed', path.name)
