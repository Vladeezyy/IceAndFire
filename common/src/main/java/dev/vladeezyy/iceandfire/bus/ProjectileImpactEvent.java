package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

/** A projectile hits something; canceling it ignores the hit */
public class ProjectileImpactEvent extends EntityEvent implements ICancellableEvent {
    private final HitResult rayTraceResult;

    public ProjectileImpactEvent(Projectile projectile, HitResult rayTraceResult) {
        super(projectile);
        this.rayTraceResult = rayTraceResult;
    }

    @Override
    public Projectile getEntity() {
        return (Projectile) super.getEntity();
    }

    public HitResult getRayTraceResult() {
        return this.rayTraceResult;
    }
}
