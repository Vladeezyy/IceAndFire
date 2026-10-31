package com.github.alexthe666.iceandfire.client.render.entity.layer;

import com.github.alexthe666.iceandfire.client.model.IafArmedModel;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import com.github.alexthe666.iceandfire.client.render.IafLegacyRender;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 1.21.1's vanilla ItemInHandLayer for the mod's Citadel-style humanoids (26.x's needs an armed render state): the
 * held items at the model's hands, with the same transforms. {@link #hidden} skips it (the dread mobs while they rise).
 */
public class IafItemInHandLayer<T extends LivingEntity, M extends EntityModel<? super IafMobRenderState> & IafArmedModel> extends IafEntityLayer<T, IafMobRenderState, M> {
    public boolean hidden;

    public IafItemInHandLayer(RenderLayerParent<IafMobRenderState, M> parent) {
        super(parent);
    }

    @Override
    protected void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, IafMobRenderState state, T entity) {
        if (this.hidden) {
            return;
        }
        boolean rightHanded = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack left = rightHanded ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack right = rightHanded ? entity.getMainHandItem() : entity.getOffhandItem();
        if (left.isEmpty() && right.isEmpty()) {
            return;
        }
        // the model posed for this entity before the hands are read (26.x poses it when it is drawn)
        this.poseParent(state);
        IafBufferSource buffers = new IafBufferSource();
        poseStack.pushPose();
        this.renderArmWithItem(entity, right, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, buffers, light);
        this.renderArmWithItem(entity, left, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, buffers, light);
        poseStack.popPose();
        buffers.submit(collector);
    }

    private void renderArmWithItem(T entity, ItemStack stack, ItemDisplayContext display, HumanoidArm arm, PoseStack poseStack, IafBufferSource buffers, int light) {
        if (stack.isEmpty()) {
            return;
        }
        poseStack.pushPose();
        this.getParentModel().translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean left = arm == HumanoidArm.LEFT;
        poseStack.translate((left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        IafLegacyRender.renderStatic(entity, stack, display, left, poseStack, buffers, entity.level(), light, OverlayTexture.NO_OVERLAY, entity.getId() + display.ordinal());
        poseStack.popPose();
    }
}
