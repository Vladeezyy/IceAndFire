# Port log: Ice and Fire 2.1.13 (Forge 1.20.1) → NeoForge 1.21.1

Source: `AlexModGuy/Ice_and_Fire`, branch `1.20` (last commit 2025-09-05, "fix: #5543"), mod version 2.1.13,
LGPL-3.0. 707 Java files, ~92k lines. Dependency: Citadel (AlexModGuy) — NeoForge 1.21.1 build 2.7.1 exists on
Modrinth, so it stays an external dependency.

## Plan

1. Build skeleton (ModDevGradle, JDK 21, NeoForge 21.1, Citadel 2.7.1, JEI API).
2. Copy sources and resources from the original; keep packages and class names.
3. Resources: singular data folders, recipes/loot/advancements/tags format of 1.21, `forge:` → `c:`/`neoforge:`.
4. Java: registries (`DeferredRegister`/`DeferredHolder`), events, network (`SimpleChannel` → payloads),
   capabilities → data attachments, item NBT → data components, attributes/effects as holders, armor materials
   as registry entries, enchantments as data, worldgen/structures codecs, rendering API.
5. Fix the bugs below, then functional, stress and compatibility tests.

## Bugs of the original to fix in the port

Collected from open issues of `AlexModGuy/Ice_and_Fire` (sorted by reactions and comments, 2026-10-18).

| Issue | Problem | Plan |
|---|---|---|
| [#5089](https://github.com/AlexModGuy/Ice_and_Fire/issues/5089), [#5360](https://github.com/AlexModGuy/Ice_and_Fire/issues/5360) | Log flooded with "UUID of added entity already exists: Dragon Part" | Multipart parts must get fresh UUIDs and must not be re-added/saved with the chunk |
| [#5335](https://github.com/AlexModGuy/Ice_and_Fire/issues/5335), [#5592](https://github.com/AlexModGuy/Ice_and_Fire/issues/5592) | Crash "asking a region for a chunk out of bound" near the world border (dragon caves, nests, siren islands) | Bounds checks in feature placement / `WorldGenRegionMixin` |
| [#5192](https://github.com/AlexModGuy/Ice_and_Fire/issues/5192), [#5208](https://github.com/AlexModGuy/Ice_and_Fire/issues/5208) | Riding a dragon on a server lags the rider out / times out | Investigate rider sync packets and position updates |
| [#5495](https://github.com/AlexModGuy/Ice_and_Fire/issues/5495) | Server watchdog crash in `EntityDataProvider.getCapability` (HashMap from several threads) | Capabilities become data attachments |
| [#4468](https://github.com/AlexModGuy/Ice_and_Fire/issues/4468) | Summoning crystal does not work when the dragon is far away (chunk not loaded, flag always false) | Load the chunk and fix the logic in `ItemSummoningCrystal` |
| [#5196](https://github.com/AlexModGuy/Ice_and_Fire/issues/5196) | Dragon horn cannot pick a dragon (clicks on a part, culled parts with EntityCulling) | Interaction through parts forwards to the parent |
| [#5146](https://github.com/AlexModGuy/Ice_and_Fire/issues/5146) | Structures (graveyards, skeletons) spawn on top of oceans | Check the biome at the real structure position |
| [#5064](https://github.com/AlexModGuy/Ice_and_Fire/issues/5064), [#5251](https://github.com/AlexModGuy/Ice_and_Fire/issues/5251) | Hippogryphs and griffins fly up and hover, never land | Flight/ground state logic |
| [#5157](https://github.com/AlexModGuy/Ice_and_Fire/issues/5157) | Sitting amphithere can be ridden and stays still in the air | Stand up on mount |
| [#5197](https://github.com/AlexModGuy/Ice_and_Fire/issues/5197), [#4297](https://github.com/AlexModGuy/Ice_and_Fire/issues/4297) | Myrmex queen leaves or vanishes from the hive | Queen must not despawn and must stay in the hive |
| [#5112](https://github.com/AlexModGuy/Ice_and_Fire/issues/5112), [#5205](https://github.com/AlexModGuy/Ice_and_Fire/issues/5205) | Armor models clip with the skin's outer layer / broken armor textures | Armor rendering in the new API |
| [#5297](https://github.com/AlexModGuy/Ice_and_Fire/issues/5297) | Dragon corpses shown as standing dragons on servers | Sync of the dead/model state |
| [#5369](https://github.com/AlexModGuy/Ice_and_Fire/issues/5369) | Dragon health above 1024 from config has no effect | Attribute cap: document or raise via `max_health` range |
| [#4768](https://github.com/AlexModGuy/Ice_and_Fire/issues/4768) | Lightning dragon head twitches while flying | Animation |

## Fixed so far (code done, not yet tested in game)

| Problem | Fix |
|---|---|
| [#5495](https://github.com/AlexModGuy/Ice_and_Fire/issues/5495) watchdog crash in the entity data cache | Entity data is a NeoForge attachment, the static cache maps are gone |
| [#4398](https://github.com/AlexModGuy/Ice_and_Fire/issues/4398) "dangerous worldgen distance from spawn" did not work | The check took the Y of the spawn point as its Z |
| [#5146](https://github.com/AlexModGuy/Ice_and_Fire/issues/5146) graveyards and mausoleums on oceans | The biome is tested at the surface of the structure start, not anywhere within `max_distance_from_center` |
| Clients could send server-only messages (e.g. `MessageUpdatePodium`: any item on any podium) | Every message is registered for one direction; only four are bidirectional |
| Summoning crystal kept the summon state in the item singleton: two players summoning at once broke each other | Per-summon `PendingSummon`, chunk tickets via `TicketController`, client side of single player no longer summons |
| White and red deathworm armor were repaired with each other's chitin | Repair materials swapped back |
| Stymphalian feathers damaged a copy of the shield, never the shield | Damage the used shield |
| Hydra arrows broke the chest slot instead of the shield hand | Slot of the used hand |
| Myrmex foraging posted a grief event for every block in the search box (TPS) | Edible check first, event only for candidates |
| Server-only `I18n` in the summoning crystal tooltip | `Component.translatable` |
| [#5335](https://github.com/AlexModGuy/Ice_and_Fire/issues/5335), [#5592](https://github.com/AlexModGuy/Ice_and_Fire/issues/5592) features and structures at the world border | No generation within 96 blocks of the world border (`IafWorldRegistry.isInsideWorldBorder`) |
| [#5089](https://github.com/AlexModGuy/Ice_and_Fire/issues/5089), [#5360](https://github.com/AlexModGuy/Ice_and_Fire/issues/5360) "UUID of added entity already exists" spam | A part is added to the level once (`addedByParent`); a part in a section without entity access was re-added every tick. Part types are `noSave()`, parts of 1.20.1 worlds discard themselves, parents recreate unloaded parts, a part left in a hidden section is moved back to its parent |
| Hydra: only one neck had a hitbox | `updatePart(headBoxes[HEADS + i])` instead of `HEADS + 1` |
| Dread spawner crashed the generation of the dread mausoleum (1.21: `BlockEntity` checks the block of its type) | `TileEntityDreadSpawner extends BlockEntity implements Spawner` with its own type |
| [#5196](https://github.com/AlexModGuy/Ice_and_Fire/issues/5196) dragon horn (and other items) did nothing on a click on a dragon part | The part offers the held item to its parent, like `Player.interactOn` does for living entities |
| [#5064](https://github.com/AlexModGuy/Ice_and_Fire/issues/5064), [#5251](https://github.com/AlexModGuy/Ice_and_Fire/issues/5251) hippogryphs/griffins stay in the air with folded wings | `FlyingMoveControl` turned the gravity off, the switch back to the land navigator did not turn it on; stuck ones of old worlds are released on load |
| [#5157](https://github.com/AlexModGuy/Ice_and_Fire/issues/5157) amphithere sits in the air after the rider gets off | "Stay" applies again only on the ground |
| [#5192](https://github.com/AlexModGuy/Ice_and_Fire/issues/5192), [#5208](https://github.com/AlexModGuy/Ice_and_Fire/issues/5208) riding lag on servers (not reproduced: net test with an adult dragon keeps 20 game ticks per second on the client) | Every dragon part called `markHurt()` every tick: a motion packet per part per tick to every player tracking it, ~200 packets/s per dragon (measured with `tools/nettest.sh`). Removed |
| Server crash when a death worm of an unknown variant, a myrmex swarmer or a troll of an unknown type died (null loot table; found by the stress test: a dragon burning myrmexes) | `BuiltInLootTables.EMPTY`; the self-test kills every creature once |
| Light gray banner patterns showed their raw key in every language (`.silver` instead of `light_gray`) | Keys renamed in the 15 languages |
| Dragon corpses in caves below y 0 floated (sink check at y > -1) | World floor |
| [#5369](https://github.com/AlexModGuy/Ice_and_Fire/issues/5369) health above 1024 from the config silently capped | Config comments name the Minecraft limit, a warning in the log when a value is above it |
| Client packets: arbitrary damage to any entity within 100 blocks (`MessageMultipartInteract`), attack at range (`MessagePlayerHitMultipart`), saddle/armor any hippogryph, teleport ridden death worms/amphitheres/hippocampi, rewrite any myrmex hive, any bestiary page on any lectern, fire breath of any dragon anywhere | Both multipart messages removed (never sent by a real client), the server reads its own inventories, positions within 10 blocks, lectern pages chosen on the server, staff edits only rooms/breeding by a player holding the staff with command reputation, fire sync server to client only |
| Every right click with a myrmex staff added an empty hive to the world data (cleanup was commented out) | Removed; hive data goes to the player using the staff, not to everybody |
| "Fell into the void" checks at y < -1 made hippogryphs and amphitheres take off in caves below 0, path starts stopped at y 0 | World floor (`getMinBuildHeight`) |
| Helmets showed the hat layer of the skin ([#5112](https://github.com/AlexModGuy/Ice_and_Fire/issues/5112)) | Helmets get their own layer (0.55) above the hat layer (0.5) |

## Port notes

- Item NBT stays in `minecraft:custom_data` with the old keys (`util/ItemNbt`): items of 1.20.1 worlds are upgraded
  into exactly that component. Dragon horns also read `minecraft:entity_data` (where the upgrade moves `EntityTag`).
- Dragon armor modifier id `minecraft:556e1665-...` = the old UUID: dragons from 1.20.1 worlds do not get it twice.
- Banner patterns are data (`data/iceandfire/banner_pattern`), keys in `IafBannerPatterns`.
- Mob types are entity type tags (`undead`, `arthropod`, `aquatic`), breathing under water too
  (`can_breathe_under_water`).
- `isFood` of the animals keeps the 1.20.1 default (wheat).
- Charges (dragon fire, hydra breath, pixie, bubbles) keep their own `xPower/yPower/zPower` fields:
  `AbstractHurtingProjectile` lost them in 1.20.5, their tick never used the vanilla acceleration.

## Current state (2026-10-18)

- 2.1.13-beta.1: self-test 27/27, client scene 28/28 (also with Sodium, Iris, Lithium, FerriteCore, ModernFix, Jade,
  EntityCulling, AttributeFix: `tools/scenetest.sh -Pwith_compat`), net test (`tools/nettest.sh`), stress test 10/10
  (`tools/stresstest.sh`, `SEED=` repeats a world), production server with the built jar.
- Not done: #5197 myrmex queen (needs a long hive simulation), #4768 lightning dragon head twitch.

## TODO

- `sendMSGToAll` for entity updates → players tracking the entity (less traffic on big servers).
- #5197 myrmex queen: a hive simulation of an hour in the stress test.
