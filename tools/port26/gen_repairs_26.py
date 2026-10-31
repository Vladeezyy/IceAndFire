"""26.x tool and armor materials repair from an item tag: data/iceandfire/tags/item/repairs/<material>.json with the
items of the 1.21.1 port's IafItemRegistry.setRepairMaterials() (the original's repair ingredients).

    python3 tools/port26/gen_repairs_26.py common/src/generated/resources
"""
import json
import pathlib
import sys

I = 'iceandfire:'
SCALES = ['red', 'bronze', 'green', 'gray', 'blue', 'white', 'sapphire', 'silver', 'electric', 'amythest', 'copper', 'black']
SERPENTS = ['blue', 'bronze', 'deepblue', 'green', 'purple', 'red', 'teal']
REPAIRS = {
    'blindfold': ['#c:strings'],
    'silver': ['#c:ingots/silver'],
    'dragonbone': [I + 'dragonbone'], 'firedragonbone': [I + 'dragonbone'], 'icedragonbone': [I + 'dragonbone'],
    'lightningdragonbone': [I + 'dragonbone'],
    'dragonsteel_fire': [I + 'dragonsteel_fire_ingot'], 'dragonsteel_ice': [I + 'dragonsteel_ice_ingot'],
    'dragonsteel_lightning': [I + 'dragonsteel_lightning_ingot'], 'dread_queen': [],
    'sheep': ['minecraft:white_wool'], 'earplugs': ['minecraft:oak_button'],
    'yellow_deathworm': [I + 'deathworm_chitin_yellow'], 'white_deathworm': [I + 'deathworm_chitin_white'],
    'red_deathworm': [I + 'deathworm_chitin_red'],
    'trollweapon': ['#c:stones'],
    'mountain_troll': [I + 'troll_leather_mountain'], 'forest_troll': [I + 'troll_leather_forest'],
    'frost_troll': [I + 'troll_leather_frost'],
    'hippogryphsword': [I + 'hippogryph_talon'], 'hippocampussword': [I + 'shiny_scales'],
    'amphitheresword': [I + 'amphithere_feather'], 'stymphaliansword': [I + 'stymphalian_bird_feather'],
    'myrmexchitin': [I + 'myrmex_desert_chitin'], 'myrmexdesert': [I + 'myrmex_desert_chitin'],
    'myrmexjungle': [I + 'myrmex_jungle_chitin'],
    'dreadsword': [I + 'dread_shard'], 'dreadknightsword': [I + 'dread_shard'],
    # materials the original never gave a repair ingredient
    'copper': [], 'ghostsword': [],
}
for i, color in enumerate(SCALES):
    REPAIRS['armor_dragon_scales' + str(i + 1)] = [I + 'dragonscales_' + color]
for color in SERPENTS:
    REPAIRS['sea_serpent_scales_' + color] = [I + 'sea_serpent_scales_' + color]

out = pathlib.Path(sys.argv[1]) / 'data' / 'iceandfire' / 'tags' / 'item' / 'repairs'
out.mkdir(parents=True, exist_ok=True)
for name, values in REPAIRS.items():
    (out / (name + '.json')).write_text(json.dumps({'values': values}, indent=2) + '\n')
print(len(REPAIRS), 'repair tags')
