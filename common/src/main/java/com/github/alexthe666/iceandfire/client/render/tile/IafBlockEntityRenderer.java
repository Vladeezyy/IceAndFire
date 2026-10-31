package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A 1.21.1-style block entity renderer on 26.x: the state keeps the block entity, and {@link #renderLegacy} (1.21.1's
 * render) draws into an {@link IafBufferSource} that is submitted with the frame.
 */
public abstract class IafBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, IafBlockEntityRenderState> {
    @Override
    public IafBlockEntityRenderState createRenderState() {
        return new IafBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, IafBlockEntityRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.blockEntity = blockEntity;
        state.partialTick = partialTicks;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(IafBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.blockEntity != null) {
            IafBufferSource buffers = new IafBufferSource();
            this.renderLegacy((T) state.blockEntity, state.partialTick, poseStack, buffers, state.lightCoords, OverlayTexture.NO_OVERLAY);
            buffers.submit(collector);
        }
    }

    /** 1.21.1's {@code render(blockEntity, partialTicks, poseStack, buffers, light, overlay)} */
    protected abstract void renderLegacy(T blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay);
}
