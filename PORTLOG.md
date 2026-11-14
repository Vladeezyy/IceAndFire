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

## Fixed (covered by the tests in `tools/`)

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
| [#5197](https://github.com/AlexModGuy/Ice_and_Fire/issues/5197), [#4297](https://github.com/AlexModGuy/Ice_and_Fire/issues/4297) myrmex queen leaves or vanishes from her hive | A queen that "saw the sky" (`canSeeSkyFromBelowWater`: also under water, e.g. a water entrance above her, or a broken ceiling) dug a new nest, teleported into it and left her hive. Only a queen without a hive digs now; the queen wanders only without a hive; myrmexes wander to their own hive first. Self-test reproduces it (water shaft above the queen) |
| Block updates (podium, lectern, dragonforge, jar, pixie house) went to every player of every dimension (a player in the Nether got the podium of the same position in the Overworld); dragon forge sent its cook time every tick to everybody while a dragon breathed on it | All 25 `sendMSGToAll` → players tracking the entity or the chunk of the block, in its level (`IceAndFire.sendMSGToTracking`) |
| Light gray banner patterns showed their raw key in every language (`.silver` instead of `light_gray`) | Keys renamed in the 15 languages |
| Dragon corpses in caves below y 0 floated (sink check at y > -1) | World floor |
| [#5369](https://github.com/AlexModGuy/Ice_and_Fire/issues/5369) health above 1024 from the config silently capped | Config comments name the Minecraft limit, a warning in the log when a value is above it |
| Client packets: arbitrary damage to any entity within 100 blocks (`MessageMultipartInteract`), attack at range (`MessagePlayerHitMultipart`), saddle/armor any hippogryph, teleport ridden death worms/amphitheres/hippocampi, rewrite any myrmex hive, any bestiary page on any lectern, fire breath of any dragon anywhere | Both multipart messages removed (never sent by a real client), the server reads its own inventories, positions within 10 blocks, lectern pages chosen on the server, staff edits only rooms/breeding by a player holding the staff with command reputation, fire sync server to client only |
| Every right click with a myrmex staff added an empty hive to the world data (cleanup was commented out) | Removed; hive data goes to the player using the staff, not to everybody |
| "Fell into the void" checks at y < -1 made hippogryphs and amphitheres take off in caves below 0, path starts stopped at y 0 | World floor (`getMinBuildHeight`) |
| Helmets showed the hat layer of the skin ([#5112](https://github.com/AlexModGuy/Ice_and_Fire/issues/5112)) | Helmets get their own layer (0.55) above the hat layer (0.5) |
| The stone statue renderer baked a pig model every frame for every statue | Baked once (CurseForge report 2026-11-01: the client scene shows a statue of every living creature and a gorgon turning mobs to stone; the self-test saves statues with a chunk and loads them again) |

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

## Next release (2026-11-14, 1.21.11 and 26.x): death worm blast with PuzzlesLib

- CurseForge report 2026-11-11 (2.1.13-beta.10, Fabric 26.3, PuzzlesLib 26.3.17): the server crashed whenever a death worm
  bit, `NullPointerException: exploded positions is null` at `puzzleslib$hurtEntities` ← `BlockLaunchExplosion.explode` ←
  `EntityDeathWorm.doHurtTarget`.
- Cause: on 1.21.11 and 26.x the ground blast (worm bite and burrowing, dragon ground blast) ran the steps of
  `ServerExplosion.explode()` by hand: `calculateExplodedPositions()`, then `hurtEntities()`. PuzzlesLib's Fabric mixin keeps the
  blocks only from `explode()` and requires them in `hurtEntities()`. On NeoForge the same path handed the Detonate event an empty
  fixed list (the deprecated `hurtEntities()`), so protection mods could not take their blocks out of the blast.
  1.21.1 is not affected: its `BlockLaunchExplosion` extends `Explosion` and keeps the vanilla `explode()`.
- Fix: a vanilla `ServerExplosion` with `BlockInteraction.KEEP` runs its whole `explode()`; mixin `ServerExplosionBlocksMixin`
  (`@ModifyVariable` on the STORE of the block list in `explode`, `require = 0`) remembers the list, and the blast removes those
  blocks through `onBlockExploded` as before. The access wideners for `calculateExplodedPositions` and `hurtEntities` are gone.
- Tests: self-test `groundBlast` now runs a DESTROY and a KEEP blast with a pig next to each (blocks broken / kept, pig hurt);
  its chunks are forced from tick 30 (a blast finds no entities in a chunk loaded by `setBlock` alone). PuzzlesLib is in the
  compat runs (`compat_*_puzzles_lib`, on Fabric with Forge Config API Port; 26.3 uses 26.3.17, as 26.3.18 needs Fabric Loader
  0.20). Negative control on the old 26.3 code, Fabric + compat: both blasts fail with `exploded positions is null`. With the
  fix: CI green on mc/1.21.11, mc/26.1.2, mc/26.2, mc/26.3 (self-test NeoForge and Fabric, bug hunt with Lithium and
  PuzzlesLib); 26.3 self-test 43/43 on both loaders.

## 2.1.13-beta.10 (2026-11-08): crash fixes after the CurseForge report and a code audit (1.21.1)

- CurseForge report (2.1.13-beta.9, Fabric 26.3): client crashes in the projectiles, `ClassCastException: ClientLevel cannot be cast to ServerLevel`. Not on 1.21.1: `getGameRules()` and `spawnAtLocation(ServerLevel, …)` are not ServerLevel-only there. Only the feather item now drops on the server.
- Right-click on a wall with a chained mob: the knot was created on the client too. On 1.21.1 its id comes from the constructor, so no crash; only a ghost knot in the chain list and a double placement sound. Fix: entities made in the interaction handler on the server only (`if (level.isClientSide()) return;` first).
- First chain lost at once, no error: fresh chain data started with `isInitialized = false`, and its first tick rebuilt the list from the empty saved ids. Fix: fresh data starts initialized (`private boolean isInitialized = true;`); only deserialization sets it to false. The first-chain check passes on 1.21.1 with the fix.
- Code audit (no report, not reproduced one by one): NBT indices clamped (troll variant and weapon, hippogryph variant and `EggOrdinal`, dragon egg `Color`, ice egg `Color` byte, pixie jar and house `PixieType`, dragon skull stage 1..5); dragon horn: `type.create` may return null, and `read("EntityUUID").orElse(null)` gave `setUUID(null)` and a server crash; myrmex staff screens rebuilt their widgets every frame (`renderables.clear()` + `addWidget`, `children` grew, old buttons answered clicks), now `clearWidgets()`, and the hive-null message runs through `enqueueWork`; cockatrice egg chance `nextInt(max + 1)` overflowed at `Integer.MAX_VALUE`. Covered by compiling and by the scenes not crashing.
- Scene, new steps: projectiles with late feathers (the server sets `tickCount = -1000`, so the client removes them by its own age) and a chain to a wall (right-click on the wall with a chained mob).
- Results: the scene caught the old feather, bubbles and hydra breath crashes and the old right-click chain crash on 26.3 Fabric. With the fixes it passes with 0 failed checks on 1.21.1, 1.21.11, 26.1.2, 26.2 and 26.3, Fabric and NeoForge.
- Server selftest: Fabric and NeoForge pass on this branch (39 checks, 0 failed, no slow chunks); all five versions × both loaders pass (10 runs, 2026-11-08). Only log noise in the errors: the Yggdrasil key fetch without network, and on 1.21.1 Fabric "No data fixer registered" for the mod's entities.

## Unreleased (2026-11-14): code audit, batches 1 and 2

Branch `multiloader` (1.21.1). This branch has batch 2 only: 840f77a (fixes), 5f72302 (self-test), 8417eaf (breath strip fix),
c11f693 (riding owner check by UUID). Batch 1 (b30a67f, b39840d) is on `mc/26.1.2` and is not merged here.

Fixed (batch 2):

Multiplayer and PvP:
- A client may get off a mob but no longer mount one by packet. The rider position a client sends is limited to one step per tick, in a loaded chunk, outside blocks.
- The riding packet checks the owner by UUID (c11f693): on 1.21.1 `isOwnedBy` looks the owner up in the level's player list.
- Chains go on no players and no other players' pets. The gorgon head and the cockatrice scepter spare other players' pets and respect PvP and claim mods (attack event); a refused target is asked again at most once a second.
- The hive packet has an NBT quota of 256 KiB (see the deviations below).
- The deathworm gauntlet and the scepter keep their use state per player, not in the item.
- The right click handler runs on the server, main hand only.

Fixes:
- `isChained()` was inverted: wild dragons fly as long as meant; only chained or tame dragons go to a forge lure.
- A crystal summon runs after the tick (a `TickTask`; `server.execute` would run it at once), not inside the level's entity tracking callback.
- A baby dragon rides its owner's shoulder on the server too (1.21.1 `Entity.startRiding` lets a player be a vehicle, so no mixin).
- The scepter damage names its caster.
- World data is kept per level (a static list was shared by all dimensions and worlds) and saved only when changed.
- Fabric: nested drop captures; both `forEachModifier` overloads (`WrapOperation` instead of `Redirect`).

Server performance:
- Dragon breath: one ray per tick instead of about 100; blocks every 5 ticks with the chances of all hits since; the fire is synced on a change and every 10 ticks, with a stop packet. The strip fix is 8417eaf.
- Dragonforge: the shape is read every 10 ticks, dragons are lured every 20.
- Pathfinding: the chunk cache reaches at most 48 blocks past start and end (was 1.5 times the follow range); a path to a moving entity is not restarted every tick.
- Myrmex: food is searched section by section with a pause after an empty search; cocoons through the block entities, `isInHive` once a tick; far rooms load no chunks; at most 64 aggressors per hive.
- Death worms and sirens sample their targets instead of reading whole cubes; the siren song packet only on a change.
- Wild dragons look for mobs within 32 blocks of height (players: the whole range).
- Animals look for feared mobs about once a second.
- Dragon parts: the parent is cached, the size is refreshed only when it changes, pushes every other tick.
- Flight targets of dragons, hippogryphs, birds and cockatrices load no chunks.
- Dragon caves: a bit mask instead of hash sets, cut to the chunks a feature may write; the lake check reads the region's structure references.

Client:
- Dragon pose: cubes and poses are looked up by index instead of by name. No per-frame pose cache on 1.21.1 (the cache is on 26.1.2 and 1.21.11; its replay check was run on 26.1.2 only).

Behaviour changes for players (release notes; batch 2 items):
- Wild dragons fly as long as meant (isChained was inverted); only chained or tame dragons go to a forge lure.
- Wild dragons look for mobs only within 32 blocks above and below (players: the whole range).
- Chains go on no players and no other players' pets; the gorgon head and the cockatrice scepter spare other players' pets and respect PvP and claim mods.
- Animals react to feared mobs within about a second (were every other tick).
- Breath: blocks change every 5 ticks with the chances of the hits since; the fire packet on a change and every 10 ticks.
- Myrmex food search pauses after an empty search (food found up to 10 s later).
- The scepter damage names its caster; the hive packet has an NBT quota of 256 KiB.

Deviations from the audit plan:
- Hive packet NBT quota: 256 KiB, not the 64 KiB of the plan (the packet had the default 2 MiB). A hive of several hundred rooms must pass; a hive over the quota is not sent (the writer drops it, the reader gets null).
- Stuck digging of wild dragons stays off: `EntityDragonBase.WILD_DRAGONS_HANDLE_STUCK = false`, as in the original.

Stress, NeoForge 26.1.2, seed 1234567, ms per tick (average, range over runs). "before" = stress base 0409e29; "after" = this batch.

| Scene | before | after |
|---|---|---|
| idle (no mod entities, the vanilla base) | 0.86-1.18 | 1.04-1.38 |
| breath (3 stage-5 fire dragons at a stone wall) | 8.47-9.98 | 1.99-2.40 |
| hive-cold (40 myrmex workers, no food) | 2.48-3.47 | 2.02-2.41 |
| hive | 2.06-3.61 | 1.92-2.42 |
| farm (200 animals, a wild dragon 30 blocks away) | 5.59-7.31 | 5.74-7.30 (run noise 1.6 ms: no change) |
| worms (2 death worms, 5 sirens) | 5.05-6.88 | 1.91-2.61 |
| idle-end | 0.65-0.90 | 0.70-1.03 |

p95: breath 12.4-13.8 -> 3.6-4.7; worms 14.8-18.8 -> 3.1-4.0; hive-cold 6.0-8.2 -> 3.7-3.9.

Fabric 26.1.2, one run each. Before: idle 1.01, breath 8.34, hive-cold 2.90, hive 2.93, farm 6.81, worms 4.49, idle-end 0.76. After (fabA, before the breath strip fix): idle 1.17, breath 2.11, hive-cold 2.08, hive 2.39, farm 6.53, worms 2.40, idle-end 0.96.

Behaviour evidence:
- Breath wall blocks broken (39 checks): before 16058-16305; after 15528-15544. The throttled version before the strip fix broke 11283-11341; 90f6d23 fixed it.
- Farm: 200/200 animals alive with the fear goal; the same flee counts by the end. Hive, worms and sirens: same as before.

Pregeneration with C2ME (Fabric 26.1.2, C2ME 0.4.0-alpha.0.71+26.1.2, Chunky 1.5.66, radius 1000 = 16129 chunks): before (0409e29) 0:00:57, after 0:01:02, one run each, the same within run noise. No ERROR, "Caused by", ConcurrentModificationException, "Accessing PalettedContainer from multiple threads", "off the server thread" or saved-data warnings in either state; a restart of the same world is clean. Only noise: C2ME's mixin WARN `ca/spottedleaf/starlight/...` (ClassNotFoundException), the same in both states.

Self-test (server, both loaders):
- 1.21.1 69/69 on both loaders (68/69 before c11f693).

Measured on 26.1.2 (not on 1.21.1; see the 26.1.2 log for the scenes and the method): the MSPT table, the Fabric line, the wall blocks, the pregeneration and the pose cache check are the same numbers as there.

Not done:
- Stress scene "5 players and 30 villagers, packets per second".
- Stress scene "flight over new chunks with lakes".
- The rider position limit was not tested against "moved wrongly" of a death worm in sand with a real client.

## 2.1.13-beta.9 (2026-11-08, 26.2 and 26.3 only)

- CurseForge report 2026-11-08 (Fabric 26.3, LambDynLights): the client crashed with "Ticking entity" as soon as a dragon came
  into view: `Tried to access entity ID before ID assignment` in `Entity.getId` ← `Entity.equals` ← LambDynLights
  `removeLightSource` ← `Entity.remove` ← `EntityMutlipartPart.remove` ← `EntityDragonBase.removeParts`. Reproduced on the
  unchanged 26.3 Fabric code with `tools/scenetest.sh -Pwith_compat` and LambDynLights.
- Cause: from 26.2 an entity takes its id from the level, and the client level gives 0. Dragon, hydra, death worm, sea serpent
  and cyclops also build their multipart parts on the client; those parts are never added to the client level, but LambDynLights
  hooks `Entity.remove` and compares entities with `equals`, which calls `getId()`.
- Fix: parts made on the client take negative ids in the `EntityMutlipartPart` constructor (from -1 000 000 down). The server never
  sends negative ids. New scene check "parts made on the client have ids"; LambDynLights in the compatibility runs
  (`compat_neo_lambdynamiclights`, `compat_fabric_lambdynamiclights`).
- Tests: the dragon scene on 26.3 and 26.2, Fabric and NeoForge: 33/33 checks each, no crash.
- Elytxtreme's report of 2026-11-05 (death worm bite, Lithium, `cachedResistances` null) is from beta.7 and is fixed in beta.8;
  the CI fuzz test with the same Lithium passes. Nothing new in beta.9 for it.
- 1.21.1, 1.21.11 and 26.1.2 stay on beta.8: their entities take ids from a static counter in the constructor, so the client
  parts are not affected.

## 2.1.13-beta.8 (2026-11-06)

- CurseForge report 2026-11-05: the bite of a death worm crashed the server with Lithium (`this.cachedResistances` is null):
  `BlockLaunchExplosion` called `explode()` and then `calculateExplodedPositions()` on one `ServerExplosion` (1.21.11 and 26.x; 1.21.1
  has its own `Explosion`). The steps of `explode()` one by one now. The same report says TPS under 15 with the mod, 20 without: NOT
  reproduced. On 1369 ticking chunks at the default settings a tick took about 5 ms, the mod 12% of it (no other mods, no hive in that
  world). Waiting for a spark report of the reporter (spark is in their pack).
- `tools/fuzztest.sh` (Fuzz, Fabric, with Lithium, on CI): every creature, item and block through bites, damage, animations, clicks,
  riding and breath, plus new chunks with every chance of the mod at 1. It found the queen spawn egg near a hive
  (`setCustomName(null)`), and with the old explosion it finds the crash of the report by itself. 0 exceptions on all 5 versions now.
  `FUZZ_RADIUS=15` locally: 961 chunks, 2543 dragons, no exception.
- From a JFR profile of the stress test (server thread samples, same seed): `DreadAIRideHorse` 50 -> 10 (a path search every tick),
  parts of dragons 60 -> 22 (a water check every tick), statues 99 -> 38. Tick 7.3 -> 6.6 ms, one run each: within the spread.
- Still open: `IafWorldData.check` (only the last entry), `Dragon Pathfinding Threads` has no effect, `isPositionClear` checks one cell.

## 2.1.13-beta.7 (2026-11-05)

- CurseForge report 2026-11-04: server crash on Fabric 1.21.11, `this.mob` is null from `PathNavigateDeathWormLand.isSafeToStandAt`.
  A worm without a rider uses its land navigation only on its first tick; `getPathTypeOfMob` reads the evaluator's mob, null outside
  a path search since 1.20.5. Now `getPathType`. Audit of the same class of bugs, on every version: the separation list of dangerous
  structures, the hive list and the shared hive and cave features were used by the worldgen threads without protection;
  `MyrmexHive.getQueen` threw on a queen without a hive; the ticket of the summoning crystal never expired.
- Self-test 38 checks (39 on 26.3) on both loaders, every version. Stress, Fabric 1.21.11, one seed (20261104): old code 10.4 and
  8.0 ms average tick, new code 7.9 and 6.7 ms, so no change beyond the spread. Without `SEED=` the same code gave 6.3 to 16.6 ms.
- Not fixed, found by the same audit: `IafWorldData.check` compares with the last entry only (`canGenerate` overwritten, as in the
  original; a fix means fewer structures in new chunks), `Dragon Pathfinding Threads` has no effect (unbounded queue, one thread),
  `PathNavigateDeathWormLand.isPositionClear` checks one cell (a list of one mutable `BlockPos`), `EntityMyrmexEgg` throws without
  `HiveUUID`, `EntityMyrmexQueen.setCustomName(null)` throws with a hive.

## Current state (2026-10-19, resume here)

- 2.1.13-beta.3: double scaling of 1.21 (`LivingEntityRenderer` and `LivingEntity.getDimensions` apply
  `getScale()` since 1.20.5; dragons and sea serpents also scaled their dimensions, six renderers scaled their models
  by the same factor) — `client/render/entity/VanillaScale.undo`, dimensions scaled once. Adult dragon hitbox 23 → 5.25
  blocks, rider on the back (net test logs the rider position and takes a third-person shot). #4768: flight cycle
  segment skipped when only its END frame equalled the flying pose (`DragonTabulaModelAnimator`), now only when both do.
- Tests on Windows (2026-10-19): self-test 29/29; client scene 28/28 with Sodium, Iris, Lithium, FerriteCore, ModernFix,
  Jade, EntityCulling, AttributeFix (`-Pwith_compat`); net test: rider on the back, dismount, no errors; stress 13/13
  (8.6 ms average tick, the queen stays 6000 ticks). Scene frames the sea serpent by its body length (the camera was
  inside it once its hitbox stopped being scaled twice).
- Tests on macOS: self-test 29/29, stress 13/13 (12.5 ms average tick with 30 dragons; the test hive has its queen,
  5 workers, 3 soldiers and a sentinel, 6000 ticks of /tick sprint, the queen stays), net test, scene.

## Citadel → Rampart (2026-10-24)

Citadel has no Fabric and no 26.x build, so Ice and Fire now depends on **Rampart** 1.1.0 (`Vladeezyy/rampart`, the
classes of Citadel the mod uses, LGPL-3.0): Advanced/Basic models, ModelAnimator, Animation(Handler), Tabula models
(TabulaModel, TabulaModelHandler, containers, ITabulaModelAnimator), custom collisions (ICustomCollisions,
CustomCollisionsNavigator), spawn biome configs, CustomToolMaterial, CitadelItemRenderProperties (NeoForge). Imports
`com.github.alexthe666.citadel.*` → `dev.vladeezyy.rampart.*` (121 files), dependency `rampart` in `neoforge.mods.toml`.
- Same scene as on Citadel (screenshots compared pair by pair: only random variants and animation phase differ),
  self-test 29/29, stress 13/13 (average tick 8.9 ms with 28 dragons and 100 creatures, 12.5 ms on Citadel: Rampart
  sends the animation packet only to the players tracking the creature), net test (riding a dragon) passes.

## TODO

- [#5297](https://github.com/AlexModGuy/Ice_and_Fire/issues/5297) dragon corpses standing on servers: not reproduced
  (the client ticks every entity of loaded chunks in 1.21.1, the dead flag is synced data; a dragon received dead
  starts in the pose since beta.1). Needs a report with the server mods.
- Next: play-test feedback.

## Multiloader split (branch `multiloader`, 2026-10-24)
- Layout common / neoforge (fabric next), as Rats: game code in `common` compiles without NeoForge; loader hooks behind
  `dev.vladeezyy.iceandfire.platform.Platform` / `ClientPlatform`; the original's `@SubscribeEvent` listeners stay and
  run on the mod's own bus (`IafBus`, index generated by `tools/port_events.py`); each loader posts its events there.
- NeoForge extension methods: vanilla where there is one (structure processors `processBlock`, piston reaction in the
  block properties, vanilla chunk ticket for the summoning crystal, own `Tier` instead of `SimpleTier`, hippocampus in
  `minecraft:can_breathe_under_water`, death worm egg size as synced entity data); dragon load/unload and multipart
  tracking through a common mixin on `ServerLevel$EntityCallbacks`; the rest keep their NeoForge methods (marked
  "NeoForge extension method") and get Fabric mixins. Item renderers and armor models in `IafItemRenderers`.
- Tests (NeoForge, macOS): selftest 29/29, scene 0 failed checks (28 mobs), net test (mount, control, flight) OK,
  stress 13/13. Same seed A/B on this machine: split 10.2 ms average tick, `main` 10.1 ms (generation 117 s vs 120 s).
- Scripts: `LOADER=neoforge|fabric` (default neoforge), run directories in `<loader>/run`.
- Fabric module (same branch): Fabric API where it has the event, mixins for the rest (`fabric/.../mixin`: living
  tick, incoming damage, fall, item use tick, death drops, target change, finalize spawn, lightning, mount, projectile
  impact; client: living/player render, screen opening, camera angles, ghost chest material), data attachments for
  the entity data, the two biome modifiers through BiomeModifications, scribe trades through TradeOfferHelper, forge
  bricks/inputs as item storages. Armor textures and config-dependent dragonsteel stats through common interfaces
  (`IafArmorTextureItem`, `IafStackStatsItem`) that NeoForge's extension methods also satisfy.
- Fabric tests (macOS): selftest 29/29, scene 0 failed checks (28 mobs), stress 13/13 with the A/B seed (7.5 ms
  average tick, generation 111 s), net test (mount, control, flight) OK.
- Not on Fabric yet: the pixie jar gives no dust to hoppers, dragonbone arrows are used up with Infinity, no camera roll.
- Fixed on the way: Tabula models were read with ClassLoader#getResourceAsStream("/assets/...") (Fabric returns null
  for the leading slash); now Class#getResourceAsStream of the mod's own class.
