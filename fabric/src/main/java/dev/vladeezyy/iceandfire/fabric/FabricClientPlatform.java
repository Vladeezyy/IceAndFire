package dev.vladeezyy.iceandfire.fabric;

import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

/** The client side on Fabric (26.x: armor in IceAndFireFabricClient, render events from the common mixin) */
public final class FabricClientPlatform implements ClientPlatform {
    @Override
    public void registerKeyMapping(KeyMapping mapping) {
        KeyBindingHelper.registerKeyBinding(mapping);
    }
}
