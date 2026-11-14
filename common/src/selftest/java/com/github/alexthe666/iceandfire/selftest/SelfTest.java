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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
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
            if (tick == 273 && !level.isPositionEntityTicking(AUDIT_CHUNK.getMiddleBlockPosition(64)) && auditWait++ < 400) {
                // the audit checks need the entities of their chunk: entity ticking, not only loaded
                tick--;
                return;
            }
            if (tick == 540 && !level.isPositionEntityTicking(PARTS_CHUNK.getMiddleBlockPosition(0)) && partsWait++ < 200) {
                // the forced chunk reaches entity ticking a few ticks after its neighbours are loaded
                tick--;
                return;
            }
            if (tick == 4700 && level.hasChunk(CRYSTAL_CHUNK.x, CRYSTAL_CHUNK.z) && crystalWait++ < 400) {
                // the unloading of the chunk takes a few more ticks
                tick--;
                return;
            }
            switch (tick) {
                case 20 -> registries(server);
                case 40 -> spawnAll(level);
                case 240 -> checkSpawned(level);
                case 250 -> level.setChunkForced(AUDIT_CHUNK.x, AUDIT_CHUNK.z, true);
                case 260 -> items(level);
                case 270 -> wormLandPath(level);
                case 272 -> groundBlast(level);
                case 273 -> {
                    auditFixes(level);
                    level.setChunkForced(AUDIT_CHUNK.x, AUDIT_CHUNK.z, false);
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

        long recipes = server.getRecipeManager().getRecipes().stream().filter(r -> r.id().getNamespace().equals(IceAndFire.MODID)).count();
        check("recipes loaded", recipes > 300, recipes);
        long forge = server.getRecipeManager().getAllRecipesFor(IafRecipeRegistry.DRAGON_FORGE_TYPE.get()).size();
        check("dragon forge recipes", forge >= 3, forge);

        int loot = 0;
        int empty = 0;
        for (ResourceLocation id : server.reloadableRegistries().getKeys(Registries.LOOT_TABLE)) {
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

        long patterns = server.registryAccess().registryOrThrow(Registries.BANNER_PATTERN).keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).count();
        check("banner patterns", patterns == 19, patterns);
    }

    private static void spawnAll(ServerLevel level) {
        int x = 0;
        int spawned = 0;
        List<String> failed = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(IceAndFire.MODID) || id.getPath().contains("multipart") || id.getPath().equals("chain_tie")) {
                continue;
            }
            BlockPos pos = new BlockPos(x, 0, 64);
            // No player on the server: keep the chunks (and the entities in them) loaded
            level.setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 1;
            Entity entity = type.create(level);
            if (entity == null) {
                failed.add(id.toString());
                continue;
            }
            entity.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
            if (entity instanceof Mob mob) {
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);
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
        BlockPos origin = new BlockPos(0, level.getMaxBuildHeight() - 21, -160);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-4, 0, -4), origin.offset(24, 0, 4))) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
        }
        var worm = IafEntityRegistry.DEATH_WORM.get().create(level);
        worm.setDeathWormScale(1F);
        worm.refreshDimensions();
        worm.moveTo(origin.getX() + 0.5, origin.getY() + 1, origin.getZ() + 0.5, 0, 0);
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

    /** CurseForge report 2026-11-05: the bite of a death worm. A ServerExplosion computes its blocks once: with Lithium a second call throws. */
    private static void groundBlast(ServerLevel level) {
        BlockPos center = new BlockPos(0, level.getMaxBuildHeight() - 41, -160);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.DIRT.defaultBlockState());
        }
        var worm = IafEntityRegistry.DEATH_WORM.get().create(level);
        worm.moveTo(center.getX() + 0.5, center.getY() + 5, center.getZ() + 0.5, 0, 0);
        try {
            var explosion = new com.github.alexthe666.iceandfire.entity.util.BlockLaunchExplosion(level, worm, center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5, 3F);
            explosion.explode();
            explosion.finalizeExplosion(true);
            int left = 0;
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
                if (level.getBlockState(pos).is(net.minecraft.world.level.block.Blocks.DIRT)) {
                    left++;
                }
            }
            check("ground blast breaks blocks", left < 125, left + " of 125 blocks left");
        } catch (RuntimeException e) {
            check("ground blast breaks blocks", false, e);
        }
    }

    private static final ChunkPos AUDIT_CHUNK = new ChunkPos(-1, -10);
    private static int auditWait;

    /**
     * Fixes of the code audit of 2026-11-14 (beta.12): each check failed on the code before them. In its own forced chunk
     * (forced at tick 250, released right after), 60 blocks below the top of the world: in a chunk that is not entity
     * ticking the gorgon head finds no mob at all (the chunks of spawnAll are released at tick 240).
     */
    private static void auditFixes(ServerLevel level) {
        MinecraftServer server = level.getServer();
        net.minecraft.server.level.ServerPlayer player = new net.minecraft.server.level.ServerPlayer(server, level,
            new com.mojang.authlib.GameProfile(UUID.randomUUID(), "iaf_selftest"), net.minecraft.server.level.ClientInformation.createDefault());
        int y = level.getMaxBuildHeight() - 60;
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
            var villager = EntityType.VILLAGER.create(level);
            var siren = EntityDataProvider.getCapability(villager).orElseThrow().sirenData;
            siren.doesClientNeedUpdate();
            siren.tickCharmed(villager);
            boolean first = siren.doesClientNeedUpdate();
            siren.tickCharmed(villager);
            boolean second = siren.doesClientNeedUpdate();
            check("siren data without a charm asks for no sync", !first && !second, "tick 1 " + first + ", tick 2 " + second);

            // cockatrice scepter: its targets reach the client (written next to miscData, read from it)
            var misc = EntityDataProvider.getCapability(villager).orElseThrow().miscData;
            misc.addScepterTarget(EntityType.PIG.create(level));
            CompoundTag miscTag = new CompoundTag();
            misc.serialize(miscTag);
            check("scepter targets in the synced data", miscTag.getCompound("miscData").getIntArray("targetedByScepterIds").length == 1, miscTag);

            // a mob that attacks gets no entity data of the mod (it was made, saved and ticked for every attacker)
            var attacked = EntityType.PIG.create(level);
            attacked.setPos(base.getX() + 0.5, y, base.getZ() - 3.5);
            level.addFreshEntity(attacked);
            made.add(attacked);
            var attacker = EntityType.ZOMBIE.create(level);
            attacker.setPos(base.getX() + 1.5, y, base.getZ() - 3.5);
            attacked.hurt(level.damageSources().mobAttack(attacker), 1);
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
                var mob = EntityType.PIG.create(level);
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
            var hippo = IafEntityRegistry.HIPPOCAMPUS.get().create(level);
            hippo.inventory.setItem(com.github.alexthe666.iceandfire.entity.EntityHippocampus.INV_SLOT_CHEST, new ItemStack(net.minecraft.world.level.block.Blocks.CHEST));
            check("hippocampus storage right after a chest", hippo.isChested() && hippo.inventory.getContainerSize() == 18, "chested " + hippo.isChested() + ", slots " + hippo.inventory.getContainerSize());

            // world data with an entry without its UUID loads and saves (1.21.1: getUUID threw while loading the world)
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
            var hiveId = new java.util.concurrent.atomic.AtomicReference<UUID>();
            boolean hiveSaves = saves(() -> {
                var loadedHive = com.github.alexthe666.iceandfire.entity.util.MyrmexHive.fromNBT(hiveTag);
                hiveId.set(loadedHive.hiveUUID);
                loadedHive.toNBT();
            });
            check("hive without UUIDs saves", hiveSaves && hiveId.get() != null, "hive " + hiveId.get());

            // hive: the wander radius is saved (read as a UUID, never loaded)
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
        var lone = IafEntityRegistry.MYRMEX_QUEEN.get().create(level);
        lone.moveTo(hive.getCenter().getX() + 0.5D, hive.getCenter().getY(), hive.getCenter().getZ() + 0.5D, 0, 0);
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
        EntityDragonBase dragon = IafEntityRegistry.FIRE_DRAGON.get().create(level);
        dragon.moveTo(top.getX() + 0.5D, level.getHeight(Heightmap.Types.MOTION_BLOCKING, top.getX(), top.getZ()) + 2, top.getZ() + 0.5D, 0, 0);
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
        level.getChunk(CRYSTAL_CHUNK.x, CRYSTAL_CHUNK.z);
        level.getChunkSource().addRegionTicket(com.github.alexthe666.iceandfire.item.ItemSummoningCrystal.CHUNK_TICKETS, CRYSTAL_CHUNK, 2, CRYSTAL_CHUNK);
        check("crystal ticket loads the chunk", level.hasChunk(CRYSTAL_CHUNK.x, CRYSTAL_CHUNK.z), CRYSTAL_CHUNK);
    }

    private static void sprint(MinecraftServer server) {
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "tick sprint 3500");
    }

    private static void crystalTicketExpired(ServerLevel level) {
        check("crystal ticket expires", !level.hasChunk(CRYSTAL_CHUNK.x, CRYSTAL_CHUNK.z), CRYSTAL_CHUNK);
    }

    private static void checkSpawned(ServerLevel level) {
        // 200 ticks of AI, pathfinding and multipart updates went by without crashing the server
        long alive = SPAWNED.stream().filter(Entity::isAlive).count();
        long mobs = SPAWNED.stream().filter(e -> e instanceof Mob).count();
        check("entities ticked 200 ticks", alive > 0, alive + " alive of " + SPAWNED.size() + " (" + mobs + " mobs)");
        long dragons = SPAWNED.stream().filter(e -> e instanceof EntityDragonBase && e.isAlive()).count();
        check("dragons alive", dragons == 3, dragons);

        // Entity data attachment survives saving (frozen state)
        Zombie zombie = EntityType.ZOMBIE.create(level);
        zombie.moveTo(0, 100, 0);
        EntityDataProvider.getCapability(zombie).ifPresent(data -> data.frozenData.setFrozen(zombie, 120));
        CompoundTag saved = zombie.saveWithoutId(new CompoundTag());
        Zombie loaded = EntityType.ZOMBIE.create(level);
        loaded.load(saved);
        int frozen = EntityDataProvider.getCapability(loaded).map(data -> data.frozenData.frozenTicks).orElse(-1);
        check("entity data saved", frozen == 120, frozen);

        // Every creature dies once: death events, drops and loot tables (a null loot table crashes 1.21)
        List<String> deathFailed = new ArrayList<>();
        for (Entity entity : SPAWNED) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity living && living.isAlive()) {
                try {
                    living.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
                } catch (Throwable throwable) {
                    deathFailed.add(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath() + ": " + throwable);
                    IceAndFire.LOGGER.error("[selftest] death of {}", entity, throwable);
                }
            }
        }
        check("every creature can die", deathFailed.isEmpty(), deathFailed);

        SPAWNED.forEach(e -> level.setChunkForced(e.chunkPosition().x, e.chunkPosition().z, false));
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
        check("old item data readable", "minecraft:zombie".equals(ItemNbt.view(statue).getString("IAFStoneStatueEntityID")), ItemNbt.view(statue));

        ItemNbt.update(statue, tag -> tag.putInt("Test", 3));
        check("item data update", ItemNbt.view(statue).getInt("Test") == 3 && ItemNbt.view(statue).contains("IAFStoneStatueEntityID"), ItemNbt.view(statue));

        ItemStack copy = ItemStack.parseOptional(level.registryAccess(), (CompoundTag) statue.save(level.registryAccess()));
        check("item data saved", ItemStack.isSameItemSameComponents(copy, statue), copy);

        // Every tool and armor can be repaired: a repair ingredient made of a tag nobody fills (forge:ingots/silver of
        // 1.20.1) matches nothing
        List<String> noRepair = new ArrayList<>();
        for (net.minecraft.world.item.Item item : BuiltInRegistries.ITEM) {
            if (!BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IceAndFire.MODID)) {
                continue;
            }
            net.minecraft.world.item.crafting.Ingredient repair = null;
            if (item instanceof net.minecraft.world.item.TieredItem tiered) {
                repair = tiered.getTier().getRepairIngredient();
            } else if (item instanceof net.minecraft.world.item.ArmorItem armor) {
                repair = armor.getMaterial().value().repairIngredient().get();
            }
            if (repair != null && repair != net.minecraft.world.item.crafting.Ingredient.EMPTY && repair.isEmpty()) {
                noRepair.add(BuiltInRegistries.ITEM.getKey(item).getPath());
            }
        }
        check("repair ingredients not empty", noRepair.isEmpty(), noRepair);
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
        var registry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput().withPermission(4);
        for (String name : List.of("gorgon_temple", "graveyard", "mausoleum")) {
            var holder = registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, name)));
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, net.minecraft.core.HolderSet.direct(holder), new BlockPos(0, 64, 0), 150, false);
            if (found == null) {
                check("structure " + name + " located", false, "none within 150 chunks");
                continue;
            }
            BlockPos pos = found.getFirst();
            String biome = level.getBiome(pos.atY(level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ()))).unwrapKey().map(k -> k.location().toString()).orElse("?");
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
        level.setChunkForced(PARTS_CHUNK.x, PARTS_CHUNK.z, load);
    }

    private static List<EntityDragonPart> parts(ServerLevel level, Entity dragon) {
        return level.getEntitiesOfClass(EntityDragonPart.class, dragon.getBoundingBox().inflate(48), part -> part.getParent() == dragon);
    }

    /** An adult dragon in a chunk without players, parts of #5089 / #5360. */
    private static void partsSpawn(ServerLevel level) {
        BlockPos pos = PARTS_CHUNK.getMiddleBlockPosition(0);
        check("parts chunk entity ticking", level.isPositionEntityTicking(pos), PARTS_CHUNK);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 2;
        EntityDragonBase dragon = IafEntityRegistry.FIRE_DRAGON.get().create(level);
        dragon.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
        dragon.setAgeInDays(75);
        dragon.setNoAi(true);
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
        dragon.setCrystalBound(true);
        partsDragon = dragon.getUUID();

        // A part saved with the chunk by 1.20.1: loaded without its owner
        oldWorldPart = IafEntityRegistry.DRAGON_MULTIPART.get().create(level);
        oldWorldPart.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(oldWorldPart);

        // Stone statues saved with the chunk and loaded again (CurseForge report 2026-11-01: a world with statues
        // would not load); a troll's own, a gorgon's victims
        STATUES.clear();
        int k = 0;
        for (EntityType<?> type : List.of(net.minecraft.world.entity.EntityType.ZOMBIE, net.minecraft.world.entity.EntityType.VILLAGER, IafEntityRegistry.TROLL.get(), IafEntityRegistry.GORGON.get())) {
            Entity source = type.create(level);
            source.moveTo(pos.getX() + 0.5 - 6 + k++ * 3, y, pos.getZ() + 4.5, 0, 0);
            com.github.alexthe666.iceandfire.entity.EntityStoneStatue statue = com.github.alexthe666.iceandfire.entity.EntityStoneStatue.buildStatueEntity((net.minecraft.world.entity.LivingEntity) source);
            statue.absMoveTo(source.getX(), source.getY(), source.getZ(), 0, 0);
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
        level.setChunkForced(PARTS_CHUNK.x, PARTS_CHUNK.z, true);
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
        level.setChunkForced(PARTS_CHUNK.x, PARTS_CHUNK.z, false);
    }

    /** Hive of #5197: the queen dug a new nest and left her hive whenever she "saw the sky", also through water. */
    private static final ChunkPos HIVE_CHUNK = new ChunkPos(490, 490);
    private static com.github.alexthe666.iceandfire.entity.util.MyrmexHive hive;
    private static com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen queen;
    private static int hivesBefore;

    private static void hiveArea(ServerLevel level, boolean load) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setChunkForced(HIVE_CHUNK.x + x, HIVE_CHUNK.z + z, load);
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
        gen.placeSmallGen(level, level.random, center);
        hive = gen.hive;
        var newQueen = IafEntityRegistry.MYRMEX_QUEEN.get().create(level);
        BlockPos ground = com.github.alexthe666.iceandfire.entity.util.MyrmexHive.getGroundedPos(level, center);
        newQueen.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), MobSpawnType.CHUNK_GENERATION, null);
        newQueen.setHive(hive);
        newQueen.moveTo(ground.getX() + 0.5D, ground.getY() + 1D, ground.getZ() + 0.5D, 0, 0);
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
        return "alive " + queen.isAlive() + ", own hive " + (queen.getHive() == hive) + ", " + String.format("%.1f", Math.sqrt(queen.distanceToSqr(hive.getCenter().getCenter())))
            + " blocks from the center, hives " + hivesBefore + " -> " + data.getHivelist().size() + ", sees the sky " + queen.canSeeSky();
    }

    private static void hiveSettled(ServerLevel level) {
        queen = findQueen(level);
        check("myrmex queen in her hive", queen != null && queen.getHive() == hive && queen.distanceToSqr(hive.getCenter().getCenter()) < 20 * 20, queenState(level));
    }

    /** A water entrance: a shaft from the queen up, water to the sea level, open sky above. */
    private static void hiveFlood(ServerLevel level) {
        if (queen == null) {
            return;
        }
        BlockPos base = queen.blockPosition();
        for (int y = base.getY() + 1; y < level.getMaxBuildHeight(); y++) {
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
            && queen.distanceToSqr(hive.getCenter().getCenter()) < 20 * 20 && com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level).getHivelist().size() == hivesBefore, queenState(level));
        hiveArea(level, false);
    }

    private static void finish(MinecraftServer server) {
        IceAndFire.LOGGER.info("[selftest] DONE ok={} fail={}", ok, fail);
        tick = -1;
        server.halt(false);
    }
}
