package com.github.alexthe666.iceandfire.entity.ai;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;

import java.util.function.Predicate;

public class VillagerAIFearUntamed extends AvoidEntityGoal<LivingEntity> {
    private long nextCheck;

    public VillagerAIFearUntamed(PathfinderMob entityIn, Class<LivingEntity> avoidClass, float distance, double nearSpeedIn, double farSpeedIn, Predicate<LivingEntity> targetPredicate) {
        super(entityIn, avoidClass, distance, nearSpeedIn, farSpeedIn, targetPredicate);
    }

    /**
     * About once a second, spread over the mobs: every animal and villager walked all living entities of a 60x6x60 box
     * every other tick (a farm of 200 animals: ~100 such walks a tick). The feared mobs are of many classes (dragons,
     * worms, dread mobs, gorgons...), so the search stays by LivingEntity.
     */
    @Override
    public boolean canUse() {
        long now = this.mob.level().getGameTime();
        if (now < this.nextCheck) {
            return false;
        }
        this.nextCheck = now + 15 + this.mob.getRandom().nextInt(11);
        return super.canUse();
    }
}
