package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Entity;

public abstract class EntityTickEvent extends EntityEvent {
    protected EntityTickEvent(Entity entity) {
        super(entity);
    }

    /** Before the entity ticks (server and client) */
    public static class Pre extends EntityTickEvent {
        public Pre(Entity entity) {
            super(entity);
        }
    }
}
