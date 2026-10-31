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
        // NeoForge 26.x constructs the mod before Minecraft exists: the loader adds the keys when it asks for them
        dragon_fireAttack = new KeyMapping("key.dragon_fireAttack", 82, KeyMapping.Category.GAMEPLAY);
        dragon_strike = new KeyMapping("key.dragon_strike", 71, KeyMapping.Category.GAMEPLAY);
        dragon_down = new KeyMapping("key.dragon_down", 88, KeyMapping.Category.GAMEPLAY);
        dragon_change_view = new KeyMapping("key.dragon_change_view", 296, KeyMapping.Category.MISC);
        // NeoForge: added to the options as in the original; Fabric: KeyBindingHelper (the options do not exist yet)
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_fireAttack);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_strike);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_down);
        dev.vladeezyy.iceandfire.platform.ClientPlatform.INSTANCE.registerKeyMapping(dragon_change_view);
    }
}
