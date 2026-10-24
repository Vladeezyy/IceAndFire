package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Entity;

public abstract class EntityEvent extends Event {
    private final Entity entity;

    protected EntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return this.entity;
    }
}
