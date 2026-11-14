package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntitySiren;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class SirenAIFindWaterTarget extends Goal {
    // Sampled instead of the whole 10x10x10 cube with a raycast for each of its ~1000 blocks (every ~4 ticks in water).
    // The first visible hit is uniform over the visible spots, as the old random pick from the full lists.
    private static final int SING_COLUMNS = 16;
    private static final int WATER_PROBES = 16;
    private static final int SING_RAYCASTS = 4;
    private static final int WATER_RAYCASTS = 3;
    private final EntitySiren mob;

    public SirenAIFindWaterTarget(EntitySiren mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!this.mob.isInWater()) {
            return false;
        }
        if (this.mob.getRandom().nextFloat() < 0.5F) {
            Path path = this.mob.getNavigation().getPath();
            if (path != null
                && path.getEndNode() != null /*
             * TODO: path is nullable here !this.mob.getNavigator().noPath() &&
             * !this.mob.isDirectPathBetweenPoints(this.mob.getPositionVec(),
             * new Vector3d(path.getFinalPathPoint().x,
             * path.getFinalPathPoint().y, path.getFinalPathPoint().z))
             */) {
                this.mob.getNavigation().stop();
            }
            if (this.mob.getNavigation().isDone()) {
                Vec3 vec3 = this.findWaterTarget();
                if (vec3 != null) {
                    this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }

    public Vec3 findWaterTarget() {
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            BlockPos blockpos1 = target.blockPosition();
            return new Vec3(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
        }
        final Level level = this.mob.level();
        final RandomSource random = this.mob.getRandom();
        final Vec3 from = this.mob.position();
        final int posX = (int) this.mob.getX();
        final int posY = (int) this.mob.getY();
        final int posZ = (int) this.mob.getZ();
        final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (this.mob.wantsToSing()) {
            // A singing spot (solid block, air above) is a thin surface: sample columns, not points, or the shore is missed
            int casts = 0;
            for (int i = 0; i < SING_COLUMNS && casts < SING_RAYCASTS; i++) {
                int x = posX - 5 + random.nextInt(10);
                int z = posZ - 5 + random.nextInt(10);
                boolean airAbove = level.getBlockState(pos.set(x, posY + 5, z)).isAir();
                for (int y = posY + 4; y >= posY - 5; y--) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (airAbove && state.isSolid()) {
                        casts++;
                        Vec3 spot = new Vec3(x, y + 1, z);
                        if (this.mob.isDirectPathBetweenPoints(from, spot)) {
                            return spot;
                        }
                        break;
                    }
                    airAbove = state.isAir();
                }
            }
        }
        int casts = 0;
        for (int i = 0; i < WATER_PROBES && casts < WATER_RAYCASTS; i++) {
            pos.set(posX - 5 + random.nextInt(10), posY - 5 + random.nextInt(10), posZ - 5 + random.nextInt(10));
            if (level.getBlockState(pos).is(Blocks.WATER)) {
                casts++;
                Vec3 spot = new Vec3(pos.getX(), pos.getY(), pos.getZ());
                if (this.mob.isDirectPathBetweenPoints(from, spot)) {
                    return spot;
                }
            }
        }
        return null;
    }
}
