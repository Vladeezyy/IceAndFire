package com.github.alexthe666.iceandfire.world.gen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * A feature of the mod on 26.3: features are data objects of a registered type (no configured features, no
 * configuration). Each of the mod's features has no configuration, so its type's codec is the feature itself, and
 * {@link Context} is 1.21.1's {@code FeaturePlaceContext} for the unchanged placement code.
 */
public abstract class IafFeature implements Feature {
    private final MapCodec<IafFeature> codec = MapCodec.unit(this);

    @Override
    public MapCodec<? extends Feature> codec() {
        return this.codec;
    }

    @Override
    public final boolean place(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
        return this.place(new Context(level, chunkGenerator, random, origin));
    }

    public abstract boolean place(Context context);

    public record Context(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin) {
    }
}
