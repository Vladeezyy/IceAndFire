package com.github.alexthe666.iceandfire.client.render.entity;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.layer.IafEntityLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelHippocampus;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;


public class RenderHippocampus extends IafMobRenderer<EntityHippocampus, ModelHippocampus> {

    private static final Identifier VARIANT_0 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_0.png");
    private static final Identifier VARIANT_0_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_0_blinking.png");
    private static final Identifier VARIANT_1 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_1.png");
    private static final Identifier VARIANT_1_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_1_blinking.png");
    private static final Identifier VARIANT_2 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_2.png");
    private static final Identifier VARIANT_2_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_2_blinking.png");
    private static final Identifier VARIANT_3 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_3.png");
    private static final Identifier VARIANT_3_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_3_blinking.png");
    private static final Identifier VARIANT_4 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_4.png");
    private static final Identifier VARIANT_4_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_4_blinking.png");
    private static final Identifier VARIANT_5 = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_5.png");
    private static final Identifier VARIANT_5_BLINK = Identifier.parse("iceandfire:textures/models/hippocampus/hippocampus_5_blinking.png");


    public RenderHippocampus(EntityRendererProvider.Context context) {
        super(context, new ModelHippocampus(), 0.8F);
        this.addLayer(new RenderHippocampus.LayerHippocampusRainbow(this));
        this.addLayer(new RenderHippocampus.LayerHippocampusSaddle(this));
    }

    @Nullable
    @Override
    protected Identifier getTexture(EntityHippocampus entity) {
        switch (entity.getVariant()) {
            default:
                return entity.isBlinking() ? VARIANT_0_BLINK : VARIANT_0;
            case 1:
                return entity.isBlinking() ? VARIANT_1_BLINK : VARIANT_1;
            case 2:
                return entity.isBlinking() ? VARIANT_2_BLINK : VARIANT_2;
            case 3:
                return entity.isBlinking() ? VARIANT_3_BLINK : VARIANT_3;
            case 4:
                return entity.isBlinking() ? VARIANT_4_BLINK : VARIANT_4;
            case 5:
                return entity.isBlinking() ? VARIANT_5_BLINK : VARIANT_5;

        }
    }


    private class LayerHippocampusSaddle extends IafEntityLayer<EntityHippocampus, IafMobRenderState, ModelHippocampus> {
        private final RenderHippocampus renderer;
        private final RenderType SADDLE_TEXTURE = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippocampus/saddle.png"), false);
        private final RenderType BRIDLE = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippocampus/bridle.png"), false);
        private final RenderType CHEST = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippocampus/chest.png"));
        private final RenderType TEXTURE_DIAMOND = RenderTypes.entityCutoutCull(Identifier.parse("iceandfire:textures/models/hippocampus/armor_diamond.png"));
        private final RenderType TEXTURE_GOLD = RenderTypes.entityCutoutCull(Identifier.parse("iceandfire:textures/models/hippocampus/armor_gold.png"));
        private final RenderType TEXTURE_IRON = RenderTypes.entityCutoutCull(Identifier.parse("iceandfire:textures/models/hippocampus/armor_iron.png"));

        public LayerHippocampusSaddle(RenderHippocampus renderer) {
            super(renderer);
            this.renderer = renderer;
        }

        @Override
        protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityHippocampus hippo) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
            if (hippo.isSaddled()) {
                submitParent(matrixStackIn, iafCollector, iafState, SADDLE_TEXTURE, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
            if (hippo.isSaddled() && hippo.getControllingPassenger() != null) {
                submitParent(matrixStackIn, iafCollector, iafState, BRIDLE, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
            if (hippo.isChested()) {
                submitParent(matrixStackIn, iafCollector, iafState, CHEST, packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
            }
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
        }
    }

    private class LayerHippocampusRainbow extends RenderLayer<EntityHippocampus, ModelHippocampus> {
        private final RenderHippocampus renderer;
        private final RenderType TEXTURE = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippocampus/rainbow.png"), false);
        private final RenderType TEXTURE_BLINK = RenderTypes.entityTranslucent(Identifier.parse("iceandfire:textures/models/hippocampus/rainbow_blink.png"), false);

        public LayerHippocampusRainbow(RenderHippocampus renderer) {
            super(renderer);
            this.renderer = renderer;
        }

        @Override
        protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityHippocampus hippo) {
        IafBufferSource bufferIn = new IafBufferSource();
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
            if (hippo.hasCustomName() && hippo.getCustomName().toString().toLowerCase().contains("rainbow")) {
                VertexConsumer ivertexbuilder = bufferIn.getBuffer(hippo.isBlinking() ? TEXTURE_BLINK : TEXTURE);
                int i1 = 25;
                int i = hippo.tickCount / 25 + hippo.getId();
                int j = DyeColor.values().length;
                int k = i % j;
                int l = (i + 1) % j;
                float f = ((float) (hippo.tickCount % 25) + partialTicks) / 25.0F;
                int color = ARGB.lerp(f, Sheep.getColor(DyeColor.byId(k)), Sheep.getColor(DyeColor.byId(l)));
                this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, LivingEntityRenderer.getOverlayCoords(hippo, 0.0F), color);
            }
        bufferIn.submit(iafCollector);
    }
    }
}
