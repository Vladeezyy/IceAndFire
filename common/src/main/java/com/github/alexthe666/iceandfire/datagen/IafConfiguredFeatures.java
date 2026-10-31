package com.github.alexthe666.iceandfire.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;


public final class IafConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> FIRE_DRAGON_ROOST = registerKey("fire_dragon_roost");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ICE_DRAGON_ROOST = registerKey("ice_dragon_roost");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIGHTNING_DRAGON_ROOST = registerKey("lightning_dragon_roost");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FIRE_DRAGON_CAVE = registerKey("fire_dragon_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ICE_DRAGON_CAVE = registerKey("ice_dragon_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIGHTNING_DRAGON_CAVE = registerKey("lightning_dragon_cave");
    //TODO: Should be a structure
    public static final ResourceKey<ConfiguredFeature<?, ?>> CYCLOPS_CAVE = registerKey("cyclops_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PIXIE_VILLAGE = registerKey("pixie_village");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SIREN_ISLAND = registerKey("siren_island");
    public static final ResourceKey<ConfiguredFeature<?, ?>> HYDRA_CAVE = registerKey("hydra_cave");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MYRMEX_HIVE_DESERT = registerKey("myrmex_hive_desert");
    public static final ResourceKey<ConfiguredFeature<?, ?>> MYRMEX_HIVE_JUNGLE = registerKey("myrmex_hive_jungle");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_DEATH_WORM = registerKey("spawn_death_worm");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_DRAGON_SKELETON_L = registerKey("spawn_dragon_skeleton_lightning");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_DRAGON_SKELETON_F = registerKey("spawn_dragon_skeleton_fire");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_DRAGON_SKELETON_I = registerKey("spawn_dragon_skeleton_ice");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_HIPPOCAMPUS = registerKey("spawn_hippocampus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_SEA_SERPENT = registerKey("spawn_sea_serpent");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_STYMPHALIAN_BIRD = registerKey("spawn_stymphalian_bird");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SPAWN_WANDERING_CYCLOPS = registerKey("spawn_wandering_cyclops");


    public static final ResourceKey<ConfiguredFeature<?, ?>> SILVER_ORE = registerKey("silver_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SAPPHIRE_ORE = registerKey("sapphire_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FIRE_LILY = registerKey("fire_lily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> FROST_LILY = registerKey("frost_lily");
    public static final ResourceKey<ConfiguredFeature<?, ?>> LIGHTNING_LILY = registerKey("lightning_lily");




    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Identifier.fromNamespaceAndPath("iceandfire", name));
    }


}
