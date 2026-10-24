package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public abstract class LivingEntityUseItemEvent extends LivingEvent {
    private final ItemStack item;
    private int duration;

    protected LivingEntityUseItemEvent(LivingEntity entity, ItemStack item, int duration) {
        super(entity);
        this.item = item;
        this.duration = duration;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public int getDuration() {
        return this.duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    /** Every tick while the item is in use; the duration left may be changed; canceling stops using it */
    public static class Tick extends LivingEntityUseItemEvent implements ICancellableEvent {
        public Tick(LivingEntity entity, ItemStack item, int duration) {
            super(entity, item, duration);
        }
    }
}
