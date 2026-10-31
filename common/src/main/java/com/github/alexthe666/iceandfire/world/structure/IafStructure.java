package com.github.alexthe666.iceandfire.world.structure;

import dev.vladeezyy.rampart.config.biome.SpawnBiomeData;
import com.github.alexthe666.iceandfire.config.BiomeConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class IafStructure extends Structure {

    protected final Holder<StructureTemplatePool> startPool;
    protected final Optional<Identifier> startJigsawName;
    protected final int size;
    protected final HeightProvider startHeight;
    protected final Optional<Heightmap.Types> projectStartToHeightmap;
    protected final int maxDistanceFromCenter;

    public IafStructure(Structure.StructureSettings config,
                        Holder<StructureTemplatePool> startPool,
                        Optional<Identifier> startJigsawName,
                        int size,
                        HeightProvider startHeight,
                        Optional<Heightmap.Types> projectStartToHeightmap,
                        int maxDistanceFromCenter) {
        super(config);
        this.startPool = startPool;
        this.startJigsawName = startJigsawName;
        this.size = size;
        this.startHeight = startHeight;
        this.projectStartToHeightmap = projectStartToHeightmap;
        this.maxDistanceFromCenter = maxDistanceFromCenter;
    }

    protected boolean isBiomeValid(GenerationContext pContext, Pair<String, SpawnBiomeData> validBiomes, BlockPos blockPos) {
        return isBiomeValid(pContext, (Map.Entry<String, SpawnBiomeData>) validBiomes, blockPos);
    }

    /**
     * Tests the biome at the surface where the structure starts. The original accepted the structure when any biome in
     * {@link #maxDistanceFromCenter} blocks matched, which put graveyards and mausoleums on oceans (issue #5146).
     */
    protected boolean isBiomeValid(GenerationContext pContext, Map.Entry<String, SpawnBiomeData> validBiomes, BlockPos blockPos) {
        int y = pContext.chunkGenerator().getFirstOccupiedHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, pContext.heightAccessor(), pContext.randomState());
        Holder<Biome> biome = pContext.chunkGenerator().getBiomeSource().createUncachedResolver(pContext.randomState()).getNoiseBiome(QuartPos.fromBlock(blockPos.getX()), QuartPos.fromBlock(y), QuartPos.fromBlock(blockPos.getZ()));
        return BiomeConfig.test(validBiomes, biome);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext pContext) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return null;
    }

}
