package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.util.ARGB;
import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class RenderNothing<T extends Entity> extends IafEntityRenderer<T> {

    public RenderNothing(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderLegacy(@NotNull T entityIn, float entityYaw, float partialTicks, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
            }

    // Only render if the debug bboxes are enabled
    @Override
    public boolean shouldRender(@NotNull T livingEntityIn, @NotNull Frustum camera, double camX, double camY, double camZ) {
        if (!net.minecraft.client.Minecraft.getInstance().debugEntries.isCurrentlyEnabled(net.minecraft.client.gui.components.debug.DebugScreenEntries.ENTITY_HITBOXES))
            return false;
        return super.shouldRender(livingEntityIn, camera, camX, camY, camZ);
    }

    public @NotNull Identifier getTextureLocation(@NotNull Entity entity) {
        return null;
    }
}
