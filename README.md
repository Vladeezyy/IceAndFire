# Ice and Fire for NeoForge 1.21.1

Port of [Ice and Fire](https://www.curseforge.com/minecraft/mc-mods/ice-and-fire-dragons) by Alexthe666 and TheBv
(version 2.1.13, Forge 1.20.1) to NeoForge 1.21.1. Work in progress.

Requires [Citadel](https://modrinth.com/mod/citadel) 2.7.1+ for 1.21.1.

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
- Server crash when a myrmex swarmer (or a death worm / troll of an unknown kind) died.
- Network: clients could deal any damage to any entity nearby, saddle and armor any hippogryph, teleport ridden
  creatures, rewrite any myrmex hive, add any bestiary page on any lectern, make any dragon breathe fire anywhere.
- Every right click with a myrmex staff added an empty hive to the world data.
- Hippogryphs and amphitheres taking off in caves below y 0, dragon corpses floating there.
- Only one hydra neck had a hitbox; deathworm armor repaired with the wrong chitin; Stymphalian feathers and hydra
  arrows damaged the wrong item.

## License

LGPL-3.0-only, like the original ([LICENSE](LICENSE)). Original code and assets © Alexthe666 and contributors.
