"""26.x villager trades are data (villager_trade, trade_set, villager_trade tags). The 1.21.1 port's scribe trades
(IafVillagerRegistry.addScribeTrades), same items, counts, uses, xp and price multiplier; two per level, as 1.21.1's
villagers took from the event's lists:

    python3 tools/port26/gen_trades_26.py common/src/generated/resources
"""
import json
import pathlib
import sys

root = pathlib.Path(sys.argv[1]) / 'data' / 'iceandfire'
I, E = 'iceandfire:', 'minecraft:emerald'
BUY, SELL, RARE = 0.05, 0.05, 0.2


def offer(wants, wants_count, gives, gives_count, uses, xp, multiplier):
    # new MerchantOffer(new ItemCost(wants, wants_count), new ItemStack(gives, gives_count), uses, xp, multiplier)
    return {'wants': {'id': wants, 'count': wants_count}, 'gives': {'id': gives, 'count': gives_count},
            'max_uses': uses, 'xp': xp, 'reputation_discount': multiplier}


SCRIBE = {
    1: [('manuscript', offer(E, 1, I + 'manuscript', 4, 25, 2, BUY)),
        ('bookshelf', offer('minecraft:bookshelf', 3, E, 1, 8, 3, SELL)),
        ('paper', offer('minecraft:paper', 15, E, 2, 4, 4, SELL)),
        ('ash', offer(I + 'ash', 10, E, 1, 8, 4, SELL))],
    2: [('silver_ingot', offer(I + 'silver_ingot', 5, E, 1, 3, 5, SELL)),
        ('fire_lily', offer(I + 'fire_lily', 8, E, 1, 3, 5, SELL)),
        ('lightning_lily', offer(I + 'lightning_lily', 7, E, 3, 2, 5, SELL)),
        ('frost_lily', offer(E, 3, I + 'frost_lily', 4, 3, 3, BUY)),
        ('dragon_ice_spikes', offer(E, 2, I + 'dragon_ice_spikes', 7, 2, 3, BUY)),
        ('sapphire_gem', offer(I + 'sapphire_gem', 1, E, 2, 30, 3, RARE)),
        ('pixie_jar_empty', offer(E, 2, I + 'pixie_jar_empty', 1, 3, 4, BUY)),
        ('myrmex_desert_resin', offer(E, 2, I + 'myrmex_desert_resin', 1, 40, 2, BUY)),
        ('myrmex_jungle_resin', offer(E, 2, I + 'myrmex_jungle_resin', 1, 40, 2, BUY)),
        ('amethyst_shard', offer('minecraft:amethyst_shard', 1, E, 3, 20, 3, RARE))],
    3: [('dragonbone', offer(I + 'dragonbone', 6, E, 1, 7, 4, SELL)),
        ('chain', offer(I + 'chain', 2, E, 3, 4, 2, SELL)),
        ('pixie_dust', offer(E, 6, I + 'pixie_dust', 2, 8, 3, BUY)),
        ('fire_dragon_flesh', offer(E, 6, I + 'fire_dragon_flesh', 2, 8, 3, BUY)),
        ('ice_dragon_flesh', offer(E, 7, I + 'ice_dragon_flesh', 1, 8, 3, BUY)),
        ('lightning_dragon_flesh', offer(E, 8, I + 'lightning_dragon_flesh', 1, 8, 3, BUY))],
    4: [('dragonbone', offer(E, 10, I + 'dragonbone', 2, 20, 5, BUY)),
        ('shiny_scales', offer(E, 4, I + 'shiny_scales', 1, 5, 2, BUY)),
        ('dread_shard', offer(I + 'dread_shard', 5, E, 1, 10, 4, SELL)),
        ('stymphalian_bird_feather', offer(E, 8, I + 'stymphalian_bird_feather', 12, 3, 6, BUY)),
        ('troll_tusk', offer(E, 4, I + 'troll_tusk', 12, 7, 3, BUY))],
    5: [('sea_serpent_fang', offer(E, 15, I + 'sea_serpent_fang', 3, 20, 3, BUY)),
        ('hydra_fang', offer(E, 12, I + 'hydra_fang', 1, 20, 3, BUY)),
        ('ectoplasm', offer(I + 'ectoplasm', 6, E, 1, 7, 3, SELL))],
}


def dump(path, obj):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2) + '\n', encoding='utf-8')


for level, trades in SCRIBE.items():
    name = 'scribe/level_' + str(level)
    ids = []
    for trade_name, trade in trades:
        dump(root / 'villager_trade' / name / (trade_name + '.json'), trade)
        ids.append(I + name + '/' + trade_name)
    dump(root / 'tags' / 'villager_trade' / (name + '.json'), {'values': ids})
    dump(root / 'trade_set' / (name + '.json'), {'amount': 2, 'trades': '#' + I + name, 'random_sequence': I + 'trade_set/' + name})
print('scribe trades written')
