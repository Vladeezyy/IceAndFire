package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerExplosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The death worm's and the dragons' ground blast. 1.21.1 subclassed {@code Explosion}: {@code explode()} hurt the
 * entities, {@code finalizeExplosion} removed the blocks through the loader's {@code onBlockExploded} without drops
 * (it also built falling blocks it never spawned, and its sound and particles only ran on the client, where the blast
 * never happens). 26.x {@code Explosion} is an interface: a vanilla {@link ServerExplosion} that keeps the blocks runs
 * its own {@code explode()} (game event, the blocks once, the entities), then the blocks it computed go the same way as
 * before. Whole {@code explode()}, not its steps one by one: other mods hook it (PuzzlesLib's Fabric detonate event keeps
 * the blocks from {@code explode()} and threw "exploded positions is null" on a bare {@code hurtEntities()}, NeoForge's
 * Detonate event got an empty fixed list there, so protection mods could not spare their blocks), and the blocks are
 * computed once (with Lithium a second time throws).
 */
public final class BlockLaunchExplosion {
    private static boolean warnedNoBlocks;

    private BlockLaunchExplosion() {}

    public static void explode(Level world, Mob entity, double x, double y, double z, float size) {
        explode(world, entity, null, x, y, z, size, Explosion.BlockInteraction.DESTROY);
    }

    public static void explode(Level world, Mob entity, @Nullable DamageSource source, double x, double y, double z, float size, Explosion.BlockInteraction mode) {
        if (!(world instanceof ServerLevel level)) {
            return;
        }
        ServerExplosion explosion = new ServerExplosion(level, entity, source, null, new Vec3(x, y, z), size, false, Explosion.BlockInteraction.KEEP);
        explosion.explode();
        List<BlockPos> blocks = ((ExplodedBlocks) explosion).iceandfire$explodedBlocks();
        if (mode == Explosion.BlockInteraction.KEEP) {
            return;
        }
        if (blocks == null) {
            // the capture is optional (require = 0): another mod that rewrote explode() left the blasts without blocks and
            // without a word. A tiny blast returns before the blocks
            if (!warnedNoBlocks && size >= 1.0E-5F) {
                warnedNoBlocks = true;
                com.github.alexthe666.iceandfire.IceAndFire.LOGGER.warn("Ground blasts break no blocks: the blocks of ServerExplosion.explode() were not captured (another mod changed it?)");
            }
            return;
        }
        List<BlockPos> toBlow = new ArrayList<>(blocks);
        Collections.shuffle(toBlow, ThreadLocalRandom.current());
        for (BlockPos blockpos : toBlow) {
            BlockState blockstate = level.getBlockState(blockpos);
            if (!blockstate.isAir()) {
                dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.onBlockExploded(blockstate, level, blockpos, explosion);
            }
        }
    }
}
