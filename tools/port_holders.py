"""RegistryObject<X> (already renamed to DeferredHolder<X>) -> DeferredItem / DeferredBlock / DeferredHolder<R, X>.

One-off, already applied. Usage: python -I tools/port_holders.py D:/IceFire
"""
import os
import re
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else '.'
SRC = os.path.join(ROOT, 'src', 'main', 'java')

GENERIC_BASES = {'EntityType', 'BlockEntityType', 'MenuType', 'RecipeSerializer', 'RecipeType', 'Feature', 'StructureType',
                 'StructureProcessorType', 'PlacementModifierType', 'ParticleType', 'LootItemFunctionType'}
PLAIN_BASES = {'CreativeModeTab', 'VillagerProfession', 'PoiType', 'MobEffect', 'Potion', 'SoundEvent', 'BannerPattern',
               'SimpleParticleType'}
ITEMS = {'Item', 'ItemDragonArmor', 'BannerPatternItem'}
BLOCKS = {'Block', 'BlockDreadBase', 'TorchBlock'}


def split_top(s):
    depth = 0
    for i, c in enumerate(s):
        if c == '<':
            depth += 1
        elif c == '>':
            depth -= 1
        elif c == ',' and depth == 0:
            return True
    return False


def convert(arg):
    head = arg.split('<', 1)[0].strip()
    if head in ITEMS:
        return f'DeferredItem<{arg}>'
    if head in BLOCKS:
        return f'DeferredBlock<{arg}>'
    if head in GENERIC_BASES:
        return f'DeferredHolder<{head}<?>, {arg}>'
    if head in PLAIN_BASES:
        return f'DeferredHolder<{head}, {arg}>'
    return None


def port(text):
    out, i = [], 0
    token = 'DeferredHolder<'
    while True:
        j = text.find(token, i)
        if j < 0:
            out.append(text[i:])
            break
        out.append(text[i:j])
        k, depth = j + len(token), 1
        while depth:
            if text[k] == '<':
                depth += 1
            elif text[k] == '>':
                depth -= 1
            k += 1
        arg = text[j + len(token):k - 1]
        new = None if split_top(arg) else convert(arg)
        out.append(new if new else text[j:k])
        i = k
    text = ''.join(out)
    nl = '\r\n' if '\r\n' in text else '\n'
    for cls in ('DeferredItem', 'DeferredBlock'):
        if re.search(r'\b' + cls + r'<', text) and f'import net.neoforged.neoforge.registries.{cls};' not in text:
            text = re.sub(r'(package [^;]+;\r?\n\r?\n)', lambda m: m.group(1) + f'import net.neoforged.neoforge.registries.{cls};{nl}', text, count=1)
    if 'DeferredHolder<' not in text:
        text = re.sub(r'import net\.neoforged\.neoforge\.registries\.DeferredHolder;\r?\n', '', text)
    return text


for d, _, fs in os.walk(SRC):
    for f in fs:
        if f.endswith('.java'):
            p = os.path.join(d, f)
            with open(p, encoding='utf-8', newline='') as fh:
                old = fh.read()
            new = port(old)
            if new != old:
                with open(p, 'w', encoding='utf-8', newline='') as fh:
                    fh.write(new)
