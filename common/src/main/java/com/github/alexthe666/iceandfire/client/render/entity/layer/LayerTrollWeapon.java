package com.github.alexthe666.iceandfire.client.render.entity.layer;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelTroll;
import com.github.alexthe666.iceandfire.client.render.entity.RenderTroll;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.github.alexthe666.iceandfire.entity.EntityTroll;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.jetbrains.annotations.NotNull;

public class LayerTrollWeapon extends IafEntityLayer<EntityTroll, IafMobRenderState, ModelTroll> {
    private final RenderTroll renderer;

    public LayerTrollWeapon(RenderTroll renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    public void render(EntityTroll entity, float f, float f1, float i, float f2, float f3, float f4, float f5) {

    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityTroll troll) {
        IafBufferSource bufferIn = new IafBufferSource();
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        if (troll.getWeaponType() != null && !EntityGorgon.isStoneMob(troll)) {
            RenderType tex = RenderTypes.entityCutoutNoCull(troll.getWeaponType().TEXTURE);
            com.github.alexthe666.iceandfire.client.render.IafModels.render(this.getParentModel(), matrixStackIn, bufferIn.getBuffer(tex), packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        }
        bufferIn.submit(iafCollector);
    }
}