package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
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
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

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
            switch (tick) {
                case 20 -> registries(server);
                case 40 -> spawnAll(level);
                case 240 -> checkSpawned(level);
                case 260 -> items(level);
                case 280 -> features(server, level);
                case 400 -> structures(server, level);
                case 600 -> finish(server);
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
            level.getChunk(pos);
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

    private static void features(MinecraftServer server, ServerLevel level) {
        var registry = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        int x = 4000;
        List<String> failed = new ArrayList<>();
        int placed = 0;
        for (ResourceLocation id : registry.keySet()) {
            if (!id.getNamespace().equals(IceAndFire.MODID)) {
                continue;
            }
            Holder<ConfiguredFeature<?, ?>> holder = registry.getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, id));
            BlockPos pos = new BlockPos(x, 0, 4000);
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz);
                }
            }
            int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, pos.getX(), pos.getZ());
            try {
                // Many features refuse to place in a wrong biome or near spawn: this checks that they do not crash
                holder.value().place(level, level.getChunkSource().getGenerator(), level.getRandom(), pos.atY(y));
                placed++;
            } catch (Throwable t) {
                failed.add(id + ": " + t);
                IceAndFire.LOGGER.error("[selftest] feature {}", id, t);
            }
            x += 200;
        }
        check("features place without errors", failed.isEmpty(), placed + " placed, failed " + failed);
    }

    private static void structures(MinecraftServer server, ServerLevel level) {
        CommandSourceStack source = server.createCommandSourceStack().withSuppressedOutput().withPermission(4);
        int x = -4000;
        List<String> failed = new ArrayList<>();
        for (String structure : List.of("gorgon_temple", "graveyard", "mausoleum")) {
            int before = fail;
            try {
                server.getCommands().getDispatcher().execute("place structure iceandfire:" + structure + " " + x + " 70 -4000", source);
            } catch (Exception e) {
                failed.add(structure + ": " + e.getMessage());
            }
            x -= 300;
        }
        check("structures place", failed.isEmpty(), failed);
    }

    private static void finish(MinecraftServer server) {
        IceAndFire.LOGGER.info("[selftest] DONE ok={} fail={}", ok, fail);
        tick = -1;
        server.halt(false);
    }
}
