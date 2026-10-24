package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import java.util.Collection;

/** The drops of a dead entity, before they spawn; listeners may change the collection; canceling drops nothing */
public class LivingDropsEvent extends LivingEvent implements ICancellableEvent {
    private final DamageSource source;
    private final Collection<ItemEntity> drops;
    private final boolean recentlyHit;

    public LivingDropsEvent(LivingEntity entity, DamageSource source, Collection<ItemEntity> drops, boolean recentlyHit) {
        super(entity);
        this.source = source;
        this.drops = drops;
        this.recentlyHit = recentlyHit;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public Collection<ItemEntity> getDrops() {
        return this.drops;
    }

    public boolean isRecentlyHit() {
        return this.recentlyHit;
    }
}
