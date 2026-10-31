package com.github.alexthe666.iceandfire.datagen.tags;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import java.util.concurrent.CompletableFuture;

/** The tags of the data provider IafEntityTagsProvider (NeoForge module), for the game code of both loaders */
public final class IafEntityTags {
    private IafEntityTags() {
    }

    public static TagKey<EntityType<?>> IMMUNE_TO_GORGON_STONE = createKey("immune_to_gorgon_stone");

    private static TagKey<EntityType<?>> createKey(final String name) {
        return TagKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }
}
