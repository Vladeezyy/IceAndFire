package com.github.alexthe666.iceandfire.client.render.entity;

import dev.vladeezyy.rampart.client.render.RampartMobRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;

/** A mob drawn with a Citadel-style model; the texture is picked from the entity ({@link #getTexture}) */
public abstract class IafMobRenderer<T extends Mob, M extends EntityModel<? super IafMobRenderState>> extends RampartMobRenderer<T, IafMobRenderState, M> {

	protected IafMobRenderer(EntityRendererProvider.Context context, M model, float shadowRadius) {
		super(context, model, shadowRadius);
	}

	@Override
	public IafMobRenderState createRenderState() {
		return new IafMobRenderState();
	}

	@Override
	public void extractRenderState(T entity, IafMobRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.texture = this.getTexture(entity);
	}

	protected abstract Identifier getTexture(T entity);

	@Override
	public Identifier getTextureLocation(IafMobRenderState state) {
		return state.texture;
	}
}
