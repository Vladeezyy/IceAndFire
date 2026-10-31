package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/** A creature with its own size ({@code getIafScale()}, 1.20.1's {@code getScale()} override) */
public interface IafScaled {
    float getIafScale();

    /** The size 1.21.1's {@code getScale()} gave: the creature's own, else the scale attribute */
    static float of(Entity entity) {
        if (entity instanceof IafScaled scaled) {
            return scaled.getIafScale();
        }
        return entity instanceof LivingEntity living ? living.getScale() : 1F;
    }
}
