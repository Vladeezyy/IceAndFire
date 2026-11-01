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
| CurseForge report 2026-11-01: a stone statue (gorgon victim, troll in daylight) crashed the client on 1.21.11 and 26.x, and again on every load of the world: the model of the trapped creature was posed with a render state of another renderer (fallback pig model, `ClassCastException`), on 26.2+ the hollow copy had no entity id (`Tried to access entity ID before ID assignment`) | Only a model of the same renderer is posed, the copy gets a negative id, a creature of another mod whose render state fails is drawn in its rest pose; the pig model is baked once instead of every frame. Scene: statue of every living creature, a gorgon in a pen; self-test: statues saved with a chunk and loaded again |

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

## 26.1.2 (branch `mc/26.1.2`, 2026-10-31)
Port of the multiloader 1.21.1 code to Minecraft 26.1.2 (NeoForge 26.1.2.121, Fabric 0.155.3, Rampart 1.1.0, JEI 29).
Mechanical rewrites in `tools/port26` (shared with the Rats port, plus `port_iaf_26.py` and the other `*_26.py`),
resources by `tools/port26/convert_iaf_26.py`. Write-up of the pitfalls: modding-kb
`porting/mc-versions/26.1/ice-and-fire.md`.
- Rendering on render states and the submit collector: mob renderers on Rampart's `RampartMobRenderer`; the 1.21.1
  drawing code of block entities, items, particles and complex layers kept through `IafBufferSource` (records the
  vertices, submits them as custom geometry). Rampart models drawn by hand go through `IafModels.render` (pose sync);
  layers that read the parent's parts pose it once per entity (`IafEntityLayer.poseParent`).
- Items drawn in code: special model `iceandfire:item_renderer` chosen per display context; ghost chest on the chest
  atlas (own `BlockEntityRenderer`, vanilla chest item model). Armor: equipment assets per material, models cached per
  item and slot (1.21.1 baked one every frame), textures from the items (NeoForge item client extensions, Fabric
  `ArmorRenderer`).
- Ghost and siren appearances: a particle group of their own (NeoForge `RegisterParticleGroupsEvent`, Fabric
  `ParticleGroupRegistry`). Dragon skins/armor: `ArrayLayeredTexture` as a reloadable texture.
- NeoForge: item capabilities on the transfer API (journaled `ResourceHandler` over the common handlers), entity data
  attachment on value input/output (same NBT), recipes synced for JEI (`OnDatapackSyncEvent` /
  `RecipesReceivedEvent`), keys through `RegisterKeyMappingsEvent`. Fabric: `RecipeSynchronization` +
  `ClientRecipeSynchronizedEvent`, `KeyMappingHelper`, mixins retargeted to 26.1 (hurtServer, causeFallDamage(double),
  alignWithEntity, startRiding(ZZ), spawnAtLocation(Vec3)); the render mixins moved to common.
- Fixed on the way: dread sword with enchantability 0 (26.x refuses it: material with 1, enchantable component
  removed); dragonsteel's config attributes were ignored by NeoForge (prototype component; `ItemAttributeModifierEvent`);
  tempt range attribute for the mod's animals; `minecraft:chain` → `minecraft:iron_chain` in the chainmail recipes.
- Tests (macOS, both loaders): selftest 29/29, client scene 0 failed checks (28 mobs, GUIs, bestiary, armor), no
  errors in the logs. Test scripts run on the knowledge base's hang watchdog; empty test servers no longer pause
  (`pause-when-empty-seconds=0`).
- Stress (SEED=1234, 4096 chunks, 30 adult dragons + 100 creatures, 5 unload/reload cycles): NeoForge 13/13, 8.6 ms
  average tick (7.8 ms in a second run), generation 97-104 s; Fabric 13/13, 9.9 ms, generation 90 s. 1.21.1 `main`
  had 10.1 ms (NeoForge) / 7.5 ms (Fabric) with another seed: same range (26.1 worlds differ anyway). Wild dragons kill
  each other in the arena (the test now logs each death and every dragon gone between checks): one run lost a dragon
  to a fight between two checks; no dragon or part is lost through the disk.
- Net test (dedicated server, client rides an adult dragon, flies, dismounts): 0 failed checks on both loaders, client
  120 FPS while riding.

## 26.2 (branch `mc/26.2`, on `mc/26.1.2`, 2026-10-31)
NeoForge 26.2.0.93, Fabric API 0.161.0, JEI 30.48, Rampart `1.1.0+26.2`. After `tools/port26/port_262.py` (the
knowledge base's generic script; the local copy skips `MyrmexHive.getCenter()`) and `port_26.py <26.2 sources>`: 226
errors in common, none in the loaders. Write-up: modding-kb `porting/mc-versions/26.2/ice-and-fire.md`.
- `MultiBufferSource` is gone: the mod's own interface of the same shape (`client/render/MultiBufferSource`), so the
  1.21.1 drawing code stays; `VertexMultiConsumer` and `net.minecraft.util.Tuple` replaced by small classes.
- `AgeableMob.isBaby()` is final: dragons and myrmexes keep the vanilla age in step with their growth stage, so every
  `isBaby()` (the mod's, vanilla's, the client's) means what it meant.
- Pipelines on bind group layouts (as vanilla's entity_cutout / end portal), structure processors as an interface with
  `codec()` (the registry holds the codecs), `knockback(..., source, 0)`, the Fabric screen hook on `Gui.setScreen`.
- Data (`tools/port26/convert_iaf_262.py`): entity predicates as maps of sub-predicates (15 files).
- 26.2 logs every worldgen read outside the write zone: the dragon caves and roosts gave hundreds of thousands of lines
  per self-test (and built a string per read); the original's `WorldGenRegionMixin` now silences that log too (the
  reads happen as in 1.21.1).
- Tests (macOS, both loaders): selftest 29/29, client scene 0 failed checks (screenshots match 26.1.2), stress 13/13
  with SEED=1234 (NeoForge 8.7 ms average tick, Fabric 6.8 ms; generation 93-95 s), net test 0 failed checks.

## 26.3 (branch `mc/26.3`, on `mc/26.2`, 2026-10-31)
NeoForge 26.3.0.99, Fabric API 0.165.0, JEI 31.17, Rampart `1.1.0+26.3`. After `port_263_input.py` (knowledge base),
`tools/port26/port_263_iaf.py` and `port_26.py <26.3 sources>`: 556 errors in common. Write-up: modding-kb
`porting/mc-versions/26.3/ice-and-fire.md`.
- Features are an interface with `codec()` and configured features are data: the 15 features of the mod extend
  `world/gen/IafFeature` (unit codec, `place(level, generator, random, origin)` → the old body on a `Context` record
  with the names of `FeaturePlaceContext`), data in `worldgen/feature/<id>.json`.
- Blocks without codecs, `AxeItem/ShovelItem/HoeItem` → `Properties.axe/shovel/hoe`, keys on SDL codes (the numeric
  GLFW codes replaced by `InputConstants.KEY_*`), swing state (`swing(hand, SwingAnimation.DEFAULT, true)`,
  `getSwingAnimation`), loot pools in code with holders, glint on the patterned shield / trimmed armor passes
  (`setUv3` in `IafBufferSource`), dread portal shaders with `#include` and explicit locations, post effects of the
  local player, NeoForge biome modifiers with `RegistryAccess` and `ModConfig.Type.LOCAL`.
- `PotionBrewing` is gone: brewing recipes as data (`recipe/brewing`), the trade check reads the brewing recipes.
- Data (`tools/port26/convert_iaf_263.py`, from the Rats/Spartan converter): features, loot `{min, max}` shorthand →
  uniform providers, advancement entity shorthand → `entity_properties`, trades.
- Self-test has a new check: every structure template of the mod is placed and puts blocks in the world.
- Tests (macOS, both loaders): selftest 30/30, client scene 0 failed checks, stress 13/13 with SEED=1234 (NeoForge
  11.9 ms average tick, Fabric 7.5 ms), net test 0 failed checks. 26.3 servers have the white list on by default, the test
  server now turns it off.
