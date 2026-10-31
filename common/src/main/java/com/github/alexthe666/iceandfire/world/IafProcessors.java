package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.gen.processor.DreadRuinProcessor;
import com.github.alexthe666.iceandfire.world.gen.processor.GorgonTempleProcessor;
import com.github.alexthe666.iceandfire.world.gen.processor.GraveyardProcessor;
import com.github.alexthe666.iceandfire.world.gen.processor.VillageHouseProcessor;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import com.mojang.serialization.MapCodec;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.function.Supplier;

public class IafProcessors {
    // 26.2: the processor registry holds the codecs
    public static final Registrar<MapCodec<? extends StructureProcessor>> PROCESSORS = Registrar.create(Registries.STRUCTURE_PROCESSOR, IceAndFire.MODID);

    public static final RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<DreadRuinProcessor>> DREADRUINPROCESSOR = registerProcessor("dread_mausoleum_processor", () -> DreadRuinProcessor.CODEC);
    public static final RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<GorgonTempleProcessor>> GORGONTEMPLEPROCESSOR = registerProcessor("gorgon_temple_processor", () -> GorgonTempleProcessor.CODEC);
    public static final RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<GraveyardProcessor>> GRAVEYARDPROCESSOR = registerProcessor("graveyard_processor", () -> GraveyardProcessor.CODEC);
    public static final RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<VillageHouseProcessor>> VILLAGEHOUSEPROCESSOR = registerProcessor("village_house_processor", () -> VillageHouseProcessor.CODEC);

    private static <P extends StructureProcessor> RegistryEntry<MapCodec<? extends StructureProcessor>, MapCodec<P>> registerProcessor(String name, Supplier<MapCodec<P>> processor) {
        return PROCESSORS.register(name, processor);
    }
}
