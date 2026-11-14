package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.util.ItemNbt;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Functional checks on a fresh dedicated server world (gradle runSelftest, tools/selftest.sh). Prints
 * "[selftest] CHECK OK/FAIL" lines and stops the server. Not part of the jar.
 */
public class SelfTest {
    private static final boolean ENABLED = Boolean.getBoolean("iceandfire.selftest");
    private static int tick = -1;
    private static int ok;
    private static int fail;
    private static final List<Entity> SPAWNED = new ArrayList<>();

    private static void check(String name, boolean result, Object detail) {
        if (result) {
            ok++;
            IceAndFire.LOGGER.info("[selftest] CHECK OK {} {}", name, detail);
        } else {
            fail++;
            IceAndFire.LOGGER.error("[selftest] CHECK FAIL {} {}", name, detail);
        }
    }

    public static void onStarted(net.minecraft.server.MinecraftServer server) {
        if (ENABLED) {
            tick = 0;
        }
    }

    public static void onTick(net.minecraft.server.MinecraftServer server) {
        if (tick < 0) {
            return;
        }
        tick++;
        ServerLevel level = server.overworld();
        try {
            if (genIndex >= 0) {
                // the schedule waits for the generation: on a slow machine it takes many more ticks
                generateStep(level);
                tick--;
                return;
            }
            if (tick == 272 && !groundBlastReady(level) && blastWait++ < 400) {
                // the forced chunks of the blasts reach entity ticking after their neighbours are loaded: slow on CI
                tick--;
                return;
            }
            if (tick == 540 && !level.isPositionEntityTicking(PARTS_CHUNK.getMiddleBlockPosition(0)) && partsWait++ < 200) {
                // the forced chunk reaches entity ticking a few ticks after its neighbours are loaded
                tick--;
                return;
            }
            if (tick == 4700 && level.hasChunk(CRYSTAL_CHUNK.x(), CRYSTAL_CHUNK.z()) && crystalWait++ < 400) {
                // the unloading of the chunk takes a few more ticks
                tick--;
                return;
            }
            switch (tick) {
                case 20 -> registries(server);
                case 30 -> groundBlastChunks(level, true);
                case 40 -> spawnAll(level);
                case 240 -> checkSpawned(level);
                case 260 -> items(level);
                case 270 -> wormLandPath(level);
                case 272 -> groundBlast(level);
                case 273 -> {
                    auditFixes(level);
                    groundBlastChunks(level, false);
                }
                case 275 -> worldDataThreads(level);
                case 280 -> features(server, level);
                case 480 -> structures(server, level);
                case 500 -> partsLoadChunk(level, true);
                case 540 -> partsSpawn(level);
                case 600 -> partsAdded(level);
                case 605 -> partsRestored(level);
                case 610 -> partsLoadChunk(level, false);
                case 700 -> partsUnloaded(level);
                case 780 -> partsReloaded(level);
                case 790 -> hivePlace(level);
                case 900 -> hiveSettled(level);
                case 901 -> hiveQueenLookup(level);
                case 902 -> crystalDragonGone(level);
                case 905 -> hiveFlood(level);
                case 1205 -> hiveAfterFlood(level);
                case 1210 -> crystalTicketAdd(level);
                case 1220 -> sprint(server);
                case 4700 -> crystalTicketExpired(level);
                case 4710 -> finish(server);
                default -> {
                }
            }
        } catch (Throwable t) {
            check("tick " + tick, false, t);
            IceAndFire.LOGGER.error("[selftest] exception", t);
        }
    }

    private static void registries(MinecraftServer server) {
        long items = BuiltInRegistries.ITEM.keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).count();
        long blocks = BuiltInRegistries.BLOCK.keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).count();
        long entities = BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).count();
        check("items registered", items > 400, items);
        check("blocks registered", blocks > 120, blocks);
        check("entity types registered", entities > 50, entities);

        long recipes = server.getRecipeManager().getRecipes().stream().filter(r -> r.id().identifier().getNamespace().equals(IceAndFire.MODID)).count();
        check("recipes loaded", recipes > 300, recipes);
        long forge = server.getRecipeManager().getRecipes().stream().filter(r -> r.value().getType() == IafRecipeRegistry.DRAGON_FORGE_TYPE.get()).count();
        check("dragon forge recipes", forge >= 3, forge);

        int loot = 0;
        int empty = 0;
        for (Identifier id : server.reloadableRegistries().lookup().lookupOrThrow(Registries.LOOT_TABLE).listElementIds().map(ResourceKey::identifier).toList()) {
            if (id.getNamespace().equals(IceAndFire.MODID)) {
                loot++;
                if (server.reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id)) == LootTable.EMPTY) {
                    empty++;
                }
            }
        }
        check("loot tables loaded", loot > 80 && empty == 0, loot + " tables, " + empty + " empty");

        long advancements = server.getAdvancements().getAllAdvancements().stream().filter(a -> a.id().getNamespace().equals(IceAndFire.MODID)).count();
        check("advancements loaded", advancements > 300, advancements);

        long patterns = server.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN).keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).count();
        check("banner patterns", patterns == 19, patterns);
    }

    private static void spawnAll(ServerLevel level) {
        int x = 0;
        int spawned = 0;
        List<String> failed = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(IceAndFire.MODID) || id.getPath().contains("multipart") || id.getPath().equals("chain_tie")) {
                continue;
            }
            BlockPos pos = new BlockPos(x, 0, 64);
            // No player on the server: keep the chunks (and the entities in them) loaded
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 1;
            Entity entity = type.create(level, EntitySpawnReason.COMMAND);
            if (entity == null) {
                failed.add(id.toString());
                continue;
            }
            entity.snapTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
                mob.setPersistenceRequired();
            }
            if (level.addFreshEntity(entity)) {
                SPAWNED.add(entity);
                spawned++;
            } else {
                failed.add(id.toString());
            }
            x += 24;
        }
        check("spawn every entity", failed.isEmpty(), spawned + " spawned, failed " + failed);
    }

    /**
     * CurseForge report 2026-11-04: a worm follows a path on its land navigation (its first tick, or with a rider) and the
     * server crashes, as the node evaluator has no mob outside a path search. A free platform high up, the worm is not added.
     */
    private static void wormLandPath(ServerLevel level) {
        BlockPos origin = new BlockPos(0, level.getMaxY() - 20, -160);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, 0, -4), origin.offset(24, 0, 4))) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        }
        var worm = IafEntityRegistry.DEATH_WORM.get().create(level, EntitySpawnReason.COMMAND);
        worm.setDeathWormScale(1F);
        worm.refreshDimensions();
        worm.snapTo(origin.getX() + 0.5, origin.getY() + 1, origin.getZ() + 0.5, 0, 0);
        worm.setOnGround(true);
        // A real search first: only after one the evaluator has a size (and no mob any more), as in the crash.
        // Its result is not used: it gives a worm this tall nothing here (OPEN costs more than WALKABLE for it), so a path by hand
        worm.getNavigation().createPath(origin.offset(5, 1, 0), 0);
        List<net.minecraft.world.level.pathfinder.Node> nodes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            nodes.add(new net.minecraft.world.level.pathfinder.Node(origin.getX() + i, origin.getY() + 1, origin.getZ()));
        }
        var path = new net.minecraft.world.level.pathfinder.Path(nodes, origin.offset(5, 1, 0), true);
        boolean moved = worm.getNavigation().moveTo(path, 1.0D);
        String detail = "height " + worm.getBbHeight() + ", path " + path;
        try {
            for (int i = 0; moved && i < 5; i++) {
                worm.getNavigation().tick();
            }
            // past the second node: the direct move check ran and let it through
            check("death worm follows a path on land", moved && path.getNextNodeIndex() >= 2, detail + ", next node " + path.getNextNodeIndex());
        } catch (RuntimeException e) {
            check("death worm follows a path on land", false, detail + ": " + e);
        }
    }

    /**
     * CurseForge reports 2026-11-05 and 2026-11-11: the bite of a death worm. A ServerExplosion computes its blocks once (with
     * Lithium a second call throws), and its explode() runs whole (PuzzlesLib's Fabric detonate event threw on a bare
     * hurtEntities()). Both cases: a blast that breaks the blocks and one that keeps them (a dragon without mob griefing),
     * each hurts the pig next to it. With -Pwith_compat (CI bug hunt) Lithium and PuzzlesLib are loaded.
     */
    private static void groundBlast(ServerLevel level) {
        groundBlast(level, new BlockPos(0, level.getMaxY() - 40, -160), net.minecraft.world.level.Explosion.BlockInteraction.DESTROY);
        groundBlast(level, new BlockPos(16, level.getMaxY() - 40, -160), net.minecraft.world.level.Explosion.BlockInteraction.KEEP);
        // released after the audit checks of the next tick (they need the entities of these chunks)
    }

    private static int blastWait;

    /** Both pigs' chunks (x 2 and 18 at z -160) are entity ticking. */
    private static boolean groundBlastReady(ServerLevel level) {
        return level.isPositionEntityTicking(new BlockPos(2, 64, -160)) && level.isPositionEntityTicking(new BlockPos(18, 64, -160));
    }

    /**
     * No player on the server: a blast finds the entities of entity-ticking chunks only (a chunk loaded by setBlock alone
     * hides its entities). Forced long before the blasts, so the tickets are through on a slow CI machine.
     */
    private static void groundBlastChunks(ServerLevel level, boolean load) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -11; z <= -10; z++) {
                level.setChunkForced(x, z, load);
            }
        }
    }

    private static void groundBlast(ServerLevel level, BlockPos center, net.minecraft.world.level.Explosion.BlockInteraction mode) {
        boolean destroy = mode == net.minecraft.world.level.Explosion.BlockInteraction.DESTROY;
        String name = destroy ? "ground blast" : "ground blast that keeps the blocks";
        // the ground under the blast (5 x 5 x 5 dirt), the blast itself in the air above it, as on a worm's target
        Iterable<BlockPos> ground = BlockPos.betweenClosed(center.offset(-2, -5, -2), center.offset(2, -1, 2));
        for (BlockPos pos : ground) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        }
        var worm = IafEntityRegistry.DEATH_WORM.get().create(level, EntitySpawnReason.COMMAND);
        worm.snapTo(center.getX() + 0.5, center.getY() + 5, center.getZ() + 0.5, 0, 0);
        // from the registry: the vanilla constants are EntityType.PIG on 26.1 and EntityTypes.PIG from 26.2
        var pig = (net.minecraft.world.entity.LivingEntity) BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace("pig")).create(level, EntitySpawnReason.COMMAND);
        pig.snapTo(center.getX() + 2.0, center.getY(), center.getZ() + 0.5, 0, 0);
        level.addFreshEntity(pig);
        try {
            com.github.alexthe666.iceandfire.entity.util.BlockLaunchExplosion.explode(level, worm, null, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 3F, mode);
            int left = 0;
            for (BlockPos pos : ground) {
                if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                    left++;
                }
            }
            check(name + (destroy ? " breaks blocks" : " leaves them"), destroy ? left < 125 : left == 125, left + " of 125 blocks left");
            check(name + " hurts a mob next to it", pig.isRemoved() || pig.getHealth() < pig.getMaxHealth(), "pig health " + pig.getHealth() + " of " + pig.getMaxHealth() + ", entity ticking " + level.isPositionEntityTicking(pig.blockPosition()) + " after " + blastWait + " ticks of waiting");
        } catch (RuntimeException e) {
            check(name + " runs", false, e);
        } finally {
            pig.discard();
        }
    }

    /** Hives and the separation of dangerous structures are written by the worldgen threads, read and saved by the server thread. */
    private static void worldDataThreads(ServerLevel level) {
        var data = com.github.alexthe666.iceandfire.world.IafWorldData.get(level);
        var hives = com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level);
        int before = hives.getHivelist().size();
        List<Throwable> errors = java.util.Collections.synchronizedList(new ArrayList<>());
        List<com.github.alexthe666.iceandfire.entity.util.MyrmexHive> added = java.util.Collections.synchronizedList(new ArrayList<>());
        var pool = java.util.concurrent.Executors.newFixedThreadPool(4);
        for (int t = 0; t < 4; t++) {
            final int thread = t;
            pool.execute(() -> {
                try {
                    for (int i = 0; i < 1500; i++) {
                        // far away: the structures of this world do not depend on these entries
                        data.check(com.github.alexthe666.iceandfire.world.IafWorldData.FeatureType.OCEAN, new BlockPos(29_000_000, 0, 29_000_000), "selftest_" + thread + "_" + i);
                        if (i % 15 == 0) {
                            var hive = new com.github.alexthe666.iceandfire.entity.util.MyrmexHive(level, new BlockPos(29_000_000, 0, i), 100);
                            added.add(hive);
                            com.github.alexthe666.iceandfire.world.MyrmexWorldData.addHive(level, hive);
                        }
                    }
                } catch (Throwable e) {
                    errors.add(e);
                }
            });
        }
        pool.shutdown();
        long deadline = System.nanoTime() + 30_000_000_000L;
        while (!pool.isTerminated() && System.nanoTime() < deadline) {
            try {
                data.save(new CompoundTag(), level.registryAccess());
                hives.getNearestHive(BlockPos.ZERO, 1);
                // as often as an autosave would dare: a save in a loop keeps the lock from the threads
                Thread.sleep(2);
            } catch (Throwable e) {
                errors.add(e);
            }
        }
        int count = hives.getHivelist().size();
        hives.getHivelist().removeAll(added);
        check("world data from worldgen threads", pool.isTerminated() && errors.isEmpty() && count == before + 400, "finished " + pool.isTerminated() + ", hives " + before + " + 400 -> " + count + ", errors " + errors.size() + (errors.isEmpty() ? "" : " first " + errors.get(0)));
    }

    /** Right-click with a myrmex staff: a queen without a hive (a spawn egg far from any hive) anywhere in the dimension threw. */
    private static void hiveQueenLookup(ServerLevel level) {
        var lone = IafEntityRegistry.MYRMEX_QUEEN.get().create(level, EntitySpawnReason.COMMAND);
        lone.snapTo(hive.getCenter().getX() + 0.5D, hive.getCenter().getY(), hive.getCenter().getZ() + 0.5D, 0, 0);
        level.addFreshEntity(lone);
        try {
            var found = hive.getQueen();
            check("hive finds its queen next to one without a hive", found == queen, found);
        } catch (RuntimeException e) {
            check("hive finds its queen next to one without a hive", false, e);
        } finally {
            lone.discard();
        }
    }

    /** A dragon that is gone for good has no last position: its crystal says so at once instead of loading that chunk for nothing. */
    private static void crystalDragonGone(ServerLevel level) {
        BlockPos top = HIVE_CHUNK.getMiddleBlockPosition(0);
        EntityDragonBase dragon = IafEntityRegistry.FIRE_DRAGON.get().create(level, EntitySpawnReason.COMMAND);
        dragon.snapTo(top.getX() + 0.5D, level.getHeight(Heightmap.Types.MOTION_BLOCKING, top.getX(), top.getZ()) + 2, top.getZ() + 0.5D, 0, 0);
        dragon.setCrystalBound(true);
        dragon.setNoAi(true);
        level.addFreshEntity(dragon);
        dragon.discard();
        BlockPos last = com.github.alexthe666.iceandfire.world.DragonPosWorldData.get(level).getDragonPos(dragon.getUUID());
        check("crystal forgets a dragon that is gone", last == null, last);
    }

    private static int crystalWait;
    private static final ChunkPos CRYSTAL_CHUNK = new ChunkPos(510, 510); // inside the generated area (a ticket counts only once the work on the chunk is over), far from the forced chunks

    /** A summon whose dragon never arrives (it moved, or a stale position): the chunk must not stay loaded until the restart. */
    private static void crystalTicketAdd(ServerLevel level) {
        level.getChunk(CRYSTAL_CHUNK.x(), CRYSTAL_CHUNK.z());
        level.getChunkSource().addTicketWithRadius(com.github.alexthe666.iceandfire.world.IafTicketTypes.SUMMONING_CRYSTAL.get(), CRYSTAL_CHUNK, 2);
        check("crystal ticket loads the chunk", level.hasChunk(CRYSTAL_CHUNK.x(), CRYSTAL_CHUNK.z()), CRYSTAL_CHUNK);
    }

    private static void sprint(MinecraftServer server) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "tick sprint 3500");
    }

    private static void crystalTicketExpired(ServerLevel level) {
        check("crystal ticket expires", !level.hasChunk(CRYSTAL_CHUNK.x(), CRYSTAL_CHUNK.z()), CRYSTAL_CHUNK);
    }

    private static void checkSpawned(ServerLevel level) {
        // 200 ticks of AI, pathfinding and multipart updates went by without crashing the server
        long alive = SPAWNED.stream().filter(Entity::isAlive).count();
        long mobs = SPAWNED.stream().filter(e -> e instanceof Mob).count();
        check("entities ticked 200 ticks", alive > 0, alive + " alive of " + SPAWNED.size() + " (" + mobs + " mobs)");
        long dragons = SPAWNED.stream().filter(e -> e instanceof EntityDragonBase && e.isAlive()).count();
        check("dragons alive", dragons == 3, dragons);

        // Entity data attachment survives saving (frozen state)
        Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        zombie.snapTo(0, 100, 0);
        EntityDataProvider.getCapability(zombie).ifPresent(data -> data.frozenData.setFrozen(zombie, 120));
        CompoundTag saved = com.github.alexthe666.iceandfire.entity.util.Values.save(zombie);
        Zombie loaded = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        com.github.alexthe666.iceandfire.entity.util.Values.load(loaded, saved);
        int frozen = EntityDataProvider.getCapability(loaded).map(data -> data.frozenData.frozenTicks).orElse(-1);
        check("entity data saved", frozen == 120, frozen);

        // Every creature dies once: death events, drops and loot tables (a null loot table crashes 1.21)
        List<String> deathFailed = new ArrayList<>();
        for (Entity entity : SPAWNED) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
                try {
                    living.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
                } catch (Throwable throwable) {
                    deathFailed.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath() + ": " + throwable);
                    IceAndFire.LOGGER.error("[selftest] death of {}", entity, throwable);
                }
            }
        }
        check("every creature can die", deathFailed.isEmpty(), deathFailed);

        SPAWNED.forEach(e -> level.setChunkForced(e.chunkPosition().x(), e.chunkPosition().z(), false));
        SPAWNED.forEach(Entity::discard);
        SPAWNED.clear();
    }

    private static void items(ServerLevel level) {
        // An item of a 1.20.1 world: the world upgrade moves its NBT into minecraft:custom_data with the same keys
        ItemStack statue = new ItemStack(IafItemRegistry.STONE_STATUE.get());
        CompoundTag old = new CompoundTag();
        old.putBoolean("IAFStoneStatuePlayerEntity", false);
        old.putString("IAFStoneStatueEntityID", "minecraft:zombie");
        statue.set(DataComponents.CUSTOM_DATA, CustomData.of(old));
        check("old item data readable", "minecraft:zombie".equals(ItemNbt.view(statue).getStringOr("IAFStoneStatueEntityID", "")), ItemNbt.view(statue));

        ItemNbt.update(statue, tag -> tag.putInt("Test", 3));
        check("item data update", ItemNbt.view(statue).getIntOr("Test", 0) == 3 && ItemNbt.view(statue).contains("IAFStoneStatueEntityID"), ItemNbt.view(statue));

        ItemStack copy = com.github.alexthe666.iceandfire.entity.util.Values.parseItem(level.registryAccess(), (CompoundTag) com.github.alexthe666.iceandfire.entity.util.Values.saveItem(level.registryAccess(), statue, new CompoundTag()));
        check("item data saved", ItemStack.isSameItemSameComponents(copy, statue), copy);

        // Every tool and armor can be repaired: a repair ingredient made of a tag nobody fills (forge:ingots/silver of
        // 1.20.1) matches nothing
        List<String> noRepair = new ArrayList<>();
        for (net.minecraft.world.item.Item item : BuiltInRegistries.ITEM) {
            if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IceAndFire.MODID)) {
                continue;
            }
            // 26.x: the materials repair with an item tag (data/iceandfire/tags/item/repairs)
            net.minecraft.tags.TagKey<net.minecraft.world.item.Item> repair = null;
            if (item instanceof com.github.alexthe666.iceandfire.item.IafTieredItem tiered) {
                repair = tiered.getTier().getRepairItems();
            } else if (item instanceof com.github.alexthe666.iceandfire.item.IafArmorItem armor) {
                repair = armor.getMaterial().vanilla().repairIngredient();
            }
            // copper, the ghost sword and the dread queen's sword have no repair item in the original (empty tags)
            if (repair != null && !java.util.Set.of("repairs/copper", "repairs/ghostsword", "repairs/dread_queen").contains(repair.location().getPath())
                && !BuiltInRegistries.ITEM.getTagOrEmpty(repair).iterator().hasNext()) {
                noRepair.add(BuiltInRegistries.ITEM.getKey(item).getPath());
            }
        }
        check("repair ingredients not empty", noRepair.isEmpty(), noRepair);
    }

    private static Entity vanilla(ServerLevel level, String id) {
        // 26.2+ moved the vanilla entity type constants: through the registry on every version
        return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.withDefaultNamespace(id)).create(level, EntitySpawnReason.COMMAND);
    }

    /**
     * Fixes of the code audit of 2026-11-14 (beta.12): each check failed on the code before them. Runs in the forced
     * chunks of the ground blasts (entity ticking since tick 272, released right after), 60 blocks below the top of the
     * world: in a chunk that is not entity ticking the gorgon head finds no mob at all.
     */
    private static void auditFixes(ServerLevel level) {
        MinecraftServer server = level.getServer();
        net.minecraft.server.level.ServerPlayer player = new net.minecraft.server.level.ServerPlayer(server, level,
            new com.mojang.authlib.GameProfile(UUID.randomUUID(), "iaf_selftest"), net.minecraft.server.level.ClientInformation.createDefault());
        int y = level.getMaxY() - 60;
        BlockPos base = new BlockPos(-12, y, -150);
        List<Entity> made = new ArrayList<>();
        try {
            // podium and lectern: the menu is valid near its block entity only (an item came back with the chunk while
            // the open menu took it out of the unloaded block entity)
            for (net.minecraft.world.level.block.Block block : List.of(com.github.alexthe666.iceandfire.block.IafBlockRegistry.PODIUM_OAK.get(), com.github.alexthe666.iceandfire.block.IafBlockRegistry.LECTERN.get())) {
                String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
                level.setBlockAndUpdate(base, block.defaultBlockState());
                if (!(level.getBlockEntity(base) instanceof net.minecraft.world.Container container)) {
                    check(name + " menu only near its block", false, level.getBlockEntity(base));
                    continue;
                }
                player.setPos(base.getX() + 0.5, y, base.getZ() + 2.5);
                boolean near = container.stillValid(player);
                player.setPos(base.getX() + 0.5, y, base.getZ() + 40.5);
                boolean far = container.stillValid(player);
                level.removeBlock(base, false);
                player.setPos(base.getX() + 0.5, y, base.getZ() + 2.5);
                boolean gone = container.stillValid(player);
                check(name + " menu only near its block", near && !far && !gone, "near " + near + ", 40 blocks away " + far + ", block removed " + gone);
            }

            // siren: data without a charm asks for no client sync (the whole entity data went to the player and everyone
            // tracking it every tick)
            var villager = (net.minecraft.world.entity.LivingEntity) vanilla(level, "villager");
            var siren = EntityDataProvider.getCapability(villager).orElseThrow().sirenData;
            siren.doesClientNeedUpdate();
            siren.tickCharmed(villager);
            boolean first = siren.doesClientNeedUpdate();
            siren.tickCharmed(villager);
            boolean second = siren.doesClientNeedUpdate();
            check("siren data without a charm asks for no sync", !first && !second, "tick 1 " + first + ", tick 2 " + second);

            // cockatrice scepter: its targets reach the client (written next to miscData, read from it)
            var misc = EntityDataProvider.getCapability(villager).orElseThrow().miscData;
            misc.addScepterTarget((net.minecraft.world.entity.LivingEntity) vanilla(level, "pig"));
            CompoundTag miscTag = new CompoundTag();
            misc.serialize(miscTag);
            check("scepter targets in the synced data", miscTag.getCompoundOrEmpty("miscData").getIntArray("targetedByScepterIds").map(ids -> ids.length == 1).orElse(false), miscTag);

            // a mob that attacks gets no entity data of the mod (it was made, saved and ticked for every attacker)
            var attacked = (net.minecraft.world.entity.LivingEntity) vanilla(level, "pig");
            attacked.setPos(base.getX() + 0.5, y, base.getZ() - 3.5);
            level.addFreshEntity(attacked);
            made.add(attacked);
            var attacker = (net.minecraft.world.entity.LivingEntity) vanilla(level, "zombie");
            attacker.setPos(base.getX() + 1.5, y, base.getZ() - 3.5);
            attacked.hurtServer(level, level.damageSources().mobAttack(attacker), 1);
            check("an attacker gets no entity data", EntityDataProvider.getExisting(attacker).isEmpty() && attacked.getHealth() < attacked.getMaxHealth(), "attacker data " + EntityDataProvider.getExisting(attacker).isPresent() + ", health " + attacked.getHealth());

            // gorgon head: no stone through a wall (it removed tamed dragons and pets behind walls); without the wall it
            // still turns the mob to stone
            BlockPos wall = base.offset(3, 1, 0);
            for (boolean walled : new boolean[]{true, false}) {
                if (walled) {
                    level.setBlockAndUpdate(wall, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
                } else {
                    level.removeBlock(wall, false);
                }
                var mob = (net.minecraft.world.entity.LivingEntity) vanilla(level, "pig");
                mob.setPos(base.getX() + 6.5, y + 1, base.getZ() + 0.5);
                level.addFreshEntity(mob);
                made.add(mob);
                player.setPos(base.getX() + 0.5, y, base.getZ() + 0.5);
                // a living entity looks where its head points (getViewYRot is yHeadRot): the body yaw alone looked along +z
                player.setYRot(-90);
                player.setYHeadRot(-90);
                player.setXRot(0);
                IafItemRegistry.GORGON_HEAD.get().releaseUsing(new ItemStack(IafItemRegistry.GORGON_HEAD.get()), level, player, 0);
                net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(base).inflate(10);
                List<com.github.alexthe666.iceandfire.entity.EntityStoneStatue> statues = level.getEntitiesOfClass(com.github.alexthe666.iceandfire.entity.EntityStoneStatue.class, box);
                made.addAll(statues);
                boolean stone = mob.isRemoved() && !statues.isEmpty();
                check(walled ? "gorgon head not through a wall" : "gorgon head turns a mob to stone", walled != stone, "removed " + mob.isRemoved() + ", statues " + statues.size());
                statues.forEach(Entity::discard);
            }

            // hippocampus: a chest put in through the menu opens the storage at once (its slots threw and ate the items)
            var hippo = IafEntityRegistry.HIPPOCAMPUS.get().create(level, EntitySpawnReason.COMMAND);
            hippo.inventory.setItem(com.github.alexthe666.iceandfire.entity.EntityHippocampus.INV_SLOT_CHEST, new ItemStack(net.minecraft.world.level.block.Blocks.CHEST));
            check("hippocampus storage right after a chest", hippo.isChested() && hippo.inventory.getContainerSize() == 18, "chested " + hippo.isChested() + ", slots " + hippo.inventory.getContainerSize());

            // world data with an entry without its UUID loads and saves (NPE on every autosave)
            CompoundTag dragons = new CompoundTag();
            net.minecraft.nbt.ListTag dragonMap = new net.minecraft.nbt.ListTag();
            CompoundTag noUuid = new CompoundTag();
            noUuid.putInt("DragonPosX", 1);
            dragonMap.add(noUuid);
            dragons.put("DragonMap", dragonMap);
            check("dragon positions without a UUID save", saves(() -> new com.github.alexthe666.iceandfire.world.DragonPosWorldData().load(dragons).save(new CompoundTag(), level.registryAccess())), dragons);
            CompoundTag hiveTag = new CompoundTag();
            net.minecraft.nbt.ListTag members = new net.minecraft.nbt.ListTag();
            members.add(new CompoundTag());
            hiveTag.put("HiveMembers", members);
            var loadedHive = com.github.alexthe666.iceandfire.entity.util.MyrmexHive.fromNBT(hiveTag);
            check("hive without UUIDs saves", loadedHive.hiveUUID != null && saves(loadedHive::toNBT), "hive " + loadedHive.hiveUUID);

            // hive: the wander radius is saved (read with the UUID codec, never loaded)
            var wander = new com.github.alexthe666.iceandfire.entity.util.MyrmexHive();
            wander.setWanderRadius(30);
            int wanderLoaded = com.github.alexthe666.iceandfire.entity.util.MyrmexHive.fromNBT(wander.toNBT()).getWanderRadius();
            check("hive wander radius saved", wanderLoaded == 30, wanderLoaded);

            // staff edits of a hive: one room per edit (a client could send thousands, merged on the main thread)
            player.setPos(base.getX() + 0.5, y, base.getZ() + 0.5);
            var real = new com.github.alexthe666.iceandfire.entity.util.MyrmexHive();
            int many = applyRooms(real, player, base, 1000);
            check("hive edit with a thousand rooms refused", many == 0, many + " food rooms");
            int one = applyRooms(real, player, base, 1);
            check("hive edit with one room applied", one == 1, one + " food rooms");

            // pathfinding: the configured number of threads (a pool with an unbounded queue never grew past one)
            int threads = com.github.alexthe666.iceandfire.pathfinding.raycoms.Pathfinding.getExecutor().getCorePoolSize();
            int expected = Math.max(1, Math.min(com.github.alexthe666.iceandfire.IafConfig.dragonPathfindingThreads, Runtime.getRuntime().availableProcessors()));
            check("pathfinding threads", threads == expected, threads + " of " + expected);
        } finally {
            made.forEach(Entity::discard);
            level.removeBlock(base.offset(3, 1, 0), false);
        }
    }

    private static boolean saves(Runnable save) {
        try {
            save.run();
            return true;
        } catch (RuntimeException e) {
            IceAndFire.LOGGER.error("[selftest] save failed", e);
            return false;
        }
    }

    /** A staff edit of the hive from the client that adds {@code count} food rooms near the player; returns its food rooms (all rooms count the center too). */
    private static int applyRooms(com.github.alexthe666.iceandfire.entity.util.MyrmexHive real, net.minecraft.world.entity.player.Player player, BlockPos base, int count) {
        CompoundTag edit = real.toNBT();
        net.minecraft.nbt.ListTag rooms = new net.minecraft.nbt.ListTag();
        for (int i = 0; i < count; i++) {
            CompoundTag room = new CompoundTag();
            room.putInt("X", base.getX() + i % 10);
            room.putInt("Y", base.getY() - 2 - i / 100);
            room.putInt("Z", base.getZ() + (i / 10) % 10);
            rooms.add(room);
        }
        edit.put("FoodRooms", rooms);
        real.applyStaffEdits(com.github.alexthe666.iceandfire.entity.util.MyrmexHive.fromNBT(edit), player);
        return real.getRooms(com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive.RoomType.FOOD).size();
    }

    private static final int GEN_RADIUS = 20;
    private static int genIndex = -1;

    /**
     * Real world generation of 40 x 40 chunks far from spawn, 16 chunks per tick: the features of the mod are placed by
     * the biome modifier as in a normal world (placing them in a finished world logs "Unprimed heightmap").
     */
    private static void features(MinecraftServer server, ServerLevel level) {
        genIndex = 0;
    }

    private static void generateStep(ServerLevel level) {
        int side = GEN_RADIUS * 2;
        int total = side * side;
        // up to 16 chunks, at most ~2 s per tick: a CI runner with 2 cores needs over a minute for 16 fresh chunks far
        // from spawn, and the watchdog (60 s, kept on to catch real hangs) stopped the server there
        long start = System.nanoTime();
        for (int n = 0; n < 16 && genIndex < total && System.nanoTime() - start < 2_000_000_000L; n++, genIndex++) {
            int cx = 500 + genIndex % side - GEN_RADIUS;
            int cz = 500 + genIndex / side - GEN_RADIUS;
            long chunkStart = System.nanoTime();
            level.getChunk(cx, cz);
            long ms = (System.nanoTime() - chunkStart) / 1_000_000L;
            if (ms > 5000) {
                IceAndFire.LOGGER.warn("[selftest] slow chunk {} {}: {} ms, structures {}", cx, cz, ms,
                    level.structureManager().startsForStructure(new net.minecraft.world.level.ChunkPos(cx, cz), structure -> true).size());
            }
        }
        if (genIndex >= total) {
            genIndex = -1;
            check("world generation " + total + " chunks", true, "no crash");
        }
    }

    /** Locates every structure of the mod (this runs their biome checks) and places it there. */
    private static void structures(MinecraftServer server, ServerLevel level) {
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput();
        for (String name : List.of("gorgon_temple", "graveyard", "mausoleum")) {
            var holder = registry.getOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name)));
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, net.minecraft.core.HolderSet.direct(holder), new BlockPos(0, 64, 0), 150, false);
            if (found == null) {
                check("structure " + name + " located", false, "none within 150 chunks");
                continue;
            }
            BlockPos pos = found.getFirst();
            String biome = level.getBiome(pos.atY(level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()))).unwrapKey().map(k -> k.identifier().toString()).orElse("?");
            check("structure " + name + " located", true, pos + " in " + biome);
        }
    }

    /** Inside the area generated by the world generation check: entity ticking right after the ticket. */
    private static final ChunkPos PARTS_CHUNK = new ChunkPos(500, 500);
    private static int partsWait;
    private static final java.util.Map<UUID, String> STATUES = new java.util.LinkedHashMap<>();
    private static UUID partsDragon;
    private static EntityDragonPart movedPart;
    private static EntityDragonPart oldWorldPart;

    private static void partsLoadChunk(ServerLevel level, boolean load) {
        level.setChunkForced(PARTS_CHUNK.x(), PARTS_CHUNK.z(), load);
    }

    private static List<EntityDragonPart> parts(ServerLevel level, Entity dragon) {
        return level.getEntitiesOfClass(EntityDragonPart.class, dragon.getBoundingBox().inflate(48), part -> part.getParent() == dragon);
    }

    /** An adult dragon in a chunk without players, parts of #5089 / #5360. */
    private static void partsSpawn(ServerLevel level) {
        BlockPos pos = PARTS_CHUNK.getMiddleBlockPosition(0);
        check("parts chunk entity ticking", level.isPositionEntityTicking(pos), PARTS_CHUNK);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 2;
        EntityDragonBase dragon = IafEntityRegistry.FIRE_DRAGON.get().create(level, EntitySpawnReason.COMMAND);
        dragon.snapTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
        dragon.setAgeInDays(75);
        dragon.setNoAi(true);
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
        dragon.setCrystalBound(true);
        partsDragon = dragon.getUUID();

        // A part saved with the chunk by 1.20.1: loaded without its owner
        oldWorldPart = IafEntityRegistry.DRAGON_MULTIPART.get().create(level, EntitySpawnReason.COMMAND);
        oldWorldPart.snapTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(oldWorldPart);

        // Stone statues saved with the chunk and loaded again (CurseForge report 2026-11-01: a world with statues
        // would not load); a troll's own, a gorgon's victims
        STATUES.clear();
        int k = 0;
        for (EntityType<?> type : List.of(net.minecraft.world.entity.EntityTypes.ZOMBIE, net.minecraft.world.entity.EntityTypes.VILLAGER, IafEntityRegistry.TROLL.get(), IafEntityRegistry.GORGON.get())) {
            Entity source = type.create(level, EntitySpawnReason.COMMAND);
            source.snapTo(pos.getX() + 0.5 - 6 + k++ * 3, y, pos.getZ() + 4.5, 0, 0);
            com.github.alexthe666.iceandfire.entity.EntityStoneStatue statue = com.github.alexthe666.iceandfire.entity.EntityStoneStatue.buildStatueEntity((net.minecraft.world.entity.LivingEntity) source);
            statue.absSnapTo(source.getX(), source.getY(), source.getZ(), 0, 0);
            level.addFreshEntity(statue);
            STATUES.put(statue.getUUID(), statue.getTrappedEntityTypeString());
        }
    }

    private static void partsAdded(ServerLevel level) {
        Entity dragon = level.getEntity(partsDragon);
        List<EntityDragonPart> parts = dragon == null ? List.of() : parts(level, dragon);
        check("dragon parts added", parts.size() == 10, parts.size());
        check("part of an old world removed", oldWorldPart.isRemoved(), oldWorldPart.getRemovalReason());
        if (!parts.isEmpty()) {
            // Pushed into a section without entity access: before the fix the dragon re-added it every tick
            movedPart = parts.get(0);
            movedPart.setPos(movedPart.getX() + 160, movedPart.getY(), movedPart.getZ());
        }
    }

    private static void partsRestored(ServerLevel level) {
        Entity dragon = level.getEntity(partsDragon);
        boolean back = dragon != null && movedPart != null && level.getEntity(movedPart.getUUID()) == movedPart && movedPart.distanceTo(dragon) < 32;
        check("part in a hidden section comes back", back, movedPart == null ? null : movedPart.position());
    }

    private static void partsUnloaded(ServerLevel level) {
        check("dragon chunk unloaded", level.getEntity(partsDragon) == null, PARTS_CHUNK);
        check("crystal remembers an unloaded dragon", com.github.alexthe666.iceandfire.world.DragonPosWorldData.get(level).getDragonPos(partsDragon) != null, partsDragon);
        check("statues unloaded", STATUES.keySet().stream().allMatch(id -> level.getEntity(id) == null), STATUES.values());
        level.setChunkForced(PARTS_CHUNK.x(), PARTS_CHUNK.z(), true);
    }

    private static void partsReloaded(ServerLevel level) {
        Entity dragon = level.getEntity(partsDragon);
        List<EntityDragonPart> parts = dragon == null ? List.of() : parts(level, dragon);
        long all = dragon == null ? 0 : level.getEntitiesOfClass(EntityDragonPart.class, dragon.getBoundingBox().inflate(48)).size();
        check("dragon parts after chunk reload", dragon != null && parts.size() == 10 && all == 10, parts.size() + " owned, " + all + " near");
        List<String> statues = new ArrayList<>();
        STATUES.forEach((id, type) -> {
            if (level.getEntity(id) instanceof com.github.alexthe666.iceandfire.entity.EntityStoneStatue statue
                && statue.getTrappedEntityTypeString().equals(type) && !statue.getTrappedTag().isEmpty()) {
                statues.add(type);
            }
        });
        check("statues after chunk reload", statues.size() == STATUES.size(), statues + " of " + STATUES.values());
        level.setChunkForced(PARTS_CHUNK.x(), PARTS_CHUNK.z(), false);
    }

    /** Hive of #5197: the queen dug a new nest and left her hive whenever she "saw the sky", also through water. */
    private static final ChunkPos HIVE_CHUNK = new ChunkPos(490, 490);
    private static com.github.alexthe666.iceandfire.entity.util.MyrmexHive hive;
    private static com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen queen;
    private static int hivesBefore;

    private static void hiveArea(ServerLevel level, boolean load) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setChunkForced(HIVE_CHUNK.x() + x, HIVE_CHUNK.z() + z, load);
            }
        }
    }

    private static void hivePlace(ServerLevel level) {
        hiveArea(level, true);
        BlockPos top = HIVE_CHUNK.getMiddleBlockPosition(0);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, top.getX(), top.getZ());
        BlockPos center = new BlockPos(top.getX(), Math.min(surface, level.getSeaLevel()) - 25, top.getZ());
        // Built in a finished world as a queen digs it (small: the surface heightmaps of the world, not of worldgen),
        // the queen placed as the worldgen does
        var gen = new com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive(true, false, net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration.CODEC);
        gen.placeSmallGen(level, level.getRandom(), center);
        hive = gen.hive;
        var newQueen = IafEntityRegistry.MYRMEX_QUEEN.get().create(level, EntitySpawnReason.COMMAND);
        BlockPos ground = com.github.alexthe666.iceandfire.entity.util.MyrmexHive.getGroundedPos(level, center);
        newQueen.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.CHUNK_GENERATION, null);
        newQueen.setHive(hive);
        newQueen.snapTo(ground.getX() + 0.5D, ground.getY() + 1D, ground.getZ() + 0.5D, 0, 0);
        level.addFreshEntity(newQueen);
        hivesBefore = com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level).getHivelist().size();
        IceAndFire.LOGGER.info("[selftest] hive at {} (surface {})", center, surface);
    }

    private static com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen findQueen(ServerLevel level) {
        return level.getEntitiesOfClass(com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen.class, new net.minecraft.world.phys.AABB(hive.getCenter()).inflate(64))
            .stream().filter(q -> q.getHive() == hive).findFirst().orElse(null);
    }

    private static String queenState(ServerLevel level) {
        if (queen == null) {
            return "no queen";
        }
        var data = com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level);
        return "alive " + queen.isAlive() + ", own hive " + (queen.getHive() == hive) + ", " + String.format("%.1f", Math.sqrt(queen.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(hive.getCenter()))))
            + " blocks from the center, hives " + hivesBefore + " -> " + data.getHivelist().size() + ", sees the sky " + queen.canSeeSky();
    }

    private static void hiveSettled(ServerLevel level) {
        queen = findQueen(level);
        check("myrmex queen in her hive", queen != null && queen.getHive() == hive && queen.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(hive.getCenter())) < 20 * 20, queenState(level));
    }

    /** A water entrance: a shaft from the queen up, water to the sea level, open sky above. */
    private static void hiveFlood(ServerLevel level) {
        if (queen == null) {
            return;
        }
        BlockPos base = queen.blockPosition();
        for (int y = base.getY() + 1; y < level.getMaxY(); y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos pos = new BlockPos(base.getX() + x, y, base.getZ() + z);
                    boolean water = y >= base.getY() + 5 && y < level.getSeaLevel();
                    level.setBlock(pos, water ? net.minecraft.world.level.block.Blocks.WATER.defaultBlockState() : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        IceAndFire.LOGGER.info("[selftest] water shaft above the queen: {}", queenState(level));
    }

    private static void hiveAfterFlood(ServerLevel level) {
        check("myrmex queen stays in her hive under a water entrance (#5197)", queen != null && queen.isAlive() && queen.getHive() == hive
            && queen.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(hive.getCenter())) < 20 * 20 && com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level).getHivelist().size() == hivesBefore, queenState(level));
        hiveArea(level, false);
    }

    private static void finish(MinecraftServer server) {
        IceAndFire.LOGGER.info("[selftest] DONE ok={} fail={}", ok, fail);
        tick = -1;
        server.halt(false);
    }
}
