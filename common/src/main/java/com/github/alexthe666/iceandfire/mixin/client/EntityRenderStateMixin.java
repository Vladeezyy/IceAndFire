package com.github.alexthe666.iceandfire.mixin.client;

import com.github.alexthe666.iceandfire.client.render.IafEntityHolder;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements IafEntityHolder {
    @Unique
    private @Nullable Entity iceandfire$entity;
    @Unique
    private float iceandfire$partialTick;

    @Override
    public @Nullable Entity iceandfire$entity() {
        return this.iceandfire$entity;
    }

    @Override
    public float iceandfire$partialTick() {
        return this.iceandfire$partialTick;
    }

    @Override
    public void iceandfire$set(@Nullable Entity entity, float partialTick) {
        this.iceandfire$entity = entity;
        this.iceandfire$partialTick = partialTick;
    }
}
