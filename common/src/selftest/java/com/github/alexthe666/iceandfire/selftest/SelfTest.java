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
            if (tick == 540 && !level.isPositionEntityTicking(PARTS_CHUNK.getMiddleBlockPosition(0)) && partsWait++ < 200) {
                // the forced chunk reaches entity ticking a few ticks after its neighbours are loaded
                tick--;
                return;
            }
            switch (tick) {
                case 20 -> registries(server);
                case 40 -> spawnAll(level);
                case 240 -> checkSpawned(level);
                case 260 -> items(level);
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
                case 905 -> hiveFlood(level);
                case 1205 -> hiveAfterFlood(level);
                case 1220 -> finish(server);
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
        for (Identifier id : server.reloadableRegistries().getKeys(Registries.LOOT_TABLE)) {
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
            Entity entity = type.create(level);
            if (entity == null) {
                failed.add(id.toString());
                continue;
            }
            entity.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
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
        var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput().withPermission(4);
        for (String name : List.of("gorgon_temple", "graveyard", "mausoleum")) {
            var holder = registry.getHolderOrThrow(ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name)));
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
        partsDragon = dragon.getUUID();

        // A part saved with the chunk by 1.20.1: loaded without its owner
        oldWorldPart = IafEntityRegistry.DRAGON_MULTIPART.get().create(level);
        oldWorldPart.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
        level.addFreshEntity(oldWorldPart);
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
        level.setChunkForced(PARTS_CHUNK.x, PARTS_CHUNK.z, true);
    }

    private static void partsReloaded(ServerLevel level) {
        Entity dragon = level.getEntity(partsDragon);
        List<EntityDragonPart> parts = dragon == null ? List.of() : parts(level, dragon);
        long all = dragon == null ? 0 : level.getEntitiesOfClass(EntityDragonPart.class, dragon.getBoundingBox().inflate(48)).size();
        check("dragon parts after chunk reload", dragon != null && parts.size() == 10 && all == 10, parts.size() + " owned, " + all + " near");
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
        newQueen.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.CHUNK_GENERATION, null);
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
            && queen.distanceToSqr(hive.getCenter().getCenter()) < 20 * 20 && com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level).getHivelist().size() == hivesBefore, queenState(level));
        hiveArea(level, false);
    }

    private static void finish(MinecraftServer server) {
        IceAndFire.LOGGER.info("[selftest] DONE ok={} fail={}", ok, fail);
        tick = -1;
        server.halt(false);
    }
}
