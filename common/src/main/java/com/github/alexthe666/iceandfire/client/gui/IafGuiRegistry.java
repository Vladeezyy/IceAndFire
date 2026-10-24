package com.github.alexthe666.iceandfire.client.gui;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.inventory.IafContainerRegistry;
import net.minecraft.client.gui.screens.MenuScreens;

public class IafGuiRegistry {

    /** Receives the menu screens (NeoForge: RegisterMenuScreensEvent, Fabric: MenuScreens) */
    public interface ScreenSink {
        <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, MenuScreens.ScreenConstructor<M, U> constructor);
    }

    public static void register(ScreenSink event) {
        event.register(IafContainerRegistry.IAF_LECTERN_CONTAINER.get(), GuiLectern::new);
        event.register(IafContainerRegistry.PODIUM_CONTAINER.get(), GuiPodium::new);
        event.register(IafContainerRegistry.DRAGON_CONTAINER.get(), GuiDragon::new);
        event.register(IafContainerRegistry.HIPPOGRYPH_CONTAINER.get(), GuiHippogryph::new);
        event.register(IafContainerRegistry.HIPPOCAMPUS_CONTAINER.get(), GuiHippocampus::new);
        event.register(IafContainerRegistry.DRAGON_FORGE_CONTAINER.get(), GuiDragonForge::new);
    }
}
