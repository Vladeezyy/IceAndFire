# Ice and Fire for NeoForge 1.21.1

Port of [Ice and Fire](https://www.curseforge.com/minecraft/mc-mods/ice-and-fire-dragons) by Alexthe666 and TheBv
(version 2.1.13, Forge 1.20.1) to NeoForge 1.21.1. Beta.

- Minecraft 1.21.1, NeoForge 21.1.197 or newer (tested with 21.1.262).
- Requires [Citadel](https://modrinth.com/mod/citadel) 2.7.1+ for NeoForge 1.21.1.
- Worlds and items of the 1.20.1 version are upgraded (item data keeps its keys in `minecraft:custom_data`).

Please report bugs of this port here, not to the original author.

## Fixed bugs of the original

- Log flooded with "UUID of added entity already exists: Dragon Part" ([#5089](https://github.com/AlexModGuy/Ice_and_Fire/issues/5089), [#5360](https://github.com/AlexModGuy/Ice_and_Fire/issues/5360)); dragon parts of old worlds
  no longer pile up.
- Features and structures generated at the world border ([#5335](https://github.com/AlexModGuy/Ice_and_Fire/issues/5335), [#5592](https://github.com/AlexModGuy/Ice_and_Fire/issues/5592)).
- Server watchdog crash in the entity data cache ([#5495](https://github.com/AlexModGuy/Ice_and_Fire/issues/5495)).
- "Dangerous worldgen distance from spawn" ignored ([#4398](https://github.com/AlexModGuy/Ice_and_Fire/issues/4398)).
- Graveyards and mausoleums on oceans ([#5146](https://github.com/AlexModGuy/Ice_and_Fire/issues/5146)).
- Dragon horn did nothing when the click hit a dragon part ([#5196](https://github.com/AlexModGuy/Ice_and_Fire/issues/5196)).
- Hippogryphs and griffins stuck in the air with folded wings ([#5064](https://github.com/AlexModGuy/Ice_and_Fire/issues/5064), [#5251](https://github.com/AlexModGuy/Ice_and_Fire/issues/5251)).
- Amphitheres sitting in the air after the rider gets off ([#5157](https://github.com/AlexModGuy/Ice_and_Fire/issues/5157)).
- Skin showing through helmets ([#5112](https://github.com/AlexModGuy/Ice_and_Fire/issues/5112)).
- Health above 1024 in the config silently capped by Minecraft: the config says so, the log warns ([#5369](https://github.com/AlexModGuy/Ice_and_Fire/issues/5369)).
- Summoning crystal state shared by all players.
- Dragons sent ~200 useless network packets per second each (a motion packet for every part every tick) to every
  player nearby; a likely cause of the riding lag on servers ([#5192](https://github.com/AlexModGuy/Ice_and_Fire/issues/5192),
  [#5208](https://github.com/AlexModGuy/Ice_and_Fire/issues/5208)), which could not be reproduced directly.
- Light gray banner patterns showed their raw translation key.
- Adult dragons (and big death worms, sea serpents, dread creatures, young myrmexes) were drawn several times too big
  and had a huge hitbox (an adult dragon 23 blocks high instead of 5): Minecraft 1.21 scales models and hitboxes by
  `getScale()` itself, and the mod scaled them again. The rider sat under the dragon's body; now on its back.
- Lightning dragon head twitched once per wing beat while flying ([#4768](https://github.com/AlexModGuy/Ice_and_Fire/issues/4768)):
  a segment of the flight cycle was skipped whenever its end frame equalled the still flying pose.
- The bestiary's contents could not be read: white entry names on the light parchment buttons; now dark ink like the
  pages.
- Server crash when a myrmex swarmer (or a death worm / troll of an unknown kind) died.
- Network: clients could deal any damage to any entity nearby, saddle and armor any hippogryph, teleport ridden
  creatures, rewrite any myrmex hive, add any bestiary page on any lectern, make any dragon breathe fire anywhere.
- Every right click with a myrmex staff added an empty hive to the world data.
- The myrmex queen left her hive whenever she "saw the sky", also through water (a water entrance above her, a broken
  ceiling): she dug a new nest and moved there ([#5197](https://github.com/AlexModGuy/Ice_and_Fire/issues/5197), [#4297](https://github.com/AlexModGuy/Ice_and_Fire/issues/4297)).
- Block updates (podium, lectern, dragon forge, jar, pixie house) went to every player of every dimension, the dragon
  forge's cook time every tick while a dragon breathed on it; now only to the players near the block.
- Hippogryphs and amphitheres taking off in caves below y 0, dragon corpses floating there.
- Only one hydra neck had a hitbox; deathworm armor repaired with the wrong chitin; Stymphalian feathers and hydra
  arrows damaged the wrong item.

## Tested with

Sodium 0.6.13, Iris 1.8.12, Lithium 0.15.4, FerriteCore 7.0.3, ModernFix 5.27.26, Jade 15.10.6, JEI 19.64,
EntityCulling 1.11.2, AttributeFix 21.1.3 (client scene, all creatures rendered). Dedicated server: a client riding an
adult dragon; load test with 30 adult dragons and 100 creatures, 4096 generated chunks, chunk reload cycles.

## Known issues


## License

LGPL-3.0-only, like the original ([LICENSE](LICENSE)). Original code and assets © Alexthe666 and contributors.
