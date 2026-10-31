package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.entity.tile.TileEntityDreadSpawner;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SpawnerRenderer;
import net.minecraft.client.renderer.blockentity.state.SpawnerRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** The spinning mob in the dread spawner: vanilla's spawner renderer (26.x render states) on the dread spawner's BaseSpawner */
public class RenderDreadSpawner<T extends TileEntityDreadSpawner> implements BlockEntityRenderer<T, SpawnerRenderState> {
    private final EntityRenderDispatcher entityRenderer;

    public RenderDreadSpawner(BlockEntityRendererProvider.Context context) {
        this.entityRenderer = context.entityRenderer();
    }

    @Override
    public SpawnerRenderState createRenderState() {
        return new SpawnerRenderState();
    }

    @Override
    public void extractRenderState(T blockEntity, SpawnerRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        if (blockEntity.getLevel() != null) {
            BaseSpawner spawner = blockEntity.getSpawner();
            Entity displayEntity = spawner.getOrCreateDisplayEntity(blockEntity.getLevel(), blockEntity.getBlockPos());
            // TrialSpawnerRenderer.extractSpawnerData (package-private)
            if (displayEntity != null) {
                state.displayEntity = this.entityRenderer.extractEntity(displayEntity, partialTicks);
                state.displayEntity.lightCoords = state.lightCoords;
                state.spin = (float) Mth.lerp(partialTicks, spawner.getOSpin(), spawner.getSpin()) * 10.0F;
                state.scale = 0.53125F;
                float maxLength = Math.max(displayEntity.getBbWidth(), displayEntity.getBbHeight());
                if (maxLength > 1.0F) {
                    state.scale /= maxLength;
                }
            }
        }
    }

    @Override
    public void submit(SpawnerRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.displayEntity != null) {
            SpawnerRenderer.submitEntityInSpawner(poseStack, collector, state.displayEntity, this.entityRenderer, state.spin, state.scale, camera);
        }
    }
}
