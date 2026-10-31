package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelDreadLichSkull;
import com.github.alexthe666.iceandfire.entity.EntityDreadLichSkull;
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

public class RenderDreadLichSkull extends IafEntityRenderer<EntityDreadLichSkull> {

    public static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/dread/dread_lich_skull.png");
    private static final ModelDreadLichSkull MODEL_SPIRIT = new ModelDreadLichSkull();

    public RenderDreadLichSkull(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderLegacy(EntityDreadLichSkull entity, float entityYaw, float partialTicks, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        float f = 0.0625F;
        if (entity.tickCount > 3) {
            matrixStackIn.pushPose();
            matrixStackIn.scale(1.5F, -1.5F, 1.5F);
            float yaw = entity.yRotO + (entity.getYRot() - entity.yRotO) * partialTicks;
            matrixStackIn.translate(0F, 0F, 0F);
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(yaw - 180.0F));
            VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderTypes.eyes(TEXTURE), false, false);
            MODEL_SPIRIT.renderToBuffer(matrixStackIn, ivertexbuilder, 240, OverlayTexture.NO_OVERLAY, -1);
            matrixStackIn.popPose();
        }

            }

    private float interpolateValue(float start, float end, float pct) {
        return start + (end - start) * pct;
    }

    @Nullable
    public Identifier getTextureLocation(@NotNull EntityDreadLichSkull entity) {
        return TEXTURE;
    }
}
