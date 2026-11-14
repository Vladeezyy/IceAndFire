package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.api.event.DragonFireDamageWorldEvent;
import com.github.alexthe666.iceandfire.block.*;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforgeInput;
import com.github.alexthe666.iceandfire.entity.util.BlockLaunchExplosion;
import com.github.alexthe666.iceandfire.entity.util.DragonUtils;
import com.github.alexthe666.iceandfire.enums.EnumParticles;
import com.github.alexthe666.iceandfire.message.MessageDragonSyncFire;
import com.github.alexthe666.iceandfire.misc.IafDamageRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class IafDragonDestructionManager {
    /** Ticks between breath syncs while the target stays put (it was sent every tick); the clients draw the flame in between. */
    private static final int BREATH_SYNC_INTERVAL = 10;
    /** The target moved this far (squared) since the last sync: send it now, so the flame keeps aiming at it. */
    private static final double BREATH_SYNC_MOVE_SQR = 1.0D;
    /** Ticks without breath before the clients are told it stopped; the ground fire attack skips single ticks. */
    private static final int BREATH_STOP_GRACE = 1;
    /** A client draws the flame this long after the last sync, in case the stop never arrives. */
    private static final int CLIENT_BREATH_HOLD = 2 * BREATH_SYNC_INTERVAL;
    /** The blocks of the breath area are attacked once per this many ticks, with the chances of the ticks in between. */
    private static final int BLOCK_PASS_INTERVAL = 5;
    private static final float FIRE_PLACE_CHANCE = 0.5F;
    private static final float ICE_PLACE_CHANCE = 1F / 9F;
    /** Lightning pushed once per blocked step; repeated knockback stops growing after a few. */
    private static final int MAX_KNOCKBACK_REPEATS = 4;

    /**
     * Breath of one dragon. Server: what the clients were sent and the blocks waiting for their attack. Client: how
     * long to keep drawing the flame.
     */
    public static final class BreathState {
        private int lastBreathTick = Integer.MIN_VALUE / 2;
        private int lastSyncTick;
        private double syncedX;
        private double syncedY;
        private double syncedZ;
        private boolean clientsBreathing;
        /** Game time of the next block pass (by time: breathAt may run more than once a tick) */
        private long nextBlockPass = Long.MIN_VALUE;
        private int pendingBlockTrials;
        /** Where the pending block hits landed */
        @Nullable
        private BlockPos pendingCenter;
        private int clientFlameTicks;
    }

    /**
     * The breath of one tick. {@code steps} and {@code visibleSteps} are the counts of the old walk: about 100
     * points over the part of the line the flame reached, each tested with its own ray. {@code blockedAt}: the wall
     * the flame hit, or null.
     */
    private record BreathTrace(Vec3 head, int steps, int visibleSteps, @Nullable Vec3 blockedAt) {
    }

    /** One ray to the flame front instead of one per step (two per step behind a wall). */
    @Nullable
    private static BreathTrace traceBreath(final EntityDragonBase dragon, final double burnX, final double burnY, final double burnZ) {
        final double distance = Math.max(2.5F * dragon.distanceToSqr(burnX, burnY, burnZ), 0);
        final double conquered = dragon.burnProgress / 40D * distance;
        if (!(conquered > 0)) {
            return null;
        }
        final int increment = (int) Math.ceil(conquered / 100);
        final int steps = (int) Math.ceil(conquered / increment);
        final Vec3 head = dragon.getHeadPosition();
        final double dx = burnX - head.x;
        final double dy = burnY - head.y;
        final double dz = burnZ - head.z;
        final double progress = conquered / distance;
        final Vec3 front = new Vec3(head.x + dx * progress, head.y + dy * progress, head.z + dz * progress);
        final Vec3 eyes = new Vec3(dragon.getX(), dragon.getY() + dragon.getEyeHeight(), dragon.getZ());
        final HitResult hit = dragon.level().clip(new ClipContext(eyes, front, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, dragon));
        // The test of canPositionBeSeen: a hit within a block of the point still sees it
        if (hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(front) <= 1.0D) {
            return new BreathTrace(head, steps, steps, null);
        }
        final Vec3 wall = hit.getLocation();
        final double lengthSqr = dx * dx + dy * dy + dz * dz;
        int visible = 0;
        if (lengthSqr > 1.0E-6D) {
            // Steps up to the wall (plus that block) were seen, the ones behind it were not
            final double seenUpTo = ((wall.x - head.x) * dx + (wall.y - head.y) * dy + (wall.z - head.z) * dz) / lengthSqr
                + 1.0D / Math.sqrt(lengthSqr);
            if (seenUpTo >= 0) {
                visible = (int) Math.min(steps - 1, Math.floor(seenUpTo * distance / increment) + 1);
            }
        }
        return new BreathTrace(head, steps, visible, wall);
    }

    /**
     * Server: sends the breath target to the players tracking the dragon when a breath starts, when the target
     * moved, and every {@link #BREATH_SYNC_INTERVAL} ticks.
     */
    public static void syncBreath(final EntityDragonBase dragon, final BreathState state, final double x, final double y, final double z) {
        final double mx = x - state.syncedX;
        final double my = y - state.syncedY;
        final double mz = z - state.syncedZ;
        if (!state.clientsBreathing || dragon.tickCount - state.lastSyncTick >= BREATH_SYNC_INTERVAL || mx * mx + my * my + mz * mz > BREATH_SYNC_MOVE_SQR) {
            IceAndFire.sendMSGToTracking(dragon, new MessageDragonSyncFire(dragon.getId(), x, y, z, 0));
            state.lastSyncTick = dragon.tickCount;
            state.syncedX = x;
            state.syncedY = y;
            state.syncedZ = z;
            state.clientsBreathing = true;
        }
    }

    /**
     * Server: the damage of one breath tick. Returns where the breath ends (the wall it hit, the target or a spot
     * near it), or null while the flame has not left the mouth.
     */
    @Nullable
    public static Vec3 breathServer(final EntityDragonBase dragon, final BreathState state, final double burnX, final double burnY, final double burnZ) {
        state.lastBreathTick = dragon.tickCount;
        final BreathTrace trace = traceBreath(dragon, burnX, burnY, burnZ);
        if (trace == null) {
            return null;
        }
        if (trace.blockedAt() != null) {
            // Every step behind the wall hit it on its own: their count keeps the old damage chances
            breathAt(dragon.level(), BlockPos.containing(trace.blockedAt()), dragon, state, trace.steps() - trace.visibleSteps());
            return trace.blockedAt();
        }
        if (dragon.burnProgress >= 40) {
            final RandomSource random = dragon.getRandom();
            final Vec3 spot = new Vec3(burnX + (random.nextFloat() * 3.0) - 1.5, burnY + (random.nextFloat() * 3.0) - 1.5, burnZ + (random.nextFloat() * 3.0) - 1.5);
            breathAt(dragon.level(), BlockPos.containing(spot), dragon, state, 1);
            return spot;
        }
        return new Vec3(burnX, burnY, burnZ);
    }

    /** Client: a breath sync arrived; the flame is drawn every tick in {@link #afterDragonTick}. */
    public static void onBreathSync(final BreathState state) {
        state.clientFlameTicks = CLIENT_BREATH_HOLD;
    }

    /** Client: the server ended the breath. */
    public static void onBreathStopped(final BreathState state) {
        state.clientFlameTicks = 0;
    }

    /**
     * After the dragon's own tick, when all breath of the tick is done. Server: tells the clients the breath
     * stopped. Client: draws the flame of this tick ({@code flame} null: the dragon has no flame particles).
     */
    public static void afterDragonTick(final EntityDragonBase dragon, final BreathState state, @Nullable final EnumParticles flame) {
        if (dragon.level().isClientSide()) {
            if (flame != null) {
                drawFlame(dragon, state, flame);
            }
            return;
        }
        if (dragon.tickCount - state.lastBreathTick > BREATH_STOP_GRACE) {
            if (state.clientsBreathing) {
                state.clientsBreathing = false;
                IceAndFire.sendMSGToTracking(dragon, new MessageDragonSyncFire(dragon.getId(), state.syncedX, state.syncedY, state.syncedZ, MessageDragonSyncFire.BREATH_STOPPED));
            }
            // The last hits of the breath still change their blocks; a new breath attacks blocks on its first tick
            if (state.pendingCenter != null && state.pendingBlockTrials > 0) {
                breathBlocks(dragon.level(), state.pendingCenter, dragon, state.pendingBlockTrials);
            }
            state.nextBlockPass = Long.MIN_VALUE;
            state.pendingBlockTrials = 0;
            state.pendingCenter = null;
        }
    }

    private static void drawFlame(final EntityDragonBase dragon, final BreathState state, final EnumParticles flame) {
        if (state.clientFlameTicks <= 0) {
            return;
        }
        state.clientFlameTicks--;
        if (!dragon.isBreathingFire()) {
            return;
        }
        final BreathTrace trace = traceBreath(dragon, dragon.burnParticleX, dragon.burnParticleY, dragon.burnParticleZ);
        if (trace == null) {
            return;
        }
        // As many flames as the old walk drew: a chance for each step the dragon could see
        final int particleCount = dragon.getDragonStage() <= 3 ? 6 : 3;
        final RandomSource random = dragon.getRandom();
        final Vec3 head = trace.head();
        for (int i = 0; i < trace.visibleSteps(); i++) {
            if (random.nextInt(particleCount) == 0) {
                IceAndFire.PROXY.spawnDragonParticle(flame, head.x, head.y, head.z, 0, 0, 0, dragon);
            }
        }
    }

    /** One breath hit at {@code center} with its blocks at once, as before the breath was throttled. */
    public static void destroyAreaBreath(final Level level, final BlockPos center, final EntityDragonBase dragon) {
        if (dev.vladeezyy.iceandfire.bus.IafBus.post(new DragonFireDamageWorldEvent(dragon, center.getX(), center.getY(), center.getZ()))) {
            return;
        }
        applyBreath(level, center, dragon, 1, 1);
    }

    /**
     * One tick of breath at {@code center}, {@code hits} times (once per step behind a wall). Entities and forge
     * inputs are hit every tick as before; blocks once per {@link #BLOCK_PASS_INTERVAL} ticks with the chances of
     * all hits since the last pass (were 125 or 343 blocks per hit, up to 100 hits per tick behind a wall).
     */
    private static void breathAt(final Level level, final BlockPos center, final EntityDragonBase dragon, final BreathState state, final int hits) {
        if (dev.vladeezyy.iceandfire.bus.IafBus.post(new DragonFireDamageWorldEvent(dragon, center.getX(), center.getY(), center.getZ()))) {
            return;
        }
        // The flame moved on: the hits gathered at the old spot change its blocks there, so a sweep leaves a strip and
        // not a patch every 5 ticks
        final BlockPos pending = state.pendingCenter;
        final int radius = blockRadius(dragon.getDragonStage());
        if (pending != null && state.pendingBlockTrials > 0 && (Math.abs(pending.getX() - center.getX()) > radius
                || Math.abs(pending.getY() - center.getY()) > radius || Math.abs(pending.getZ() - center.getZ()) > radius)) {
            breathBlocks(level, pending, dragon, state.pendingBlockTrials);
            state.pendingBlockTrials = 0;
        }
        state.pendingCenter = center.immutable();
        state.pendingBlockTrials += hits;
        int blockTrials = 0;
        final long now = level.getGameTime();
        if (now >= state.nextBlockPass || state.nextBlockPass - now > BLOCK_PASS_INTERVAL) {
            state.nextBlockPass = now + BLOCK_PASS_INTERVAL;
            blockTrials = state.pendingBlockTrials;
            state.pendingBlockTrials = 0;
        }
        applyBreath(level, center, dragon, hits, blockTrials);
    }

    private static int blockRadius(final int stage) {
        return stage <= 3 ? 1 : stage == 4 ? 2 : 3;
    }

    /** The blocks of {@code trials} breath hits at {@code center} (see {@link #attackBreathBlocks}). */
    private static void breathBlocks(final Level level, final BlockPos center, final EntityDragonBase dragon, final int trials) {
        if (trials <= 0 || !dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.canEntityGrief(level, dragon) || !DragonUtils.canGrief(dragon)) {
            return;
        }
        final int stage = dragon.getDragonStage();
        final int radius = blockRadius(stage);
        // Radius of the sphere of older dragons; young ones attack the whole 3x3x3 cube
        final float f = (float) (radius * 3) * 0.333F + 0.5F;
        attackBreathBlocks(level, center, dragon, radius, stage <= 3 ? 0 : f * f, trials);
    }

    private static void applyBreath(final Level level, final BlockPos center, final EntityDragonBase dragon, final int hits, final int blockTrials) {
        final int statusDuration;
        final float damageScale;

        if (dragon.dragonType == DragonType.FIRE) {
            statusDuration = 5 + dragon.getDragonStage() * 5;
            damageScale = (float) IafConfig.dragonAttackDamageFire;
        } else if (dragon.dragonType == DragonType.ICE) {
            statusDuration = 50 * dragon.getDragonStage();
            damageScale = (float) IafConfig.dragonAttackDamageIce;
        } else if (dragon.dragonType == DragonType.LIGHTNING) {
            statusDuration = 3;
            damageScale = (float) IafConfig.dragonAttackDamageLightning;
        } else {
            return;
        }

        final int stage = dragon.getDragonStage();
        final int radius = blockRadius(stage);
        final float f = (float) (radius * 3) * 0.333F + 0.5F;
        final double damageRadius = stage <= 3 ? 3.5 : 2.5F + f * 1.2F;

        heatForgeInputs(level, center, radius);

        breathBlocks(level, center, dragon, blockTrials);

        final DamageSource damageSource = getDamageSource(dragon);
        final float stageDamage = stage * damageScale;
        final int effectRepeats = dragon.dragonType == DragonType.LIGHTNING ? Math.min(hits, MAX_KNOCKBACK_REPEATS) : 1;

        level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        (double) center.getX() - damageRadius,
                        (double) center.getY() - damageRadius,
                        (double) center.getZ() - damageRadius,
                        (double) center.getX() + damageRadius,
                        (double) center.getY() + damageRadius,
                        (double) center.getZ() + damageRadius
                )
        ).forEach(target -> {
            if (!DragonUtils.onSameTeam(dragon, target) && !dragon.is(target) && dragon.hasLineOfSight(target)) {
                target.hurt(damageSource, stageDamage);
                for (int i = 0; i < effectRepeats; i++) {
                    applyDragonEffect(target, dragon, statusDuration);
                }
            }
        });
    }

    /** Forge inputs in the cube get the flame; found among the chunks' block entities instead of a lookup per block. */
    private static void heatForgeInputs(final Level level, final BlockPos center, final int radius) {
        final int minX = center.getX() - radius;
        final int minY = center.getY() - radius;
        final int minZ = center.getZ() - radius;
        final int maxX = center.getX() + radius;
        final int maxY = center.getY() + radius;
        final int maxZ = center.getZ() + radius;
        for (int chunkX = minX >> 4; chunkX <= maxX >> 4; chunkX++) {
            for (int chunkZ = minZ >> 4; chunkZ <= maxZ >> 4; chunkZ++) {
                final LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                for (final BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof TileEntityDragonforgeInput forge && !forge.isRemoved()) {
                        final BlockPos pos = forge.getBlockPos();
                        if (pos.getX() >= minX && pos.getX() <= maxX && pos.getY() >= minY && pos.getY() <= maxY && pos.getZ() >= minZ && pos.getZ() <= maxZ) {
                            forge.onHitWithFlame();
                        }
                    }
                }
            }
        }
    }

    /**
     * Attacks the blocks of the breath area as {@code trials} breath hits would: a block is attacked if any of
     * them would, and gets its fire or ice spike if any of those would place one.
     *
     * @param sphereSqr squared radius of the sphere of older dragons, 0 for the whole cube at even chances
     */
    private static void attackBreathBlocks(final Level level, final BlockPos center, final EntityDragonBase dragon, final int radius, final float sphereSqr, final int trials) {
        final RandomSource random = dragon.getRandom();
        final float placeChance = dragon.dragonType == DragonType.FIRE ? FIRE_PLACE_CHANCE : dragon.dragonType == DragonType.ICE ? ICE_PLACE_CHANCE : 0;
        final BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    position.setWithOffset(center, x, y, z);
                    final float chance;
                    if (sphereSqr <= 0) {
                        chance = 0.5F;
                    } else {
                        final double distSqr = center.distSqr(position);
                        if (distSqr > sphereSqr) {
                            continue;
                        }
                        chance = 1F - (float) distSqr / sphereSqr;
                    }
                    if (chance <= 0) {
                        continue;
                    }
                    final double missAll = Math.pow(1 - chance, trials);
                    if (random.nextFloat() >= 1 - missAll) {
                        continue;
                    }
                    final float place = placeChance <= 0 ? 0 : (float) ((1 - Math.pow(1 - chance * placeChance, trials)) / (1 - missAll));
                    attackBlock(level, dragon, position.immutable(), level.getBlockState(position), place);
                }
            }
        }
    }

    public static void destroyAreaCharge(final Level level, final BlockPos center, final EntityDragonBase dragon) {
        if (dragon == null) {
            return;
        }

        if (dev.vladeezyy.iceandfire.bus.IafBus.post(new DragonFireDamageWorldEvent(dragon, center.getX(), center.getY(), center.getZ()))) {
            return;
        }

        int x = 2;
        int y = 2;
        int z = 2;

        boolean canBreakBlocks = DragonUtils.canGrief(dragon) && dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.canEntityGrief(level, dragon);

        if (canBreakBlocks) {
            if (dragon.getDragonStage() <= 3) {
                BlockPos.betweenClosedStream(center.offset(-x, -y, -z), center.offset(x, y, z)).forEach(position -> {
                    BlockState state = level.getBlockState(position);

                    if (state.getBlock() instanceof IDragonProof) {
                        return;
                    }

                    if (dragon.getRandom().nextFloat() * 3 > center.distSqr(position) && DragonUtils.canDragonBreak(state, dragon)) {
                        level.destroyBlock(position, false);
                    }

                    if (dragon.getRandom().nextBoolean()) {
                        attackBlock(level, dragon, position, state);
                    }
                });
            } else {
                final int radius = dragon.getDragonStage() == 4 ? 2 : 3;
                x = radius + level.random.nextInt(2);
                y = radius + level.random.nextInt(2);
                z = radius + level.random.nextInt(2);
                final float f = (float) (x + y + z) * 0.333F + 0.5F;
                final float ff = f * f;

                destroyBlocks(level, center, x, y, z, ff, dragon);

                x++;
                y++;
                z++;

                BlockPos.betweenClosedStream(center.offset(-x, -y, -z), center.offset(x, y, z)).forEach(position -> {
                    if (center.distSqr(position) <= ff) {
                        attackBlock(level, dragon, position);
                    }
                });
            }
        }

        final int statusDuration;

        if (dragon.dragonType == DragonType.FIRE) {
            statusDuration = 15;
        } else if (dragon.dragonType == DragonType.ICE) {
            statusDuration = 400;
        } else if (dragon.dragonType == DragonType.LIGHTNING) {
            statusDuration = 9;
        } else {
            return;
        }

        final float stageDamage = Math.max(1, dragon.getDragonStage() - 1) * 2F;
        DamageSource damageSource = getDamageSource(dragon);

        level.getEntitiesOfClass(
                LivingEntity.class,
                new AABB(
                        (double) center.getX() - x,
                        (double) center.getY() - y,
                        (double) center.getZ() - z,
                        (double) center.getX() + x,
                        (double) center.getY() + y,
                        (double) center.getZ() + z
                )
        ).forEach(target -> {
            if (!dragon.isAlliedTo(target) && !dragon.is(target) && dragon.hasLineOfSight(target)) {
                target.hurt(damageSource, stageDamage);
                applyDragonEffect(target, dragon, statusDuration);
            }
        });

        if (IafConfig.explosiveDragonBreath) {
            causeExplosion(level, center, dragon, damageSource, dragon.getDragonStage());
        }
    }

    private static DamageSource getDamageSource(final EntityDragonBase dragon) {
        Player player = dragon.getRidingPlayer();

        if (dragon.dragonType == DragonType.FIRE) {
            return player != null ? IafDamageRegistry.causeIndirectDragonFireDamage(dragon, player) : IafDamageRegistry.causeDragonFireDamage(dragon);
        } else if (dragon.dragonType == DragonType.ICE) {
            return player != null ? IafDamageRegistry.causeIndirectDragonIceDamage(dragon, player) : IafDamageRegistry.causeDragonIceDamage(dragon);
        } else if (dragon.dragonType == DragonType.LIGHTNING) {
            return player != null ? IafDamageRegistry.causeIndirectDragonLightningDamage(dragon, player) : IafDamageRegistry.causeDragonLightningDamage(dragon);
        } else {
            return dragon.level().damageSources().mobAttack(dragon);
        }
    }

    private static void attackBlock(final Level level, final EntityDragonBase dragon, final BlockPos position, final BlockState state) {
        attackBlock(level, dragon, position, state, dragon.dragonType == DragonType.ICE ? ICE_PLACE_CHANCE : FIRE_PLACE_CHANCE);
    }

    /** @param placeChance chance to put fire or an ice spike on top */
    private static void attackBlock(final Level level, final EntityDragonBase dragon, final BlockPos position, final BlockState state, final float placeChance) {
        if (state.getBlock() instanceof IDragonProof || !DragonUtils.canDragonBreak(state, dragon)) {
            return;
        }

        BlockState transformed;

        if (dragon.dragonType == DragonType.FIRE) {
            transformed = transformBlockFire(state);
        } else if (dragon.dragonType == DragonType.ICE) {
            transformed = transformBlockIce(state);
        } else if (dragon.dragonType == DragonType.LIGHTNING) {
            transformed = transformBlockLightning(state);
        } else {
            return;
        }

        if (!transformed.is(state.getBlock())) {
            level.setBlockAndUpdate(position, transformed);
        }

        Block elementalBlock;

        if (dragon.dragonType == DragonType.FIRE) {
            elementalBlock = Blocks.FIRE;
        } else if (dragon.dragonType == DragonType.ICE) {
            elementalBlock = IafBlockRegistry.DRAGON_ICE_SPIKES.get();
        } else {
            return;
        }
        boolean doPlaceBlock = dragon.getRandom().nextFloat() < placeChance;

        BlockState stateAbove = level.getBlockState(position.above());

        if (doPlaceBlock && transformed.isSolid() && stateAbove.getFluidState().isEmpty() && !stateAbove.canOcclude() && state.canOcclude() && DragonUtils.canDragonBreak(stateAbove, dragon)) {
            level.setBlockAndUpdate(position.above(), elementalBlock.defaultBlockState());
        }
    }

    private static void attackBlock(final Level level, final EntityDragonBase dragon, final BlockPos position) {
        attackBlock(level, dragon, position, level.getBlockState(position));
    }

    private static void applyDragonEffect(final LivingEntity target, final EntityDragonBase dragon, int statusDuration) {
        if (dragon.dragonType == DragonType.FIRE) {
            target.igniteForSeconds(statusDuration);
        } else if (dragon.dragonType == DragonType.ICE) {
            EntityDataProvider.getCapability(target).ifPresent(data -> data.frozenData.setFrozen(target, statusDuration));
        } else if (dragon.dragonType == DragonType.LIGHTNING) {
            double x = dragon.getX() - target.getX();
            double y = dragon.getZ() - target.getZ();
            target.knockback((double) statusDuration / 10, x, y);
        }
    }

    private static void causeExplosion(Level world, BlockPos center, EntityDragonBase destroyer, DamageSource source, int stage) {
        Explosion.BlockInteraction mode = dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.canEntityGrief(world, destroyer) ? Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.KEEP;
        BlockLaunchExplosion explosion = new BlockLaunchExplosion(world, destroyer, source, center.getX(), center.getY(), center.getZ(), Math.min(2, stage - 2), mode);
        explosion.explode();
        explosion.finalizeExplosion(true);
    }

    private static void destroyBlocks(Level world, BlockPos center, int x, int y, int z, double radius2, Entity destroyer) {
        BlockPos.betweenClosedStream(center.offset(-x, -y, -z), center.offset(x, y, z)).forEach(pos -> {
            if (center.distSqr(pos) <= radius2) {
                BlockState state = world.getBlockState(pos);

                if (state.getBlock() instanceof IDragonProof) {
                    return;
                }

                if (world.random.nextFloat() * 3 > (float) center.distSqr(pos) / radius2 && DragonUtils.canDragonBreak(state, destroyer)) {
                    world.destroyBlock(pos, false);
                }
            }
        });
    }

    public static BlockState transformBlockFire(BlockState in) {
        if (in.getBlock() instanceof SpreadingSnowyDirtBlock) {
            return IafBlockRegistry.CHARRED_GRASS.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(Blocks.DIRT)) {
            return IafBlockRegistry.CHARRED_DIRT.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.SAND) && in.getBlock() == Blocks.GRAVEL) {
            return IafBlockRegistry.CHARRED_GRAVEL.get().defaultBlockState().setValue(BlockFallingReturningState.REVERTS, true);
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && (in.getBlock() == Blocks.COBBLESTONE || in.getBlock().getDescriptionId().contains("cobblestone"))) {
            return IafBlockRegistry.CHARRED_COBBLESTONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && in.getBlock() != IafBlockRegistry.CHARRED_COBBLESTONE.get()) {
            return IafBlockRegistry.CHARRED_STONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.getBlock() == Blocks.DIRT_PATH) {
            return IafBlockRegistry.CHARRED_DIRT_PATH.get().defaultBlockState().setValue(BlockCharedPath.REVERTS, true);
        } else if (in.is(BlockTags.LOGS) || in.is(BlockTags.PLANKS)) {
            return IafBlockRegistry.ASH.get().defaultBlockState();
        } else if (in.is(BlockTags.LEAVES) || in.is(BlockTags.FLOWERS) || in.is(BlockTags.CROPS) || in.getBlock() == Blocks.SNOW) {
            return Blocks.AIR.defaultBlockState();
        }
        return in;
    }

    public static BlockState transformBlockIce(BlockState in) {
        if (in.getBlock() instanceof SpreadingSnowyDirtBlock) {
            return IafBlockRegistry.FROZEN_GRASS.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.DIRT) && in.getBlock() == Blocks.DIRT || in.is(BlockTags.SNOW)) {
            return IafBlockRegistry.FROZEN_DIRT.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.SAND) && in.getBlock() == Blocks.GRAVEL) {
            return IafBlockRegistry.FROZEN_GRAVEL.get().defaultBlockState().setValue(BlockFallingReturningState.REVERTS, true);
        } else if (in.is(BlockTags.SAND) && in.getBlock() != Blocks.GRAVEL) {
            return in;
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && (in.getBlock() == Blocks.COBBLESTONE || in.getBlock().getDescriptionId().contains("cobblestone"))) {
            return IafBlockRegistry.FROZEN_COBBLESTONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && in.getBlock() != IafBlockRegistry.FROZEN_COBBLESTONE.get()) {
            return IafBlockRegistry.FROZEN_STONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.getBlock() == Blocks.DIRT_PATH) {
            return IafBlockRegistry.FROZEN_DIRT_PATH.get().defaultBlockState().setValue(BlockCharedPath.REVERTS, true);
        } else if (in.is(BlockTags.LOGS) || in.is(BlockTags.PLANKS)) {
            return IafBlockRegistry.FROZEN_SPLINTERS.get().defaultBlockState();
        } else if (in.is(Blocks.WATER)) {
            return Blocks.ICE.defaultBlockState();
        } else if (in.is(BlockTags.LEAVES) || in.is(BlockTags.FLOWERS) || in.is(BlockTags.CROPS) || in.getBlock() == Blocks.SNOW) {
            return Blocks.AIR.defaultBlockState();
        }
        return in;
    }

    public static BlockState transformBlockLightning(BlockState in) {
        if (in.getBlock() instanceof SpreadingSnowyDirtBlock) {
            return IafBlockRegistry.CRACKLED_GRASS.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.DIRT) && in.getBlock() == Blocks.DIRT) {
            return IafBlockRegistry.CRACKLED_DIRT.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.SAND) && in.getBlock() == Blocks.GRAVEL) {
            return IafBlockRegistry.CRACKLED_GRAVEL.get().defaultBlockState().setValue(BlockFallingReturningState.REVERTS, true);
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && (in.getBlock() == Blocks.COBBLESTONE || in.getBlock().getDescriptionId().contains("cobblestone"))) {
            return IafBlockRegistry.CRACKLED_COBBLESTONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.is(BlockTags.BASE_STONE_OVERWORLD) && in.getBlock() != IafBlockRegistry.CRACKLED_COBBLESTONE.get()) {
            return IafBlockRegistry.CRACKLED_STONE.get().defaultBlockState().setValue(BlockReturningState.REVERTS, true);
        } else if (in.getBlock() == Blocks.DIRT_PATH) {
            return IafBlockRegistry.CRACKLED_DIRT_PATH.get().defaultBlockState().setValue(BlockCharedPath.REVERTS, true);
        } else if (in.is(BlockTags.LOGS) || in.is(BlockTags.PLANKS)) {
            return IafBlockRegistry.ASH.get().defaultBlockState();
        } else if (in.is(BlockTags.LEAVES) || in.is(BlockTags.FLOWERS) || in.is(BlockTags.CROPS) || in.getBlock() == Blocks.SNOW) {
            return Blocks.AIR.defaultBlockState();
        }
        return in;
    }
}
