package com.github.alexthe666.iceandfire.world.gen.processor;

import com.mojang.serialization.MapCodec;
import com.github.alexthe666.iceandfire.world.IafProcessors;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GorgonTempleProcessor implements StructureProcessor {

    public static final GorgonTempleProcessor INSTANCE = new GorgonTempleProcessor();
    public static final MapCodec<GorgonTempleProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    public GorgonTempleProcessor() {
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(@NotNull LevelReader worldReader, @NotNull BlockPos pos, @NotNull BlockPos pos2, @NotNull BlockPos templateRelativePos, StructureTemplate.@NotNull StructureBlockInfo infoIn2, StructurePlaceSettings settings) {
        // 26.2: no original info any more; the processed one is the template's block (no processor runs before these)
        StructureTemplate.StructureBlockInfo infoIn1 = infoIn2;

        // Workaround for https://bugs.mojang.com/browse/MC-130584
        // Due to a hardcoded field in Templates, any waterloggable blocks in structures replacing water in the world will become waterlogged.
        // Idea of workaround is detect if we are placing a waterloggable block and if so, remove the water in the world instead.
        if (infoIn2.state().getBlock() instanceof SimpleWaterloggedBlock) {
            if (worldReader.getFluidState(infoIn2.pos()).is(FluidTags.WATER)) {
                ChunkPos currentChunk = ChunkPos.containing(infoIn2.pos());
                worldReader.getChunk(currentChunk.x(), currentChunk.z()).setBlockState(infoIn2.pos(), Blocks.AIR.defaultBlockState(), 0);
            }
        }

        return infoIn2;
    }

    @Override
    public @NotNull MapCodec<? extends StructureProcessor> codec() {
        return CODEC;
    }
}
