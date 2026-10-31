package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;

import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

public class LayerSeaSerpentAncient extends IafEntityLayer<EntitySeaSerpent, IafMobRenderState, AdvancedEntityModel<EntitySeaSerpent>> {

    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/seaserpent/ancient_overlay.png");
    private static final Identifier TEXTURE_BLINK = Identifier.parse("iceandfire:textures/models/seaserpent/ancient_overlay_blink.png");

    public LayerSeaSerpentAncient(MobRenderer<EntitySeaSerpent, AdvancedEntityModel<EntitySeaSerpent>> renderer) {
        super(renderer);
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntitySeaSerpent serpent) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        if (serpent.isAncient()) {
            RenderType tex = RenderType.entityNoOutline(serpent.isBlinking() ? TEXTURE_BLINK : TEXTURE);
            submitParent(matrixStackIn, iafCollector, iafState, tex, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);

        }
    }
}
