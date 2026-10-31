package com.github.alexthe666.iceandfire.datagen;

import net.minecraft.core.registries.Registries;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** The tags of the data provider IafBiomeTagGeneratorProvider (NeoForge module), for the game code of both loaders */
public final class IafBiomeTagGenerator {
    private IafBiomeTagGenerator() {
    }

    public static final TagKey<Biome> HAS_GORGON_TEMPLE = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "has_structure/gorgon_temple"));
    public static final TagKey<Biome> HAS_MAUSOLEUM = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "has_structure/mausoleum"));
    public static final TagKey<Biome> HAS_GRAVEYARD = TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "has_structure/graveyard"));
}
