"""1.21.1 -> 26.1 entity renderers and layers of the mod (render states), the mechanical part:

    python3 tools/port26/port_renderers.py      (repo root; idempotent)

- MobRenderer<E, M> / LivingEntityRenderer<E, M> -> IafMobRenderer / IafLivingRenderer (Rampart pose in the state);
- getTextureLocation(E) -> getTexture(E); scale / setupRotations / getFlipDegrees take the state, the entity is
  read back from the state's pose; render(E, ...) -> submit(state, ...) with the same locals (draw calls to fix by hand);
- RenderLayer<E, M> -> IafEntityLayer<E, IafMobRenderState, M>: render(...) -> submit(..., state, entity), the
  parent model drawn again with another render type -> submitParent.
"""
import pathlib
import re

ROOT = pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire/client/render/entity')
P = r'(?:@\w+ )?(?:final )?'
PKG = 'com.github.alexthe666.iceandfire.client.render.entity.'


def body_end(text, i):
    depth = 1
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


def rewrite_method(text, header_re, new_header, prelude, body_fix=None):
    out, pos = '', 0
    for m in re.finditer(header_re, text):
        end = body_end(text, m.end())
        body = text[m.end():end]
        if body_fix:
            body = body_fix(body, m)
        out += text[pos:m.start()] + new_header(m) + prelude(m) + body
        pos = end
    return out + text[pos:]


def add_import(text, fqn):
    if 'import ' + fqn + ';' in text or text.startswith('package ' + fqn.rsplit('.', 1)[0] + ';'):
        return text
    return re.sub(r'^(package [\w.]+;\n)', r'\1import ' + fqn + ';\n', text, count=1, flags=re.M)


for path in sorted(ROOT.rglob('*.java')):
    text = path.read_text(encoding='utf-8')
    new = text
    m = re.search(r'extends (MobRenderer|LivingEntityRenderer)<(\w+), ([^{]+?)>(\s+implements [^{]+)?\s*\{', new)
    if m and path.name not in ('IafMobRenderer.java', 'IafLivingRenderer.java'):
        base = 'IafMobRenderer' if m.group(1) == 'MobRenderer' else 'IafLivingRenderer'
        entity = m.group(2)
        new = new[:m.start()] + 'extends ' + base + '<' + entity + ', ' + m.group(3) + '>' + (m.group(4) or '') + ' {' + new[m.end():]
        new = re.sub(r'public (?:@NotNull )?Identifier getTextureLocation\(' + P + entity + r' (\w+)\)', r'protected Identifier getTexture(' + entity + r' \1)', new)
        new = rewrite_method(new, r'protected void scale\(' + P + entity + r' (\w+), ' + P + r'PoseStack (\w+), ' + P + r'float (\w+)\)\s*\{',
                             lambda mm: 'protected void scale(IafMobRenderState iafState, PoseStack ' + mm.group(2) + ') {',
                             lambda mm: '\n        ' + entity + ' ' + mm.group(1) + ' = (' + entity + ') iafState.pose.entity;\n        float ' + mm.group(3) + ' = iafState.partialTick;')
        new = rewrite_method(new, r'protected void setupRotations\(' + P + entity + r' (\w+), ' + P + r'PoseStack (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+)\)\s*\{',
                             lambda mm: 'protected void setupRotations(IafMobRenderState iafState, PoseStack ' + mm.group(2) + ', float ' + mm.group(4) + ', float ' + mm.group(6) + ') {',
                             lambda mm: '\n        ' + entity + ' ' + mm.group(1) + ' = (' + entity + ') iafState.pose.entity;\n        float ' + mm.group(3) + ' = iafState.ageInTicks;\n        float ' + mm.group(5) + ' = iafState.partialTick;',
                             lambda b, mm: re.sub(r'super\.setupRotations\(\w+, (\w+), \w+, (\w+), \w+, (\w+)\)', r'super.setupRotations(iafState, \1, \2, \3)', b))
        new = re.sub(r'protected float getFlipDegrees\(' + P + entity + r' \w+\)', 'protected float getFlipDegrees()', new)
        new = re.sub(r'protected boolean shouldShowName\(' + P + entity + r' (\w+)\)', r'protected boolean shouldShowName(' + entity + r' \1, double iafDistanceSq)', new)
        new = re.sub(r'super\.shouldShowName\((\w+)\)', r'super.shouldShowName(\1, iafDistanceSq)', new)
        new = rewrite_method(new, r'public void render\(' + P + entity + r' (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'PoseStack (\w+), ' + P + r'MultiBufferSource (\w+), ' + P + r'int (\w+)\)\s*\{',
                             lambda mm: 'public void submit(IafMobRenderState iafState, PoseStack ' + mm.group(4) + ', SubmitNodeCollector iafCollector, CameraRenderState iafCamera) {',
                             lambda mm: '\n        ' + entity + ' ' + mm.group(1) + ' = (' + entity + ') iafState.pose.entity;\n        float ' + mm.group(2) + ' = iafState.bodyRot;\n        float ' + mm.group(3) + ' = iafState.partialTick;\n        int ' + mm.group(6) + ' = iafState.lightCoords;',
                             lambda b, mm: re.sub(r'super\.render\(\w+, \w+, \w+, (\w+), \w+, \w+\)', r'super.submit(iafState, \1, iafCollector, iafCamera)', b))
        new = new.replace('this.layers.add(', 'this.addLayer(')
        for fqn in [PKG + base, PKG + 'IafMobRenderState', 'net.minecraft.client.renderer.SubmitNodeCollector', 'net.minecraft.client.renderer.state.level.CameraRenderState']:
            simple = fqn.rsplit('.', 1)[1]
            if re.search(r'\b' + simple + r'\b', new):
                new = add_import(new, fqn)
    m = re.search(r'extends RenderLayer<(\w+), ([^{]+?)>\s*\{', new)
    if m and path.name != 'IafEntityLayer.java':
        entity, model = m.group(1), m.group(2)
        new = new[:m.start()] + 'extends IafEntityLayer<' + entity + ', IafMobRenderState, ' + model + '> {' + new[m.end():]
        new = re.sub(r'RenderLayerParent<' + re.escape(entity) + r', ' + re.escape(model) + '>', 'RenderLayerParent<IafMobRenderState, ' + model + '>', new)
        new = rewrite_method(new, r'public void render\(' + P + r'PoseStack (\w+), ' + P + r'MultiBufferSource (\w+), ' + P + r'int (\w+), ' + P + r'[\w.]+ (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+), ' + P + r'float (\w+)\)\s*\{',
                             lambda mm: 'protected void submit(PoseStack ' + mm.group(1) + ', SubmitNodeCollector iafCollector, int ' + mm.group(3) + ', IafMobRenderState iafState, ' + entity + ' ' + mm.group(4) + ') {',
                             lambda mm: '\n        float ' + mm.group(5) + ' = iafState.pose.limbSwing, ' + mm.group(6) + ' = iafState.pose.limbSwingAmount, ' + mm.group(7) + ' = iafState.partialTick, ' + mm.group(8) + ' = iafState.ageInTicks, ' + mm.group(9) + ' = iafState.pose.netHeadYaw, ' + mm.group(10) + ' = iafState.pose.headPitch;',
                             lambda b, mm: re.sub(r'VertexConsumer (\w+) = ' + mm.group(2) + r'\.getBuffer\(([^;]+)\);\s*(?:this\.)?getParentModel\(\)\.renderToBuffer\(' + mm.group(1) + r', \1, ([^,]+), ([^,]+), ([^)]+)\);',
                                                  r'submitParent(' + mm.group(1) + r', iafCollector, iafState, \2, \3, \4, \5, 1);', b))
        for fqn in [PKG + 'layer.IafEntityLayer', PKG + 'IafMobRenderState', 'net.minecraft.client.renderer.SubmitNodeCollector']:
            simple = fqn.rsplit('.', 1)[1]
            if re.search(r'\b' + simple + r'\b', new):
                new = add_import(new, fqn)
    if new != text:
        path.write_text(new, encoding='utf-8')
        print('changed', path.name)
