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
 * never happens). 26.x {@code Explosion} is an interface: a vanilla {@link ServerExplosion} that keeps the blocks does
 * the entity part, the blocks it would break go the same way as before. The steps of {@code explode()} one by one: an
 * explosion computes its blocks once (a second call does all the rays again, and with Lithium, which drops its cache of
 * block resistances at the end of the first one, throws).
 */
public final class BlockLaunchExplosion {
    private BlockLaunchExplosion() {}

    public static void explode(Level world, Mob entity, double x, double y, double z, float size) {
        explode(world, entity, null, x, y, z, size, Explosion.BlockInteraction.DESTROY);
    }

    public static void explode(Level world, Mob entity, @Nullable DamageSource source, double x, double y, double z, float size, Explosion.BlockInteraction mode) {
        if (!(world instanceof ServerLevel level)) {
            return;
        }
        ServerExplosion explosion = new ServerExplosion(level, entity, source, null, new Vec3(x, y, z), size, false, Explosion.BlockInteraction.KEEP);
        level.gameEvent(entity, net.minecraft.world.level.gameevent.GameEvent.EXPLODE, new Vec3(x, y, z));
        if (mode == Explosion.BlockInteraction.KEEP) {
            explosion.hurtEntities();
            return;
        }
        List<BlockPos> toBlow = new ArrayList<>(explosion.calculateExplodedPositions());
        explosion.hurtEntities();
        Collections.shuffle(toBlow, ThreadLocalRandom.current());
        for (BlockPos blockpos : toBlow) {
            BlockState blockstate = level.getBlockState(blockpos);
            if (!blockstate.isAir()) {
                dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.onBlockExploded(blockstate, level, blockpos, explosion);
            }
        }
    }
}
