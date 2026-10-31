package com.github.alexthe666.iceandfire.client.render;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

/** The entity behind a render state (EntityRenderStateMixin): 26.x states carry no entity, the mod's overlays need it */
public interface IafEntityHolder {
    @Nullable
    Entity iceandfire$entity();

    float iceandfire$partialTick();

    void iceandfire$set(@Nullable Entity entity, float partialTick);
}
