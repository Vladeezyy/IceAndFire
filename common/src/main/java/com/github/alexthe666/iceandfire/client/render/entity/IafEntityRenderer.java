package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;

/**
 * A 1.21.1-style entity renderer on 26.x: the state keeps the entity, and {@link #renderLegacy} (1.21.1's render)
 * draws into an {@link IafBufferSource} that is submitted with the frame.
 */
public abstract class IafEntityRenderer<T extends Entity> extends EntityRenderer<T, IafEntityRenderState> {
    protected IafEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public IafEntityRenderState createRenderState() {
        return new IafEntityRenderState();
    }

    @Override
    public void extractRenderState(T entity, IafEntityRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.entity = entity;
        state.partialTick = partialTicks;
        state.yRot = entity.getYRot(partialTicks);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(IafEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.entity != null) {
            IafBufferSource buffers = new IafBufferSource();
            this.renderLegacy((T) state.entity, state.yRot, state.partialTick, poseStack, buffers, state.lightCoords);
            buffers.submit(collector);
        }
        super.submit(state, poseStack, collector, camera);
    }

    /** 1.21.1's {@code render(entity, yaw, partialTicks, poseStack, buffers, light)} */
    protected void renderLegacy(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light) {
    }
}
