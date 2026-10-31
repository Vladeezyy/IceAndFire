package com.github.alexthe666.iceandfire.client.render.entity.layer;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;

import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexBase;
import com.github.alexthe666.iceandfire.client.render.entity.RenderMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LayerMyrmexItem extends IafEntityLayer<EntityMyrmexBase, IafMobRenderState, AdvancedEntityModel<EntityMyrmexBase>> {

    protected final RenderMyrmexBase livingEntityRenderer;

    public LayerMyrmexItem(RenderMyrmexBase livingEntityRendererIn) {
        super(livingEntityRendererIn);
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    private void renderHeldItem(EntityMyrmexBase myrmex, ItemStack stack, ItemDisplayContext transform, HumanoidArm handSide) {

    }

    protected void translateToHand(HumanoidArm side, PoseStack stack) {
        ((ModelMyrmexBase) this.livingEntityRenderer.getModel()).postRenderArm(0, stack);
    }

    public boolean shouldCombineTextures() {
        return false;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityMyrmexBase entitylivingbaseIn) {
        IafBufferSource bufferIn = new IafBufferSource();
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        if (entitylivingbaseIn instanceof EntityMyrmexWorker) {
            ItemStack itemstack = entitylivingbaseIn.getItemInHand(InteractionHand.MAIN_HAND);
            if (!itemstack.isEmpty()) {
                matrixStackIn.pushPose();
                if (!itemstack.isEmpty()) {
                    matrixStackIn.pushPose();

                    if (entitylivingbaseIn.isShiftKeyDown()) {
                        matrixStackIn.translate(0.0F, 0.2F, 0.0F);
                    }
                    this.translateToHand(HumanoidArm.RIGHT, matrixStackIn);
                    matrixStackIn.translate(0F, 0.3F, -1.6F);
                    if (itemstack.getItem() instanceof BlockItem) {
                        matrixStackIn.translate(0F, 0, 0.2F);
                    } else {
                        matrixStackIn.translate(0F, 0.2F, 0.3F);
                    }
                    matrixStackIn.mulPose(Axis.XP.rotationDegrees(160.0F));
                    matrixStackIn.mulPose(Axis.YP.rotationDegrees(180.0F));
                    Minecraft.getInstance().getItemRenderer().renderStatic(itemstack, ItemDisplayContext.FIXED, packedLightIn, OverlayTexture.NO_OVERLAY, matrixStackIn, bufferIn, Minecraft.getInstance().level, 0);
                    matrixStackIn.popPose();
                }
                matrixStackIn.popPose();
            }
        }
        bufferIn.submit(iafCollector);
    }
}