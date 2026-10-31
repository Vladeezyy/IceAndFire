package com.github.alexthe666.iceandfire.datagen;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.Structure;

/** 26.x: the keys only, the data is in the generated JSON (the bootstraps of the 1.21.1 datagen were removed) */
public class IafStructures {

    public static final ResourceKey<Structure> GRAVEYARD = registerKey("graveyard");
    public static final ResourceKey<Structure> MAUSOLEUM = registerKey("mausoleum");
    public static final ResourceKey<Structure> GORGON_TEMPLE = registerKey("gorgon_temple");

    public static ResourceKey<Structure> registerKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }

}
