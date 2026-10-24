package com.github.alexthe666.iceandfire.loot;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IafLootRegistry {

    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, IceAndFire.MODID);

    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToDragon>> CUSTOMIZE_TO_DRAGON =
            LOOT_FUNCTIONS.register("customize_to_dragon", () -> new LootItemFunctionType<>(CustomizeToDragon.CODEC));
    public static final DeferredHolder<LootItemFunctionType<?>, LootItemFunctionType<CustomizeToSeaSerpent>> CUSTOMIZE_TO_SERPENT =
            LOOT_FUNCTIONS.register("customize_to_sea_serpent", () -> new LootItemFunctionType<>(CustomizeToSeaSerpent.CODEC));

}
