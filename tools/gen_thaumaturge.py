#!/usr/bin/env python3
"""Thaumaturge compat data (NeoForge only): python3 -I tools/gen_thaumaturge.py

Writes the built-in data pack neoforge/src/main/resources/data/iceandfire/datapacks/thaumaturge, which
ThaumaturgeCompat registers only when Thaumaturge is loaded (without it NeoForge would warn about data map
files of a data map type that does not exist):
  data/iceandfire/thaumaturge/aspect/draco.json, fulgur.json   the Draco (dragon) and Fulgur (lightning) aspects
  data/thaumaturge/data_maps/item/base_aspects.json            aspects of the items that have no plain recipe
  data/thaumaturge/data_maps/entity_type/entity_aspects.json   what the thaumometer reads from the mobs
Items crafted, smelted or stonecut from these get their aspects from the recipe (Thaumaturge derives them).
Every id is checked against the item and entity ids the self-test wrote (tools/thaumaturge_ids.txt, refresh it with
SELFTEST_ONLY=thaumaturge tools/selftest.sh -Pwith_thaumaturge), so a renamed one fails here and not in game.
"""
import json
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
PACK = ROOT / "neoforge/src/main/resources/data/iceandfire/datapacks/thaumaturge"
IDS = ROOT / "tools/thaumaturge_ids.txt"
# data pack format of the branch's Minecraft version (26.1.2: 101; since 1.21.9 min_format and max_format instead of pack_format)
PACK_FORMAT = {"min_format": 101, "max_format": 101}

OWN_ASPECTS = {
    # Dragon: a beast of structured magic
    "draco": {"color": 0xB0283C, "components": ["thaumaturge:bestia", "thaumaturge:praecantatio"]},
    # Lightning: energy in motion (Potentia + Aer is already Praecantatio)
    "fulgur": {"color": 0x8E7CFF, "components": ["thaumaturge:potentia", "thaumaturge:motus"]},
}

COLORS = ["red", "green", "bronze", "gray", "blue", "white", "sapphire", "silver", "electric", "amythest", "copper", "black"]
FIRE, ICE, LIGHTNING = COLORS[0:4], COLORS[4:8], COLORS[8:12]
ELEMENT = {**{c: "ignis" for c in FIRE}, **{c: "gelum" for c in ICE}, **{c: "fulgur" for c in LIGHTNING}}
DRAGONS = (("fire", "ignis"), ("ice", "gelum"), ("lightning", "fulgur"))
SERPENTS = ["blue", "bronze", "deepblue", "green", "purple", "red", "teal"]

# item -> aspects (short names: thaumaturge:<name>, draco and fulgur: iceandfire:<name>). Only what has no plain
# recipe: drops, ores, world blocks, dragon forge ingots. Tools, armor, blocks of nine and the rest come from recipes.
ITEMS = {
    # metals and gems (the silver ingot also ends a recipe loop that gave it Instrumentum 55 and silver blocks 500)
    "sapphire_gem": "vitreus 15, desiderium 10",
    "sapphire_ore": "terra 5, vitreus 15, desiderium 10",
    "raw_silver": "metallum 10, desiderium 5, terra 2",
    "silver_ore": "terra 5, metallum 10, desiderium 5",
    "deepslate_silver_ore": "terra 5, metallum 10, desiderium 5, tenebrae 1",
    "silver_ingot": "metallum 10, desiderium 5",
    "silver_nugget": "metallum 1",
    "silver_pile": "metallum 1, desiderium 1",
    "manuscript": "cognitio 5, praecantatio 2",
    # dragons: Draco, and the element of the dragon (Ignis, Gelum, Fulgur)
    **{f"dragonscales_{c}": f"draco 5, praemunio 5, {ELEMENT[c]} 3" for c in COLORS},
    **{f"dragonegg_{c}": f"draco 15, victus 10, {ELEMENT[c]} 10, bestia 5" for c in COLORS},
    "dragonbone": "draco 5, mortuus 5, victus 3",
    **{f"dragon_skull_{e}": f"draco 15, mortuus 10, spiritus 5, {el} 10" for e, el in DRAGONS},
    **{f"{e}_dragon_flesh": f"draco 3, victus 5, {el} 5" for e, el in DRAGONS},
    **{f"{e}_dragon_heart": f"draco 10, victus 15, {el} 15" for e, el in DRAGONS},
    **{f"{e}_dragon_blood": f"draco 5, victus 5, alkimia 5, {el} 10" for e, el in DRAGONS},
    **{f"dragonsteel_{e}_ingot": f"metallum 15, draco 10, {el} 15, potentia 5" for e, el in DRAGONS},
    "creative_dragon_meal": "draco 50, victus 50, praecantatio 20",
    "dragon_debug_stick": "ordo 5, praecantatio 5",
    # the lit forge core (only the unlit one has a recipe)
    **{f"dragonforge_{e}_core": f"terra 15, ordo 15, draco 5, {el} 15, potentia 5" for e, el in DRAGONS},
    "egginice": "gelum 20, draco 10, victus 5",
    "dragon_ice": "gelum 20, draco 3",
    "dragon_ice_spikes": "gelum 10, aversio 5, draco 2",
    # what the breath leaves of the ground: burnt (Ignis), frozen (Gelum), struck by lightning (Fulgur)
    **{f"{prefix}_{block}": f"{spec}, {el} {n}"
       for prefix, el in (("chared", "ignis"), ("frozen", "gelum"), ("crackled", "fulgur"))
       for block, spec, n in (("dirt", "terra 5, perditio 1", 2), ("grass", "terra 5, perditio 2", 2), ("stone", "terra 5", 3),
                              ("cobblestone", "terra 5, perditio 1", 2), ("gravel", "terra 5, perditio 2", 2),
                              ("dirt_path", "terra 5, motus 1", 2))},
    "ash": "perditio 5, ignis 3, terra 1",
    "frozen_splinters": "gelum 3, herba 3",
    "fire_lily": "herba 5, ignis 5",
    "frost_lily": "herba 5, gelum 5",
    "lightning_lily": "herba 5, fulgur 5",
    # pixies
    "pixie_dust": "praecantatio 5, lux 5, volatus 2",
    "pixie_wings": "volatus 10, praecantatio 5, sensus 3",
    **{f"pixie_house_{w}": "herba 15, praecantatio 3" for w in ("birch", "dark_oak", "oak", "spruce")},
    **{f"pixie_house_mushroom_{c}": "herba 10, praecantatio 3, tenebrae 1" for c in ("brown", "red")},
    **{f"pixie_jar_{n}": "vitreus 10, praecantatio 10, bestia 5, vinculum 5" for n in range(5)},
    # gorgon, cyclops, siren, hippocampus, hippogryph
    "gorgon_head": "mortuus 15, terra 15, sensus 10, vinculum 10",
    "stone_statue": "terra 10, mortuus 5, vinculum 5",
    "cyclops_eye": "sensus 15, humanus 5, praecantatio 5",
    "cyclops_skull": "mortuus 15, humanus 5, sensus 5",
    "siren_tear": "aqua 10, sensus 10, spiritus 5",
    "hippocampus_fin": "aqua 10, bestia 5, motus 5",
    "shiny_scales": "aqua 5, desiderium 5, bestia 2",
    "fishing_spear": "aqua 5, aversio 5, instrumentum 5",
    "hippogryph_talon": "bestia 5, aversio 5, volatus 3",
    "hippogryph_skull": "mortuus 10, bestia 5, volatus 5",
    # death worms, cockatrices, stymphalian birds, amphitheres
    **{f"deathworm_chitin_{c}": "bestia 5, praemunio 5, terra 3" for c in ("yellow", "white", "red")},
    "deathworm_tounge": "bestia 5, vinculum 5, motus 3",
    "cockatrice_eye": "sensus 10, mortuus 5, tenebrae 5",
    "cockatrice_skull": "mortuus 10, sensus 5, bestia 5",
    "rotten_egg": "perditio 5, victus 2, mortuus 2",
    "stymphalian_bird_feather": "volatus 5, aer 3, metallum 3, aversio 2",
    "stymphalian_skull": "mortuus 10, volatus 5, metallum 5",
    "amphithere_feather": "volatus 5, aer 3, bestia 2",
    "amphithere_skull": "mortuus 10, volatus 5, aer 5",
    # trolls (their weapons are dropped, not crafted)
    "troll_tusk": "bestia 5, aversio 5, mortuus 2",
    "troll_leather_forest": "bestia 5, praemunio 8, herba 2",
    "troll_leather_frost": "bestia 5, praemunio 8, gelum 2",
    "troll_leather_mountain": "bestia 5, praemunio 8, terra 2",
    "troll_skull": "mortuus 15, bestia 10, terra 5",
    "troll_weapon_axe": "aversio 15, metallum 10, instrumentum 5",
    "troll_weapon_column": "aversio 15, terra 15",
    "troll_weapon_column_forest": "aversio 15, terra 10, herba 5",
    "troll_weapon_column_frost": "aversio 15, terra 10, gelum 5",
    "troll_weapon_hammer": "aversio 15, metallum 10, terra 5",
    "troll_weapon_trunk": "aversio 15, herba 15",
    "troll_weapon_trunk_frost": "aversio 15, herba 10, gelum 5",
    # myrmex
    **{f"myrmex_{b}_chitin": f"bestia 5, praemunio 5, {t} 2" for b, t in (("desert", "terra"), ("jungle", "herba"))},
    **{f"myrmex_{b}_resin": "vinculum 5, bestia 2, alkimia 2" for b in ("desert", "jungle")},
    "myrmex_stinger": "bestia 5, aversio 5, alkimia 3",
    # the resin glass is smelted from the block
    **{f"myrmex_{b}_resin_block": "vinculum 15, bestia 5, alkimia 5" for b in ("desert", "jungle")},
    **{f"myrmex_resin_{b}": "vinculum 5, terra 3" for b in ("desert", "jungle")},
    **{f"myrmex_resin_sticky_{b}": "vinculum 10, terra 3" for b in ("desert", "jungle")},
    **{f"{b}_myrmex_cocoon": "vacuos 10, bestia 5, vinculum 5" for b in ("desert", "jungle")},
    **{f"myrmex_{b}_biolight": "lux 10, herba 5, bestia 2" for b in ("desert", "jungle")},
    # sea serpents
    **{f"sea_serpent_scales_{c}": "aqua 5, praemunio 5, bestia 3" for c in SERPENTS},
    "sea_serpent_fang": "aqua 5, aversio 5, bestia 3",
    "seaserpent_skull": "mortuus 15, aqua 10, bestia 5",
    "tide_trident_inventory": "aqua 10, aversio 10, praecantatio 5",
    # hydra
    "hydra_fang": "aversio 5, alkimia 5, mortuus 3",
    "hydra_heart": "victus 25, praecantatio 10, potentia 10",
    "hydra_skull": "mortuus 15, victus 10, aqua 5",
    # the Dread (frozen undead) and ghosts
    "dread_shard": "tenebrae 5, gelum 3, exanimis 2",
    "dread_key": "tenebrae 10, vinculum 10, mortuus 5",
    "lich_staff": "exanimis 10, praecantatio 10, tenebrae 5",
    "dread_queen_staff": "exanimis 15, praecantatio 15, gelum 10",
    "dread_stone": "terra 5, tenebrae 2",
    "dread_portal": "alienis 15, tenebrae 10, vacuos 5",
    "dread_spawner": "exanimis 15, praecantatio 10, tenebrae 5",
    "dreadwood_log": "herba 15, tenebrae 5",
    "dreadwood_planks_lock": "vinculum 5, herba 5",
    "burnt_torch": "perditio 2, herba 1",
    "ectoplasm": "spiritus 10, exanimis 5",
    "ghost_ingot": "metallum 10, spiritus 10, exanimis 5",
    # wither bones (the #c:bones tag gave them plain bone aspects)
    "witherbone": "mortuus 5, exanimis 5, tenebrae 3",
    "weezer_blue_album": "sensus 15, aer 5, desiderium 10",
    "cannoli": "victus 5, desiderium 5",
}

ENTITIES = {
    "fire_dragon": "draco 50, ignis 40, bestia 20, volatus 15",
    "ice_dragon": "draco 50, gelum 40, bestia 20, volatus 15",
    "lightning_dragon": "draco 50, fulgur 40, bestia 20, volatus 15",
    "hippogryph": "bestia 15, volatus 15, aer 5",
    "gorgon": "humanus 10, mortuus 10, terra 10, sensus 5",
    "pixie": "praecantatio 10, volatus 5, lux 5, desiderium 3",
    "cyclops": "humanus 20, bestia 10, aversio 10, sensus 5",
    "siren": "humanus 10, aqua 15, sensus 10, praecantatio 5",
    "hippocampus": "bestia 10, aqua 15, motus 5",
    "deathworm": "bestia 15, terra 20, aversio 5",
    "cockatrice": "bestia 10, sensus 10, mortuus 5, volatus 2",
    "stymphalian_bird": "bestia 10, volatus 10, metallum 10, aversio 5",
    "troll": "bestia 15, terra 15, aversio 10",
    "myrmex_worker": "bestia 10, terra 5, fabrico 5",
    "myrmex_soldier": "bestia 10, aversio 10, praemunio 5",
    "myrmex_sentinel": "bestia 15, aversio 10, praemunio 5",
    "myrmex_royal": "bestia 10, volatus 10, ordo 5",
    "myrmex_queen": "bestia 25, victus 15, ordo 10",
    "myrmex_swarmer": "bestia 5, volatus 5, aversio 3",
    "amphithere": "bestia 15, volatus 15, aer 10, herba 5",
    "sea_serpent": "bestia 30, aqua 30, aversio 10",
    "dread_thrall": "exanimis 15, humanus 5, tenebrae 5",
    "dread_ghoul": "exanimis 20, bestia 5, tenebrae 5",
    "dread_beast": "exanimis 15, bestia 10, tenebrae 5",
    "dread_scuttler": "exanimis 20, bestia 10, terra 5",
    "dread_lich": "exanimis 25, praecantatio 20, tenebrae 10, gelum 5",
    "dread_knight": "exanimis 25, humanus 10, aversio 10, praemunio 10",
    "dread_horse": "exanimis 15, bestia 10, motus 5",
    "hydra": "bestia 30, victus 30, aqua 10, aversio 10",
    "ghost": "spiritus 20, exanimis 10, tenebrae 5",
}

OWN = set(OWN_ASPECTS)
THAUMATURGE_ASPECTS = {
    "aer", "alienis", "alkimia", "aqua", "auram", "aversio", "bestia", "cognitio", "desiderium", "exanimis", "fabrico",
    "gelum", "herba", "humanus", "ignis", "instrumentum", "lux", "machina", "metallum", "mortuus", "motus", "ordo",
    "perditio", "permutatio", "potentia", "praecantatio", "praemunio", "sensus", "spiritus", "tenebrae", "terra",
    "vacuos", "victus", "vinculum", "vitium", "vitreus", "volatus",
}


def aspects(spec, owner):
    out = []
    for part in spec.split(","):
        name, amount = part.split()
        if name in OWN:
            aspect = "iceandfire:" + name
        elif name in THAUMATURGE_ASPECTS:
            aspect = "thaumaturge:" + name
        else:
            sys.exit(f"{owner}: unknown aspect {name}")
        if any(a["aspect"] == aspect for a in out):
            sys.exit(f"{owner}: {name} twice")
        out.append({"aspect": aspect, "amount": int(amount)})
    return out


def write(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")


def main():
    items, entities = set(), set()
    if IDS.exists():
        for line in IDS.read_text(encoding="utf-8").splitlines():
            kind, _, name = line.partition(" ")
            (items if kind == "item" else entities).add(name)
        for item in ITEMS:
            if item not in items:
                sys.exit(f"no item iceandfire:{item}")
        for entity in ENTITIES:
            if entity not in entities:
                sys.exit(f"no entity type iceandfire:{entity}")
    write(PACK / "pack.mcmeta", {"pack": {"description": {"translate": "pack.iceandfire.thaumaturge"}, **PACK_FORMAT}})
    for tag, aspect in OWN_ASPECTS.items():
        write(PACK / f"data/iceandfire/thaumaturge/aspect/{tag}.json",
              {"tag": tag, **aspect, "texture": f"iceandfire:textures/aspects/{tag}.png"})
    if ITEMS:
        write(PACK / "data/thaumaturge/data_maps/item/base_aspects.json",
              {"values": {f"iceandfire:{k}": aspects(v, k) for k, v in ITEMS.items()}})
    if ENTITIES:
        write(PACK / "data/thaumaturge/data_maps/entity_type/entity_aspects.json",
              {"values": {f"iceandfire:{k}": aspects(v, k) for k, v in ENTITIES.items()}})
    print(f"{len(ITEMS)} items, {len(ENTITIES)} mobs -> {PACK.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
