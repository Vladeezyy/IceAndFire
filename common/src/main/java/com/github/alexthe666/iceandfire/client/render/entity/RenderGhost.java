package com.github.alexthe666.iceandfire.client.render.entity;

import com.github.alexthe666.iceandfire.client.model.ModelGhost;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import com.github.alexthe666.iceandfire.client.render.IafRenderType;
import com.github.alexthe666.iceandfire.entity.EntityGhost;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * The ghost: its own blending (or vanilla's translucent entity by day), a pulsing alpha, full brightness, no shadow;
 * the haunted shopping list is a flat quad. 1.21.1 copied LivingEntityRenderer.render for this, 26.x has the hooks.
 */
public class RenderGhost extends IafMobRenderer<EntityGhost, ModelGhost> {
    public static final Identifier TEXTURE_0 = Identifier.parse("iceandfire:textures/models/ghost/ghost_white.png");
    public static final Identifier TEXTURE_1 = Identifier.parse("iceandfire:textures/models/ghost/ghost_blue.png");
    public static final Identifier TEXTURE_2 = Identifier.parse("iceandfire:textures/models/ghost/ghost_green.png");
    public static final Identifier TEXTURE_SHOPPING_LIST = Identifier.parse("iceandfire:textures/models/ghost/haunted_shopping_list.png");

    public RenderGhost(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ModelGhost(0.0F), 0.0F);
    }

    public static Identifier getGhostOverlayForType(int ghost) {
        switch (ghost) {
            case 1:
                return TEXTURE_1;
            case 2:
                return TEXTURE_2;
            case -1:
                return TEXTURE_SHOPPING_LIST;
            default:
                return TEXTURE_0;
        }
    }

    @Override
    protected @Nullable RenderType getRenderType(IafMobRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        EntityGhost ghost = (EntityGhost) state.pose.entity;
        if (ghost == null || ghost.isInvisible()) {
            return null;
        }
        return ghost.isDaytimeMode() ? IafRenderType.getGhostDaytime(state.texture) : IafRenderType.getGhost(state.texture);
    }

    @Override
    protected int getModelTint(IafMobRenderState state) {
        EntityGhost ghost = (EntityGhost) state.pose.entity;
        return ghost == null ? -1 : ARGB.white(this.getAlphaForRender(ghost, state.partialTick));
    }

    @Override
    public void submit(IafMobRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        // the ghost is fully lit (1.21.1 drew it with light 240)
        state.lightCoords = 240;
        EntityGhost ghost = (EntityGhost) state.pose.entity;
        if (ghost == null || !ghost.isHauntedShoppingList()) {
            super.submit(state, poseStack, collector, camera);
            return;
        }
        RenderType type = this.getRenderType(state, true, false, false);
        if (type == null) {
            return;
        }
        // the same transforms LivingEntityRenderer applies before the model, then the list's two faces
        poseStack.pushPose();
        this.setupRotations(state, poseStack, state.bodyRot, state.scale);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        this.scale(state, poseStack);
        poseStack.translate(0.0D, -1.501F, 0.0D);
        IafBufferSource buffers = new IafBufferSource();
        VertexConsumer builder = buffers.getBuffer(type);
        int overlay = LivingEntityRenderer.getOverlayCoords(state, this.getWhiteOverlayProgress(state));
        int alpha = (int) (this.getAlphaForRender(ghost, state.partialTick) * 255);
        poseStack.pushPose();
        poseStack.translate(0, 0.8F + Mth.sin((ghost.tickCount + state.partialTick) * 0.15F) * 0.1F, 0);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.drawFace(poseStack, builder, overlay, alpha, 1F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        this.drawFace(poseStack, builder, overlay, alpha, 0.0F, 0.5F);
        poseStack.popPose();
        poseStack.popPose();
        buffers.submit(collector);
    }

    private void drawFace(PoseStack poseStack, VertexConsumer builder, int overlay, int alpha, float u0, float u1) {
        PoseStack.Pose entry = poseStack.last();
        Matrix4f matrix4f = entry.pose();
        this.drawVertex(matrix4f, entry, builder, overlay, alpha, -1, -2, 0, u0, 0.0F, 0, 1, 0, 240);
        this.drawVertex(matrix4f, entry, builder, overlay, alpha, 1, -2, 0, u1, 0.0F, 0, 1, 0, 240);
        this.drawVertex(matrix4f, entry, builder, overlay, alpha, 1, 2, 0, u1, 1, 0, 1, 0, 240);
        this.drawVertex(matrix4f, entry, builder, overlay, alpha, -1, 2, 0, u0, 1, 0, 1, 0, 240);
    }

    @Override
    protected float getFlipDegrees() {
        return 0.0F;
    }

    public float getAlphaForRender(EntityGhost entityIn, float partialTicks) {
        if (entityIn.isDaytimeMode()) {
            return Mth.clamp((101 - Math.min(entityIn.getDaytimeCounter(), 100)) / 100F, 0, 1);
        }
        return Mth.clamp((Mth.sin((entityIn.tickCount + partialTicks) * 0.1F) + 1F) * 0.5F + 0.1F, 0F, 1F);
    }

    @Override
    protected Identifier getTexture(EntityGhost ghost) {
        return getGhostOverlayForType(ghost.getColor());
    }

    public void drawVertex(Matrix4f stack, PoseStack.Pose normal, VertexConsumer builder, int packedRed, int alphaInt, int x, int y, int z, float u, float v, int lightmap, int lightmap3, int lightmap2, int lightmap4) {
        builder.addVertex(stack, (float) x, (float) y, (float) z).setColor(255, 255, 255, alphaInt).setUv(u, v).setOverlay(packedRed).setLight(lightmap4).setNormal(normal, (float) lightmap, (float) lightmap2, (float) lightmap3);
    }
}
