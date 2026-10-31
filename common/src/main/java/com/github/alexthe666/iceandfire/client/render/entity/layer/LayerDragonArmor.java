package com.github.alexthe666.iceandfire.client.render.entity.layer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.client.texture.ArrayLayeredTexture;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.enums.EnumDragonTextures;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LayerDragonArmor extends IafEntityLayer<EntityDragonBase, IafMobRenderState, AdvancedEntityModel<EntityDragonBase>> {
    private static final Map<String, Identifier> LAYERED_ARMOR_CACHE = Maps.newHashMap();
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final MobRenderer render;

    public LayerDragonArmor(MobRenderer renderIn, int type) {
        super(renderIn);
        this.render = renderIn;
    }

    public static void clearCache(String str) {
        LAYERED_ARMOR_CACHE.remove(str);
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityDragonBase dragon) {
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        int armorHead = dragon.getArmorOrdinal(dragon.getItemBySlot(EquipmentSlot.HEAD));
        int armorNeck = dragon.getArmorOrdinal(dragon.getItemBySlot(EquipmentSlot.CHEST));
        int armorLegs = dragon.getArmorOrdinal(dragon.getItemBySlot(EquipmentSlot.LEGS));
        int armorFeet = dragon.getArmorOrdinal(dragon.getItemBySlot(EquipmentSlot.FEET));
        String armorTexture = dragon.dragonType.getName() + "_" + armorHead + "_" + armorNeck + "_" + armorLegs + "_" + armorFeet;
        if (!armorTexture.equals(dragon.dragonType.getName() + "_0_0_0_0")) {
            Identifier resourcelocation = LAYERED_ARMOR_CACHE.get(armorTexture);
            if (resourcelocation == null) {
                resourcelocation = Identifier.parse("iceandfire" + "dragon_armor_" + armorTexture);
                List<String> tex = new ArrayList<String>();
                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    if (dragon.dragonType == DragonType.FIRE) {
                        tex.add(EnumDragonTextures.Armor.getArmorForDragon(dragon, slot).FIRETEXTURE.toString());
                    } else if (dragon.dragonType == DragonType.ICE) {
                        tex.add(EnumDragonTextures.Armor.getArmorForDragon(dragon, slot).ICETEXTURE.toString());
                    } else {
                        tex.add(EnumDragonTextures.Armor.getArmorForDragon(dragon, slot).LIGHTNINGTEXTURE.toString());
                    }
                }
                ArrayLayeredTexture layeredBase = new ArrayLayeredTexture(resourcelocation, tex);
                Minecraft.getInstance().getTextureManager().registerAndLoad(resourcelocation, layeredBase);
                LAYERED_ARMOR_CACHE.put(armorTexture, resourcelocation);
            }
            submitParent(matrixStackIn, iafCollector, iafState, RenderTypes.entityCutout(resourcelocation), packedLightIn, OverlayTexture.NO_OVERLAY, -1, 1);
        }
    }
}