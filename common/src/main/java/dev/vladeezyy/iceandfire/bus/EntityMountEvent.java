package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

/** An entity starts or stops riding another one (both sides); canceling it is not supported */
public class EntityMountEvent extends EntityEvent {
    private final Entity entityBeingMounted;
    private final boolean mounting;

    public EntityMountEvent(Entity entityMounting, Entity entityBeingMounted, boolean mounting) {
        super(entityMounting);
        this.entityBeingMounted = entityBeingMounted;
        this.mounting = mounting;
    }

    public Entity getEntityMounting() {
        return this.getEntity();
    }

    public Entity getEntityBeingMounted() {
        return this.entityBeingMounted;
    }

    public Level getLevel() {
        return this.getEntity().level();
    }

    public boolean isMounting() {
        return this.mounting;
    }

    public boolean isDismounting() {
        return !this.mounting;
    }
}
