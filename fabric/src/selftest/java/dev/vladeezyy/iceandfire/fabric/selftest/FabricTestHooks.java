package dev.vladeezyy.iceandfire.fabric.selftest;

import com.github.alexthe666.iceandfire.selftest.NetTest;
import com.github.alexthe666.iceandfire.selftest.SelfTest;
import com.github.alexthe666.iceandfire.selftest.StressTest;
import com.github.alexthe666.iceandfire.selftest.TagDump;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** The common tests (common/src/selftest) on Fabric's events; dev runs only */
public final class FabricTestHooks implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            SelfTest.onStarted(server);
            StressTest.onStarted(server);
            TagDump.onStarted(server);
        });
        ServerTickEvents.START_SERVER_TICK.register(StressTest::onTickStart);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            SelfTest.onTick(server);
            StressTest.onTick(server);
            NetTest.onServerTick(server);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> NetTest.onLogin(handler.player));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> NetTest.onLogout(handler.player));
    }
}
