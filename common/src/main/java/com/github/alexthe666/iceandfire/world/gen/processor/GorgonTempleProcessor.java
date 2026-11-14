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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class GorgonTempleProcessor extends StructureProcessor {

    public static final GorgonTempleProcessor INSTANCE = new GorgonTempleProcessor();
    public static final MapCodec<GorgonTempleProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    public GorgonTempleProcessor() {
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(@NotNull LevelReader worldReader, @NotNull BlockPos pos, @NotNull BlockPos pos2, StructureTemplate.@NotNull StructureBlockInfo infoIn1, StructureTemplate.StructureBlockInfo infoIn2, StructurePlaceSettings settings) {

        // Workaround for https://bugs.mojang.com/browse/MC-130584
        // Due to a hardcoded field in Templates, any waterloggable blocks in structures replacing water in the world will become waterlogged.
        // Idea of workaround is detect if we are placing a waterloggable block and if so, remove the water in the world instead.
        clearWater(worldReader, infoIn2, settings);
        return infoIn2;
    }

    /**
     * Removes the water under a waterloggable block, only inside the chunk being placed (the box of the settings): the
     * processor sees every block of the template, and the direct chunk write (past the checks of the world gen region)
     * into a neighbour that another thread was still filling crashed the server with "Accessing PalettedContainer from
     * multiple threads". Each block is cleared when its own chunk is placed.
     */
    public static void clearWater(LevelReader worldReader, StructureTemplate.StructureBlockInfo info, StructurePlaceSettings settings) {
        if (!(info.state().getBlock() instanceof SimpleWaterloggedBlock)) {
            return;
        }
        net.minecraft.world.level.levelgen.structure.BoundingBox box = settings.getBoundingBox();
        if ((box == null || box.isInside(info.pos())) && worldReader.getFluidState(info.pos()).is(FluidTags.WATER)) {
            worldReader.getChunk(info.pos()).setBlockState(info.pos(), Blocks.AIR.defaultBlockState(), 0);
        }
    }

    @Override
    protected @NotNull StructureProcessorType getType() {
        return IafProcessors.GORGONTEMPLEPROCESSOR.get();
    }
}
