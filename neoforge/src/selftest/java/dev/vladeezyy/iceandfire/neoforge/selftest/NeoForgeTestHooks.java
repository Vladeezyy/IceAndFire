package dev.vladeezyy.iceandfire.neoforge.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.selftest.DevScene;
import com.github.alexthe666.iceandfire.selftest.NetTest;
import com.github.alexthe666.iceandfire.selftest.SelfTest;
import com.github.alexthe666.iceandfire.selftest.StressTest;
import com.github.alexthe666.iceandfire.selftest.TagDump;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;

/** The common tests (common/src/selftest) on NeoForge's events; dev runs only */
@EventBusSubscriber(modid = IceAndFire.MODID)
public final class NeoForgeTestHooks {
    private NeoForgeTestHooks() {
    }

    @SubscribeEvent
    public static void onStarted(ServerStartedEvent event) {
        SelfTest.onStarted(event.getServer());
        StressTest.onStarted(event.getServer());
        TagDump.onStarted(event.getServer());
    }

    @SubscribeEvent
    public static void onTickStart(ServerTickEvent.Pre event) {
        StressTest.onTickStart(event.getServer());
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) {
        SelfTest.onTick(event.getServer());
        StressTest.onTick(event.getServer());
        NetTest.onServerTick(event.getServer());
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        NetTest.onLogin(event.getEntity());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        NetTest.onLogout(event.getEntity());
    }

    @EventBusSubscriber(modid = IceAndFire.MODID, value = Dist.CLIENT)
    public static final class Client {
        static {
            DevScene.OTHER_MODS = () -> {
                List<String> mods = new ArrayList<>();
                ModList.get().forEachModContainer((id, container) -> {
                    if (!List.of("minecraft", "neoforge", IceAndFire.MODID, "rampart").contains(id)) {
                        mods.add(id + " " + container.getModInfo().getVersion());
                    }
                });
                return mods;
            };
        }

        private Client() {
        }

        @SubscribeEvent
        public static void onClientTickPre(ClientTickEvent.Pre event) {
            NetTest.Client.onClientTickPre();
        }

        @SubscribeEvent
        public static void onClientTickPost(ClientTickEvent.Post event) {
            NetTest.Client.onClientTickPost();
            DevScene.onTick();
        }
    }
}
