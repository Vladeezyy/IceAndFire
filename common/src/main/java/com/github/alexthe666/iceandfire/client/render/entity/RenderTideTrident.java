package com.github.alexthe666.iceandfire.client.render.entity;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelTideTrident;
import com.github.alexthe666.iceandfire.entity.EntityTideTrident;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RenderTideTrident extends IafEntityRenderer<EntityTideTrident> {
    public static final Identifier TRIDENT = Identifier.parse("iceandfire:textures/models/misc/tide_trident.png");
    private final ModelTideTrident tridentModel = new ModelTideTrident();

    public RenderTideTrident(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderLegacy(EntityTideTrident entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entityIn.yRotO, entityIn.getYRot()) - 90.0F));
        matrixStackIn.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entityIn.xRotO, entityIn.getXRot()) + 90.0F));
        VertexConsumer ivertexbuilder = net.minecraft.client.renderer.entity.ItemRenderer.getFoilBuffer(bufferIn, this.tridentModel.renderType(this.getTextureLocation(entityIn)), false, entityIn.isFoil());
        this.tridentModel.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
            }

    /**
     * Returns the location of an entity's texture.
     */
    public @NotNull Identifier getTextureLocation(@NotNull EntityTideTrident entity) {
        return TRIDENT;
    }

}