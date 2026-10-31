package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelPixie;
import com.github.alexthe666.iceandfire.client.render.entity.RenderPixie;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class LayerPixieGlow extends IafEntityLayer<EntityPixie, IafMobRenderState, ModelPixie> {

    private final RenderPixie render;

    public LayerPixieGlow(RenderPixie renderIn) {
        super(renderIn);
        this.render = renderIn;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityPixie pixie) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        Identifier texture = RenderPixie.TEXTURE_0;
        switch (pixie.getColor()) {
            default:
                texture = RenderPixie.TEXTURE_0;
                break;
            case 1:
                texture = RenderPixie.TEXTURE_1;
                break;
            case 2:
                texture = RenderPixie.TEXTURE_2;
                break;
            case 3:
                texture = RenderPixie.TEXTURE_3;
                break;
            case 4:
                texture = RenderPixie.TEXTURE_4;
                break;
            case 5:
                texture = RenderPixie.TEXTURE_5;
                break;
        }
        RenderType eyes = RenderTypes.eyes(texture);
        submitParent(matrixStackIn, iafCollector, iafState, eyes, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
    }
}