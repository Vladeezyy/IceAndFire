package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** {@link IafMobRenderer} for living entities that are not mobs (the dragon and myrmex eggs) */
public abstract class IafLivingRenderer<T extends LivingEntity, M extends EntityModel<? super IafMobRenderState>> extends LivingEntityRenderer<T, IafMobRenderState, M> {
    protected IafLivingRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Override
    public IafMobRenderState createRenderState() {
        return new IafMobRenderState();
    }

    @Override
    public void extractRenderState(T entity, IafMobRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.partialTick = partialTicks;
        state.pose.fromLiving(state, entity, partialTicks);
        state.texture = this.getTexture(entity);
    }

    protected abstract Identifier getTexture(T entity);

    @Override
    public Identifier getTextureLocation(IafMobRenderState state) {
        return state.texture;
    }
}
