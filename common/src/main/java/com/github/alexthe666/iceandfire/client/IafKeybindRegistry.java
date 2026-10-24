package com.github.alexthe666.iceandfire.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.ArrayUtils;

public class IafKeybindRegistry {
    public static KeyMapping dragon_fireAttack;
    public static KeyMapping dragon_strike;
    public static KeyMapping dragon_down;
    public static KeyMapping dragon_change_view;

    public static void init() {
        // Minecraft instance is null during data gen (NeoForge)
        if (Minecraft.getInstance() == null && dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.name().equals("neoforge"))
            return;
        dragon_fireAttack = new KeyMapping("key.dragon_fireAttack", 82, "key.categories.gameplay");
        dragon_strike = new KeyMapping("key.dragon_strike", 71, "key.categories.gameplay");
        dragon_down = new KeyMapping("key.dragon_down", 88, "key.categories.gameplay");
        dragon_change_view = new KeyMapping("key.dragon_change_view", 296, "key.categories.misc");
        // NeoForge: added to the options as in the original; Fabric: KeyBindingHelper (the options do not exist yet)
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_fireAttack);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_strike);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_down);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_change_view);
    }
}
