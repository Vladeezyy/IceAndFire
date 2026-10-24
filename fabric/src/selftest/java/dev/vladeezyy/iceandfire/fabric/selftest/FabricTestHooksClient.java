package dev.vladeezyy.iceandfire.fabric.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.selftest.DevScene;
import com.github.alexthe666.iceandfire.selftest.NetTest;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.util.ArrayList;
import java.util.List;

/** The client tests (scene, net test client) on Fabric's client ticks */
public final class FabricTestHooksClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevScene.OTHER_MODS = () -> {
            List<String> mods = new ArrayList<>();
            FabricLoader.getInstance().getAllMods().forEach(mod -> {
                String id = mod.getMetadata().getId();
                if (!id.startsWith("fabric") && !List.of("minecraft", "java", "mixinextras", IceAndFire.MODID, "iceandfire_selftest", "rampart").contains(id)) {
                    mods.add(id + " " + mod.getMetadata().getVersion().getFriendlyString());
                }
            });
            return mods;
        };
        ClientTickEvents.START_CLIENT_TICK.register(client -> NetTest.Client.onClientTickPre());
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            NetTest.Client.onClientTickPost();
            DevScene.onTick();
        });
    }
}
