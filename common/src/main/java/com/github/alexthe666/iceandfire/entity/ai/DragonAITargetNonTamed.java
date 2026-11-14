package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class DragonAITargetNonTamed<T extends LivingEntity> extends NearestAttackableTargetGoal<T> {
    /** Mobs are searched this far above and below the dragon (it was the whole follow range, 256+ blocks high) */
    private static final double MOB_VERTICAL_REACH = 32.0D;
    /** Asleep, only a player this close wakes the dragon (squared below) */
    private static final double SLEEP_WAKE_DISTANCE = 4.0D;
    private final EntityDragonBase dragon;

    public DragonAITargetNonTamed(EntityDragonBase entityIn, Class<T> classTarget, boolean checkSight, Predicate<LivingEntity> targetSelector) {
        super(entityIn, classTarget, 5, checkSight, false, targetSelector);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.dragon = entityIn;
    }

    @Override
    public boolean canUse() {
        if (dragon.isTame()) {
            return false;
        }

        if (dragon.lookingForRoostAIFlag) {
            return false;
        }

        // Read before the search: findTarget narrows the area for a sleeping dragon
        boolean isSleeping = dragon.isSleeping();
        boolean canUse = super.canUse();

        if (canUse) {
            if (isSleeping && target instanceof Player) {
                return dragon.distanceToSqr(target) <= SLEEP_WAKE_DISTANCE * SLEEP_WAKE_DISTANCE;
            }

            return !isSleeping;
        }

        return false;
    }

    @Override
    protected void findTarget() {
        // goals tick on the server only
        if (!(this.mob.level() instanceof ServerLevel level)) {
            super.findTarget();
            return;
        }
        double followDistance = this.getFollowDistance();
        AABB box = this.dragon.getBoundingBox();
        if (dragon.isSleeping()) {
            // Asleep it only takes a player within 4 blocks of its feet, and only if nothing valid is nearer to its
            // eyes: anything nearer than that player is within 4 + eye height of the eyes, so this box gives the
            // same answer as the full search
            double reach = SLEEP_WAKE_DISTANCE + Math.abs(this.dragon.getEyeHeight());
            AABB near = box.inflate(reach).minmax(AABB.ofSize(this.dragon.getEyePosition(), reach * 2, reach * 2, reach * 2));
            this.target = level.getNearestEntity(level.getEntitiesOfClass(this.targetType, near, e -> true),
                this.targetConditions.range(followDistance), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            return;
        }
        // Mobs: +-32 blocks of height (cave mobs deep below are skipped); airborne, down to 32 below the ground under it
        double up = Math.min(followDistance, MOB_VERTICAL_REACH);
        double down = up;
        if (dragon.isFlying() || dragon.isHovering()) {
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.dragon.getBlockX(), this.dragon.getBlockZ());
            down = Math.min(followDistance, Math.max(down, box.minY - ground + MOB_VERTICAL_REACH));
        }
        AABB mobArea = new AABB(box.minX - followDistance, box.minY - down, box.minZ - followDistance,
            box.maxX + followDistance, box.maxY + up, box.maxZ + followDistance);
        List<T> candidates = level.getEntitiesOfClass(this.targetType, mobArea, e -> !(e instanceof Player));
        // Players keep the full range (a player flying over a roost is still seen): a few, read from the player list.
        // range() sets the current follow range (1.21.1 conditions are mutable; vanilla kept the one of construction)
        AABB playerArea = this.getTargetSearchArea(followDistance);
        for (ServerPlayer player : level.players()) {
            if (this.targetType.isInstance(player) && player.getBoundingBox().intersects(playerArea)) {
                candidates.add(this.targetType.cast(player));
            }
        }
        this.target = level.getNearestEntity(candidates, this.targetConditions.range(followDistance), this.mob,
            this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

    @Override
    protected @NotNull AABB getTargetSearchArea(double targetDistance) {
        return this.dragon.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
    }

    @Override
    protected double getFollowDistance() {
        AttributeInstance iattributeinstance = this.mob.getAttribute(Attributes.FOLLOW_RANGE);
        return iattributeinstance == null ? 128.0D : iattributeinstance.getValue();
    }
}
