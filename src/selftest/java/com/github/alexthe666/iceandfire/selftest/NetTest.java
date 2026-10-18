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
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.io.File;

/**
 * Riding a dragon on a dedicated server (#5192, #5208: the rider lags out). tools/nettest.sh starts the server
 * ({@code -Diceandfire.nettest=server}) and a client that joins it ({@code client}). The server gives the player an
 * adult tamed fire dragon and puts the player on it; the client flies up and forward, then gets off. Both log
 * "[nettest]" lines every second: server tick time, client tick time, fps, packets per second, game time.
 */
@EventBusSubscriber(modid = IceAndFire.MODID)
public class NetTest {
    private static final String MODE = System.getProperty("iceandfire.nettest", "");

    // Server
    private static ServerPlayer rider;
    private static int serverTicks = -1;
    private static EntityDragonBase dragon;

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (MODE.equals("server") && event.getEntity() instanceof ServerPlayer player) {
            rider = player;
            serverTicks = 0;
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (MODE.equals("server") && event.getEntity() == rider) {
            IceAndFire.LOGGER.info("[nettest] server DONE");
            rider.getServer().halt(false);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (serverTicks < 0 || rider == null) {
            return;
        }
        serverTicks++;
        MinecraftServer server = event.getServer();
        if (serverTicks == 60) {
            rider.setGameMode(GameType.CREATIVE);
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
        }
    }

    // Client
    private static int riddenTicks = -1;
    private static int clientTicks;
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
        mc.getConnection().getConnection().channel().pipeline().addBefore("packet_handler", "nettest_counter", new io.netty.channel.ChannelInboundHandlerAdapter() {
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

    private static String name(Object packet) {
        if (packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom) {
            return custom.payload().type().id().getPath();
        }
        return packet.getClass().getSimpleName().replace("Clientbound", "").replace("Packet", "");
    }

    @SubscribeEvent
    public static void onClientTickPre(ClientTickEvent.Pre event) {
        if (MODE.equals("client")) {
            tickStart = System.nanoTime();
        }
    }

    @SubscribeEvent
    public static void onClientTickPost(ClientTickEvent.Post event) {
        if (!MODE.equals("client")) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        double ms = (System.nanoTime() - tickStart) / 1.0E6;
        tickMsSum += ms;
        tickMsMax = Math.max(tickMsMax, ms);
        clientTicks++;
        if (mc.player == null || mc.level == null || mc.getConnection() == null) {
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
            Screenshot.grab(new File("."), "nettest_flying.png", mc.getMainRenderTarget(), c -> IceAndFire.LOGGER.info("[nettest] screenshot"));
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
            mc.level.disconnect();
            mc.disconnect();
            mc.stop();
        }
    }
}
