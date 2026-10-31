package com.github.alexthe666.iceandfire.world.gen.processor;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.mojang.serialization.MapCodec;
import com.github.alexthe666.iceandfire.world.IafProcessors;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class VillageHouseProcessor implements StructureProcessor {

    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("iceandfire", "chest/village_scribe"));
    public static final VillageHouseProcessor INSTANCE = new VillageHouseProcessor();
    public static final MapCodec<VillageHouseProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    public VillageHouseProcessor() {
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(@NotNull LevelReader worldReader, @NotNull BlockPos pos, @NotNull BlockPos pos2, @NotNull BlockPos templateRelativePos, StructureTemplate.@NotNull StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        // 26.2: no original info any more; the processed one is the template's block (no processor runs before these)
        StructureTemplate.StructureBlockInfo infoIn1 = infoIn2;
        RandomSource random = settings.getRandom(infoIn2.pos());
        if (infoIn2.state().getBlock() == Blocks.CHEST) {
            CompoundTag tag = new CompoundTag();
            tag.putString("LootTable", LOOT.identifier().toString());
            tag.putLong("LootTableSeed", random.nextLong());
            return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), infoIn2.state(), tag);
        }
        return infoIn2;
    }


    @Override
    public @NotNull MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }

}
