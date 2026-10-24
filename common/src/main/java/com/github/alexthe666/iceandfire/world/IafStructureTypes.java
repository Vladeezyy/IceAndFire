package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.structure.GorgonTempleStructure;
import com.github.alexthe666.iceandfire.world.structure.GraveyardStructure;
import com.github.alexthe666.iceandfire.world.structure.MausoleumStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.function.Supplier;

public class IafStructureTypes {
    public static final Registrar<StructureType<?>> STRUCTURE_TYPES = Registrar.create(Registries.STRUCTURE_TYPE, IceAndFire.MODID);

    public static final RegistryEntry<StructureType<?>, StructureType<GraveyardStructure>> GRAVEYARD = registerType("graveyard", () -> () -> GraveyardStructure.CODEC);
    public static final RegistryEntry<StructureType<?>, StructureType<MausoleumStructure>> MAUSOLEUM = registerType("mausoleum", () -> () -> MausoleumStructure.CODEC);
    public static final RegistryEntry<StructureType<?>, StructureType<GorgonTempleStructure>> GORGON_TEMPLE = registerType("gorgon_temple", () -> () -> GorgonTempleStructure.CODEC);
    private static <P extends Structure> RegistryEntry<StructureType<?>, StructureType<P>> registerType(String name, Supplier<StructureType<P>> factory) {
        return STRUCTURE_TYPES.register(name, factory);
    }
}
