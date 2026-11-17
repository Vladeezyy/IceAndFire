package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import com.github.alexthe666.iceandfire.entity.EntityHydraHead;
import com.github.alexthe666.iceandfire.world.gen.WorldGenCyclopsCave;
import com.github.alexthe666.iceandfire.world.gen.WorldGenHydraCave;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The hydra cave and the cyclops cave (GitHub #9) placed as the worldgen does, on a stone yard in the air, far from the
 * spawn: what they put in (the creature, sheep of the pens, chests with their loot table). Then the hydra of the cave
 * after a fight (a head cut off, another number of heads) through NBT and through an unload and load of its chunk.
 * Driven by {@link SelfTest#onTick}: {@link #hold} holds the schedule while chunks load or unload.
 */
final class CaveCheck {
    // 8000 blocks from the spawn (dangerousWorldGenDistanceLimit), away from the parts (500, 500) and hive (490, 490) chunks
    private static final ChunkPos HYDRA_CHUNK = new ChunkPos(520, 470);
    private static final ChunkPos CYCLOPS_CHUNK = new ChunkPos(527, 470);
    private static final int Y = 200;
    static final int LOAD = 906;
    static final int PLACE = 950;
    static final int FIGHT = 955;
    static final int UNLOAD = 965;
    static final int UNLOADED = 1000;
    static final int RELOADED = 1060;
    static final int PARTS = 1080;
    private static int waited;
    private static UUID hydraId;
    private static EntityHydra hydraBefore;
    private static String savedState;
    private static float savedHealth;
    private static EntityHydra hydraAfter;
    private static String loadedState;
    private static float loadedHealth;
    private static int loadedTicks;

    private CaveCheck() {
    }

    private static void area(ServerLevel level, ChunkPos center, boolean load) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                level.setChunkForced(center.x() + x, center.z() + z, load);
            }
        }
    }

    private static BlockPos origin(ChunkPos chunk) {
        return chunk.getMiddleBlockPosition(Y);
    }

    /** True while the schedule waits at this tick (chunks entity ticking, unloaded, loaded again) */
    static boolean hold(int tick, ServerLevel level) {
        // the reloaded hydra as soon as it is back, before its regeneration heals it (one tick at most)
        if (tick > UNLOADED && tick <= RELOADED && hydraAfter == null && hydraId != null && level.getEntity(hydraId) instanceof EntityHydra back && back != hydraBefore) {
            hydraAfter = back;
            loadedState = shape(back);
            loadedHealth = back.getHealth();
            loadedTicks = back.tickCount;
        }
        boolean wait = switch (tick) {
            case PLACE -> !level.isPositionEntityTicking(origin(HYDRA_CHUNK)) || !level.isPositionEntityTicking(origin(CYCLOPS_CHUNK));
            case UNLOADED -> hydraBefore != null && !hydraBefore.isRemoved();
            case RELOADED -> hydraId != null && hydraAfter == null;
            default -> false;
        };
        if (wait && waited++ < 400) {
            return true;
        }
        waited = 0;
        return false;
    }

    static void onTick(int tick, ServerLevel level) {
        switch (tick) {
            case LOAD -> {
                area(level, HYDRA_CHUNK, true);
                area(level, CYCLOPS_CHUNK, true);
            }
            case PLACE -> {
                hydraCave(level);
                cyclopsCave(level);
            }
            // the parts are added by the hydra's first ticks
            case FIGHT -> fight(level);
            case UNLOAD -> {
                area(level, HYDRA_CHUNK, false);
                area(level, CYCLOPS_CHUNK, false);
            }
            case UNLOADED -> unloaded(level);
            case RELOADED -> reloaded(level);
            case PARTS -> {
                parts(level);
                area(level, HYDRA_CHUNK, false);
            }
            default -> {
            }
        }
    }

    /** A stone yard 4 deep with air above: the corner probes of the caves find ground, nothing of the terrain is near */
    private static void yard(ServerLevel level, BlockPos origin, int radius) {
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                for (int dy = -4; dy <= 16; dy++) {
                    level.setBlock(at.set(origin.getX() + x, origin.getY() + dy, origin.getZ() + z),
                        dy < 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
    }

    /**
     * Places a feature as the worldgen does (switched on, chance 1). The separation from the last dangerous feature is
     * off: the check records every position asked, the world generation of the self-test asked many.
     */
    private static boolean place(ServerLevel level, com.github.alexthe666.iceandfire.world.gen.IafFeature feature, BlockPos origin, long seed) {
        double separation = IafConfig.dangerousWorldGenSeparationLimit;
        IafConfig.dangerousWorldGenSeparationLimit = 0;
        try {
            // 26.3: the feature registry holds codecs, the feature is made here (as in checkSwitchedOff)
            return feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(seed), origin);
        } finally {
            IafConfig.dangerousWorldGenSeparationLimit = separation;
        }
    }

    /** Chests in the box: how many, how many with the loot table, items in the first one once its loot is rolled */
    private static String chests(ServerLevel level, AABB box, ResourceKey<LootTable> table, int[] counts) {
        List<ChestBlockEntity> withTable = new ArrayList<>();
        int all = 0;
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(box.minX, box.minY, box.minZ), BlockPos.containing(box.maxX, box.maxY, box.maxZ))) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ChestBlockEntity chest) {
                all++;
                if (table.equals(chest.getLootTable())) {
                    withTable.add(chest);
                }
            }
        }
        int items = 0;
        if (!withTable.isEmpty()) {
            ChestBlockEntity first = withTable.get(0);
            first.unpackLootTable(null);
            for (int i = 0; i < first.getContainerSize(); i++) {
                items += first.getItem(i).isEmpty() ? 0 : 1;
            }
        }
        counts[0] = all;
        counts[1] = withTable.size();
        counts[2] = items;
        return all + " chests, " + withTable.size() + " with " + table + ", the first one rolled " + items + " stacks";
    }

    private static void hydraCave(ServerLevel level) {
        BlockPos origin = origin(HYDRA_CHUNK);
        yard(level, origin, 14);
        boolean placed;
        boolean on = IafConfig.generateHydraCaves;
        int chance = IafConfig.generateHydraChance;
        IafConfig.generateHydraCaves = true;
        IafConfig.generateHydraChance = 1;
        try {
            placed = place(level, new WorldGenHydraCave(), origin, 9);
        } finally {
            IafConfig.generateHydraCaves = on;
            IafConfig.generateHydraChance = chance;
        }
        AABB box = new AABB(origin).inflate(12, 0, 12).expandTowards(0, 10, 0);
        List<EntityHydra> hydras = level.getEntitiesOfClass(EntityHydra.class, box);
        int[] counts = new int[3];
        String chests = chests(level, box, WorldGenHydraCave.HYDRA_CHEST, counts);
        SelfTest.check("hydra cave: placed with a hydra and its chests", placed && hydras.size() == 1 && counts[1] > 0 && counts[1] == counts[0] && counts[2] > 0,
            "placed " + placed + ", hydras " + hydras.size() + ", " + chests + ", floor " + level.getBlockState(origin).getBlock());
        if (!hydras.isEmpty()) {
            hydraBefore = hydras.get(0);
            hydraId = hydraBefore.getUUID();
        }
    }

    private static void cyclopsCave(ServerLevel level) {
        BlockPos origin = origin(CYCLOPS_CHUNK);
        yard(level, origin, 22);
        boolean placed;
        boolean on = IafConfig.generateCyclopsCaves;
        int chance = IafConfig.spawnCyclopsCaveChance;
        IafConfig.generateCyclopsCaves = true;
        IafConfig.spawnCyclopsCaveChance = 1;
        try {
            placed = place(level, new WorldGenCyclopsCave(), origin, 9);
        } finally {
            IafConfig.generateCyclopsCaves = on;
            IafConfig.spawnCyclopsCaveChance = chance;
        }
        AABB box = new AABB(origin).inflate(20, 0, 20).expandTowards(0, 14, 0);
        List<EntityCyclops> cyclopes = level.getEntitiesOfClass(EntityCyclops.class, box);
        List<Sheep> sheep = level.getEntitiesOfClass(Sheep.class, box);
        int[] counts = new int[3];
        String chests = chests(level, box, WorldGenCyclopsCave.CYCLOPS_CHEST, counts);
        SelfTest.check("cyclops cave: placed with a cyclops, sheep in pens and its chests", placed && cyclopes.size() == 1 && !sheep.isEmpty()
                && counts[1] > 0 && counts[1] == counts[0] && counts[2] > 0,
            "placed " + placed + ", cyclopes " + cyclopes.size() + ", sheep " + sheep.size() + ", " + chests + ", floor " + level.getBlockState(origin).getBlock());
        cyclopes.forEach(Entity::discard);
        sheep.forEach(Entity::discard);
    }

    static String hydraState(EntityHydra hydra) {
        return String.format("health %.2f/%.1f, %s", hydra.getHealth(), hydra.getMaxHealth(), shape(hydra));
    }

    /** The saved state but the health, which the hydra's regeneration changes every few ticks */
    private static String shape(EntityHydra hydra) {
        StringBuilder damage = new StringBuilder();
        for (float d : hydra.headDamageTracker) {
            damage.append(String.format("%.1f ", d));
        }
        return String.format("heads %d, severed %d, variant %d, head damage %s", hydra.getHeadCount(), hydra.getSeveredHead(), hydra.getVariant(), damage.toString().trim());
    }

    /**
     * The hydra of the cave after a fight: a head hit past its threshold (cut off), the number of heads changed. Its save
     * read into a new hydra must give the same state.
     */
    private static void fight(ServerLevel level) {
        if (hydraBefore == null) {
            SelfTest.check("hydra after a fight saved and loaded", false, "no hydra in the cave");
            return;
        }
        EntityHydra hydra = hydraBefore;
        List<EntityHydraHead> heads = level.getEntitiesOfClass(EntityHydraHead.class, hydra.getBoundingBox().inflate(8), p -> p.getParent() == hydra && p.headIndex == 1);
        boolean hit = !heads.isEmpty() && heads.get(0).hurtServer(level, level.damageSources().generic(), 30);
        hydra.setHeadCount(6);
        CompoundTag tag = com.github.alexthe666.iceandfire.entity.util.Values.save(hydra);
        EntityHydra copy = new EntityHydra(com.github.alexthe666.iceandfire.entity.IafEntityRegistry.HYDRA.get(), level);
        com.github.alexthe666.iceandfire.entity.util.Values.load(copy, tag);
        String before = hydraState(hydra);
        String after = hydraState(copy);
        SelfTest.check("hydra after a fight through NBT", hit && hydra.getSeveredHead() == 1 && hydra.getHealth() < hydra.getMaxHealth() && before.equals(after),
            "head hit " + hit + ", " + before + (before.equals(after) ? "" : " -> loaded " + after));
    }

    private static void unloaded(ServerLevel level) {
        if (hydraBefore == null) {
            return;
        }
        savedState = shape(hydraBefore);
        savedHealth = hydraBefore.getHealth();
        SelfTest.check("hydra chunk unloaded", hydraBefore.isRemoved(), hydraBefore.getRemovalReason() + ", " + hydraState(hydraBefore));
        area(level, HYDRA_CHUNK, true);
    }

    private static void reloaded(ServerLevel level) {
        if (hydraId == null) {
            return;
        }
        // the health: as saved, or healed by at most one regeneration step if it ticked once before it was seen
        boolean same = hydraAfter != null && loadedState.equals(savedState) && loadedHealth >= savedHealth && loadedHealth <= savedHealth + 1;
        SelfTest.check("hydra after a fight through a chunk unload and load", same,
            String.format("unloaded health %.2f, %s -> loaded health %.2f, %s (tick count %d)", savedHealth, savedState, loadedHealth,
                hydraAfter == null ? "no hydra" : loadedState.equals(savedState) ? "same" : loadedState, loadedTicks));
    }

    /** 20 ticks after the load: the hydra made its head and neck parts again, none are left of the unloaded one */
    private static void parts(ServerLevel level) {
        if (!(hydraId != null && level.getEntity(hydraId) instanceof EntityHydra hydra)) {
            return;
        }
        List<EntityHydraHead> own = level.getEntitiesOfClass(EntityHydraHead.class, hydra.getBoundingBox().inflate(16), p -> p.getParent() == hydra);
        int all = level.getEntitiesOfClass(EntityHydraHead.class, hydra.getBoundingBox().inflate(16)).size();
        SelfTest.check("hydra parts after the chunk load", own.size() == hydra.getHeadCount() * 2 && all == own.size(),
            own.size() + " own of " + hydra.getHeadCount() + " heads, " + all + " near");
        IceAndFire.LOGGER.info("[selftest] hydra after the reload: {}", hydraState(hydra));
    }
}
