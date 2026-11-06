package dev.vladeezyy.iceandfire.fabric.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import dev.vladeezyy.rampart.animation.Animation;
import dev.vladeezyy.rampart.animation.IAnimatedEntity;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Bug hunt (tools/fuzztest.sh, Fabric only: it needs Fabric's fake player): every creature, item and block of the mod through what the
 * self-test never does (bites, damage, animations, right clicks with every item, riding, breath), the structures of the mod, and new
 * chunks with every chance of the mod at 1. Both crashes of the report of 2026-11-04 were in code no test reached. Each distinct
 * exception is logged once; "[fuzz] DONE ... 0 distinct" is a pass.
 */
public final class Fuzz {
    public static final boolean ENABLED = Boolean.getBoolean("iceandfire.fuzz");
    private static final Map<String, int[]> SEEN = new LinkedHashMap<>();
    private static final Deque<Runnable> WORK = new ArrayDeque<>();
    private static final List<Item> ITEMS = new ArrayList<>();
    private static long calls;
    private static int tick;
    private static BlockPos base;
    private static FakePlayer player;
    private static String what = "";

    private interface Op {
        void run() throws Throwable;
    }

    private static void safe(String op, Op r) {
        calls++;
        try {
            r.run();
        } catch (Throwable t) {
            StackTraceElement[] st = t.getStackTrace();
            String mod = "";
            for (StackTraceElement e : st) {
                if (e.getClassName().startsWith("com.github.alexthe666") || e.getClassName().startsWith("dev.vladeezyy.iceandfire")) {
                    mod = e.getClassName().substring(e.getClassName().lastIndexOf('.') + 1) + "." + e.getMethodName() + ":" + e.getLineNumber();
                    break;
                }
            }
            Throwable root = t;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            final Throwable rootCause = root;
            StackTraceElement[] rs = root.getStackTrace();
            String key = what + " | " + root.getClass().getSimpleName() + " @ " + mod + " top " + (st.length > 0 ? st[0].getClassName().substring(st[0].getClassName().lastIndexOf('.') + 1) + "." + st[0].getMethodName() : "?");
            int[] n = SEEN.computeIfAbsent(key, k -> {
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < Math.min(st.length, 9); i++) {
                    b.append("\n      at ").append(st[i]);
                }
                for (int i = 0; i < Math.min(rs.length, 6); i++) {
                    b.append("\n      root ").append(rs[i]);
                }
                IceAndFire.LOGGER.error("[fuzz] FOUND {} during {} on {}: {} / root {}{}", k, op, what, t.getMessage(), rootCause.getMessage(), b);
                return new int[1];
            });
            n[0]++;
        }
    }

    private static final List<net.minecraft.world.level.ChunkPos> LOADING = new ArrayList<>();
    private static final int RADIUS = Integer.getInteger("iceandfire.fuzz.radius", 3);
    private static int waited;

    private static void forceArea(ServerLevel level, int cx, int cz, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setChunkForced(cx + x, cz + z, true);
                LOADING.add(new net.minecraft.world.level.ChunkPos(cx + x, cz + z));
            }
        }
    }

    private static boolean loading(ServerLevel level) {
        if (LOADING.isEmpty()) {
            return false;
        }
        LOADING.removeIf(c -> level.hasChunk(c.x(), c.z()));
        if (++waited % 200 == 0) {
            IceAndFire.LOGGER.info("[fuzz] worldgen waiting: {} chunks left, tick {} ms", LOADING.size(), String.format("%.1f", level.getServer().getAverageTickTimeNanos() / 1.0E6));
        }
        if (waited > 20000) {
            IceAndFire.LOGGER.error("[fuzz] worldgen gave up with {} chunks left", LOADING.size());
            LOADING.clear();
        }
        return !LOADING.isEmpty();
    }

    private static void census(ServerLevel level, String label) {
        Map<String, Integer> count = new java.util.TreeMap<>();
        int all = 0;
        for (Entity e : level.getAllEntities()) {
            all++;
            var id = BuiltInRegistries.ENTITY_TYPE.getKey(e.getType());
            if (id.getNamespace().equals(IceAndFire.MODID)) {
                count.merge(id.getPath(), 1, Integer::sum);
            }
        }
        IceAndFire.LOGGER.info("[fuzz] {}: {} entities, hives {}, tick {} ms, mod: {}", label, all, com.github.alexthe666.iceandfire.world.MyrmexWorldData.get(level).getHivelist().size(), String.format("%.2f", level.getServer().getAverageTickTimeNanos() / 1.0E6), count);
    }

    public static void onTick(MinecraftServer server) {
        ServerLevel level = server.overworld();
        if (loading(level)) {
            return;
        }
        tick++;
        if (tick == 20) {
            int cx = 600, cz = 600;
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    level.setChunkForced(cx + x, cz + z, true);
                    level.getChunk(cx + x, cz + z);
                }
            }
            int bx = cx << 4, bz = cz << 4;
            base = new BlockPos(bx, level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz) + 1, bz);
            for (BlockPos pos : BlockPos.betweenClosed(base.offset(-12, -1, -12), base.offset(12, -1, 12))) {
                level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
            }
            for (BlockPos pos : BlockPos.betweenClosed(base.offset(-12, 0, -12), base.offset(12, 10, 12))) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            player = FakePlayer.get(level);
            player.snapTo(base.getX() + 8.5, base.getY(), base.getZ() + 0.5, 0, 0);
            for (Item item : BuiltInRegistries.ITEM) {
                if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(IceAndFire.MODID)) {
                    ITEMS.add(item);
                }
            }
            ITEMS.add(Items.NAME_TAG);
            ITEMS.add(Items.BUCKET);
            ITEMS.add(Items.STICK);
            ITEMS.add(Items.AIR);
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                if (BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(IceAndFire.MODID)) {
                    WORK.add(() -> entity(level, type, false));
                    if (type.create(level, EntitySpawnReason.COMMAND) instanceof EntityDragonBase || type.create(level, EntitySpawnReason.COMMAND) instanceof EntityDeathWorm) {
                        WORK.add(() -> entity(level, type, true));
                    }
                }
            }
            for (Item item : ITEMS) {
                WORK.add(() -> item(level, item));
            }
            for (Block block : BuiltInRegistries.BLOCK) {
                if (BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(IceAndFire.MODID)) {
                    WORK.add(() -> block(level, block));
                }
            }
            int[] at = {0};
            com.github.alexthe666.iceandfire.IafConfig.generateDragonDenChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.generateDragonRoostChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.generateHydraChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.myrmexColonyGenChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.generateSirenChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.dangerousWorldGenSeparationLimit = 0;
            com.github.alexthe666.iceandfire.IafConfig.dangerousWorldGenDistanceLimit = 0;
            for (var entry : level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).listElements().toList()) {
                var id = entry.key().identifier();
                if (id.getNamespace().equals(IceAndFire.MODID)) {
                    int place = 2000 + 400 * at[0]++;
                    WORK.add(() -> {
                        what = "structure " + id;
                        level.setChunkForced(500, 500, true);
                        level.getChunk(500, 500);
                        safe("place structure", () -> {
                            try {
                                server.getCommands().getDispatcher().execute("place structure " + id + " 8000 100 8000", server.createCommandSourceStack().withSuppressedOutput());
                            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException notHere) {
                                IceAndFire.LOGGER.info("[fuzz] {}: {}", what, notHere.getMessage()); // the biome or the ground: no bug
                            }
                        });
                    });
                }
            }
            com.github.alexthe666.iceandfire.IafConfig.spawnCyclopsCaveChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.spawnPixiesChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.spawnWanderingCyclopsChance = 1;
            com.github.alexthe666.iceandfire.IafConfig.generateDragonSkeletonChance = 1;
            WORK.add(() -> { what = "worldgen"; census(level, "before worldgen"); forceArea(level, 1500, 1500, RADIUS); });
            WORK.add(() -> { what = "worldgen"; census(level, "after worldgen"); });
            IceAndFire.LOGGER.info("[fuzz] {} steps, {} items", WORK.size(), ITEMS.size());
        }
        if (tick < 60 || tick % 2 != 0) {
            return;
        }
        Runnable next = WORK.poll();
        if (next == null) {
            IceAndFire.LOGGER.info("[fuzz] DONE {} calls, {} distinct", calls, SEEN.size());
            SEEN.forEach((k, v) -> IceAndFire.LOGGER.info("[fuzz] SUMMARY {} x {}", v[0], k));
            server.halt(false);
            return;
        }
        next.run();
        for (Entity e : level.getEntities(player, new AABB(base).inflate(40))) {
            if (!e.isRemoved()) {
                e.discard();
            }
        }
    }

    private static void heal(LivingEntity e) {
        e.setHealth(e.getMaxHealth());
    }

    private static void entity(ServerLevel level, EntityType<?> type, boolean big) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
        what = id + (big ? " big" : "");
        if (id.contains("multipart")) {
            return;
        }
        Entity e = type.create(level, EntitySpawnReason.COMMAND);
        if (e == null) {
            return;
        }
        e.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0, 0);
        if (e instanceof Mob mob) {
            safe("finalizeSpawn", () -> mob.finalizeSpawn(level, level.getCurrentDifficultyAt(base), EntitySpawnReason.COMMAND, null));
        }
        if (e instanceof EntityDragonBase d && big) {
            safe("grow", () -> d.setAgeInDays(100));
        }
        if (e instanceof EntityDeathWorm w && big) {
            safe("grow", () -> { w.setDeathWormScale(4F); w.setWormAge(5); w.refreshDimensions(); });
        }
        safe("add", () -> level.addFreshEntity(e));
        player.snapTo(base.getX() + 4.5, base.getY(), base.getZ() + 0.5, 0, 0);
        if (e instanceof Mob mob) {
            mob.setTarget(player);
        }
        for (int i = 0; i < 40 && !e.isRemoved(); i++) {
            safe("tick with target", e::tick);
        }
        if (!(e instanceof LivingEntity living)) {
            return;
        }
        heal(living);
        // Skulls, pixies and dread horses have no attack and no attack damage: nothing asks them to bite
        if (e instanceof Mob mob && mob.getAttributes().hasAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)) {
            for (int i = 0; i < 4; i++) {
                safe("doHurtTarget", () -> mob.doHurtTarget(level, player));
                player.setHealth(player.getMaxHealth());
            }
            safe("tick after bite", e::tick);
        }
        var sources = level.damageSources();
        for (var source : List.of(sources.generic(), sources.playerAttack(player), sources.inFire(), sources.lava(), sources.fall(), sources.drown(), sources.magic(), sources.explosion(player, player), sources.lightningBolt(), sources.wither())) {
            safe("hurt " + source.getMsgId(), () -> living.hurtServer(level, source, 2F));
            heal(living);
        }
        if (e instanceof IAnimatedEntity animated) {
            for (Animation animation : animated.getAnimations()) {
                safe("animation", () -> animated.setAnimation(animation));
                int n = Math.min(animation.getDuration() + 2, 60);
                for (int i = 0; i < n && !e.isRemoved(); i++) {
                    safe("tick animation " + animation.getDuration(), e::tick);
                }
                heal(living);
                player.setHealth(player.getMaxHealth());
            }
        }
        if (e instanceof Mob mob) {
            safe("spawn rules", () -> mob.checkSpawnRules(level, EntitySpawnReason.NATURAL));
            safe("spawn obstruction", () -> mob.checkSpawnObstruction(level));
        }
        if (e instanceof EntityDragonBase d) {
            for (boolean flying : new boolean[]{false, true}) {
                safe("fly", () -> { d.setFlying(flying); d.setHovering(flying); d.setBreathingFire(true); });
                for (int i = 0; i < 30 && !e.isRemoved(); i++) {
                    final int n = i;
                    safe("breath", () -> { d.setBreathingFire(true); d.stimulateFire(player.getX(), player.getY(), player.getZ(), n % 2 == 0 ? 1 : 5); });
                    safe("tick breath", e::tick);
                    player.setHealth(player.getMaxHealth());
                    heal(living);
                }
            }
            safe("riderShootFire", () -> d.riderShootFire(player));
            d.setFlying(false);
            d.setHovering(false);
        }
        safe("save and load", () -> {
            var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess());
            e.saveWithoutId(out);
            Entity copy = type.create(level, EntitySpawnReason.LOAD);
            copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
            copy.discard();
        });
        if (e instanceof TamableAnimal t) {
            safe("tame", () -> t.tame(player));
        }
        for (Item item : ITEMS) {
            if (e.isRemoved()) {
                break;
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            safe("interact " + item, () -> player.interactOn(e, InteractionHand.MAIN_HAND, e.position()));
            safe("attack " + item, () -> player.attack(e));
            heal(living);
            if (player.isPassenger()) {
                for (int i = 0; i < 10; i++) {
                    safe("tick ridden", e::tick);
                }
                safe("stop riding", player::stopRiding);
            }
            if (e.isPassenger()) {
                safe("stop riding", e::stopRiding);
            }
            player.stopUsingItem();
            player.getInventory().clearContent();
        }
        if (e.isRemoved()) {
            return;
        }
        safe("ride", () -> player.startRiding(e, true, true));
        for (int i = 0; i < 20; i++) {
            safe("tick ridden", e::tick);
        }
        safe("stop riding", player::stopRiding);
        safe("kill", () -> living.kill(level));
        for (int i = 0; i < 25 && !e.isRemoved(); i++) {
            safe("tick dead", e::tick);
        }
    }

    private static void item(ServerLevel level, Item item) {
        what = String.valueOf(BuiltInRegistries.ITEM.getKey(item));
        BlockPos floor = base.below();
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(floor).add(0, 0.5, 0), Direction.UP, floor, false);
        var zombie = net.minecraft.world.entity.EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        zombie.snapTo(base.getX() + 2.5, base.getY(), base.getZ() + 0.5, 0, 0);
        level.addFreshEntity(zombie);
        player.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0, 0);
        InteractionHand hand = InteractionHand.MAIN_HAND;
        for (int charge : new int[]{1, 25, 100}) {
            ItemStack stack = new ItemStack(item);
            player.setItemInHand(hand, stack);
            safe("use", () -> stack.use(level, player, hand));
            safe("useOn", () -> stack.useOn(new UseOnContext(player, hand, hit)));
            safe("interactLiving", () -> stack.interactLivingEntity(player, zombie, hand));
            safe("inventoryTick", () -> stack.inventoryTick(level, player, EquipmentSlot.MAINHAND));
            safe("hurtEnemy", () -> stack.getItem().hurtEnemy(stack, zombie, player));
            safe("postHurtEnemy", () -> stack.getItem().postHurtEnemy(stack, zombie, player));
            safe("mine", () -> stack.getItem().mineBlock(stack, level, Blocks.STONE.defaultBlockState(), floor, player));
            player.startUsingItem(hand);
            safe("tick player using", player::tick);
            safe("releaseUsing " + charge, () -> stack.getItem().releaseUsing(stack, level, player, stack.getUseDuration(player) - charge));
            safe("finishUsing", () -> stack.getItem().finishUsingItem(stack.copy(), level, player));
            player.stopUsingItem();
            safe("attack zombie", () -> player.attack(zombie));
            zombie.setHealth(zombie.getMaxHealth());
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, new ItemStack(item));
        }
        safe("tick player wearing", player::tick);
        safe("hurt player wearing", () -> player.hurtServer(level, level.damageSources().generic(), 2F));
        player.setHealth(player.getMaxHealth());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            player.setItemSlot(slot, ItemStack.EMPTY);
        }
        player.getInventory().clearContent();
        player.removeAllEffects();
        player.clearFire();
        for (BlockPos pos : BlockPos.betweenClosed(base.offset(-12, 0, -12), base.offset(12, 6, 12))) {
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        if (!level.getBlockState(floor).is(Blocks.STONE)) {
            level.setBlock(floor, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @SuppressWarnings("unchecked")
    private static void block(ServerLevel level, Block block) {
        what = String.valueOf(BuiltInRegistries.BLOCK.getKey(block));
        BlockPos pos = base.offset(0, 1, 0);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        player.snapTo(pos.getX() + 2.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        InteractionHand hand = InteractionHand.MAIN_HAND;
        List<BlockState> states = block.getStateDefinition().getPossibleStates();
        BlockState first = states.get(0);
        safe("place", () -> level.setBlockAndUpdate(pos, first));
        BlockState state = level.getBlockState(pos);
        for (int i = 0; i < 5; i++) {
            safe("randomTick", () -> level.getBlockState(pos).randomTick(level, pos, level.getRandom()));
            safe("tick", () -> level.getBlockState(pos).tick(level, pos, level.getRandom()));
        }
        safe("useWithoutItem", () -> level.getBlockState(pos).useWithoutItem(level, player, hit));
        for (int i = 0; i < ITEMS.size(); i += 3) {
            ItemStack stack = new ItemStack(ITEMS.get(i));
            player.setItemInHand(hand, stack);
            safe("useItemOn " + stack.getItem(), () -> level.getBlockState(pos).useItemOn(stack, level, player, hand, hit));
        }
        player.getInventory().clearContent();
        safe("attack", () -> level.getBlockState(pos).attack(level, pos, player));
        safe("entityInside", () -> level.getBlockState(pos).entityInside(level, pos, player, InsideBlockEffectApplier.NOOP, false));
        safe("stepOn", () -> level.getBlockState(pos).getBlock().stepOn(level, pos, level.getBlockState(pos), player));
        safe("fallOn", () -> level.getBlockState(pos).getBlock().fallOn(level, level.getBlockState(pos), pos, player, 5));
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            BlockEntityTicker<BlockEntity> ticker = (BlockEntityTicker<BlockEntity>) level.getBlockState(pos).getTicker(level, (BlockEntityType<BlockEntity>) be.getType());
            if (ticker != null) {
                for (int i = 0; i < 60 && !be.isRemoved(); i++) {
                    safe("be tick", () -> ticker.tick(level, pos, level.getBlockState(pos), be));
                }
            }
            safe("be save", () -> be.saveWithoutMetadata(level.registryAccess()));
        }
        safe("explode", () -> level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1.5F, Level.ExplosionInteraction.BLOCK));
        safe("place again", () -> level.setBlockAndUpdate(pos, first));
        safe("destroy", () -> level.destroyBlock(pos, true, player));
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, 0, -3), base.offset(3, 3, 3))) {
            level.removeBlockEntity(p);
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        for (BlockPos p : BlockPos.betweenClosed(base.offset(-3, -1, -3), base.offset(3, -1, 3))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }
}
