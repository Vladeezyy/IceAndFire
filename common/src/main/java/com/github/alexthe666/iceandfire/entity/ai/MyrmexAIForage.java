package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.api.event.GenericGriefEvent;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.AdvancedPathNavigate;
import com.github.alexthe666.iceandfire.pathfinding.raycoms.PathResult;
import dev.vladeezyy.iceandfire.bus.IafBus;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;

public class MyrmexAIForage extends Goal {

    private static final int RADIUS = 16;
    private static final Predicate<BlockState> EDIBLE = EntityMyrmexBase::isEdibleBlock;
    // pause after a scan that found nothing: this many ticks plus up to as many again
    private static final int EMPTY_SCAN_PAUSE = 100;
    // a worker that moved farther than this (squared) from an empty scan scans again
    private static final int MOVED_FOR_RESCAN_SQ = 4 * 4;
    private final EntityMyrmexWorker myrmex;
    private BlockPos targetBlock = null;
    private BlockPos emptyScanPos = null;
    private long emptyScanUntil;
    private int wanderRadius;
    private final int chance;
    private PathResult path;
    private int failedToFindPath = 0;

    public MyrmexAIForage(EntityMyrmexWorker myrmex, int chanceIn) {
        super();
        this.myrmex = myrmex;
        this.setFlags(EnumSet.of(Flag.MOVE));
        this.chance = chanceIn;
    }

    @Override
    public boolean canUse() {
        if (!this.myrmex.canMove() || this.myrmex.holdingSomething() || !this.myrmex.getNavigation().isDone()
            || this.myrmex.isInHive() || this.myrmex.shouldEnterHive()) {
            return false;
        }
        if (!(this.myrmex.getNavigation() instanceof AdvancedPathNavigate) || this.myrmex.isPassenger()) {
            return false;
        }
        if (this.myrmex.getWaitTicks() > 0) {
            return false;
        }

        // Closest edible block nearby
        BlockPos edible = findEdibleBlock(false);
        // If there are no edible blocks nearby
        if (edible == null) {
            return myrmex.getRandom().nextInt(chance) == 0 && increaseRadiusAndWander();
        }
        this.targetBlock = edible;
        this.path = ((AdvancedPathNavigate) this.myrmex.getNavigation()).moveToXYZ(targetBlock.getX(),
            targetBlock.getY(), targetBlock.getZ(), 1D);
        return myrmex.getRandom().nextInt(chance) == 0;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.targetBlock == null) return false;
        if (this.myrmex.getWaitTicks() > 0) return false;
        if (myrmex.shouldEnterHive()) {
            this.myrmex.keepSearching = false;
            return false;
        }
        return !this.myrmex.holdingSomething();
    }

    @Override
    public void tick() {
        // If we haven't found an edible block as a target
        if (targetBlock != null && this.myrmex.keepSearching) {
            // If the myrmex is close enough to the target or at the end of the path
            if (this.myrmex.isCloseEnoughToTarget(targetBlock, 12)
                || !this.myrmex.pathReachesTarget(path, targetBlock, 12)) {
                failedToFindPath = 0;
                BlockPos edible = findEdibleBlock(false);
                // If we found an edible block nearby
                if (edible != null) {
                    this.myrmex.keepSearching = false;
                    this.targetBlock = edible;
                    this.path = ((AdvancedPathNavigate) this.myrmex.getNavigation()).moveToXYZ(targetBlock.getX(),
                        targetBlock.getY(), targetBlock.getZ(), 1D);
                }
                // If there are still no edible blocks nearby
                else {
                    increaseRadiusAndWander();
                }
            }
            // if we have found an edible block
        } else if (!this.myrmex.keepSearching) {
            failedToFindPath = 0;
            BlockState block = this.myrmex.level().getBlockState(this.targetBlock);
            // Test if the block is edible
            if (EntityMyrmexBase.isEdibleBlock(block)) {
                final double distance = this.getDistanceSq(this.targetBlock);
                if (distance < 6) {
                    block.getBlock();
                    // Routine to break block and add item to myrmex
                    List<ItemStack> drops = Block.getDrops(block, (ServerLevel) this.myrmex.level(), this.targetBlock,
                        this.myrmex.level().getBlockEntity(targetBlock)); // use the old method until it gets removed, for
                    // backward compatibility
                    if (!drops.isEmpty()) {
                        this.myrmex.level().destroyBlock(this.targetBlock, false);
                        ItemStack heldStack = drops.get(0).copy();
                        heldStack.setCount(1);
                        drops.get(0).shrink(1);
                        this.myrmex.setItemInHand(InteractionHand.MAIN_HAND, heldStack);
                        for (ItemStack stack : drops) {
                            ItemEntity itemEntity = new ItemEntity(this.myrmex.level(),
                                this.targetBlock.getX() + this.myrmex.getRandom().nextDouble(),
                                this.targetBlock.getY() + this.myrmex.getRandom().nextDouble(),
                                this.targetBlock.getZ() + this.myrmex.getRandom().nextDouble(), stack);
                            itemEntity.setDefaultPickUpDelay();
                            if (!this.myrmex.level().isClientSide) {
                                this.myrmex.level().addFreshEntity(itemEntity);
                            }
                        }
                        this.targetBlock = null;
                        this.stop();
                        this.myrmex.keepSearching = false;
                        this.wanderRadius = RADIUS;
                    }
                }
                // If the myrmex reached the end of the path but is still not close enough to
                // the target
                else if (!this.myrmex.pathReachesTarget(path, targetBlock, 12)) {
                    BlockPos edible = findEdibleBlock(true);
                    // If we found an edible block nearby
                    if (edible != null) {
                        this.myrmex.keepSearching = false;
                        // This time choose a different random edible block
                        this.targetBlock = edible;
                        this.path = ((AdvancedPathNavigate) this.myrmex.getNavigation()).moveToXYZ(targetBlock.getX(),
                            targetBlock.getY(), targetBlock.getZ(), 1D);
                    } else {
                        this.myrmex.keepSearching = true;
                    }
                }
            }
        }

    }

    @Override
    public void stop() {
        this.targetBlock = null;
        this.myrmex.keepSearching = true;
    }

    private double getDistanceSq(BlockPos pos) {
        return getDistanceSq(pos.getX(), pos.getY(), pos.getZ());
    }

    private double getDistanceSq(int x, int y, int z) {
        final double deltaX = this.myrmex.getX() - (x + 0.5);
        final double deltaY = this.myrmex.getY() + this.myrmex.getEyeHeight() - (y + 0.5);
        final double deltaZ = this.myrmex.getZ() - (z + 0.5);
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    /**
     * An edible block in the 33x17x33 box around the worker (18,513 blocks): the closest one, or a random one. Null if
     * there is none.
     * <p>
     * Read section by section in loaded chunks only (getBlockState of an unloaded chunk loads it on the server
     * thread); a section of air only or without an edible state in its palette is skipped whole. After a scan that
     * found nothing the same spot is not read again for 100..200 ticks (random per worker, so a hive does not scan in
     * step): it counts as empty. A worker that walked away scans its new spot at once.
     */
    @Nullable
    private BlockPos findEdibleBlock(boolean random) {
        Level level = this.myrmex.level();
        BlockPos center = this.myrmex.blockPosition();
        long now = level.getGameTime();
        if (this.emptyScanPos != null && now < this.emptyScanUntil && center.distSqr(this.emptyScanPos) <= MOVED_FOR_RESCAN_SQ) {
            return null;
        }
        int minX = center.getX() - RADIUS, maxX = center.getX() + RADIUS;
        int minZ = center.getZ() - RADIUS, maxZ = center.getZ() + RADIUS;
        int minY = Math.max(center.getY() - RADIUS / 2, level.getMinBuildHeight());
        int maxY = Math.min(center.getY() + RADIUS / 2, level.getMaxBuildHeight() - 1);
        // without a listener nothing can cancel the event: no event object per block
        boolean griefListeners = IafBus.hasListeners(GenericGriefEvent.class);
        long found = 0L;
        int count = 0;
        double bestDistance = Double.MAX_VALUE;
        BlockState lastState = null;
        boolean lastEdible = false;
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                LevelChunkSection[] sections = chunk.getSections();
                int x0 = Math.max(minX, cx << 4), x1 = Math.min(maxX, (cx << 4) + 15);
                int z0 = Math.max(minZ, cz << 4), z1 = Math.min(maxZ, (cz << 4) + 15);
                for (int sy = minY >> 4; sy <= maxY >> 4; sy++) {
                    int index = chunk.getSectionIndexFromSectionY(sy);
                    if (index < 0 || index >= sections.length) {
                        continue;
                    }
                    LevelChunkSection section = sections[index];
                    if (section.hasOnlyAir() || !section.maybeHas(EDIBLE)) {
                        continue;
                    }
                    int y0 = Math.max(minY, sy << 4), y1 = Math.min(maxY, (sy << 4) + 15);
                    for (int z = z0; z <= z1; z++) {
                        for (int y = y0; y <= y1; y++) {
                            for (int x = x0; x <= x1; x++) {
                                BlockState state = section.getBlockState(x & 15, y & 15, z & 15);
                                // runs of the same state are common: the tag is looked up once per run
                                if (state != lastState) {
                                    lastState = state;
                                    lastEdible = EntityMyrmexBase.isEdibleBlock(state);
                                }
                                if (!lastEdible || griefListeners && IafBus.post(new GenericGriefEvent(this.myrmex, x, y, z))) {
                                    continue;
                                }
                                count++;
                                if (random) {
                                    // reservoir sampling: uniform over all candidates without a list of them
                                    if (ThreadLocalRandom.current().nextInt(count) == 0) {
                                        found = BlockPos.asLong(x, y, z);
                                    }
                                } else {
                                    double distance = getDistanceSq(x, y, z);
                                    if (distance < bestDistance) {
                                        bestDistance = distance;
                                        found = BlockPos.asLong(x, y, z);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (count == 0) {
            this.emptyScanUntil = now + EMPTY_SCAN_PAUSE + this.myrmex.getRandom().nextInt(EMPTY_SCAN_PAUSE + 1);
            this.emptyScanPos = center;
            return null;
        }
        this.emptyScanPos = null;
        this.myrmex.keepSearching = false;
        return BlockPos.of(found);
    }

    private boolean increaseRadiusAndWander() {
        this.myrmex.keepSearching = true;
        if (myrmex.getHive() != null) {
            wanderRadius = myrmex.getHive().getWanderRadius();
            myrmex.getHive().setWanderRadius(wanderRadius * 2);
        }
        // Increase wander radius
        wanderRadius *= 2;
        // this.myrmex.setWaitTicks(80+new Random().nextInt(40));
        // Set target as random position inside wanderRadius
        if (wanderRadius >= IafConfig.myrmexMaximumWanderRadius) {
            wanderRadius = IafConfig.myrmexMaximumWanderRadius;
            this.myrmex.setWaitTicks(80 + ThreadLocalRandom.current().nextInt(40));
            // Keep track of how many times the myrmex has potentially not found a path to a
            // target
            failedToFindPath++;
            if (failedToFindPath >= 10) {
                this.myrmex.setWaitTicks(800 + ThreadLocalRandom.current().nextInt(40));
            }
        }
        // DefaultRandomPos reads blocks up to the radius away with no loaded check: at the edge of the loaded area the
        // radius is halved until its box is loaded (up to 32 around a ticking mob always is)
        int radius = wanderRadius;
        BlockPos at = this.myrmex.blockPosition();
        while (radius > RADIUS && !EntityMyrmexBase.isAreaLoaded(this.myrmex.level(), at.getX() - radius, at.getZ() - radius, at.getX() + radius, at.getZ() + radius)) {
            radius >>= 1;
        }
        Vec3 vec = DefaultRandomPos.getPos(this.myrmex, radius, 7);
        if (vec != null) {
            this.targetBlock = BlockPos.containing(vec);
        }
        if (this.targetBlock != null) {
            this.path = ((AdvancedPathNavigate) this.myrmex.getNavigation()).moveToXYZ(targetBlock.getX(),
                targetBlock.getY(), targetBlock.getZ(), 1D);
            return true;
        }
        return false;
    }

}
