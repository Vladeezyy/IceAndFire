package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.LivingEntity;

public abstract class LivingEvent extends EntityEvent {
    protected LivingEvent(LivingEntity entity) {
        super(entity);
    }

    @Override
    public LivingEntity getEntity() {
        return (LivingEntity) super.getEntity();
    }
}
