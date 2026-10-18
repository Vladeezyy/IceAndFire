package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * Since 1.20.5 {@code LivingEntityRenderer.render} scales every model by {@code entity.getScale()} before the
 * renderer's own {@code scale(...)}. The creatures of the mod override {@code getScale()} for their hitbox size (as in
 * 1.20.1) and scale their models themselves, so in 1.21 they were scaled twice: an adult dragon 4.4 times its size,
 * the rider under its body. The renderers divide this factor out again.
 */
public final class VanillaScale {
    private VanillaScale() {}

    /** The factor that cancels the vanilla scaling of this entity. */
    public static float undo(LivingEntity entity) {
        float scale = entity.getScale();
        return scale > 1.0E-4F ? 1F / scale : 1F;
    }
}
