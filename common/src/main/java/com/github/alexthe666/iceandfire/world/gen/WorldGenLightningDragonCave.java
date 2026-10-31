package com.github.alexthe666.iceandfire.world.gen;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.datagen.tags.IafBlockTags;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class WorldGenLightningDragonCave extends WorldGenDragonCave {
    public static ResourceKey<LootTable> LIGHTNING_DRAGON_CHEST = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "chest/lightning_dragon_female_cave"));
    public static ResourceKey<LootTable> LIGHTNING_DRAGON_CHEST_MALE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "chest/lightning_dragon_male_cave"));

    public WorldGenLightningDragonCave(final Codec<NoneFeatureConfiguration> configuration) {
        super(configuration);
        DRAGON_CHEST = LIGHTNING_DRAGON_CHEST;
        DRAGON_MALE_CHEST = LIGHTNING_DRAGON_CHEST_MALE;
        CEILING_DECO = new WorldGenCaveStalactites(IafBlockRegistry.CRACKLED_STONE.get(), 6);
        PALETTE_BLOCK1 = IafBlockRegistry.CRACKLED_STONE.get().defaultBlockState();
        PALETTE_BLOCK2 = IafBlockRegistry.CRACKLED_COBBLESTONE.get().defaultBlockState();
        TREASURE_PILE = IafBlockRegistry.COPPER_PILE.get().defaultBlockState();
        dragonTypeOreTag = IafBlockTags.LIGHTNING_DRAGON_CAVE_ORES;
    }

    @Override
    public EntityType<? extends EntityDragonBase> getDragonType() {
        return IafEntityRegistry.LIGHTNING_DRAGON.get();
    }
}
