package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import com.github.alexthe666.iceandfire.entity.EntityHydraHead;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * IAF_NETTEST=hydra (GitHub #8): a hydra with nine heads fights the client's player on a dedicated server. The hydra
 * stands still (no AI, its own attack code runs) with the player as its target: first 8 blocks away, where its heads
 * breathe until every head has ended a breath (entity events 70 - 78; 60 + head was a vanilla event, 63 threw the
 * client out with "Network Protocol Error"), then 4.5 blocks away, where the heads bite and the player hits them with a
 * sword; then the server kills it. The player cannot be hurt (resistance V) but gets the bites, the poison and the knockback.
 * Checks: server - every head ended a breath, bites landed, the player's hits reached the heads, the hydra died and was
 * removed; client - it stayed connected, saw every head of its copy start and end a breath, saw bites and the death.
 */
final class NetTestHydra {
    static final boolean ACTIVE = "hydra".equals(System.getenv("IAF_NETTEST"));
    private static final int ALL_HEADS = (1 << EntityHydra.HEADS) - 1;

    // Server
    private static ServerPlayer player;
    private static int ticks = -1;
    private static EntityHydra hydra;
    private static BlockPos stand;
    private static int fails;
    private static int breathEnded;
    private static int breathEnds;
    private static int breathDoneAt = -1;
    private static int biteStart = -1;
    private static int bites;
    private static int hits;
    private static float headDamage;
    private static int severed;
    private static int killedAt = -1;
    private static boolean deathChecked;
    private static boolean done;
    private static final boolean[] BREATHING = new boolean[EntityHydra.HEADS];
    private static final boolean[] STRIKING = new boolean[EntityHydra.HEADS];

    private NetTestHydra() {
    }

    private static void check(String name, boolean ok, Object detail) {
        if (!ok) {
            fails++;
        }
        IceAndFire.LOGGER.info("[nettest] CHECK {} server: {} ({})", ok ? "OK" : "FAIL", name, detail);
    }

    static void onLogin(ServerPlayer joined) {
        player = joined;
        ticks = 0;
        IceAndFire.LOGGER.info("[nettest] server scenario hydra");
    }

    static void onLogout(net.minecraft.world.entity.player.Player left) {
        if (left != player) {
            return;
        }
        if (!done) {
            check("the client stayed until the end", false, "left at tick " + ticks + ", breath ends " + breathEnds + ", bites " + bites + ", hits " + hits);
        }
        IceAndFire.LOGGER.info("[nettest] server DONE, failed checks: {}", fails);
        player.getServer().halt(false);
    }

    private static void teleport(double dx) {
        // west of the hydra, looking at it (east)
        player.connection.teleport(stand.getX() + 0.5 - dx, stand.getY(), stand.getZ() + 0.5, -90, 10);
    }

    private static void setUp(ServerLevel level) {
        player.setGameMode(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20000, 4, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 20000, 0, false, false));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_SWORD));
        // a flat stone yard, cleared above, 300 blocks from the spawn: no other mob on it
        BlockPos spawn = level.getSharedSpawnPos();
        int x0 = spawn.getX() + 300;
        int z0 = spawn.getZ();
        int top = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING, x0, z0), level.getSeaLevel() + 1);
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = -16; x <= 12; x++) {
            for (int z = -12; z <= 12; z++) {
                for (int dy = -1; dy <= 20; dy++) {
                    level.setBlock(at.set(x0 + x, top + dy, z0 + z), dy < 0 ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        stand = new BlockPos(x0, top, z0);
    }

    private static void spawnHydra(ServerLevel level) {
        hydra = IafEntityRegistry.HYDRA.get().create(level);
        // facing west, at the player: the heads are on that side
        hydra.moveTo(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, 90, 0);
        hydra.finalizeSpawn(level, level.getCurrentDifficultyAt(stand), MobSpawnType.COMMAND, null);
        hydra.setHeadCount(EntityHydra.HEADS);
        hydra.setYBodyRot(90);
        hydra.setYHeadRot(90);
        hydra.setNoAi(true);
        hydra.setPersistenceRequired();
        level.addFreshEntity(hydra);
        hydra.setTarget(player);
        IceAndFire.LOGGER.info("[nettest] hydra: spawned at {} with {} heads, the player {} blocks away", hydra.blockPosition().toShortString(), hydra.getHeadCount(),
            String.format("%.1f", hydra.distanceTo(player)));
    }

    /** What the hydra did this tick: breath ends and bites by head (the flags are cleared by its own tick) */
    private static void watch() {
        float damage = 0;
        for (int i = 0; i < EntityHydra.HEADS; i++) {
            if (BREATHING[i] && !hydra.isBreathing[i]) {
                breathEnded |= 1 << i;
                breathEnds++;
            }
            if (STRIKING[i] && !hydra.isStriking[i] && biteStart >= 0 && hydra.distanceTo(player) < 6) {
                bites++;
            }
            BREATHING[i] = hydra.isBreathing[i];
            STRIKING[i] = hydra.isStriking[i];
            damage += hydra.headDamageTracker[i];
        }
        // the head damage only grows by hits (it is reset when a head is cut off)
        if (damage > headDamage || hydra.getSeveredHead() >= 0 && hydra.getSeveredHead() != severed) {
            hits++;
        }
        if (hydra.getSeveredHead() >= 0 && hydra.getSeveredHead() != severed) {
            IceAndFire.LOGGER.info("[nettest] hydra: head {} cut off", hydra.getSeveredHead());
        }
        severed = hydra.getSeveredHead();
        headDamage = damage;
    }

    static void serverTick(MinecraftServer server) {
        if (ticks < 0 || player == null) {
            return;
        }
        ticks++;
        ServerLevel level = player.serverLevel();
        if (ticks == 60) {
            setUp(level);
            teleport(8);
        }
        if (ticks == 100) {
            spawnHydra(level);
        }
        if (ticks % 20 == 0) {
            IceAndFire.LOGGER.info("[nettest] server tick {} mspt {} hydra {}, breath ended by heads {}/{} ({} ends), bites {}, hits {}, player {} blocks away",
                ticks, String.format("%.1f", server.getAverageTickTimeNanos() / 1.0E6), hydra == null ? "-" : CaveCheck.hydraState(hydra),
                Integer.bitCount(breathEnded), EntityHydra.HEADS, breathEnds, bites, hits, hydra == null ? "-" : String.format("%.1f", hydra.distanceTo(player)));
        }
        if (hydra == null) {
            return;
        }
        if (killedAt < 0) {
            watch();
            // the target is the player all the time (no AI clears or changes it)
            hydra.setTarget(player);
        }
        // the breath part: until every head has ended a breath, at most 2 minutes; then 60 ticks for the client to get the last end
        if (breathDoneAt < 0 && (breathEnded == ALL_HEADS || ticks == 2500)) {
            breathDoneAt = ticks;
            check("every head of the hydra ended a breath", breathEnded == ALL_HEADS, Integer.bitCount(breathEnded) + " heads, " + breathEnds + " breath ends, tick " + ticks);
        }
        if (breathDoneAt > 0 && biteStart < 0 && ticks == breathDoneAt + 60) {
            biteStart = ticks;
            teleport(4.5);
            IceAndFire.LOGGER.info("[nettest] hydra: the player comes close, bites and hits");
        }
        if (biteStart > 0 && killedAt < 0) {
            // the bites knock the player back: back in reach every 2 s
            if ((ticks - biteStart) % 40 == 39 && hydra.distanceTo(player) > 5.5) {
                teleport(4.5);
            }
            if (ticks - biteStart >= 300 && bites >= 3 && hits >= 6 || ticks - biteStart == 1200) {
                check("the hydra bit the player", bites >= 3, bites + " bites, poisoned " + player.hasEffect(MobEffects.POISON));
                check("the player's hits reached the heads", hits >= 6, hits + " hits, head cut off now " + hydra.getSeveredHead() + ", " + CaveCheck.hydraState(hydra));
                hydra.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
                killedAt = ticks;
                IceAndFire.LOGGER.info("[nettest] hydra: killed, dying {} health {}", hydra.isDeadOrDying(), hydra.getHealth());
            }
        }
        if (killedAt > 0 && !deathChecked && (hydra.isRemoved() || ticks == killedAt + 200)) {
            deathChecked = true;
            done = true;
            check("the hydra died and was removed", hydra.isDeadOrDying() && hydra.isRemoved(), "dying " + hydra.isDeadOrDying() + ", removed " + hydra.getRemovalReason() + " after " + (ticks - killedAt) + " ticks");
        }
        if (ticks == 7000) {
            check("the client finished in time", false, "tick " + ticks);
            server.halt(false);
        }
    }

    /** Client side: the dedicated server must not load it (Minecraft in the signatures). */
    static final class Client {
        private static boolean joined;
        private static boolean finished;
        private static int ticks;
        private static int fails;
        private static EntityHydra hydra;
        private static int firstHeads = -1;
        private static int breathStarted;
        private static int breathEnded;
        private static int breathEnds;
        private static boolean breathReported;
        private static int bitesSeen;
        private static int attacks;
        private static int headAttacks;
        private static boolean sawDying;
        private static int removedAt = -1;
        private static final boolean[] BREATHING = new boolean[EntityHydra.HEADS];
        private static final boolean[] STRIKING = new boolean[EntityHydra.HEADS];

        private Client() {
        }

        private static void check(String name, boolean ok, Object detail) {
            if (!ok) {
                fails++;
            }
            IceAndFire.LOGGER.info("[nettest] CHECK {} client: {} ({})", ok ? "OK" : "FAIL", name, detail);
        }

        /** Not in a world: after being in one this is a disconnect, the test ends. True when it handled the tick. */
        static boolean offline(Minecraft mc) {
            if (!joined || finished) {
                return false;
            }
            finished = true;
            StringBuilder reason = new StringBuilder(mc.screen == null ? "no screen" : mc.screen.getClass().getSimpleName() + " " + mc.screen.getTitle().getString());
            if (mc.screen != null) {
                for (Class<?> type = mc.screen.getClass(); type != null && type != Object.class; type = type.getSuperclass()) {
                    for (java.lang.reflect.Field field : type.getDeclaredFields()) {
                        if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        try {
                            field.setAccessible(true);
                            Object value = field.get(mc.screen);
                            if (value instanceof net.minecraft.network.chat.Component text) {
                                reason.append(" | ").append(text.getString());
                            } else if (value != null && value.getClass().isRecord()) {
                                reason.append(" | ").append(value);
                            }
                        } catch (ReflectiveOperationException | RuntimeException ignored) {
                            // a field the module system keeps closed: the title is enough
                        }
                    }
                }
            }
            check("stayed connected", false, "disconnected at client tick " + ticks + ": " + reason);
            IceAndFire.LOGGER.info("[nettest] client DONE, failed checks: {}", fails);
            mc.stop();
            return true;
        }

        static void tick(Minecraft mc) {
            joined = true;
            ticks++;
            if (finished) {
                return;
            }
            if (hydra == null || hydra.isRemoved() && removedAt < 0 && !sawDying) {
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof EntityHydra found && !found.isRemoved()) {
                        hydra = found;
                        if (firstHeads < 0) {
                            firstHeads = found.getHeadCount();
                            IceAndFire.LOGGER.info("[nettest] client: hydra {} in view with {} heads", found.getId(), firstHeads);
                        }
                    }
                }
            }
            if (hydra != null && removedAt < 0) {
                watch(mc);
            }
            if (removedAt > 0 && ticks == removedAt + 40) {
                verdict(mc);
            }
            if (ticks == 6000) {
                check("the fight ended in time", false, "client tick " + ticks + ", hydra " + (hydra == null ? "never seen" : "alive " + hydra.isAlive()));
                verdict(mc);
            }
        }

        private static void watch(Minecraft mc) {
            for (int i = 0; i < EntityHydra.HEADS; i++) {
                boolean breathing = hydra.isBreathing[i];
                if (breathing && !BREATHING[i]) {
                    breathStarted |= 1 << i;
                }
                if (!breathing && BREATHING[i]) {
                    breathEnded |= 1 << i;
                    breathEnds++;
                }
                BREATHING[i] = breathing;
                if (hydra.isStriking[i] && !STRIKING[i]) {
                    bitesSeen++;
                }
                STRIKING[i] = hydra.isStriking[i];
            }
            if (!breathReported && breathEnded == ALL_HEADS) {
                breathReported = true;
                IceAndFire.LOGGER.info("[nettest] client: every head of its hydra ended a breath, {} breath ends, client tick {}", breathEnds, ticks);
            }
            if (hydra.isDeadOrDying()) {
                sawDying = true;
            }
            if (hydra.isRemoved()) {
                removedAt = ticks;
                IceAndFire.LOGGER.info("[nettest] client: hydra removed ({}), seen dying {}", hydra.getRemovalReason(), sawDying);
                return;
            }
            // close to it: hit the nearest head part (or the body) with the sword every second, as a player does
            if (hydra.isAlive() && mc.player.distanceTo(hydra) < 6 && ticks % 20 == 0) {
                Entity target = hydra;
                double best = mc.player.getEyePosition().distanceTo(hydra.getBoundingBox().getCenter());
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity instanceof EntityHydraHead head && hydra.getUUID().equals(head.getParentId())) {
                        double d = mc.player.getEyePosition().distanceTo(head.getBoundingBox().getCenter());
                        if (d < best) {
                            best = d;
                            target = head;
                        }
                    }
                }
                mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getBoundingBox().getCenter());
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                attacks++;
                if (target != hydra) {
                    headAttacks++;
                }
            }
        }

        private static void verdict(Minecraft mc) {
            if (finished) {
                return;
            }
            finished = true;
            check("every head of the client's hydra ended its breath", breathEnded == ALL_HEADS,
                "heads started " + Integer.bitCount(breathStarted) + ", ended " + Integer.bitCount(breathEnded) + " of " + EntityHydra.HEADS + ", " + breathEnds + " ends, heads at first sight " + firstHeads);
            check("the client saw bites and sent hits", bitesSeen > 0 && attacks > 0, bitesSeen + " bites, " + attacks + " hits sent (" + headAttacks + " on head parts)");
            check("the hydra died on the client", sawDying && removedAt > 0, "seen dying " + sawDying + ", removed " + (removedAt > 0));
            check("stayed connected", mc.getConnection() != null && mc.getConnection().getConnection().isConnected(), "to the end");
            IceAndFire.LOGGER.info("[nettest] client DONE, failed checks: {}", fails);
            mc.level.disconnect();
            mc.disconnect();
            mc.stop();
        }
    }
}
