package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

/** A mob picks a new target; listeners may change it; canceling keeps the old target */
public class LivingChangeTargetEvent extends LivingEvent implements ICancellableEvent {
    @Nullable
    private final LivingEntity originalTarget;
    @Nullable
    private LivingEntity newTarget;

    public LivingChangeTargetEvent(LivingEntity entity, @Nullable LivingEntity target) {
        super(entity);
        this.originalTarget = target;
        this.newTarget = target;
    }

    @Nullable
    public LivingEntity getOriginalAboutToBeSetTarget() {
        return this.originalTarget;
    }

    @Nullable
    public LivingEntity getNewAboutToBeSetTarget() {
        return this.newTarget;
    }

    public void setNewAboutToBeSetTarget(@Nullable LivingEntity target) {
        this.newTarget = target;
    }
}
