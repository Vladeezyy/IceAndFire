package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.layer.IafEntityLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelHippogryph;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class RenderHippogryph extends IafMobRenderer<EntityHippogryph, ModelHippogryph> {

    public RenderHippogryph(EntityRendererProvider.Context context) {
        super(context, new ModelHippogryph(), 0.8F);
        this.addLayer(new LayerHippogriffSaddle(this));

    }

    @Override
    protected void scale(IafMobRenderState iafState, PoseStack matrix) {
        EntityHippogryph entity = (EntityHippogryph) iafState.pose.entity;
        float partialTickTime = iafState.partialTick;
        matrix.scale(1.2F, 1.2F, 1.2F);
    }

    @Nullable
    @Override
    protected Identifier getTexture(EntityHippogryph entity) {
        return entity.isBlinking() ? entity.getEnumVariant().TEXTURE_BLINK : entity.getEnumVariant().TEXTURE;
    }


    private class LayerHippogriffSaddle extends IafEntityLayer<EntityHippogryph, IafMobRenderState, ModelHippogryph> {
        private final RenderHippogryph renderer;
        private final RenderType SADDLE_TEXTURE = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/saddle.png"), false);
        private final RenderType BRIDLE = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/bridle.png"), false);
        private final RenderType CHEST = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/chest.png"));
        private final RenderType TEXTURE_DIAMOND = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/armor_diamond.png"), false);
        private final RenderType TEXTURE_GOLD = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/armor_gold.png"), false);
        private final RenderType TEXTURE_IRON = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippogryph/armor_iron.png"), false);


        public LayerHippogriffSaddle(RenderHippogryph renderer) {
            super(renderer);
            this.renderer = renderer;
        }

        @Override
        protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityHippogryph hippo) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
            if (hippo.getArmor() != 0) {
                RenderType type = null;
                switch (hippo.getArmor()) {
                    case 1:
                        type = TEXTURE_IRON;
                        break;
                    case 2:
                        type = TEXTURE_GOLD;
                        break;
                    case 3:
                        type = TEXTURE_DIAMOND;
                        break;
                }
                submitParent(matrixStackIn, iafCollector, iafState, type, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
            if (hippo.isSaddled()) {
                submitParent(matrixStackIn, iafCollector, iafState, SADDLE_TEXTURE, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
            if (hippo.isSaddled() && hippo.getControllingPassenger() != null) {
                submitParent(matrixStackIn, iafCollector, iafState, BRIDLE, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
            if (hippo.isChested()) {
                submitParent(matrixStackIn, iafCollector, iafState, CHEST, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
        }
    }
}
