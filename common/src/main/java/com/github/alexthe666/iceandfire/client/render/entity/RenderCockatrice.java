package com.github.alexthe666.iceandfire.client.render.entity;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;

import net.minecraft.util.ARGB;
import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import com.github.alexthe666.iceandfire.client.model.ModelCockatrice;
import com.github.alexthe666.iceandfire.client.model.ModelCockatriceChick;
import com.github.alexthe666.iceandfire.client.particle.CockatriceBeamRender;
import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class RenderCockatrice extends IafMobRenderer<EntityCockatrice, AdvancedEntityModel<EntityCockatrice>> {

    public static final Identifier TEXTURE_ROOSTER = Identifier.parse("iceandfire:textures/models/cockatrice/cockatrice_0.png");
    public static final Identifier TEXTURE_HEN = Identifier.parse("iceandfire:textures/models/cockatrice/cockatrice_1.png");
    public static final Identifier TEXTURE_ROOSTER_CHICK = Identifier.parse("iceandfire:textures/models/cockatrice/cockatrice_0_chick.png");
    public static final Identifier TEXTURE_HEN_CHICK = Identifier.parse("iceandfire:textures/models/cockatrice/cockatrice_1_chick.png");
    public static final ModelCockatrice ADULT_MODEL = new ModelCockatrice();
    public static final ModelCockatriceChick BABY_MODEL = new ModelCockatriceChick();

    public RenderCockatrice(EntityRendererProvider.Context context) {
        super(context, new ModelCockatrice(), 0.6F);
    }


    private Vec3 getPosition(LivingEntity LivingEntityIn, double p_177110_2_, float p_177110_4_) {
        double d0 = LivingEntityIn.xOld + (LivingEntityIn.getX() - LivingEntityIn.xOld) * (double) p_177110_4_;
        double d1 = p_177110_2_ + LivingEntityIn.yOld + (LivingEntityIn.getY() - LivingEntityIn.yOld) * (double) p_177110_4_;
        double d2 = LivingEntityIn.zOld + (LivingEntityIn.getZ() - LivingEntityIn.zOld) * (double) p_177110_4_;
        return new Vec3(d0, d1, d2);
    }

    @Override
    public boolean shouldRender(@NotNull EntityCockatrice livingEntityIn, @NotNull Frustum camera, double camX, double camY, double camZ, float partialTicks) {
        if (super.shouldRender(livingEntityIn, camera, camX, camY, camZ, partialTicks)) {
            return true;
        } else {
            if (livingEntityIn.hasTargetedEntity()) {
                LivingEntity livingentity = livingEntityIn.getTargetedEntity();
                if (livingentity != null) {
                    Vec3 Vector3d = this.getPosition(livingentity, (double) livingentity.getBbHeight() * 0.5D, 1.0F);
                    Vec3 Vector3d1 = this.getPosition(livingEntityIn, livingEntityIn.getEyeHeight(), 1.0F);
                    return camera.isVisible(new AABB(Vector3d1.x, Vector3d1.y, Vector3d1.z, Vector3d.x, Vector3d.y, Vector3d.z));
                }
            }

            return false;
        }
    }

    @Override
    public void submit(IafMobRenderState iafState, PoseStack matrixStackIn, SubmitNodeCollector iafCollector, CameraRenderState iafCamera) {
        IafBufferSource bufferIn = new IafBufferSource();
        EntityCockatrice entityIn = (EntityCockatrice) iafState.pose.entity;
        float entityYaw = iafState.bodyRot;
        float partialTicks = iafState.partialTick;
        int packedLightIn = iafState.lightCoords;
        if (entityIn.isBaby()) {
            model = BABY_MODEL;
        } else {
            model = ADULT_MODEL;
        }
        super.submit(iafState, matrixStackIn, iafCollector, iafCamera);
        LivingEntity livingentity = entityIn.getTargetedEntity();
        boolean blindness = entityIn.hasEffect(MobEffects.BLINDNESS) || livingentity != null && livingentity.hasEffect(MobEffects.BLINDNESS);
        if (!blindness && livingentity != null && EntityGorgon.isEntityLookingAt(entityIn, livingentity, EntityCockatrice.VIEW_RADIUS) && EntityGorgon.isEntityLookingAt(livingentity, entityIn, EntityCockatrice.VIEW_RADIUS)) {
            if (livingentity != null) {
                CockatriceBeamRender.render(entityIn, livingentity, matrixStackIn, bufferIn, partialTicks);
            }
        }
        bufferIn.submit(iafCollector);
    }

    @Override
    protected void scale(IafMobRenderState iafState, PoseStack matrixStackIn) {
        EntityCockatrice entity = (EntityCockatrice) iafState.pose.entity;
        float partialTickTime = iafState.partialTick;
        if (entity.isBaby()) {
            matrixStackIn.scale(0.5F, 0.5F, 0.5F);
        }
    }

    @Override
    protected Identifier getTexture(EntityCockatrice cockatrice) {
        if (cockatrice.isBaby()) {
            return cockatrice.isHen() ? TEXTURE_HEN_CHICK : TEXTURE_ROOSTER_CHICK;
        } else {
            return cockatrice.isHen() ? TEXTURE_HEN : TEXTURE_ROOSTER;
        }
    }

}
