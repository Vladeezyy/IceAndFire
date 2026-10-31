package com.github.alexthe666.iceandfire.datagen;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

/** 26.x: the keys only, the data is in the generated JSON (the bootstraps of the 1.21.1 datagen were removed) */
public class IafStructurePieces {
    public static final ResourceKey<StructureTemplatePool> GRAVEYARD_START = createKey("graveyard/start_pool");
    public static final ResourceKey<StructureTemplatePool> MAUSOLEUM_START = createKey("mausoleum/start_pool");
    public static final ResourceKey<StructureTemplatePool> GORGON_TEMPLE_START = createKey("gorgon_temple/start_pool");
    private static ResourceKey<StructureTemplatePool> createKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, Identifier.fromNamespaceAndPath("iceandfire", name));
    }


}
