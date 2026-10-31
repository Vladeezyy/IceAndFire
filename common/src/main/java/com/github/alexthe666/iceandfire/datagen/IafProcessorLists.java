package com.github.alexthe666.iceandfire.datagen;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;


/** 26.x: the keys only, the data is in the generated JSON (the bootstraps of the 1.21.1 datagen were removed) */
public class IafProcessorLists {
    public static final ResourceKey<StructureProcessorList> GRAVEYARD_PROCESSORS = createKey("graveyard_processors");
    public static final ResourceKey<StructureProcessorList> MAUSOLEUM_PROCESSORS = createKey("mausoleum_processors");
    public static final ResourceKey<StructureProcessorList> GORGON_TEMPLE_PROCESSORS = createKey("gorgon_temple_processors");
    public static final ResourceKey<StructureProcessorList> HOUSE_PROCESSOR = createKey("village_house_processor");

    private static ResourceKey<StructureProcessorList> createKey(String name) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }


}
