package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.util.ARGB;
import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.client.model.ModelMyrmexPupa;
import com.github.alexthe666.iceandfire.client.render.entity.layer.LayerMyrmexItem;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;


public class RenderMyrmexBase extends IafMobRenderer<EntityMyrmexBase, AdvancedEntityModel<EntityMyrmexBase>> {

    private static final AdvancedEntityModel<EntityMyrmexBase> LARVA_MODEL = new ModelMyrmexPupa();
    private static final AdvancedEntityModel<EntityMyrmexBase> PUPA_MODEL = new ModelMyrmexPupa();
    private final AdvancedEntityModel<EntityMyrmexBase> adultModel;

    public RenderMyrmexBase(EntityRendererProvider.Context context, AdvancedEntityModel<EntityMyrmexBase> model, float shadowSize) {
        super(context, model, shadowSize);
        this.addLayer(new LayerMyrmexItem(this));
        this.adultModel = model;
    }

    @Override
    public void submit(IafMobRenderState iafState, PoseStack matrixStackIn, SubmitNodeCollector iafCollector, CameraRenderState iafCamera) {
        EntityMyrmexBase entityIn = (EntityMyrmexBase) iafState.pose.entity;
        float entityYaw = iafState.bodyRot;
        float partialTicks = iafState.partialTick;
        int packedLightIn = iafState.lightCoords;
        if (entityIn.getGrowthStage() == 0) {
            model = LARVA_MODEL;
        } else if (entityIn.getGrowthStage() == 1) {
            model = PUPA_MODEL;
        } else {
            model = adultModel;
        }
        super.submit(iafState, matrixStackIn, iafCollector, iafCamera);

    }

    @Override
    protected void scale(IafMobRenderState iafState, PoseStack matrixStackIn) {
        EntityMyrmexBase myrmex = (EntityMyrmexBase) iafState.pose.entity;
        float partialTickTime = iafState.partialTick;
        float scale = myrmex.getModelScale();
        if (myrmex.getGrowthStage() == 0) {
            scale /= 2;
        }
        if (myrmex.getGrowthStage() == 1) {
            scale /= 1.5F;
        }
        scale *= VanillaScale.undo(myrmex);
        matrixStackIn.scale(scale, scale, scale);
        if (myrmex.isPassenger() && myrmex.getGrowthStage() < 2) {
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90.0F));
        }
    }

    @Override
    protected Identifier getTexture(EntityMyrmexBase myrmex) {
        return myrmex.getTexture();
    }

}
