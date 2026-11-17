package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
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

    // Server
    private static ServerPlayer rider;
    private static int serverTicks = -1;
    private static EntityDragonBase dragon;
    private static com.github.alexthe666.iceandfire.entity.EntityDeathWorm worm;
    private static BlockPos sandCenter;
    private static BlockPos fieldBase;
    private static int offDragonTicks = -1;
    private static boolean wasOnDragon;

    public static void onLogin(net.minecraft.world.entity.player.Player joined) {
        if (MODE.equals("server") && NetTestHydra.ACTIVE && joined instanceof ServerPlayer player) {
            NetTestHydra.onLogin(player);
            return;
        }
        if (MODE.equals("server") && joined instanceof ServerPlayer player) {
            rider = player;
            serverTicks = 0;
        }
    }

    public static void onLogout(net.minecraft.world.entity.player.Player player) {
        if (MODE.equals("server") && NetTestHydra.ACTIVE) {
            NetTestHydra.onLogout(player);
            return;
        }
        if (MODE.equals("server") && player == rider) {
            IceAndFire.LOGGER.info("[nettest] server DONE");
            rider.level().getServer().halt(false);
        }
    }

    /** The sand field of the worm ride: 65 x 65, 6 deep, its top at the ground 40 blocks east of the start (dry land, unlike
     * where the dragon flight may end), built once the player is back there so its chunks are loaded. */
    private static void buildWormField() {
        net.minecraft.server.level.ServerLevel level = rider.level();
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
        worm = IafEntityRegistry.DEATH_WORM.get().create(level, EntitySpawnReason.COMMAND);
        worm.snapTo(sandCenter.getX() + 0.5, top + 1, sandCenter.getZ() + 0.5, 0, 0);
        worm.finalizeSpawn(level, level.getCurrentDifficultyAt(sandCenter), EntitySpawnReason.COMMAND, null);
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
        if (serverTicks == 60) {
            rider.setGameMode(GameType.CREATIVE);
            fieldBase = rider.blockPosition().offset(40, 0, 0);
            BlockPos pos = rider.blockPosition().offset(4, 0, 0);
            int y = rider.level().getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            dragon = IafEntityRegistry.FIRE_DRAGON.get().create(rider.level(), EntitySpawnReason.COMMAND);
            dragon.snapTo(pos.getX() + 0.5, y, pos.getZ() + 0.5, 0, 0);
            dragon.finalizeSpawn(rider.level(), rider.level().getCurrentDifficultyAt(pos), EntitySpawnReason.COMMAND, null);
            dragon.setAgeInDays(75);
            dragon.tame(rider);
            dragon.setPersistenceRequired();
            rider.level().addFreshEntity(dragon);
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
                boolean mounted = rider.startRiding(worm, true, true);
                IceAndFire.LOGGER.info("[nettest] worm mounted {} age {} scale {} at {}", mounted, worm.getWormAge(), worm.getDeathwormScale(), worm.blockPosition().toShortString());
            }
        }
        if (serverTicks == 80 && dragon != null) {
            boolean mounted = rider.startRiding(dragon, true, true);
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

        private static final java.util.Map<String, Integer> PACKETS = new java.util.concurrent.ConcurrentHashMap<>();
        private static boolean counterInstalled;

        private static void countVehicleMotion(Object packet) {
            if (packet instanceof net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket motion && motion.id() == vehicleId) {
                vehicleMotions++;
                lastVehicleMotion = motion.movement().toString();
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
                Screenshot.grab(new File("."), "nettest_flying.png", mc.getMainRenderTarget(), 1, c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
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
                Screenshot.grab(new File("."), "nettest_flying_third.png", mc.getMainRenderTarget(), 1, c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
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
                Screenshot.grab(new File("."), "nettest_worm.png", mc.getMainRenderTarget(), 1, c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
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
                finish(mc);
            }
        }

        private static void finish(Minecraft mc) {
            IceAndFire.LOGGER.info("[nettest] client DONE, failed checks: {}", fails);
            mc.disconnectFromWorld(net.minecraft.network.chat.Component.literal("nettest done"));
            mc.stop();
        }
    }
}
