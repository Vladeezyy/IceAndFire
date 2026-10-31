"""1.21.1 -> 26.1: the mod's TextureSheetParticle classes -> SingleQuadParticle with an atlas sprite (run once).

Each drew a camera-facing quad of its own texture with the full UV, and changed its size and speed in render(): the
26.1 particle keeps those changes in extract() and lets SingleQuadParticle draw the same quad from the sprite.
"""
import pathlib
import re

ROOT = pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire/client/particle')


def body_end(text, i):
    depth = 1
    while depth:
        depth += {'{': 1, '}': -1}.get(text[i], 0)
        i += 1
    return i


for path in sorted(ROOT.glob('*.java')):
    text = path.read_text()
    if 'extends TextureSheetParticle' not in text:
        continue
    tex = re.search(r'RenderSystem\.setShaderTexture\(0, ([^;]+)\);', text)
    if not tex:
        print('no texture', path.name)
        continue
    texture = tex.group(1)
    first = re.findall(r'\b[A-Z_]{3,}\b', texture)[-1] if not re.fullmatch(r'\w+', texture) else texture
    new = text.replace('extends TextureSheetParticle', 'extends SingleQuadParticle')
    # constructors: the sprite of the texture the particle drew
    def ctor(m):
        args = m.group(1)
        tail = '' if first == texture else '\n        this.iafSpriteLater = true;'
        return 'super(' + args + ', IafParticleSprites.get(' + first + '));' + tail
    new = re.sub(r'super\(((?:world|worldIn|level|levelIn|w)\w*, [^;]*?)\);', ctor, new, count=0)
    # render -> extract: the code before the quad geometry stays
    m = re.search(r'(?:@Override\s*)?public void render\((?:@\w+ )?VertexConsumer \w+, (?:@\w+ )?Camera (\w+), (?:@\w+ )?float (\w+)\)\s*\{', new)
    if m:
        end = body_end(new, m.end())
        body = new[m.end():end - 1]
        cut = re.search(r'\n[^\n]*(?:Vec3 \w+ = ' + m.group(1) + r'\.getPosition\(\);\s*\n[^\n]*)?Mth\.lerp\(' + m.group(2) + r', this\.xo', body)
        keep = body[:cut.start()] if cut else body
        # the lerped position of the original quad only fed its own geometry
        keep = re.sub(r'\n[ \t]*Vec3 \w+ = ' + m.group(1) + r'\.getPosition\(\);', '', keep)
        new = new[:m.start()] + '@Override\n    public void extract(QuadParticleRenderState iafRenderState, Camera ' + m.group(1) + ', float ' + m.group(2) + ') {' + keep.rstrip() + '\n        super.extract(iafRenderState, ' + m.group(1) + ', ' + m.group(2) + ');\n    }' + new[end:]
    if first != texture:
        # the texture depends on a field set in the constructor: the sprite is picked on the first extract
        new = new.replace('\n        this.iafSpriteLater = true;', '')
        new = new.replace('@Override\n    public void extract(QuadParticleRenderState iafRenderState, Camera ', '@Override\n    public void extract(QuadParticleRenderState iafRenderState, Camera ', 1)
        new = re.sub(r'(public void extract\(QuadParticleRenderState iafRenderState, Camera \w+, float \w+\) \{)', r'\1\n        this.setSprite(IafParticleSprites.get(' + texture + '));', new, count=1)
    new = re.sub(r'public int getLightColor\(float (\w+)\)', r'protected int getLightCoords(float \1)', new)
    new = re.sub(r'(?:@Override\s*)?public (?:@NotNull )?ParticleRenderType getRenderType\(\)\s*\{\s*return ParticleRenderType\.\w+;\s*\}',
                 '@Override\n    protected Layer getLayer() {\n        return Layer.TRANSLUCENT;\n    }', new)
    new = new.replace('import net.minecraft.client.particle.TextureSheetParticle;', 'import net.minecraft.client.particle.SingleQuadParticle;\nimport net.minecraft.client.renderer.state.level.QuadParticleRenderState;')
    path.write_text(new)
    print('changed', path.name, 'texture', texture)
