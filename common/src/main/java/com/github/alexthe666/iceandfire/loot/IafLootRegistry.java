package com.github.alexthe666.iceandfire.loot;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;
import dev.vladeezyy.iceandfire.platform.Registrar;

public class IafLootRegistry {

    public static final Registrar<LootItemFunctionType<?>> LOOT_FUNCTIONS = Registrar.create(Registries.LOOT_FUNCTION_TYPE, IceAndFire.MODID);

    public static final RegistryEntry<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToDragon>> CUSTOMIZE_TO_DRAGON =
            LOOT_FUNCTIONS.register("customize_to_dragon", () -> new LootItemFunctionType<>(CustomizeToDragon.CODEC));
    public static final RegistryEntry<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToSeaSerpent>> CUSTOMIZE_TO_SERPENT =
            LOOT_FUNCTIONS.register("customize_to_sea_serpent", () -> new LootItemFunctionType<>(CustomizeToSeaSerpent.CODEC));

}
