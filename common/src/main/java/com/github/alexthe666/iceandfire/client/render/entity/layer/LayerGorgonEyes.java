package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelGorgon;
import com.github.alexthe666.iceandfire.client.render.entity.RenderGorgon;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class LayerGorgonEyes extends IafEntityLayer<EntityGorgon, IafMobRenderState, ModelGorgon> {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/gorgon/gorgon_eyes.png");
    private final RenderGorgon render;

    public LayerGorgonEyes(RenderGorgon renderIn) {
        super(renderIn);
        this.render = renderIn;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityGorgon entity) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        if (entity.getAnimation() == EntityGorgon.ANIMATION_SCARE || entity.getAnimation() == EntityGorgon.ANIMATION_HIT) {
            RenderType eyes = RenderTypes.eyes(TEXTURE);
            submitParent(matrixStackIn, iafCollector, iafState, eyes, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }
}