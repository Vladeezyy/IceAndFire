package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelDreadLichSkull;
import com.github.alexthe666.iceandfire.entity.EntityDragonLightningCharge;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RenderDragonLightningCharge extends IafEntityRenderer<EntityDragonLightningCharge> {

    public static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/lightningdragon/charge.png");
    public static final Identifier TEXTURE_CORE = Identifier.parse("iceandfire:textures/models/lightningdragon/charge_core.png");
    private static final ModelDreadLichSkull MODEL_SPIRIT = new ModelDreadLichSkull();

    public RenderDragonLightningCharge(EntityRendererProvider.Context context) {
        super(context);
    }


    @Override
    protected void renderLegacy(EntityDragonLightningCharge entity, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        float f = (float) entity.tickCount + partialTicks;
        float yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
        VertexConsumer ivertexbuilder2 = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_CORE));
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.energySwirl(TEXTURE, f * 0.01F, f * 0.01F));

        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.5F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(f * 20.0F));
        matrixStackIn.translate(0F, 0.25F, 0F);
        MODEL_SPIRIT.renderToBuffer(matrixStackIn, ivertexbuilder2, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.5F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(f * 15.0F));
        matrixStackIn.translate(0F, 0.25F, 0F);
        matrixStackIn.scale(1.5F, 1.5F, 1.5F);
        MODEL_SPIRIT.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();

        matrixStackIn.pushPose();
        matrixStackIn.translate(0F, 0.75F, 0F);
        matrixStackIn.translate(0F, -0.25F, 0F);
        matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
        matrixStackIn.mulPose(Axis.XP.rotationDegrees(f * 10.0F));
        matrixStackIn.translate(0F, 0.75F, 0F);
        matrixStackIn.scale(2.5F, 2.5F, 2.5F);
        MODEL_SPIRIT.renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();

            }

    private float interpolateValue(float start, float end, float pct) {
        return start + (end - start) * pct;
    }

    @Nullable
    public Identifier getTextureLocation(@NotNull EntityDragonLightningCharge entity) {
        return TEXTURE;
    }
}
