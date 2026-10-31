package com.github.alexthe666.iceandfire.client.render.entity.layer;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.github.alexthe666.iceandfire.item.IafArmorItem;

import net.minecraft.util.ARGB;
import dev.vladeezyy.rampart.animation.IAnimatedEntity;
import com.github.alexthe666.iceandfire.client.model.ModelBipedBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

//TODO: Consider support for default minecraft armors/ dynamically selecting custom armors

//Base code from minecrafts ArmorBipedLayer

public class LayerBipedArmor<T extends LivingEntity & IAnimatedEntity,
    M extends ModelBipedBase<T>,
    A extends ModelBipedBase<T>> extends IafEntityLayer<T, IafMobRenderState, M> {

    private final A modelLeggings;
    private final A modelArmor;
    private final Identifier defaultLegArmor;
    private final Identifier defaultArmor;

    public LayerBipedArmor(RenderLayerParent<IafMobRenderState, M> mobRenderer, A modelLeggings, A modelArmor, Identifier defaultArmor, Identifier defaultLegArmor) {
        super(mobRenderer);
        this.modelLeggings = modelLeggings;
        this.modelArmor = modelArmor;
        this.defaultLegArmor = defaultLegArmor;
        this.defaultArmor = defaultArmor;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, T entitylivingbaseIn) {
        IafBufferSource bufferIn = new IafBufferSource();
        // the parent posed for this entity (26.x poses models when they are drawn): the armor copies its pose
        this.poseParent(iafState);
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        this.renderEquipment(matrixStackIn, bufferIn, entitylivingbaseIn, EquipmentSlot.CHEST, packedLightIn, this.getSlotModel(EquipmentSlot.CHEST));
        this.renderEquipment(matrixStackIn, bufferIn, entitylivingbaseIn, EquipmentSlot.LEGS, packedLightIn, this.getSlotModel(EquipmentSlot.LEGS));
        this.renderEquipment(matrixStackIn, bufferIn, entitylivingbaseIn, EquipmentSlot.FEET, packedLightIn, this.getSlotModel(EquipmentSlot.FEET));
        this.renderEquipment(matrixStackIn, bufferIn, entitylivingbaseIn, EquipmentSlot.HEAD, packedLightIn, this.getSlotModel(EquipmentSlot.HEAD));
        bufferIn.submit(iafCollector);
    }

    private void renderEquipment(PoseStack matrixStackIn, MultiBufferSource bufferIn, T entityIn, EquipmentSlot slotType, int packedLightIn, A modelIn) {
        ItemStack itemstack = entityIn.getItemBySlot(slotType);
        // any armor worn in the slot (1.21.1: an ArmorItem), drawn with the mob's own armor texture
        net.minecraft.world.item.equipment.Equippable equippable = itemstack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null && equippable.assetId().isPresent()) {
            if (equippable.slot() == slotType) {
                this.getParentModel().setModelAttributes(modelIn);
                this.setModelSlotVisible(modelIn, slotType);
                boolean flag1 = itemstack.hasFoil();
                this.renderArmorItem(matrixStackIn, bufferIn, packedLightIn, flag1, modelIn, 1.0F, 1.0F, 1.0F, this.getArmorResource(entityIn, itemstack, slotType, null));
            }
        }
    }

    protected void setModelSlotVisible(A modelIn, EquipmentSlot slotIn) {
        modelIn.setVisible(false);
        switch (slotIn) {
            case HEAD:
                modelIn.head.invisible = false;
                modelIn.headware.invisible = false;
                break;
            case CHEST:
                modelIn.body.invisible = false;
                modelIn.armRight.invisible = false;
                modelIn.armLeft.invisible = false;
                break;
            case LEGS:
                modelIn.body.invisible = false;
                modelIn.legRight.invisible = false;
                modelIn.legLeft.invisible = false;
                break;
            case FEET:
                modelIn.legRight.invisible = false;
                modelIn.legLeft.invisible = false;
        }
    }

    private void renderArmorItem(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean p_241738_5_, A modelIn, float red, float green, float blue, Identifier armorResource) {
        // 26.x: the glint is a render type of its own (1.21.1: ItemRenderer.getArmorFoilBuffer)
        com.github.alexthe666.iceandfire.client.render.IafModels.render(modelIn, matrixStackIn, bufferIn.getBuffer(RenderTypes.armorCutoutNoCull(armorResource)), packedLightIn, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(1.0F, red, green, blue));
        if (p_241738_5_) {
            com.github.alexthe666.iceandfire.client.render.IafModels.render(modelIn, matrixStackIn, bufferIn.getBuffer(RenderTypes.armorEntityGlint()), packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        }
    }

    private A getSlotModel(EquipmentSlot equipmentSlotType) {
        return this.isLegSlot(equipmentSlotType) ? this.modelLeggings : this.modelArmor;
    }

    protected boolean isLegSlot(EquipmentSlot slotIn) {
        return slotIn == EquipmentSlot.LEGS;
    }

    public Identifier getArmorResource(T entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        if (isLegSlot(slot))
            return defaultLegArmor;
        return defaultArmor;
    }

}
