package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.world.entity.LivingEntity;

/**
 * 1.20.5 - 1.21.1: {@code LivingEntityRenderer.render} scaled every model by {@code entity.getScale()}, which the mod's
 * creatures overrode for their hitbox size, so they were scaled twice and the renderers divided it out again. 26.x
 * {@code getScale()} is final and only the scale attribute (the creatures' size is {@code getIafScale()}): the vanilla
 * scaling is the attribute's alone, as for vanilla mobs, and nothing has to be undone.
 */
public final class VanillaScale {
    private VanillaScale() {}

    /** The factor that cancels the vanilla scaling of this entity: 1 since 26.x (see the class comment). */
    public static float undo(LivingEntity entity) {
        return 1F;
    }
}
