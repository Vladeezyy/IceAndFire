package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;

import java.io.File;

/**
 * Riding a dragon on a dedicated server (#5192, #5208: the rider lags out). tools/nettest.sh starts the server
 * ({@code -Diceandfire.nettest=server}) and a client that joins it ({@code client}). The server gives the player an
 * adult tamed fire dragon and puts the player on it; the client flies up and forward, then gets off. Both log
 * "[nettest]" lines every second: server tick time, client tick time, fps, packets per second, game time.
 */
public class NetTest {
    private static final String MODE = System.getProperty("iceandfire.nettest", "");
    /** The test server: IAF_NETPORT=<port> when 25570 is taken (tools/nettest.sh) */
    static final String ADDRESS = "localhost:" + System.getenv().getOrDefault("IAF_NETPORT", "25570");
    /** IAF_NETTEST=corpse: only the dragon corpse check (#5297), without the rides before it */
    static final boolean CORPSE_ONLY = "corpse".equals(System.getenv("IAF_NETTEST"));

    // Server
    private static ServerPlayer rider;
    private static int serverTicks = -1;
    private static EntityDragonBase dragon;
    private static com.github.alexthe666.iceandfire.entity.EntityDeathWorm worm;
    private static BlockPos sandCenter;
    private static BlockPos fieldBase;
    private static int offDragonTicks = -1;
    private static boolean wasOnDragon;
    private static boolean wasOnWorm;
    private static int offWormTicks;
    // The corpse check (#5297): a dragon killed in view, then the client gets the corpse again after it was saved
    // with its chunk and loaded back, and once more after the client reconnects
    private static int corpseTicks = -1;
    private static EntityDragonBase corpse;
    private static java.util.UUID corpseId;
    private static BlockPos corpseStand;
    private static int corpseUnloadedAt = -1;
    private static int corpseRejoinedAt = -1;
    private static int corpseLeftAt = -1;
    private static int corpseBackAt = -1;
    private static boolean corpseReloaded;
    private static int corpseLogouts;

    public static void onLogin(net.minecraft.world.entity.player.Player joined) {
        if (MODE.equals("server") && NetTestHydra.ACTIVE && joined instanceof ServerPlayer player) {
            NetTestHydra.onLogin(player);
            return;
        }
        if (MODE.equals("server") && joined instanceof ServerPlayer player) {
            rider = player;
            if (corpseTicks >= 0) {
                corpseRejoinedAt = corpseTicks;
                IceAndFire.LOGGER.info("[nettest] server: the client joined again at {}", player.blockPosition().toShortString());
                return;
            }
            IceAndFire.LOGGER.info("[nettest] server scenario {}", CORPSE_ONLY ? "corpse only" : "dragon ride, worm ride, corpse");
            serverTicks = 0;
        }
    }

    public static void onLogout(net.minecraft.world.entity.player.Player player) {
        if (MODE.equals("server") && NetTestHydra.ACTIVE) {
            NetTestHydra.onLogout(player);
            return;
        }
        if (MODE.equals("server") && player == rider) {
            // the corpse check reconnects once: the first logout in it is not the end
            if (corpseTicks >= 0 && corpseLogouts++ == 0) {
                IceAndFire.LOGGER.info("[nettest] server: the client left to reconnect");
                return;
            }
            IceAndFire.LOGGER.info("[nettest] server DONE");
            rider.getServer().halt(false);
        }
    }

    /** Server state of a dragon for the log: what the client should get */
    static String dragonState(EntityDragonBase d) {
        return String.format("health %.1f, dying %s, model dead %s, dead progress %.1f, death stage %d, age %d days, pose %s, removed %s",
            d.getHealth(), d.isDeadOrDying(), d.isModelDead(), d.modelDeadProgress, d.getDeathStage(), d.getAgeInDays(), d.getPose(), d.getRemovalReason());
    }

    private static void teleport(BlockPos pos) {
        rider.connection.teleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, -90, 25);
    }

    /**
     * The corpse check, server side. The client gets the corpse fresh three times: from memory after it went out of
     * tracking range while the corpse's chunk stayed loaded (another player near it, spawn or forced chunks: the
     * corpse keeps its runtime state such as the dying pose), from memory again after the client reconnects, and
     * from disk after the chunk was unloaded and loaded back.
     */
    private static void corpseTick(MinecraftServer server) {
        corpseTicks++;
        net.minecraft.server.level.ServerLevel level = rider.serverLevel();
        if (corpseTicks == 1) {
            if (dragon != null) {
                dragon.discard();
            }
            if (worm != null) {
                worm.discard();
            }
            rider.setGameMode(GameType.CREATIVE);
            rider.getAbilities().flying = true;
            rider.onUpdateAbilities();
            // 600 blocks from the world spawn: the spawn chunks would keep the corpse loaded
            BlockPos spawn = level.getSharedSpawnPos();
            corpseStand = new BlockPos(spawn.getX() + 600, 200, spawn.getZ());
            teleport(corpseStand);
        }
        if (corpseTicks == 40) {
            // a flat stone yard, cleared above: the corpse lies in the open for the screenshots
            int top = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING, corpseStand.getX() + 14, corpseStand.getZ()), level.getSeaLevel() + 1);
            BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
            for (int x = -8; x <= 32; x++) {
                for (int z = -16; z <= 16; z++) {
                    for (int dy = -1; dy <= 30; dy++) {
                        level.setBlock(at.set(corpseStand.getX() + x, top + dy, corpseStand.getZ() + z),
                            dy < 0 ? net.minecraft.world.level.block.Blocks.STONE.defaultBlockState() : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
            corpseStand = new BlockPos(corpseStand.getX(), top + 6, corpseStand.getZ());
            corpse = IafEntityRegistry.FIRE_DRAGON.get().create(level);
            corpse.moveTo(corpseStand.getX() + 14.5, top, corpseStand.getZ() + 0.5, 90, 0);
            corpse.finalizeSpawn(level, level.getCurrentDifficultyAt(corpseStand), MobSpawnType.COMMAND, null);
            corpse.setAgeInDays(75);
            corpse.setPersistenceRequired();
            level.addFreshEntity(corpse);
            corpseId = corpse.getUUID();
            // the player hovers 14 blocks west of it, looking down at it
            teleport(corpseStand);
            IceAndFire.LOGGER.info("[nettest] corpse: dragon {} spawned at {}", corpseId, corpse.blockPosition().toShortString());
        }
        if (corpse == null) {
            return;
        }
        if (corpseTicks == 100) {
            corpse.hurt(level.damageSources().genericKill(), Float.MAX_VALUE);
            IceAndFire.LOGGER.info("[nettest] corpse: killed, server {}", dragonState(corpse));
        }
        net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(corpseStand.offset(14, 0, 0));
        BlockPos far = corpseStand.offset(3000, 40, 0);
        if (corpseTicks == 200) {
            IceAndFire.LOGGER.info("[nettest] corpse: leaving with the corpse's chunk kept loaded, server {}", dragonState(corpse));
            level.setChunkForced(chunk.x, chunk.z, true);
            teleport(far);
        }
        if (corpseTicks == 440) {
            IceAndFire.LOGGER.info("[nettest] corpse: back, the server kept it in memory ({}), {}", corpse.isRemoved() ? "removed!" : "loaded", dragonState(corpse));
            teleport(corpseStand);
        }
        // the client reconnects after its check of that copy; 200 ticks after it joined again: away until the
        // server unloads the corpse with its chunk, then back
        if (corpseRejoinedAt > 0 && corpseTicks == corpseRejoinedAt + 200) {
            IceAndFire.LOGGER.info("[nettest] corpse: leaving with the chunk free to unload, server {}", dragonState(corpse));
            level.setChunkForced(chunk.x, chunk.z, false);
            corpseLeftAt = corpseTicks;
            teleport(far);
        }
        if (corpseLeftAt > 0 && corpseUnloadedAt < 0 && corpse.isRemoved()) {
            corpseUnloadedAt = corpseTicks;
            IceAndFire.LOGGER.info("[nettest] corpse: the server unloaded it {} ticks after the player left, {}", corpseTicks - corpseLeftAt, dragonState(corpse));
        }
        if (corpseLeftAt > 0 && corpseBackAt < 0 && (corpseUnloadedAt > 0 && corpseTicks == corpseUnloadedAt + 40 || corpseTicks == corpseLeftAt + 1200)) {
            corpseBackAt = corpseTicks;
            if (corpseUnloadedAt < 0) {
                IceAndFire.LOGGER.info("[nettest] corpse: the server did not unload it in 60 s");
            }
            teleport(corpseStand);
        }
        if (corpseBackAt > 0 && !corpseReloaded) {
            Entity loaded = level.getEntity(corpseId);
            if (loaded instanceof EntityDragonBase reloaded) {
                corpseReloaded = true;
                IceAndFire.LOGGER.info("[nettest] corpse: on the server again ({}), {}", reloaded == corpse ? "same object, not unloaded" : "loaded from disk",
                    dragonState(reloaded));
                corpse = reloaded;
            } else if (corpseTicks - corpseBackAt == 400) {
                IceAndFire.LOGGER.info("[nettest] corpse: the server has no entity {} 20 s after coming back", corpseId);
            }
        }
        if (corpseTicks % 200 == 0) {
            IceAndFire.LOGGER.info("[nettest] corpse tick {}: server mspt {}, {}", corpseTicks, String.format("%.1f", server.getAverageTickTimeNanos() / 1.0E6), dragonState(corpse));
        }
        if (corpseTicks == 8000) {
            IceAndFire.LOGGER.info("[nettest] corpse: the client never finished, stopping");
            server.halt(false);
        }
    }

    /** The sand field of the worm ride: 65 x 65, 6 deep, its top at the ground 40 blocks east of the start (dry land, unlike
     * where the dragon flight may end), built once the player is back there so its chunks are loaded. */
    private static void buildWormField() {
        net.minecraft.server.level.ServerLevel level = rider.serverLevel();
        BlockPos base = fieldBase;
        // not below the sea, and walled in with stone: a shore or an ocean nearby drowned the worm
        int top = Math.max(level.getHeight(Heightmap.Types.MOTION_BLOCKING, base.getX(), base.getZ()) - 1, level.getSeaLevel());
        sandCenter = new BlockPos(base.getX(), top, base.getZ());
        BlockPos.MutableBlockPos at = new BlockPos.MutableBlockPos();
        for (int x = -33; x <= 33; x++) {
            for (int z = -33; z <= 33; z++) {
                boolean wall = Math.abs(x) == 33 || Math.abs(z) == 33;
                for (int dy = -6; dy <= 8; dy++) {
                    net.minecraft.world.level.block.state.BlockState state = wall || dy == -6 ? net.minecraft.world.level.block.Blocks.STONE.defaultBlockState()
                        : dy <= 0 ? net.minecraft.world.level.block.Blocks.SAND.defaultBlockState() : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                    level.setBlock(at.set(sandCenter.getX() + x, top + dy, sandCenter.getZ() + z), state, 2);
                }
            }
        }
        worm = IafEntityRegistry.DEATH_WORM.get().create(level);
        worm.moveTo(sandCenter.getX() + 0.5, top + 1, sandCenter.getZ() + 0.5, 0, 0);
        worm.finalizeSpawn(level, level.getCurrentDifficultyAt(sandCenter), MobSpawnType.COMMAND, null);
        worm.setWormAge(5);
        worm.setDeathWormScale(1.0F);
        worm.tame(rider);
        worm.setPersistenceRequired();
        level.addFreshEntity(worm);
    }

    /** What slows a ridden mob: effects on it and its rider, its speed attribute with modifiers, the block it stands in */
    static String rideState(net.minecraft.world.entity.LivingEntity mob, net.minecraft.world.entity.LivingEntity rider) {
        net.minecraft.world.entity.ai.attributes.AttributeInstance speed = mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        return String.format("y %.2f, on ground %s, health %.1f, speed attr %.3f (base %.3f, modifiers %s), getSpeed %.3f, mob effects %s, rider effects %s, in block %s, on block %s, in wall %s, eye in fluid %s, delta %s, rider shift %s",
            mob.getY(), mob.onGround(), mob.getHealth(), speed.getValue(), speed.getBaseValue(), speed.getModifiers(), mob.getSpeed(), mob.getActiveEffects(), rider.getActiveEffects(),
            mob.level().getBlockState(mob.blockPosition()).getBlock(), mob.level().getBlockState(mob.blockPosition().below()).getBlock(), mob.isInWall(),
            mob.isEyeInFluid(net.minecraft.tags.FluidTags.WATER), mob.getDeltaMovement(), rider.isShiftKeyDown());
    }

    public static void onServerTick(net.minecraft.server.MinecraftServer server) {
        if (NetTestHydra.ACTIVE) {
            NetTestHydra.serverTick(server);
            return;
        }
        if (serverTicks < 0 || rider == null) {
            return;
        }
        serverTicks++;
        if (CORPSE_ONLY && serverTicks == 60 && corpseTicks < 0) {
            corpseTicks = 0;
        }
        if (worm != null && rider.getVehicle() == worm) {
            wasOnWorm = true;
        }
        // after the worm ride: the corpse check, 60 ticks after the player got off
        if (wasOnWorm && corpseTicks < 0 && rider.getVehicle() == null && ++offWormTicks == 60) {
            corpseTicks = 0;
        }
        if (corpseTicks >= 0) {
            corpseTick(server);
            return;
        }
        if (CORPSE_ONLY) {
            return;
        }
        if (serverTicks == 60) {
            rider.setGameMode(GameType.CREATIVE);
            fieldBase = rider.blockPosition().offset(40, 0, 0);
            BlockPos pos = rider.blockPosition().offset(4, 0, 0);
            int y = rider.serverLevel().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            dragon = IafEntityRegistry.FIRE_DRAGON.get().create(rider.serverLevel());
            dragon.moveTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
            dragon.finalizeSpawn(rider.serverLevel(), rider.serverLevel().getCurrentDifficultyAt(pos), MobSpawnType.COMMAND, null);
            dragon.setAgeInDays(75);
            dragon.tame(rider);
            dragon.setPersistenceRequired();
            rider.serverLevel().addFreshEntity(dragon);
        }
        // the worm ride: 100 ticks after the player got off the dragon (the client checks the dismount 40 ticks after it lets go of shift)
        if (dragon != null && rider.getVehicle() == dragon) {
            wasOnDragon = true;
        }
        if (wasOnDragon && offDragonTicks < 0 && rider.getVehicle() == null) {
            offDragonTicks = 0;
        }
        if (offDragonTicks >= 0) {
            offDragonTicks++;
            if (offDragonTicks == 1) {
                // back to the start, hovering: no fall from the flight's height, and the field's chunks get loaded
                rider.getAbilities().flying = true;
                rider.onUpdateAbilities();
                rider.connection.teleport(fieldBase.getX() + 0.5, fieldBase.getY() + 12, fieldBase.getZ() + 0.5, rider.getYRot(), rider.getXRot());
            }
            if (offDragonTicks == 20) {
                buildWormField();
            }
            if (offDragonTicks == 100 && worm != null) {
                rider.teleportTo(worm.getX(), worm.getY() + 2, worm.getZ());
                boolean mounted = rider.startRiding(worm, true);
                IceAndFire.LOGGER.info("[nettest] worm mounted {} age {} scale {} at {}", mounted, worm.getWormAge(), worm.getDeathwormScale(), worm.blockPosition().toShortString());
            }
        }
        if (serverTicks == 80 && dragon != null) {
            boolean mounted = rider.startRiding(dragon, true);
            IceAndFire.LOGGER.info("[nettest] mounted {} stage {}", mounted, dragon.getDragonStage());
        }
        if (serverTicks % 20 == 0) {
            Entity vehicle = rider.getVehicle();
            IceAndFire.LOGGER.info("[nettest] server mspt {} riding {} dragon {} flying {} hovering {} control {}",
                String.format("%.1f", server.getAverageTickTimeNanos() / 1.0E6), vehicle != null,
                dragon == null ? "-" : dragon.blockPosition().toShortString(), dragon != null && dragon.isFlying(),
                dragon != null && dragon.isHovering(), dragon == null ? 0 : dragon.getControlState());
            if (worm != null && rider.getVehicle() == worm && serverTicks % 100 == 0) {
                IceAndFire.LOGGER.info("[nettest] server worm state {}", rideState(worm, rider));
            }
            if (worm != null && rider.getVehicle() == worm) {
                IceAndFire.LOGGER.info("[nettest] server worm at {} gameTime {} in sand {} rider at {}", String.format("%.2f %.2f %.2f", worm.getX(), worm.getY(), worm.getZ()),
                    server.overworld().getGameTime(), worm.isInSandStrict(), String.format("%.2f %.2f %.2f", rider.getX(), rider.getY(), rider.getZ()));
            }
        }
    }

    /** Client side: the dedicated server must not load it (Minecraft in the signatures). */
        public static class Client {
        // Client
        private static int riddenTicks = -1;
        private static int clientTicks;
        private static boolean joinTried;
        private static long tickStart;
        private static double tickMsSum;
        private static double tickMsMax;
        private static long lastGameTime = -1;
        private static long lastSecond;
        private static boolean dismounted;
        private static int fails;
        private static int wormTicks = -1;
        private static boolean dragonDone;
        private static net.minecraft.world.phys.Vec3 lastWormPos;
        private static int wormJumps;
        private static double wormMaxJump;
        private static double wormPath;
        private static int wormVehicleCorrections;
        private static int dragonVehicleCorrections;
        private static int riderOffMount;
        private static volatile int vehicleCorrections;
        /** Motion packets the server sends for the player's own vehicle: they overwrite the client's own motion */
        private static volatile int vehicleId = -1;
        private static volatile int vehicleMotions;
        private static volatile String lastVehicleMotion = "-";
        // The corpse check: the dragon the client saw alive and then dead (by UUID, which comes with the spawn packet,
        // not the synced data) and its age then; client entities of it first seen after that are fresh copies
        private static java.util.UUID testCorpse;
        private static int testCorpseAge;
        private static int diedInViewAt;
        // by object: Entity.equals compares the network id, and a reconnected client gets the corpse under the same id
        private static final java.util.Map<Entity, Boolean> SEEN_ALIVE = new java.util.IdentityHashMap<>();
        private static final java.util.Map<Entity, Boolean> FRESH_CORPSE = new java.util.IdentityHashMap<>();
        private static final java.util.Map<Entity, Boolean> FIRST_LOOK = new java.util.IdentityHashMap<>();
        private static int totalTicks;
        private static boolean wormDone;
        private static int corpseStartTick = -1;
        private static int corpseChecks;
        private static int reconnectAt = -1;
        private static boolean poseLogged;

        private static final java.util.Map<String, Integer> PACKETS = new java.util.concurrent.ConcurrentHashMap<>();
        private static boolean counterInstalled;

        private static void countVehicleMotion(Object packet) {
            if (packet instanceof net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket motion && motion.getId() == vehicleId) {
                vehicleMotions++;
                lastVehicleMotion = String.format("(%.3f, %.3f, %.3f)", motion.getXa(), motion.getYa(), motion.getZa());
            }
        }

        /** Counts the packets by type before the vanilla handler (bundles are counted by their content). */
        private static void installCounter(Minecraft mc) {
            if (counterInstalled) {
                return;
            }
            counterInstalled = true;
            channel(mc.getConnection().getConnection()).pipeline().addBefore("packet_handler", "nettest_counter", new io.netty.channel.ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket) {
                        vehicleCorrections++;
                    }
                    countVehicleMotion(msg);
                    if (msg instanceof net.minecraft.network.protocol.BundlePacket<?> motionBundle) {
                        for (Object packet : motionBundle.subPackets()) {
                            countVehicleMotion(packet);
                        }
                    }
                    if (msg instanceof net.minecraft.network.protocol.BundlePacket<?> bundle) {
                        for (Object packet : bundle.subPackets()) {
                            PACKETS.merge(name(packet), 1, Integer::sum);
                        }
                    } else {
                        PACKETS.merge(name(msg), 1, Integer::sum);
                    }
                    super.channelRead(ctx, msg);
                }
            });
        }

        /** The netty channel of the connection (NeoForge has a getter, vanilla a private field) */
        private static io.netty.channel.Channel channel(net.minecraft.network.Connection connection) {
            for (java.lang.reflect.Field field : net.minecraft.network.Connection.class.getDeclaredFields()) {
                if (field.getType() == io.netty.channel.Channel.class) {
                    try {
                        field.setAccessible(true);
                        return (io.netty.channel.Channel) field.get(connection);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }
            throw new IllegalStateException("No channel in " + connection);
        }

        private static String name(Object packet) {
            if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom) {
                return custom.payload().type().id().getPath();
            }
            return packet.getClass().getSimpleName().replace("Clientbound", "").replace("Packet", "");
        }

        public static void onClientTickPre() {
            if (MODE.equals("client")) {
                tickStart = System.nanoTime();
            }
        }

        public static void onClientTickPost() {
            if (!MODE.equals("client")) {
                return;
            }
            Minecraft mc = Minecraft.getInstance();
            double ms = (System.nanoTime() - tickStart) / 1.0E6;
            tickMsSum += ms;
            tickMsMax = Math.max(tickMsMax, ms);
            clientTicks++;
            totalTicks++;
            if (reconnectAt > 0 && (mc.player == null || mc.level == null) && totalTicks >= reconnectAt
                && !(mc.screen instanceof net.minecraft.client.gui.screens.ConnectScreen)) {
                reconnectAt = -1;
                IceAndFire.LOGGER.info("[nettest] corpse: connecting again");
                String address = ADDRESS;
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(new net.minecraft.client.gui.screens.TitleScreen(), mc,
                    net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                    new net.minecraft.client.multiplayer.ServerData("nettest", address, net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
                return;
            }
            if (mc.player == null || mc.level == null || mc.getConnection() == null) {
                if (NetTestHydra.ACTIVE && NetTestHydra.Client.offline(mc)) {
                    return;
                }
                // --quickPlayMultiplayer does not always join (seen on macOS): after 10 s on the title screen join from here
                if (!joinTried && clientTicks > 200 && mc.screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                    joinTried = true;
                    IceAndFire.LOGGER.info("[nettest] quick play did not join, connecting from the title screen");
                    String address = ADDRESS;
                    net.minecraft.client.gui.screens.ConnectScreen.startConnecting(mc.screen, mc, net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
                        new net.minecraft.client.multiplayer.ServerData("nettest", address, net.minecraft.client.multiplayer.ServerData.Type.OTHER), false, null);
                }
                return;
            }
            mc.options.pauseOnLostFocus = false;
            installCounter(mc);
            long now = System.currentTimeMillis();
            if (now - lastSecond >= 1000) {
                long gameTime = mc.level.getGameTime();
                IceAndFire.LOGGER.info("[nettest] client riding {} rideTick {} fps {} tick avg {} max {} ms rx {} tx {} gameTime +{} pos {}",
                    mc.player.getVehicle() != null, riddenTicks, mc.getFps(), String.format("%.1f", tickMsSum / Math.max(1, clientTicks)),
                    String.format("%.1f", tickMsMax), String.format("%.0f", mc.getConnection().getConnection().getAverageReceivedPackets()),
                    String.format("%.0f", mc.getConnection().getConnection().getAverageSentPackets()),
                    lastGameTime < 0 ? 0 : gameTime - lastGameTime, mc.player.blockPosition().toShortString());
                // The packet types the client received in this second, most first
                StringBuilder top = new StringBuilder();
                PACKETS.entrySet().stream().sorted((a, b) -> b.getValue() - a.getValue()).limit(6)
                    .forEach(e -> top.append(e.getKey()).append('=').append(e.getValue()).append(' '));
                IceAndFire.LOGGER.info("[nettest] client packets {}", top);
                PACKETS.clear();
                lastGameTime = gameTime;
                lastSecond = now;
                tickMsSum = 0;
                tickMsMax = 0;
                clientTicks = 0;
            }
            if (NetTestHydra.ACTIVE) {
                NetTestHydra.Client.tick(mc);
                return;
            }
            if (watchCorpses(mc)) {
                return;
            }
            if (riddenTicks < 0) {
                if (mc.player.getVehicle() instanceof EntityDragonBase) {
                    riddenTicks = 0;
                    vehicleCorrections = 0;
                }
                return;
            }
            if (dragonDone) {
                wormRide(mc);
                return;
            }
            riddenTicks++;
            boolean up = riddenTicks > 100 && riddenTicks < 300;
            boolean forward = riddenTicks > 100 && riddenTicks < 600;
            mc.options.keyJump.setDown(up);
            mc.options.keyUp.setDown(forward);
            if (riddenTicks == 450) {
                Screenshot.grab(new File("."), "nettest_flying.png", mc.getMainRenderTarget(), c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
            }
            // Where the rider is: feet and eyes against the dragon and its saddle point, and how it looks from behind
            if (riddenTicks % 100 == 50 && mc.player.getVehicle() instanceof EntityDragonBase dragon) {
                IceAndFire.LOGGER.info("[nettest] rider feet {} eyes {} above the dragon, saddle point {}, dragon height {}, render size {}, pitch {}",
                    String.format("%.2f", mc.player.getY() - dragon.getY()), String.format("%.2f", mc.player.getEyeY() - dragon.getY()),
                    String.format("%.2f", dragon.getRiderPosition().y - dragon.getY()), String.format("%.2f", dragon.getBbHeight()),
                    String.format("%.1f", dragon.getRenderSize()), String.format("%.1f", dragon.getDragonPitch()));
            }
            if (riddenTicks == 470) {
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            }
            if (riddenTicks == 490) {
                Screenshot.grab(new File("."), "nettest_flying_third.png", mc.getMainRenderTarget(), c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
            }
            if (riddenTicks == 500) {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            }
            if (riddenTicks == 650) {
                dragonVehicleCorrections = vehicleCorrections;
            }
            // Get off: shift as a player presses it
            mc.options.keyShift.setDown(riddenTicks >= 700 && riddenTicks < 720);
            if (riddenTicks == 760) {
                dismounted = mc.player.getVehicle() == null;
                if (!dismounted) {
                    fails++;
                }
                IceAndFire.LOGGER.info("[nettest] CHECK {} dismount", dismounted ? "OK" : "FAIL");
                IceAndFire.LOGGER.info("[nettest] dragon flight: vehicle position corrections from the server {}", dragonVehicleCorrections);
                dragonDone = true;
            }
        }

        /** The worm ride: forward for 30 s, turning slowly, so the worm circles in the sand field; then off and done. */
        private static void wormRide(Minecraft mc) {
            Entity vehicle = mc.player.getVehicle();
            if (wormTicks < 0) {
                riddenTicks++;
                if (vehicle instanceof com.github.alexthe666.iceandfire.entity.EntityDeathWorm) {
                    wormTicks = 0;
                    vehicleCorrections = 0;
                    lastWormPos = vehicle.position();
                } else if (riddenTicks > 1400) {
                    fails++;
                    IceAndFire.LOGGER.info("[nettest] CHECK FAIL worm ride: the server never put the player on the worm");
                    finish(mc);
                }
                return;
            }
            wormTicks++;
            boolean riding = vehicle instanceof com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
            boolean go = wormTicks > 20 && wormTicks <= 620;
            mc.options.keyUp.setDown(go && riding);
            if (go && riding) {
                mc.player.setYRot(mc.player.getYRot() + 1.5F);
            }
            if (riding) {
                net.minecraft.world.phys.Vec3 pos = vehicle.position();
                double jump = pos.distanceTo(lastWormPos);
                // the path counts horizontal steps only: a worm that sinks and is pushed back up goes nowhere
                wormPath += Math.hypot(pos.x - lastWormPos.x, pos.z - lastWormPos.z);
                wormMaxJump = Math.max(wormMaxJump, jump);
                // the first ticks after the mount the client catches up with the worm's position after the teleport
                if (jump > 2 && wormTicks > 3) {
                    wormJumps++;
                    IceAndFire.LOGGER.info("[nettest] worm jump {} blocks at worm tick {}: {} -> {}, on ground {}, in wall {}", String.format("%.2f", jump), wormTicks,
                        String.format("%.2f %.2f %.2f", lastWormPos.x, lastWormPos.y, lastWormPos.z), String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z),
                        vehicle.onGround(), vehicle.isInWall());
                }
                lastWormPos = pos;
                if (mc.player.position().distanceTo(pos) > 6) {
                    riderOffMount++;
                }
                if (wormTicks % 20 == 0) {
                    IceAndFire.LOGGER.info("[nettest] client worm at {} gameTime {} rider at {}", String.format("%.2f %.2f %.2f", pos.x, pos.y, pos.z),
                        mc.level.getGameTime(), String.format("%.2f %.2f %.2f", mc.player.getX(), mc.player.getY(), mc.player.getZ()));
                }
            }
            if (riding) {
                vehicleId = vehicle.getId();
            }
            if (riding && wormTicks % 100 == 50) {
                IceAndFire.LOGGER.info("[nettest] client worm state {}, motion packets for the worm so far {} (last {})", rideState((net.minecraft.world.entity.LivingEntity) vehicle, mc.player),
                    vehicleMotions, lastVehicleMotion);
            }
            if (wormTicks == 330) {
                mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
            }
            if (wormTicks == 350) {
                Screenshot.grab(new File("."), "nettest_worm.png", mc.getMainRenderTarget(), c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
            }
            if (wormTicks == 360) {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            }
            if (wormTicks == 640) {
                wormVehicleCorrections = vehicleCorrections;
                boolean moved = wormPath > 20;
                boolean steady = wormJumps == 0 && riderOffMount == 0 && wormVehicleCorrections <= 20;
                if (!moved || !steady) {
                    fails++;
                }
                IceAndFire.LOGGER.info("[nettest] CHECK {} worm ride: path {} blocks in 30 s, still riding {}, position jumps over 2 blocks {} (max {}), rider ticks away from the worm {}, vehicle position corrections from the server {}, motion packets for the worm {}",
                    moved && steady ? "OK" : "FAIL", String.format("%.1f", wormPath), riding, wormJumps, String.format("%.2f", wormMaxJump), riderOffMount, wormVehicleCorrections, vehicleMotions);
            }
            // off the worm: the dismount key of the mod is the shift key here too
            mc.options.keyShift.setDown(wormTicks >= 660 && wormTicks < 680);
            if (wormTicks == 720) {
                // the server starts the corpse check 60 ticks after the player got off
                wormDone = true;
            }
        }

        /**
         * The corpse check (#5297): the server kills a dragon in view, then sends the player far away (the corpse
         * is unloaded and saved) and back, and the client reconnects once. Every client copy of the corpse made after
         * the client saw it dead must start dead: dead flag, dead pose fully on (progress 20 on the first look and after 60 ticks),
         * its age, and the model posed dead by the dragon animator. Three copies: after leaving tracking range (corpse
         * kept in the server's memory), after reconnecting, after the server unloaded the corpse and loaded it again. Returns true while it runs (the rides are over).
         */
        private static boolean watchCorpses(Minecraft mc) {
            boolean active = CORPSE_ONLY || wormDone;
            if (!active) {
                return false;
            }
            if (corpseStartTick < 0) {
                corpseStartTick = totalTicks;
                IceAndFire.LOGGER.info("[nettest] client: corpse check");
            }
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (!(entity instanceof EntityDragonBase d)) {
                    continue;
                }
                java.util.UUID id = d.getUUID();
                Boolean fresh = FRESH_CORPSE.get(d);
                if (fresh == null) {
                    fresh = id.equals(testCorpse);
                    FRESH_CORPSE.put(d, fresh);
                    IceAndFire.LOGGER.info("[nettest] client: dragon {} came into view ({}), tick count {}, {}", d.getId(),
                        fresh ? "the corpse the client saw die" : "first time or another dragon", d.tickCount, clientState(d));
                    if (!d.isModelDead()) {
                        SEEN_ALIVE.put(d, Boolean.TRUE);
                    }
                }
                // the test corpse: the dragon the client saw alive and then dead (dragon skeletons of the world come dead)
                if (testCorpse == null && d.isModelDead() && SEEN_ALIVE.containsKey(d)) {
                    testCorpse = id;
                    testCorpseAge = d.getAgeInDays();
                    diedInViewAt = totalTicks;
                    IceAndFire.LOGGER.info("[nettest] client: dragon {} died in view, {}", d.getId(), clientState(d));
                }
                if (id.equals(testCorpse) && !fresh && totalTicks - diedInViewAt == 60) {
                    corpseVerdict(mc, d, "died in view, 60 ticks later", testCorpseAge, false);
                }
                boolean firstLook = fresh && FIRST_LOOK.put(d, Boolean.TRUE) == null;
                if (fresh && (firstLook || d.tickCount == 60)) {
                    boolean last = !firstLook && d.tickCount == 60;
                    corpseVerdict(mc, d, "received again (" + (corpseChecks == 0 ? "out of range and back" : corpseChecks == 1 ? "reconnected" : "unloaded and loaded") + "), "
                        + (firstLook ? "first look at client tick count " : "tick count ") + d.tickCount, testCorpseAge, last);
                    if (last) {
                        corpseChecks++;
                        if (corpseChecks == 1) {
                            // the first fresh copy came from the server's memory: now reconnect
                            IceAndFire.LOGGER.info("[nettest] corpse: disconnecting to reconnect");
                            joinTried = true;
                            reconnectAt = totalTicks + 40;
                            mc.level.disconnect();
                            mc.disconnect(new net.minecraft.client.gui.screens.TitleScreen());
                            return true;
                        }
                        if (corpseChecks == 3) {
                            finish(mc);
                            return true;
                        }
                    }
                }
            }
            if (totalTicks - corpseStartTick == 6000) {
                fails++;
                IceAndFire.LOGGER.info("[nettest] CHECK FAIL corpse: only {} fresh copies of the corpse checked in 300 s", corpseChecks);
                finish(mc);
            }
            return true;
        }

        static String clientState(EntityDragonBase d) {
            return String.format("health %.1f, model dead %s, dead progress %.1f (prev %.1f), death stage %d, age %d days, render size %.1f, pose %s",
                d.getHealth(), d.isModelDead(), d.modelDeadProgress, d.prevModelDeadProgress, d.getDeathStage(), d.getAgeInDays(), d.getRenderSize(), d.getPose());
        }

        private static void corpseVerdict(Minecraft mc, EntityDragonBase d, String when, int age, boolean screenshot) {
            float[] pose = deadPose(mc, d);
            boolean dead = d.isModelDead() && d.modelDeadProgress >= 20.0F && d.prevModelDeadProgress >= 20.0F;
            boolean sameAge = d.getAgeInDays() == age;
            // the model's cubes match the dead pose (radians summed over all cubes; the animator's angle transition
            // leaves about 1 % of it) and are away from the standing pose
            boolean posed = pose != null && pose[1] > 1.0F && pose[0] < pose[1] * 0.03F;
            boolean ok = dead && sameAge && posed;
            if (!ok) {
                fails++;
            }
            IceAndFire.LOGGER.info("[nettest] CHECK {} corpse {}: {}, age then {}, model off the dead pose {} rad, off the standing pose {} rad",
                ok ? "OK" : "FAIL", when, clientState(d), age, pose == null ? "-" : String.format("%.3f", pose[0]), pose == null ? "-" : String.format("%.3f", pose[1]));
            if (screenshot) {
                String name = "nettest_corpse_" + (corpseChecks + 1) + ".png";
                Screenshot.grab(new File("."), name, mc.getMainRenderTarget(), c -> IceAndFire.LOGGER.info("[nettest] screenshot {}", name));
            }
        }

        /** Poses the dragon's model as the renderer does and measures it against the dead pose and the standing pose */
        private static float[] deadPose(Minecraft mc, EntityDragonBase d) {
            if (!(mc.getEntityRenderDispatcher().getRenderer(d) instanceof net.minecraft.client.renderer.entity.LivingEntityRenderer<?, ?> renderer)
                || !(renderer.getModel() instanceof dev.vladeezyy.rampart.client.model.TabulaModel model)) {
                return null;
            }
            com.github.alexthe666.iceandfire.client.model.util.EnumDragonModelTypes type = d instanceof com.github.alexthe666.iceandfire.entity.EntityIceDragon
                ? com.github.alexthe666.iceandfire.client.model.util.EnumDragonModelTypes.ICE_DRAGON_MODEL
                : d instanceof com.github.alexthe666.iceandfire.entity.EntityLightningDragon
                ? com.github.alexthe666.iceandfire.client.model.util.EnumDragonModelTypes.LIGHTNING_DRAGON_MODEL
                : com.github.alexthe666.iceandfire.client.model.util.EnumDragonModelTypes.FIRE_DRAGON_MODEL;
            dev.vladeezyy.rampart.client.model.TabulaModel dead = com.github.alexthe666.iceandfire.client.model.util.DragonAnimationsLibrary.getModel(
                com.github.alexthe666.iceandfire.client.model.util.EnumDragonPoses.DEAD, type);
            model.setupAnim(d, 0, 0, d.tickCount, 0, 0);
            float offDead = 0;
            float offStanding = 0;
            StringBuilder worst = new StringBuilder();
            for (java.util.Map.Entry<String, dev.vladeezyy.rampart.client.model.AdvancedModelBox> e : model.getCubes().entrySet()) {
                dev.vladeezyy.rampart.client.model.AdvancedModelBox cube = e.getValue();
                dev.vladeezyy.rampart.client.model.AdvancedModelBox to = dead.getCube(e.getKey());
                if (to == null) {
                    continue;
                }
                float off = Math.abs(Mth.wrapDegrees((cube.rotateAngleX - to.rotateAngleX) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD
                    + Math.abs(Mth.wrapDegrees((cube.rotateAngleY - to.rotateAngleY) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD
                    + Math.abs(Mth.wrapDegrees((cube.rotateAngleZ - to.rotateAngleZ) * Mth.RAD_TO_DEG)) * Mth.DEG_TO_RAD;
                offDead += off;
                if (off > 0.02F) {
                    worst.append(e.getKey()).append(String.format(" %.2f (%.2f %.2f %.2f / %.2f %.2f %.2f); ", off, cube.rotateAngleX, cube.rotateAngleY, cube.rotateAngleZ,
                        to.rotateAngleX, to.rotateAngleY, to.rotateAngleZ));
                }
                offStanding += Math.abs(cube.rotateAngleX - cube.defaultRotationX) + Math.abs(cube.rotateAngleY - cube.defaultRotationY)
                    + Math.abs(cube.rotateAngleZ - cube.defaultRotationZ);
            }
            if (!worst.isEmpty() && !poseLogged) {
                poseLogged = true;
                IceAndFire.LOGGER.info("[nettest] corpse: cubes off the dead pose (now / dead pose): {}", worst);
            }
            return new float[]{offDead, offStanding};
        }

        private static void finish(Minecraft mc) {
            IceAndFire.LOGGER.info("[nettest] client DONE, failed checks: {}", fails);
            mc.level.disconnect();
            mc.disconnect();
            mc.stop();
        }
    }
}
