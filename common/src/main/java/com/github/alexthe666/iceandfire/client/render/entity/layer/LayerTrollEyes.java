package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelTroll;
import com.github.alexthe666.iceandfire.client.render.entity.RenderTroll;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;

public class LayerTrollEyes extends IafEntityLayer<EntityTroll, IafMobRenderState, ModelTroll> {

    private final RenderTroll renderer;

    public LayerTrollEyes(RenderTroll renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityTroll troll) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        if (!EntityGorgon.isStoneMob(troll)) {
            RenderType tex = RenderTypes.eyes(troll.getTrollType().TEXTURE_EYES);
            submitParent(matrixStackIn, iafCollector, iafState, tex, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);

        }
    }
}
