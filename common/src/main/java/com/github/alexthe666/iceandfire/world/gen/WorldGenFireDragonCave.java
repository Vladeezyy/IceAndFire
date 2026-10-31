package com.github.alexthe666.iceandfire.world.gen;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.datagen.tags.IafBlockTags;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

public class WorldGenFireDragonCave extends WorldGenDragonCave {
    public static ResourceKey<LootTable> FIRE_DRAGON_CHEST = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "chest/fire_dragon_female_cave"));
    public static ResourceKey<LootTable> FIRE_DRAGON_CHEST_MALE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "chest/fire_dragon_male_cave"));

    public WorldGenFireDragonCave() {
        DRAGON_CHEST = FIRE_DRAGON_CHEST;
        DRAGON_MALE_CHEST = FIRE_DRAGON_CHEST_MALE;
        CEILING_DECO = new WorldGenCaveStalactites(IafBlockRegistry.CHARRED_STONE.get(), 3);
        PALETTE_BLOCK1 = IafBlockRegistry.CHARRED_STONE.get().defaultBlockState();
        PALETTE_BLOCK2 = IafBlockRegistry.CHARRED_COBBLESTONE.get().defaultBlockState();
        TREASURE_PILE = IafBlockRegistry.GOLD_PILE.get().defaultBlockState();
        dragonTypeOreTag = IafBlockTags.FIRE_DRAGON_CAVE_ORES;
    }

    @Override
    public EntityType<? extends EntityDragonBase> getDragonType() {
        return IafEntityRegistry.FIRE_DRAGON.get();
    }
}
