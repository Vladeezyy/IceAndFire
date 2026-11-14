package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

/** A {@code ServerExplosion} after {@code explode()}: the blocks it computed (mixin {@code ServerExplosionBlocksMixin}). */
public interface ExplodedBlocks {
    /** The list {@code explode()} worked with (other mods' detonate events may have changed it), null before it ran. */
    @Nullable
    List<BlockPos> iceandfire$explodedBlocks();
}
