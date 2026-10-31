package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class LayerGenericGlowing<T extends LivingEntity, M extends EntityModel<T>> extends IafEntityLayer<T, IafMobRenderState, M> {
    private final LivingEntityRenderer render;
    private final Identifier texture;

    public LayerGenericGlowing(LivingEntityRenderer renderIn, Identifier texture) {
        super(renderIn);
        this.render = renderIn;
        this.texture = texture;
    }

    public boolean shouldCombineTextures() {
        return true;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, T entity) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        RenderType eyes = RenderTypes.eyes(texture);
        submitParent(matrixStackIn, iafCollector, iafState, eyes, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
    }
}