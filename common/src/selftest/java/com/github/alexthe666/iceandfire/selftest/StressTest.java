package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDragonPart;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Load on a dedicated server (gradle runSelftest -Diceandfire.stress=true, tools/stresstest.sh): fast generation of
 * 64 x 64 chunks, 30 adult dragons with AI and 100 other creatures for a minute, chunk unload/reload cycles. Prints
 * "[stress]" lines with tick times and memory, and CHECK lines.
 */
public class StressTest {
    private static final boolean ENABLED = Boolean.getBoolean("iceandfire.stress");
    /** The arena: 5 x 5 forced chunks inside the generated area. */
    private static final ChunkPos ARENA = new ChunkPos(-1000, -1000);
    private static final int GEN_SIDE = 64;

    private static int tick = -1;
    private static int ok;
    private static int fail;
    private static int genIndex = -1;
    private static long genStart;
    private static int phaseStart;
    private static final List<Long> TICK_NANOS = new ArrayList<>();
    private static long tickStart;
    private static int cycle;

    private static void check(String name, boolean result, Object detail) {
        if (result) {
            ok++;
        } else {
            fail++;
        }
        IceAndFire.LOGGER.info("[stress] CHECK {} {} {}", result ? "OK" : "FAIL", name, detail);
    }

    public static void onStarted(net.minecraft.server.MinecraftServer server) {
        if (ENABLED) {
            tick = 0;
            // deaths of dragons in the arena (a lost dragon check tells a fight from a dragon lost on the disk)
            dev.vladeezyy.iceandfire.bus.IafBus.add(dev.vladeezyy.iceandfire.bus.LivingDeathEvent.class, dev.vladeezyy.iceandfire.bus.EventPriority.LOWEST, event -> {
                if (event.getEntity() instanceof EntityDragonBase dragon) {
                    IceAndFire.LOGGER.warn("[stress] dragon {} died at tick {}: {}", dragon.getUUID(), tick, event.getSource().getLocalizedDeathMessage(dragon).getString());
                }
            });
        }
    }

    public static void onTickStart(net.minecraft.server.MinecraftServer server) {
        tickStart = System.nanoTime();
    }

    public static void onTick(net.minecraft.server.MinecraftServer server) {
        if (tick < 0) {
            return;
        }
        tick++;
        ServerLevel level = server.overworld();
        long now = System.nanoTime();
        // The work of the tick, without the wait for the next one
        TICK_NANOS.add(now - tickStart);
        try {
            if (tick == 20) {
                genIndex = 0;
                genStart = now;
                // A slow generation depends on the world: SEED=<seed> tools/stresstest.sh repeats it
                IceAndFire.LOGGER.info("[stress] seed {}", level.getSeed());
            }
            if (genIndex >= 0) {
                generate(level, now);
                return;
            }
            if (phaseStart == 0) {
                return;
            }
            int t = tick - phaseStart;
            if (t == 1) {
                arena(level, true);
            } else if (t == 40) {
                spawn(level);
                TICK_NANOS.clear();
            } else if (t > 40 && t <= 1240 && t % 200 == 0) {
                report(server, level, "creatures");
            } else if (t == 1260) {
                check("tick time with 30 dragons and 100 creatures", average() < 50, String.format("%.1f ms average, %.1f ms max", average(), max()));
                check("dragons alive", dragons(level).size() >= 20, dragons(level).size());
                parts(level, "before unload");
                cycle = 0;
            } else if (t > 1260 && cycle < 5) {
                // Unload and load the arena again: dragons, parts and the data of the creatures go through the disk
                int c = t - 1260;
                if (c % 120 == 1) {
                    arena(level, false);
                } else if (c % 120 == 100) {
                    arena(level, true);
                } else if (c % 120 == 119) {
                    cycle++;
                    parts(level, "after reload " + cycle);
                }
            } else if (cycle >= 5 && hiveStart == 0) {
                hiveStart = tick;
            } else if (hiveStart > 0 && !hiveDone) {
                hive(server, level, tick - hiveStart);
            } else if (hiveDone) {
                Runtime runtime = Runtime.getRuntime();
                IceAndFire.LOGGER.info("[stress] memory {} MB used of {} MB", (runtime.totalMemory() - runtime.freeMemory()) >> 20, runtime.maxMemory() >> 20);
                IceAndFire.LOGGER.info("[stress] DONE ok={} fail={}", ok, fail);
                tick = -1;
                server.halt(false);
            }
        } catch (Throwable throwable) {
            check("tick " + tick, false, throwable);
            IceAndFire.LOGGER.error("[stress] exception", throwable);
        }
    }

    /** 64 x 64 chunks around the arena, 16 per tick (64 per tick kept one tick busy for more than the 60 s of the watchdog). */
    private static void generate(ServerLevel level, long now) {
        int total = GEN_SIDE * GEN_SIDE;
        for (int n = 0; n < 16 && genIndex < total; n++, genIndex++) {
            level.getChunk(ARENA.x - GEN_SIDE / 2 + genIndex % GEN_SIDE, ARENA.z - GEN_SIDE / 2 + genIndex / GEN_SIDE);
        }
        if (genIndex % 256 == 0) {
            double seconds = (System.nanoTime() - genStart) / 1.0E9;
            IceAndFire.LOGGER.info("[stress] generated {} chunks in {} s", genIndex, String.format("%.0f", seconds));
        }
        if (genIndex >= total) {
            double seconds = (now - genStart) / 1.0E9;
            check("generation of " + total + " chunks", true, String.format("%.0f s, %.1f chunks/s", seconds, total / seconds));
            genIndex = -1;
            phaseStart = tick;
            Runtime runtime = Runtime.getRuntime();
            IceAndFire.LOGGER.info("[stress] memory after generation {} MB used", (runtime.totalMemory() - runtime.freeMemory()) >> 20);
        }
    }

    /**
     * The myrmex hive of #5197 over a long time: a hive with its queen in another chunk of the generated area, about
     * five Minecraft hours run with /tick sprint (workers dig, forage and wander), the queen must still be at home.
     */
    private static final ChunkPos HIVE = new ChunkPos(ARENA.x + 12, ARENA.z + 12);
    private static final int HIVE_TICKS = 6000;
    private static int hiveStart;
    private static boolean hiveDone;
    private static TestHive testHive;

    private static void hive(MinecraftServer server, ServerLevel level, int t) {
        if (t == 1) {
            TestHive.forceArea(level, HIVE, true);
        } else if (t == 40) {
            testHive = TestHive.place(level, HIVE);
            check("myrmexes added to the test hive", testHive.populate(level) == 9, "9 workers, soldiers and a sentinel");
        } else if (t == 140) {
            check("myrmex queen in her hive", testHive.queenAtHome(level), testHive.state(level));
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "tick sprint " + HIVE_TICKS);
        } else if (t > 140 && t < 140 + HIVE_TICKS && (t - 140) % 1000 == 0) {
            IceAndFire.LOGGER.info("[stress] hive after {} ticks: {}", t - 140, testHive.state(level));
        } else if (t == 160 + HIVE_TICKS) {
            check("myrmex queen still in her hive after " + HIVE_TICKS + " ticks (#5197)", testHive.queenAtHome(level), testHive.state(level));
            TestHive.forceArea(level, HIVE, false);
            hiveDone = true;
        }
    }

    private static void arena(ServerLevel level, boolean load) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setChunkForced(ARENA.x + x, ARENA.z + z, load);
            }
        }
    }

    private static void spawn(ServerLevel level) {
        BlockPos center = ARENA.getMiddleBlockPosition(0);
        List<EntityType<?>> dragons = List.of(IafEntityRegistry.FIRE_DRAGON.get(), IafEntityRegistry.ICE_DRAGON.get(), IafEntityRegistry.LIGHTNING_DRAGON.get());
        List<EntityType<?>> others = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            String path = id.getPath();
            if (id.getNamespace().equals(IceAndFire.MODID) && !path.contains("dragon") && !path.contains("multipart") && type.getCategory() != net.minecraft.world.entity.MobCategory.MISC) {
                others.add(type);
            }
        }
        int spawned = 0;
        for (int i = 0; i < 30; i++) {
            Entity entity = place(level, dragons.get(i % 3), center.offset((i % 6) * 12 - 30, 0, (i / 6) * 12 - 30));
            if (entity instanceof EntityDragonBase dragon) {
                dragon.setAgeInDays(50 + i * 3);
                spawned++;
            }
        }
        for (int i = 0; i < 100; i++) {
            if (place(level, others.get(i % others.size()), center.offset((i % 10) * 6 - 30, 0, (i / 10) * 6 - 30)) != null) {
                spawned++;
            }
        }
        check("spawned in the arena", spawned >= 120, spawned + " of 130, " + others.size() + " creature types");
    }

    private static Entity place(ServerLevel level, EntityType<?> type, BlockPos pos) {
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) + 1;
        Entity entity = type.create(level, EntitySpawnReason.COMMAND);
        if (entity == null) {
            return null;
        }
        entity.snapTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, level.getRandom().nextFloat() * 360, 0);
        if (entity instanceof Mob mob) {
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
            mob.setPersistenceRequired();
        }
        return level.addFreshEntity(entity) ? entity : null;
    }

    private static List<EntityDragonBase> dragons(ServerLevel level) {
        return level.getEntitiesOfClass(EntityDragonBase.class, area());
    }

    private static AABB area() {
        BlockPos center = ARENA.getMiddleBlockPosition(0);
        return new AABB(center).inflate(160, 400, 160);
    }

    private static int dragonsBefore = -1;
    private static final java.util.Map<java.util.UUID, String> LAST_SEEN = new java.util.HashMap<>();

    /**
     * Every ticking dragon has its 10 parts, no dragon more than 10, no part without a dragon, no dragon lost. Dragons
     * that flew out of the forced chunks do not tick after a reload and have no parts yet.
     */
    private static void parts(ServerLevel level, String when) {
        List<EntityDragonBase> dragons = dragons(level);
        List<EntityDragonPart> parts = level.getEntitiesOfClass(EntityDragonPart.class, area().inflate(64));
        long orphans = parts.stream().filter(part -> !(part.getParent() instanceof EntityDragonBase)).count();
        long ticking = dragons.stream().filter(dragon -> level.isPositionEntityTicking(dragon.blockPosition())).count();
        long withAllParts = dragons.stream().filter(dragon -> parts.stream().filter(part -> part.getParent() == dragon).count() == 10).count();
        long tooMany = dragons.stream().filter(dragon -> parts.stream().filter(part -> part.getParent() == dragon).count() > 10).count();
        if (dragonsBefore < 0) {
            dragonsBefore = dragons.size();
        }
        // a dragon gone since the last check: where it was and how it was (died in a fight, or lost on the disk)
        java.util.Set<java.util.UUID> now = new java.util.HashSet<>();
        dragons.forEach(dragon -> now.add(dragon.getUUID()));
        LAST_SEEN.forEach((uuid, seen) -> {
            if (!now.contains(uuid)) {
                net.minecraft.world.entity.Entity anywhere = level.getEntity(uuid);
                IceAndFire.LOGGER.warn("[stress] dragon {} gone {}: last seen {}, now {}", uuid, when, seen, anywhere == null ? "not in the level" : anywhere.position() + " removed " + anywhere.getRemovalReason());
            }
        });
        LAST_SEEN.clear();
        dragons.forEach(dragon -> LAST_SEEN.put(dragon.getUUID(), String.format("%s at %.0f %.0f %.0f, health %.1f, alive %s, ticking %s", BuiltInRegistries.ENTITY_TYPE.getKey(dragon.getType()).getPath(), dragon.getX(), dragon.getY(), dragon.getZ(), dragon.getHealth(), dragon.isAlive(), level.isPositionEntityTicking(dragon.blockPosition()))));
        check("dragon parts " + when, orphans == 0 && tooMany == 0 && withAllParts >= ticking && dragons.size() == dragonsBefore,
            dragons.size() + " dragons (" + ticking + " ticking, " + withAllParts + " with 10 parts, " + tooMany + " with more), "
                + parts.size() + " parts, " + orphans + " without a dragon");
    }

    private static void report(MinecraftServer server, ServerLevel level, String phase) {
        Runtime runtime = Runtime.getRuntime();
        IceAndFire.LOGGER.info("[stress] {} tick {} average {} ms max {} ms, {} dragons, {} entities, memory {} MB", phase, tick,
            String.format("%.1f", average()), String.format("%.1f", max()), dragons(level).size(),
            level.getEntities((Entity) null, area(), e -> true).size(), (runtime.totalMemory() - runtime.freeMemory()) >> 20);
    }

    private static double average() {
        return TICK_NANOS.stream().mapToLong(Long::longValue).average().orElse(0) / 1.0E6;
    }

    private static double max() {
        return TICK_NANOS.stream().mapToLong(Long::longValue).max().orElse(0) / 1.0E6;
    }
}
