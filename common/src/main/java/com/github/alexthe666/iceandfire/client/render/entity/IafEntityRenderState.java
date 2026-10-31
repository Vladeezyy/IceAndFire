package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

/** Render state of the mod's plain entity renderers: the entity itself (the 1.21.1 renderers read it while drawing) */
public class IafEntityRenderState extends EntityRenderState {
    public Entity entity;
    public float partialTick;
    public float yRot;
}
