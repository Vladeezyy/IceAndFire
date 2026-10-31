"""Ice and Fire's 1.21.1 resources in the 26.x formats (run from the repo root; running it again changes nothing):

    python3 tools/port26/convert_iaf_26.py

- recipes: ingredients as strings ("item" -> id, "tag" -> "#tag"; a list with tags becomes an item tag of its own),
  the dragon forge's input and blood too;
- client item definitions (assets/iceandfire/items): the 1.21.1 overrides as range_dispatch / condition models
  (iceandfire:iceorfire, iceandfire:has_dragon, vanilla's using_item + use_duration), the builtin/entity items as the
  mod's special model (iceandfire:item_renderer) chosen per display context (the renderers draw per context), the
  ghost chest as vanilla's chest special model on the ghost chest textures;
- spawn eggs: textures made from the 1.21.1 egg template and the original's two colors (26.1 eggs have no colors);
- equipment assets of the armor materials (assets/iceandfire/equipment/<material>.json): 26.1 draws armor only with an
  asset; the textures are the items' own (NeoForge: the item client extensions, Fabric: the armor renderer);
- renamed vanilla ids in the data (26.1: minecraft:chain is minecraft:iron_chain);
- worldgen: the flower patches (Feature.FLOWER is gone in 26.1) as their placed feature's count and triangular offset.
"""
import io
import json
import pathlib
import re
import zipfile

from PIL import Image

GEN = pathlib.Path('common/src/generated/resources')
MAIN = pathlib.Path('common/src/main/resources')
ASSETS = MAIN / 'assets/iceandfire'
JAVA = pathlib.Path('common/src/main/java/com/github/alexthe666/iceandfire')


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(obj, indent=2) + '\n'
    if not path.exists() or path.read_text(encoding='utf-8') != text:
        path.write_text(text, encoding='utf-8')


# --- recipes (26.1: an ingredient is an id, a #tag or a list of ids) ---
def ingredient(value):
    if isinstance(value, list):
        values = [ingredient(v) for v in value]
        return values[0] if len(values) == 1 else values
    if isinstance(value, dict):
        if 'item' in value:
            return value['item']
        if 'tag' in value:
            return '#' + value['tag']
    return value


def merged(data_root, recipe_name, index, value):
    """A list of choices holds items only in 26.1; choices with tags become an item tag of their own."""
    if isinstance(value, list) and any(v.startswith('#') for v in value):
        tag = 'recipe/%s_%s' % (recipe_name.replace('/', '_'), index)
        dump(data_root / 'iceandfire/tags/item' / (tag + '.json'), {'values': [{'id': v, 'required': False} for v in value]})
        return '#iceandfire:' + tag
    return value


for data_root in (GEN / 'data', MAIN / 'data'):
    for recipe in sorted(data_root.glob('*/recipe/**/*.json')):
        obj = load(recipe)
        name = recipe.relative_to(data_root / recipe.relative_to(data_root).parts[0] / 'recipe').with_suffix('').as_posix()
        if 'key' in obj:
            obj['key'] = {k: merged(data_root, name, k, ingredient(v)) for k, v in obj['key'].items()}
        if 'ingredients' in obj:
            obj['ingredients'] = [merged(data_root, name, i, ingredient(v)) for i, v in enumerate(obj['ingredients'])]
        for key in ('ingredient', 'base', 'addition', 'template', 'input', 'blood'):
            if key in obj:
                obj[key] = merged(data_root, name, key, ingredient(obj[key]))
        if obj.get('type') == 'minecraft:stonecutting' and 'count' in obj:
            count = obj.pop('count')
            if count != 1:
                obj['result']['count'] = count
        dump(recipe, obj)

# --- spawn eggs: the original's colors on the 1.21.1 egg template ---
JARS = pathlib.Path.home() / '.gradle/caches/neoformruntime/artifacts'
VANILLA_1211 = zipfile.ZipFile(JARS / 'minecraft_1.21.1_client.jar')


def vanilla_image(jar, path):
    return Image.open(io.BytesIO(jar.read('assets/minecraft/' + path))).convert('RGBA')


def tinted(image, rgb):
    out = Image.new('RGBA', image.size)
    r0, g0, b0 = rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255
    for x in range(image.width):
        for y in range(image.height):
            r, g, b, a = image.getpixel((x, y))
            out.putpixel((x, y), (r * r0 // 255, g * g0 // 255, b * b0 // 255, a))
    return out


eggs = {}
for source in sorted(JAVA.rglob('*.java')):
    text = source.read_text(encoding='utf-8')
    for name, base, spots in re.findall(r'registerItem\("([a-z_0-9]+)",[^;]*?spawnEgg\([^,]+, (0[xX][0-9A-Fa-f]+), (0[xX][0-9A-Fa-f]+)', text):
        eggs[name] = (int(base, 16), int(spots, 16))
egg_base = vanilla_image(VANILLA_1211, 'textures/item/spawn_egg.png')
egg_overlay = vanilla_image(VANILLA_1211, 'textures/item/spawn_egg_overlay.png')
for egg, (base, spots) in sorted(eggs.items()):
    texture = ASSETS / 'textures/item' / (egg + '.png')
    if not texture.exists():
        Image.alpha_composite(tinted(egg_base, base), tinted(egg_overlay, spots)).save(texture)
    old = ASSETS / 'models/item' / (egg + '.json')
    if not old.exists() or 'spawn_egg' in load(old).get('parent', ''):
        dump(old, {'parent': 'minecraft:item/generated', 'textures': {'layer0': 'iceandfire:item/' + egg}})

# --- client item definitions (26.1: assets/<ns>/items/<id>.json) ---
def model(ref):
    return {'type': 'minecraft:model', 'model': ref}


CONTEXTS = ['thirdperson_lefthand', 'thirdperson_righthand', 'firstperson_lefthand', 'firstperson_righthand', 'head',
            'gui', 'ground', 'fixed', 'on_shelf']


def special(base, renderer):
    """The mod's item renderer, told the display context (1.21.1 renderers drew per context)"""
    def one(display):
        return {'type': 'minecraft:special', 'base': base,
                'model': {'type': 'iceandfire:item_renderer', 'renderer': renderer, 'display': display}}
    return {'type': 'minecraft:select', 'property': 'minecraft:display_context',
            'cases': [{'when': c, 'model': one(c)} for c in CONTEXTS], 'fallback': one('none')}


item_models = {f.stem: f for f in sorted((ASSETS / 'models/item').glob('*.json'))}
# the 1.21.1 overrides and builtin/entity parents, kept beside this script: the models lose them on the first run
STATE_FILE = pathlib.Path(__file__).with_name('iaf_item_models_1.21.1.json')
state = load(STATE_FILE) if STATE_FILE.exists() else {'overrides': {}, 'builtin': []}
for name, file in item_models.items():
    obj = load(file)
    if 'overrides' in obj:
        state['overrides'][name] = obj['overrides']
    if obj.get('parent') in ('builtin/entity', 'minecraft:builtin/entity') and name not in state['builtin']:
        state['builtin'].append(name)
state['builtin'].sort()
dump(STATE_FILE, {'overrides': dict(sorted(state['overrides'].items())), 'builtin': state['builtin']})
overrides_by_item = state['overrides']
builtin = set(state['builtin'])


def builtin_root(name, seen=()):
    """The builtin/entity model an item model inherits from (troll weapons, pixie houses), or None"""
    if name in builtin:
        return name
    file = item_models.get(name)
    parent = load(file).get('parent', '') if file else ''
    if parent.startswith('iceandfire:item/') and name not in seen:
        return builtin_root(parent.split('/')[-1], seen + (name,))
    return None


RENDERERS = {'deathworm_gauntlet_yellow': 'deathworm_gauntlet', 'deathworm_gauntlet_white': 'deathworm_gauntlet',
             'deathworm_gauntlet_red': 'deathworm_gauntlet', 'gorgon_head': 'gorgon_head', 'tide_trident': 'tide_trident',
             'tide_trident_throwing': 'tide_trident', 'troll_weapon': 'troll_weapon', 'dread_portal': 'block_items',
             'pixie_house': 'block_items'}
# the particle of the builtin/entity models (1.21.1 had none; 26.1 wants one)
PARTICLES = {'deathworm_gauntlet_yellow': 'iceandfire:item/deathworm_chitin_yellow',
             'deathworm_gauntlet_white': 'iceandfire:item/deathworm_chitin_white',
             'deathworm_gauntlet_red': 'iceandfire:item/deathworm_chitin_red', 'gorgon_head': 'iceandfire:item/gorgon_head',
             'tide_trident': 'iceandfire:item/tide_trident', 'tide_trident_throwing': 'iceandfire:item/tide_trident',
             'troll_weapon': 'minecraft:block/stone', 'dread_portal': 'iceandfire:block/dread_stone',
             'pixie_house': 'minecraft:block/oak_planks', 'ghost_chest': 'minecraft:block/oak_planks'}
submodels = {o['model'].split('/')[-1] for overrides in overrides_by_item.values() for o in overrides}
for name in builtin:
    file = item_models[name]
    obj = load(file)
    if obj.get('parent') in ('builtin/entity', 'minecraft:builtin/entity'):
        obj.pop('parent')  # 26.1 has no builtin/entity: the model only gives the transforms and the particle
    if name in PARTICLES:
        obj.setdefault('textures', {})['particle'] = PARTICLES[name]
    dump(file, obj)

for name, file in sorted(item_models.items()):
    obj = load(file)
    if name in submodels or name in builtin and name not in RENDERERS and name != 'ghost_chest':
        continue
    if obj.pop('overrides', None) is not None:
        dump(file, obj)
    overrides = overrides_by_item.get(name, [])
    predicates = {key.split(':')[-1] for o in overrides for key in o['predicate']}
    base = model('iceandfire:item/' + name)
    root = builtin_root(name)
    if name == 'ghost_chest':
        definition = {'type': 'minecraft:special', 'base': 'iceandfire:item/ghost_chest',
                      'model': {'type': 'minecraft:chest', 'texture': 'iceandfire:ghost_chest'}}
    elif name == 'tide_trident':
        definition = {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
                      'on_false': special('iceandfire:item/tide_trident', 'tide_trident'),
                      'on_true': special('iceandfire:item/tide_trident_throwing', 'tide_trident')}
    elif root is not None:
        definition = special('iceandfire:item/' + name, RENDERERS[root])
    elif 'pulling' in predicates:
        pulls = sorted(overrides, key=lambda o: next((v for k, v in o['predicate'].items() if k.endswith('pull')), 0.0))
        definition = {'type': 'minecraft:condition', 'property': 'minecraft:using_item', 'on_false': base,
                      'on_true': {'type': 'minecraft:range_dispatch', 'property': 'minecraft:use_duration', 'scale': 0.05,
                                  'entries': [{'model': model(o['model']), 'threshold': next(v for k, v in o['predicate'].items() if k.endswith('pull'))} for o in pulls[1:]],
                                  'fallback': model(pulls[0]['model'])}}
    elif 'iceorfire' in predicates:
        definition = {'type': 'minecraft:range_dispatch', 'property': 'iceandfire:iceorfire', 'fallback': base,
                      'entries': [{'model': model(o['model']), 'threshold': next(iter(o['predicate'].values()))} for o in overrides]}
    elif 'has_dragon' in predicates:
        definition = {'type': 'minecraft:condition', 'property': 'iceandfire:has_dragon', 'on_true': model(overrides[0]['model']), 'on_false': base}
    elif overrides:
        raise SystemExit('overrides not converted: %s %s' % (name, predicates))
    else:
        definition = base
    dump(ASSETS / 'items' / (name + '.json'), {'model': definition})

# block models that only give a particle had the builtin/entity parent in 1.21.1 (gone in 26.1)
for file in sorted((ASSETS / 'models/block').glob('*.json')):
    obj = load(file)
    if obj.get('parent') in ('builtin/entity', 'minecraft:builtin/entity'):
        obj.pop('parent')
        dump(file, obj)

# --- equipment assets of the armor materials (IafArmorMaterial ids) ---
materials = set()
for source in sorted(JAVA.rglob('*.java')):
    for name in re.findall(r'new (?:IafArmorMaterial|DragonsteelArmorMaterial)\("([^"]+)"', source.read_text(encoding='utf-8')):
        materials.add(name)
serpents = re.findall(r'^    ([A-Z]+)\(ChatFormatting', (JAVA / 'enums/EnumSeaSerpent.java').read_text(encoding='utf-8'), re.M)
dragon_armor = re.findall(r'^    (armor_[a-z_]+)\(', (JAVA / 'enums/EnumDragonArmor.java').read_text(encoding='utf-8'), re.M)
ids = set()
for name in materials:
    if name.endswith('"'):
        continue
    if 'sea_serpent_scales_' in name:
        ids.update('sea_serpent_scales_' + s.lower() for s in serpents)
    elif 'armor_dragon_scales' in name:
        ids.update('armor_dragon_scales%d' % (i + 1) for i in range(len(dragon_armor)))
    else:
        ids.add(name.split(':')[-1].lower().replace(' ', '_'))
for material in sorted(ids):
    dump(ASSETS / 'equipment' / (material + '.json'),
         {'layers': {'humanoid': [{'texture': 'iceandfire:' + material}], 'humanoid_leggings': [{'texture': 'iceandfire:' + material}]}})
# --- renamed vanilla ids (recipes, loot tables, advancements) ---
RENAMED = {'"minecraft:chain"': '"minecraft:iron_chain"'}
for data_root in (GEN / 'data', MAIN / 'data'):
    for file in sorted(data_root.rglob('*.json')):
        text = file.read_text(encoding='utf-8')
        new = text
        for old, renamed in RENAMED.items():
            new = new.replace(old, renamed)
        if new != text:
            file.write_text(new, encoding='utf-8')

# --- worldgen: Feature.FLOWER (RandomPatchConfiguration) is gone; the tries are a count with a triangular offset ---
for data_root in (GEN / 'data', MAIN / 'data'):
    for configured in sorted(data_root.glob('*/worldgen/configured_feature/*.json')):
        feature = load(configured)
        if feature.get('type') != 'minecraft:flower':
            continue
        patch = feature['config']
        ref = configured.parts[configured.parts.index('data') + 1] + ':' + configured.stem
        for placed_file in sorted(data_root.glob('*/worldgen/placed_feature/*.json')):
            placed = load(placed_file)
            if placed.get('feature') != ref:
                continue
            placed['placement'] += [
                {'type': 'minecraft:count', 'count': patch['tries']},
                {'type': 'minecraft:random_offset',
                 'xz_spread': {'type': 'minecraft:trapezoid', 'max': patch['xz_spread'], 'min': -patch['xz_spread'], 'plateau': 0},
                 'y_spread': {'type': 'minecraft:trapezoid', 'max': patch['y_spread'], 'min': -patch['y_spread'], 'plateau': 0}},
            ] + patch['feature']['placement']
            dump(placed_file, placed)
        dump(configured, patch['feature']['feature'])

print(len(eggs), 'eggs,', len(list((ASSETS / 'items').glob('*.json'))), 'item definitions,', len(ids), 'equipment assets')
