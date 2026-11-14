package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DeathWormAIFindSandTarget extends Goal {
    private static final int TAME_RANGE = 25;
    // Random probes per try instead of the whole cube (8,000 blocks wild, 125,000 tame, every ~4 ticks). The first sand
    // hit is uniform over the sand in the cube, as the old random pick from the full list; a miss retries next try.
    private static final int WILD_PROBES = 16;
    private static final int TAME_PROBES = 20;
    private final EntityDeathWorm mob;
    private final int range;

    public DeathWormAIFindSandTarget(EntityDeathWorm mob, int range) {
        this.mob = mob;
        this.range = range;
    }

    @Override
    public boolean canUse() {
        if (this.mob.getTarget() != null) {
            return false;
        }

        if (!this.mob.isInSand() || this.mob.isPassenger() || this.mob.isVehicle()) {
            return false;
        }
        if (this.mob.getRandom().nextFloat() < 0.5F) {
            final Path path = this.mob.getNavigation().getPath();
            if (path != null /*
             * || !this.mob.getNavigator().noPath() && !isDirectPathBetweenPoints(this.mob,
             * this.mob.getPositionVec(), new Vector3d(path.getFinalPathPoint().x,
             * path.getFinalPathPoint().y, path.getFinalPathPoint().z))
             */) {
                this.mob.getNavigation().stop();
            }
            if (this.mob.getNavigation().isDone() /* && !this.mob.getMoveControl().hasWanted()*/) {
                BlockPos vec3 = this.findSandTarget();
                if (vec3 != null) {
                    this.mob.getMoveControl().setWantedPosition(vec3.getX(), vec3.getY(), vec3.getZ(), 1.0);
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

    public BlockPos findSandTarget() {
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            return target.blockPosition().below();
        }
        final int r;
        final int cx, cy, cz;
        final int probes;
        BlockPos home = this.mob.getWormHome();
        if (this.mob.isTame() && home != null) {
            r = TAME_RANGE;
            cx = home.getX();
            cy = home.getY();
            cz = home.getZ();
            probes = TAME_PROBES;
        } else {
            r = this.range;
            cx = (int) this.mob.getX();
            cy = (int) this.mob.getY();
            cz = (int) this.mob.getZ();
            probes = WILD_PROBES;
        }
        if (r <= 0) {
            return null;
        }
        Level level = this.mob.level();
        RandomSource random = this.mob.getRandom();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int size = r * 2;
        for (int i = 0; i < probes; i++) {
            pos.set(cx - r + random.nextInt(size), cy - r + random.nextInt(size), cz - r + random.nextInt(size));
            // The home may be far from the worm: never load a chunk for a probe
            if (level.hasChunkAt(pos) && level.getBlockState(pos).is(BlockTags.SAND)) {
                return pos.immutable();
            }
        }
        return null;
    }

    public boolean isDirectPathBetweenPoints(Entity entity, Vec3 vec1, Vec3 vec2) {
        return true;
    }
}
