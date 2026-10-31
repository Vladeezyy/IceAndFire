package com.github.alexthe666.iceandfire.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public final class IafPlacedFeatures {

    public static final ResourceKey<PlacedFeature> PLACED_FIRE_DRAGON_ROOST = registerKey("fire_dragon_roost");
    public static final ResourceKey<PlacedFeature> PLACED_ICE_DRAGON_ROOST = registerKey("ice_dragon_roost");
    public static final ResourceKey<PlacedFeature> PLACED_LIGHTNING_DRAGON_ROOST = registerKey("lightning_dragon_roost");
    public static final ResourceKey<PlacedFeature> PLACED_FIRE_DRAGON_CAVE = registerKey("fire_dragon_cave");
    public static final ResourceKey<PlacedFeature> PLACED_ICE_DRAGON_CAVE = registerKey("ice_dragon_cave");
    public static final ResourceKey<PlacedFeature> PLACED_LIGHTNING_DRAGON_CAVE = registerKey("lightning_dragon_cave");
    public static final ResourceKey<PlacedFeature> PLACED_CYCLOPS_CAVE = registerKey("cyclops_cave");
    public static final ResourceKey<PlacedFeature> PLACED_PIXIE_VILLAGE = registerKey("pixie_village");
    public static final ResourceKey<PlacedFeature> PLACED_SIREN_ISLAND = registerKey("siren_island");
    public static final ResourceKey<PlacedFeature> PLACED_HYDRA_CAVE = registerKey("hydra_cave");
    public static final ResourceKey<PlacedFeature> PLACED_MYRMEX_HIVE_DESERT = registerKey("myrmex_hive_desert");
    public static final ResourceKey<PlacedFeature> PLACED_MYRMEX_HIVE_JUNGLE = registerKey("myrmex_hive_jungle");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DEATH_WORM = registerKey("spawn_death_worm");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_L = registerKey("spawn_dragon_skeleton_lightning");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_F = registerKey("spawn_dragon_skeleton_fire");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_DRAGON_SKELETON_I = registerKey("spawn_dragon_skeleton_ice");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_HIPPOCAMPUS = registerKey("spawn_hippocampus");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_SEA_SERPENT = registerKey("spawn_sea_serpent");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_STYMPHALIAN_BIRD = registerKey("spawn_stymphalian_bird");
    public static final ResourceKey<PlacedFeature> PLACED_SPAWN_WANDERING_CYCLOPS = registerKey("spawn_wandering_cyclops");
    public static final ResourceKey<PlacedFeature> PLACED_SILVER_ORE = registerKey("silver_ore");
    public static final ResourceKey<PlacedFeature> PLACED_SAPPHIRE_ORE = registerKey("sapphire_ore");
    public static final ResourceKey<PlacedFeature> PLACED_FIRE_LILY = registerKey("fire_lily");
    public static final ResourceKey<PlacedFeature> PLACED_LIGHTNING_LILY = registerKey("lightning_lily");
    public static final ResourceKey<PlacedFeature> PLACED_FROST_LILY = registerKey("frost_lily");


    private static List<PlacementModifier> orePlacement(PlacementModifier pCountPlacement, PlacementModifier pHeightRange) {
        return List.of(pCountPlacement, InSquarePlacement.spread(), pHeightRange, BiomeFilter.biome());
    }
    private static List<PlacementModifier> commonOrePlacement(int pCount, PlacementModifier pHeightRange) {
        return orePlacement(CountPlacement.of(pCount), pHeightRange);
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("iceandfire",name));
    }
}
