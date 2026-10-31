"""Ice and Fire's 26.1 resources in the 26.2 formats (run from the repo root after convert_iaf_26.py; running it again
changes nothing):

    python3 tools/port26/convert_iaf_262.py

- entity predicates are maps of registered sub-predicates (advancements, loot tables): "type" -> "minecraft:entity_type",
  "flags" -> "minecraft:flags", type_specific {type: x} -> "minecraft:type_specific/x" (as the Rats port's conversion).
"""
import json
import pathlib

GEN = pathlib.Path('common/src/generated/resources')
MAIN = pathlib.Path('common/src/main/resources')


def load(path):
    return json.loads(path.read_text(encoding='utf-8'))


def dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    text = json.dumps(obj, indent=2) + '\n'
    if not path.exists() or path.read_text(encoding='utf-8') != text:
        path.write_text(text, encoding='utf-8')


# --- 26.2: entity predicates are maps of registered sub-predicates ("type" -> "minecraft:entity_type",
# "flags" -> "minecraft:flags", type_specific {type: x} -> "minecraft:type_specific/x"); nested ones too ---
ENTITY_FIELDS = {'type', 'location', 'stepping_on', 'movement_affected_by', 'distance', 'movement', 'effects', 'nbt',
                 'flags', 'equipment', 'periodic_tick', 'vehicle', 'passenger', 'targeted_entity', 'team', 'slots',
                 'components', 'predicates', 'type_specific'}
NESTED_ENTITY = {'vehicle', 'passenger', 'targeted_entity'}
ENTITY_HOLDERS = {'player', 'entity', 'victim', 'killer', 'source_entity', 'direct_entity', 'parent', 'partner', 'child',
                  'zombie', 'villager', 'projectile', 'shooter', 'lightning', 'bystander'}


def entity_predicate(pred):
    if not isinstance(pred, dict) or not pred or not set(pred) <= ENTITY_FIELDS:
        return pred  # already 26.2 (namespaced keys) or not an entity predicate
    out = {}
    for key, value in pred.items():
        if key == 'type':
            out['minecraft:entity_type'] = value
        elif key == 'type_specific':
            rest = {k: v for k, v in value.items() if k != 'type'}
            if 'looking_at' in rest:
                rest['looking_at'] = entity_predicate(rest['looking_at'])
            out['minecraft:type_specific/' + value['type'].split(':')[-1]] = rest
        elif key in NESTED_ENTITY:
            out['minecraft:' + key] = entity_predicate(value)
        else:
            out['minecraft:' + key] = value
    return dict(sorted(out.items()))


def entity_predicates(node):
    if isinstance(node, dict):
        if node.get('condition') == 'minecraft:entity_properties' and 'predicate' in node:
            node['predicate'] = entity_predicate(node['predicate'])
        for key in list(node):
            if key in ENTITY_HOLDERS and isinstance(node[key], dict):
                node[key] = entity_predicate(node[key])
            entity_predicates(node[key])
    elif isinstance(node, list):
        for item in node:
            entity_predicates(item)
    return node


for data_root in (GEN / 'data', MAIN / 'data'):
    for file in sorted(data_root.rglob('*.json')):
        if not any(part in ('advancement', 'loot_table', 'loot_modifiers', 'predicate') for part in file.parts):
            continue
        text = file.read_text(encoding='utf-8')
        if '"type"' not in text and '"flags"' not in text and '"nbt"' not in text and '"type_specific"' not in text:
            continue
        obj = load(file)
        before = json.dumps(obj, sort_keys=True)
        entity_predicates(obj)
        if json.dumps(obj, sort_keys=True) != before:
            dump(file, obj)

