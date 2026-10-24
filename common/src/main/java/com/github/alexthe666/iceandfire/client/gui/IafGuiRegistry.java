package com.github.alexthe666.iceandfire.client.gui;

import com.github.alexthe666.iceandfire.IceAndFire;
import dev.vladeezyy.iceandfire.bus.Dist;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.github.alexthe666.iceandfire.inventory.IafContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

@EventBusSubscriber(modid = IceAndFire.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IafGuiRegistry {

    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(IafContainerRegistry.IAF_LECTERN_CONTAINER.get(), GuiLectern::new);
        event.register(IafContainerRegistry.PODIUM_CONTAINER.get(), GuiPodium::new);
        event.register(IafContainerRegistry.DRAGON_CONTAINER.get(), GuiDragon::new);
        event.register(IafContainerRegistry.HIPPOGRYPH_CONTAINER.get(), GuiHippogryph::new);
        event.register(IafContainerRegistry.HIPPOCAMPUS_CONTAINER.get(), GuiHippocampus::new);
        event.register(IafContainerRegistry.DRAGON_FORGE_CONTAINER.get(), GuiDragonForge::new);
    }
}
