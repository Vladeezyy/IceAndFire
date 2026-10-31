package com.github.alexthe666.iceandfire.loot;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;
import dev.vladeezyy.iceandfire.platform.Registrar;

public class IafLootRegistry {

    // 26.x: the loot function registry holds the functions' codecs (no LootItemFunctionType)
    public static final Registrar<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = Registrar.create(Registries.LOOT_FUNCTION_TYPE, IceAndFire.MODID);

    public static final RegistryEntry<MapCodec<? extends LootItemFunction>, MapCodec<CustomizeToDragon>> CUSTOMIZE_TO_DRAGON =
            LOOT_FUNCTIONS.register("customize_to_dragon", () -> CustomizeToDragon.CODEC);
    public static final RegistryEntry<MapCodec<? extends LootItemFunction>, MapCodec<CustomizeToSeaSerpent>> CUSTOMIZE_TO_SERPENT =
            LOOT_FUNCTIONS.register("customize_to_sea_serpent", () -> CustomizeToSeaSerpent.CODEC);

}
