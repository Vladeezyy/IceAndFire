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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Functional checks on a fresh dedicated server world (gradle runSelftest, tools/selftest.sh). Prints
 * "[selftest] CHECK OK/FAIL" lines and stops the server. Not part of the jar.
 */
@EventBusSubscriber(modid = IceAndFire.MODID)
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

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        if (ENABLED) {
            tick = 0;
        }
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        if (tick < 0) {
            return;
        }
        tick++;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        try {
            if (genIndex >= 0) {
                generateStep(level);
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
                case 800 -> finish(server);
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
        for (int n = 0; n < 16 && genIndex < total; n++, genIndex++) {
            int cx = 500 + genIndex % side - GEN_RADIUS;
            int cz = 500 + genIndex / side - GEN_RADIUS;
            level.getChunk(cx, cz);
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

    private static void finish(MinecraftServer server) {
        IceAndFire.LOGGER.info("[selftest] DONE ok={} fail={}", ok, fail);
        tick = -1;
        server.halt(false);
    }
}
