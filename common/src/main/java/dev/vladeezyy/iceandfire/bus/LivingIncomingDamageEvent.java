package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

/** An entity is about to take damage; canceling it stops the damage */
public class LivingIncomingDamageEvent extends LivingEvent implements ICancellableEvent {
    private final DamageSource source;
    private float amount;

    public LivingIncomingDamageEvent(LivingEntity entity, DamageSource source, float amount) {
        super(entity);
        this.source = source;
        this.amount = amount;
    }

    public DamageSource getSource() {
        return this.source;
    }

    public float getAmount() {
        return this.amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
