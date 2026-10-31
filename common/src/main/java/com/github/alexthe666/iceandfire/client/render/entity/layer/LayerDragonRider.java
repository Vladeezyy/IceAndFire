package com.github.alexthe666.iceandfire.client.render.entity.layer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import com.github.alexthe666.iceandfire.client.render.entity.IafMobRenderState;

import net.minecraft.util.ARGB;
import dev.vladeezyy.rampart.client.model.AdvancedEntityModel;
import dev.vladeezyy.rampart.client.model.AdvancedModelBox;
import dev.vladeezyy.rampart.client.model.TabulaModel;
import com.github.alexthe666.iceandfire.client.ClientProxy;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDreadQueen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.animal.equine.HorseModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.QuadrupedModel;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class LayerDragonRider extends IafEntityLayer<EntityDragonBase, IafMobRenderState, AdvancedEntityModel<EntityDragonBase>> {
    private final MobRenderer render;
    private final boolean excludeDreadQueenMob;

    public LayerDragonRider(MobRenderer renderIn, boolean excludeDreadQueenMob) {
        super(renderIn);
        this.render = renderIn;
        this.excludeDreadQueenMob = excludeDreadQueenMob;
    }

    @Override
    protected void submit(PoseStack matrixStackIn, SubmitNodeCollector iafCollector, int packedLightIn, IafMobRenderState iafState, EntityDragonBase dragon) {
        // the parts read below must hold this entity's pose (26.x poses the model when it is drawn)
        this.poseParent(iafState);
        IafBufferSource bufferIn = new IafBufferSource();
        float limbSwing = iafState.pose.limbSwing, limbSwingAmount = iafState.pose.limbSwingAmount, partialTicks = iafState.partialTick, ageInTicks = iafState.ageInTicks, netHeadYaw = iafState.pose.netHeadYaw, headPitch = iafState.pose.headPitch;
        matrixStackIn.pushPose();
        if (!dragon.getPassengers().isEmpty()) {
            float dragonScale = dragon.getRenderSize() / 3;
            for (Entity passenger : dragon.getPassengers()) {
                boolean prey = dragon.getControllingPassenger() == null || dragon.getControllingPassenger().getId() != passenger.getId();
                if (excludeDreadQueenMob && passenger instanceof EntityDreadQueen) {
                    prey = false;
                }
                ClientProxy.currentDragonRiders.remove(passenger.getUUID());
                float riderRot = passenger.yRotO + (passenger.getYRot() - passenger.yRotO) * partialTicks;
                int animationTicks = 0;
                if (dragon.getAnimation() == EntityDragonBase.ANIMATION_SHAKEPREY) {
                    animationTicks = dragon.getAnimationTick();
                }
                if (animationTicks == 0 || animationTicks >= 15) {
                    translateToBody(matrixStackIn);
                }
                if (prey) {
                    if (animationTicks == 0 || animationTicks >= 15 || dragon.isFlying()) {
                        translateToHead(matrixStackIn);
                        offsetPerDragonType(dragon.dragonType, matrixStackIn);
                        EntityRenderer render = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(passenger);
                        EntityModel modelBase = null;
                        if (render instanceof MobRenderer) {
                            modelBase = ((MobRenderer) render).getModel();
                        }
                        if ((passenger.getBbHeight() > passenger.getBbWidth() || modelBase instanceof HumanoidModel) && !(modelBase instanceof QuadrupedModel) && !(modelBase instanceof HorseModel)) {
                            matrixStackIn.translate(-0.15F * passenger.getBbHeight(), 0.1F * dragonScale - 0.1F * passenger.getBbHeight(), -0.1F * dragonScale - 0.1F * passenger.getBbWidth());
                            matrixStackIn.rotate(Axis.ZP.rotationDegrees(90.0F));
                            matrixStackIn.rotate(Axis.YP.rotationDegrees(45.0F));
                        } else {
                            boolean horse = modelBase instanceof HorseModel;
                            matrixStackIn.translate((horse ? -0.08F : -0.15F) * passenger.getBbWidth(), 0.1F * dragonScale - 0.15F * passenger.getBbWidth(), -0.1F * dragonScale - 0.1F * passenger.getBbWidth());
                            matrixStackIn.rotate(Axis.XN.rotationDegrees(90.0F));
                        }
                    } else {
                        matrixStackIn.translate(0, 0.555F * dragonScale, -0.5F * dragonScale);
                    }

                } else {
                    matrixStackIn.translate(0, -0.01F * dragonScale, -0.035F * dragonScale);
                }
                matrixStackIn.pushPose();
                matrixStackIn.rotate(Axis.ZP.rotationDegrees(180.0F));
                matrixStackIn.rotate(Axis.YP.rotationDegrees(riderRot+180));
                matrixStackIn.scale(1 / dragonScale, 1 / dragonScale, 1 / dragonScale);
                matrixStackIn.translate(0, -0.25F, 0);
                renderEntity(passenger, 0, 0, 0, 0.0F, partialTicks, matrixStackIn, iafCollector, packedLightIn);
                matrixStackIn.popPose();
                ClientProxy.currentDragonRiders.add(passenger.getUUID());
            }
        }
        matrixStackIn.popPose();
        bufferIn.submit(iafCollector);
    }

    protected void translateToBody(PoseStack stack) {
        postRender(((TabulaModel) this.render.getModel()).getCube("BodyUpper"), stack, 0.0625F);
        postRender(((TabulaModel) this.render.getModel()).getCube("Neck1"), stack, 00.0625F);
    }

    protected void translateToHead(PoseStack stack) {
        postRender(((TabulaModel) this.render.getModel()).getCube("Neck2"), stack, 0.0625F);
        postRender(((TabulaModel) this.render.getModel()).getCube("Neck3"), stack, 0.0625F);
        postRender(((TabulaModel) this.render.getModel()).getCube("Head"), stack, 0.0625F);
    }

    protected void postRender(AdvancedModelBox renderer, PoseStack matrixStackIn, float scale) {
        if (renderer.rotateAngleX == 0.0F && renderer.rotateAngleY == 0.0F && renderer.rotateAngleZ == 0.0F) {
            if (renderer.rotationPointX != 0.0F || renderer.rotationPointY != 0.0F || renderer.rotationPointZ != 0.0F) {
                matrixStackIn.translate(renderer.rotationPointX * scale, renderer.rotationPointY * scale, renderer.rotationPointZ * scale);
            }
        } else {
            matrixStackIn.translate(renderer.rotationPointX * scale, renderer.rotationPointY * scale, renderer.rotationPointZ * scale);

            if (renderer.rotateAngleZ != 0.0F) {
                matrixStackIn.rotate(Axis.ZP.rotation(renderer.rotateAngleZ));
            }

            if (renderer.rotateAngleY != 0.0F) {
                matrixStackIn.rotate(Axis.YP.rotation(renderer.rotateAngleY));
            }

            if (renderer.rotateAngleX != 0.0F) {
                matrixStackIn.rotate(Axis.XP.rotation(renderer.rotateAngleX));
            }
        }
    }

    private void offsetPerDragonType(DragonType dragonType, PoseStack stackIn) {
        if (dragonType == DragonType.LIGHTNING) {
            stackIn.translate(0.1F, -0.2F, -0.1F);
        }
    }


    public <E extends Entity> void renderEntity(E entityIn, int x, int y, int z, float yaw, float partialTicks, PoseStack matrixStack, SubmitNodeCollector collector, int packedLight) {
        // 26.x: the rider's render state, submitted where the dragon holds it (its own render is cancelled while it rides)
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            EntityRenderState state = manager.extractEntity(entityIn, partialTicks);
            state.shadowPieces.clear();
            manager.submit(state, Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState, 0, 0, 0, matrixStack, collector);
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.forThrowable(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            entityIn.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Location", new BlockPos(x, y, z));
            crashreportcategory1.setDetail("Rotation", yaw);
            crashreportcategory1.setDetail("Delta", partialTicks);
            throw new ReportedException(crashreport);
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }


}