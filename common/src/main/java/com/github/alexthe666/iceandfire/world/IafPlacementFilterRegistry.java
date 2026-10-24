package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

public class IafPlacementFilterRegistry {
    public static final Registrar<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = Registrar.create(Registries.PLACEMENT_MODIFIER_TYPE, IceAndFire.MODID);

    public static RegistryEntry<PlacementModifierType<?>, PlacementModifierType<CustomBiomeFilter>> CUSTOM_BIOME_FILTER = PLACEMENT_MODIFIER_TYPES.register("biome_extended", () -> () -> CustomBiomeFilter.CODEC);
}
