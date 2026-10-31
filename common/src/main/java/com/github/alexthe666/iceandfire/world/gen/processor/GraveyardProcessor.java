package com.github.alexthe666.iceandfire.world.gen.processor;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.iceandfire.world.IafProcessors;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GraveyardProcessor implements StructureProcessor {

    private final float integrity = 1.0F;
    public static final GraveyardProcessor INSTANCE = new GraveyardProcessor();
    public static final MapCodec<GraveyardProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    public GraveyardProcessor() {
    }

    public static BlockState getRandomCobblestone(@Nullable BlockState prev, RandomSource random) {
        float rand = random.nextFloat();
        if (rand < 0.5) {
            return Blocks.COBBLESTONE.defaultBlockState();
        } else if (rand < 0.9) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        } else {
            return Blocks.INFESTED_COBBLESTONE.defaultBlockState();
        }
    }

    public static BlockState getRandomCrackedBlock(@Nullable BlockState prev, RandomSource random) {
        float rand = random.nextFloat();
        if (rand < 0.5) {
            return Blocks.STONE_BRICKS.defaultBlockState();
        } else if (rand < 0.9) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        } else {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
        }
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(@NotNull LevelReader worldReader, @NotNull BlockPos pos, @NotNull BlockPos pos2, @NotNull BlockPos templateRelativePos, StructureTemplate.@NotNull StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        // 26.2: no original info any more; the processed one is the template's block (no processor runs before these)
        StructureTemplate.StructureBlockInfo infoIn1 = infoIn2;
        RandomSource random = settings.getRandom(infoIn2.pos());
        if (infoIn2.state().getBlock() == Blocks.STONE_BRICKS) {
            BlockState state = getRandomCrackedBlock(null, random);
            return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), state, null);
        }
        if (infoIn2.state().getBlock() == Blocks.COBBLESTONE) {
            BlockState state = getRandomCobblestone(null, random);
            return new StructureTemplate.StructureBlockInfo(infoIn2.pos(), state, null);
        }
        return infoIn2;
    }


    @Override
    public @NotNull MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }

}
