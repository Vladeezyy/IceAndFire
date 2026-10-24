package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.function.Supplier;

public class IafContainerRegistry {

    public static final Registrar<MenuType<?>> CONTAINERS = Registrar
        .create(BuiltInRegistries.MENU, IceAndFire.MODID);

    public static final RegistryEntry<MenuType<?>, MenuType<ContainerLectern>> IAF_LECTERN_CONTAINER = register(
        () -> new MenuType<>(ContainerLectern::new, FeatureFlags.VANILLA_SET), "iaf_lectern");
    public static final RegistryEntry<MenuType<?>, MenuType<ContainerPodium>> PODIUM_CONTAINER = register(
        () -> new MenuType<>(ContainerPodium::new, FeatureFlags.VANILLA_SET), "podium");
    public static final RegistryEntry<MenuType<?>, MenuType<ContainerDragon>> DRAGON_CONTAINER = register(
        () -> new MenuType<>(ContainerDragon::new, FeatureFlags.VANILLA_SET), "dragon");
    public static final RegistryEntry<MenuType<?>, MenuType<ContainerHippogryph>> HIPPOGRYPH_CONTAINER = register(
        () -> new MenuType<>(ContainerHippogryph::new, FeatureFlags.VANILLA_SET), "hippogryph");
    public static final RegistryEntry<MenuType<?>, MenuType<HippocampusContainerMenu>> HIPPOCAMPUS_CONTAINER = register(
        () -> new MenuType<>(HippocampusContainerMenu::new, FeatureFlags.VANILLA_SET), "hippocampus");
    public static final RegistryEntry<MenuType<?>, MenuType<ContainerDragonForge>> DRAGON_FORGE_CONTAINER = register(
        () -> new MenuType<>(ContainerDragonForge::new, FeatureFlags.VANILLA_SET), "dragon_forge");

    public static <C extends AbstractContainerMenu> RegistryEntry<MenuType<?>, MenuType<C>> register(Supplier<MenuType<C>> type,
                                                                                         String name) {
        return CONTAINERS.register(name, type);
    }

}
