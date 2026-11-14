package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

/**
 * A layer of a mob drawn with a Citadel-style model (26.x): the render state carries the entity (Rampart's pose), so
 * the layer reads the entity as the 1.21.1 layers did, and draws by submitting to the collector.
 */
public abstract class IafEntityLayer<T extends LivingEntity, S extends RampartLivingRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

	protected IafEntityLayer(RenderLayerParent<S, M> parent) {
		super(parent);
	}

	@Override
	@SuppressWarnings("unchecked")
	public final void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
		if (state.pose.entity != null) {
			this.submit(poseStack, collector, light, state, (T) state.pose.entity);
		}
	}

	protected abstract void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, T entity);

	/**
	 * Counts parent posings by layers; a state keeps the count it was posed at. Same test as "last posed for this
	 * state", without a static reference to a state (it holds the entity, and so the level after leaving it)
	 */
	private static long poseCount = 1;

	/**
	 * Poses the parent model for this state now, for layers that read its parts (1.21.1 posed it before the layers;
	 * 26.x poses it when it is drawn, after the layers ran). Once per entity and frame, however many layers ask.
	 */
	protected void poseParent(S state) {
		if (state instanceof IafMobRenderState iafState) {
			if (iafState.parentPosedAt != poseCount) {
				this.getParentModel().setupAnim(state);
				iafState.parentPosedAt = ++poseCount;
			}
		} else {
			this.getParentModel().setupAnim(state);
		}
	}

	/**
	 * The parent model again (posed from the state when it is drawn) with another render type. {@code order} draws it
	 * after the body (vanilla's overlays use 1; stacked overlays count up).
	 */
	protected void submitParent(PoseStack poseStack, SubmitNodeCollector collector, S state, RenderType type, int light, int overlay, int color, int order) {
		collector.order(order).submitModel(this.getParentModel(), state, poseStack, type, light, overlay, color, null, state.outlineColor);
	}

	protected void submitParent(PoseStack poseStack, SubmitNodeCollector collector, S state, RenderType type, int light) {
		this.submitParent(poseStack, collector, state, type, light, OverlayTexture.NO_OVERLAY, -1, 1);
	}
}
