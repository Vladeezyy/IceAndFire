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

    // Server
    private static ServerPlayer rider;
    private static int serverTicks = -1;
    private static EntityDragonBase dragon;

    public static void onLogin(net.minecraft.world.entity.player.Player joined) {
        if (MODE.equals("server") && joined instanceof ServerPlayer player) {
            rider = player;
            serverTicks = 0;
        }
    }

    public static void onLogout(net.minecraft.world.entity.player.Player player) {
        if (MODE.equals("server") && player == rider) {
            IceAndFire.LOGGER.info("[nettest] server DONE");
            rider.level().getServer().halt(false);
        }
    }

    public static void onServerTick(net.minecraft.server.MinecraftServer server) {
        if (serverTicks < 0 || rider == null) {
            return;
        }
        serverTicks++;
        if (serverTicks == 60) {
            rider.setGameMode(GameType.CREATIVE);
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

        private static final java.util.Map<String, Integer> PACKETS = new java.util.concurrent.ConcurrentHashMap<>();
        private static boolean counterInstalled;

        /** Counts the packets by type before the vanilla handler (bundles are counted by their content). */
        private static void installCounter(Minecraft mc) {
            if (counterInstalled) {
                return;
            }
            counterInstalled = true;
            channel(mc.getConnection().getConnection()).pipeline().addBefore("packet_handler", "nettest_counter", new io.netty.channel.ChannelInboundHandlerAdapter() {
                @Override
                public void channelRead(io.netty.channel.ChannelHandlerContext ctx, Object msg) throws Exception {
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
                // --quickPlayMultiplayer does not always join (seen on macOS): after 10 s on the title screen join from here
                if (!joinTried && clientTicks > 200 && mc.gui.screen() instanceof net.minecraft.client.gui.screens.TitleScreen) {
                    joinTried = true;
                    IceAndFire.LOGGER.info("[nettest] quick play did not join, connecting from the title screen");
                    String address = "localhost:25570";
                    net.minecraft.client.gui.screens.ConnectScreen.startConnecting(mc.gui.screen(), mc, net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(address),
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
            if (riddenTicks < 0) {
                if (mc.player.getVehicle() instanceof EntityDragonBase) {
                    riddenTicks = 0;
                }
                return;
            }
            riddenTicks++;
            boolean up = riddenTicks > 100 && riddenTicks < 300;
            boolean forward = riddenTicks > 100 && riddenTicks < 600;
            mc.options.keyJump.setDown(up);
            mc.options.keyUp.setDown(forward);
            if (riddenTicks == 450) {
                Screenshot.grab(new File("."), "nettest_flying.png", mc.gameRenderer.mainRenderTarget(), 1, c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
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
                Screenshot.grab(new File("."), "nettest_flying_third.png", mc.gameRenderer.mainRenderTarget(), 1, c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
            }
            if (riddenTicks == 500) {
                mc.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            }
            // Get off: shift as a player presses it
            mc.options.keyShift.setDown(riddenTicks >= 700 && riddenTicks < 720);
            if (riddenTicks == 760) {
                dismounted = mc.player.getVehicle() == null;
                if (!dismounted) {
                    fails++;
                }
                IceAndFire.LOGGER.info("[nettest] CHECK {} dismount", dismounted ? "OK" : "FAIL");
            }
            if (riddenTicks == 800) {
                IceAndFire.LOGGER.info("[nettest] client DONE, failed checks: {}", fails);
                mc.disconnectFromWorld(net.minecraft.network.chat.Component.literal("nettest done"));
                mc.stop();
            }
        }
    }
}
