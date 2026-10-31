package com.github.alexthe666.iceandfire.world;
import com.mojang.serialization.MapCodec;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

public class IafPlacementFilterRegistry {
    // 26.3: the placement modifier registry holds the codecs
    public static final Registrar<MapCodec<? extends net.minecraft.world.level.levelgen.placement.PlacementModifier>> PLACEMENT_MODIFIER_TYPES = Registrar.create(Registries.PLACEMENT_MODIFIER_TYPE, IceAndFire.MODID);

    public static RegistryEntry<MapCodec<? extends net.minecraft.world.level.levelgen.placement.PlacementModifier>, MapCodec<CustomBiomeFilter>> CUSTOM_BIOME_FILTER = PLACEMENT_MODIFIER_TYPES.register("biome_extended", () -> CustomBiomeFilter.CODEC);
}
