package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelGhost;
import com.github.alexthe666.iceandfire.client.render.IafRenderType;
import com.github.alexthe666.iceandfire.client.render.entity.RenderGhost;
import com.github.alexthe666.iceandfire.entity.EntityGhost;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class ParticleGhostAppearance extends IafAppearanceParticle {
    private final ModelGhost model = new ModelGhost(0.0F);
    private final int ghost;
    private boolean fromLeft = false;

    public ParticleGhostAppearance(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, int ghost) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.gravity = 0.0F;
        this.lifetime = 15;
        this.ghost = ghost;
        fromLeft = worldIn.getRandom().nextBoolean();
    }

    @Override
    protected void renderAppearance(IafBufferSource irendertypebuffer$impl, Camera renderInfo, float partialTicks) {
        float f = ((float) this.age + partialTicks) / (float) this.lifetime;
        float f1 = 0.05F + 0.5F * Mth.sin(f * (float) Math.PI);
        Entity entity = level.getEntity(ghost);
        if (entity instanceof EntityGhost && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) {
            EntityGhost ghostEntity = (EntityGhost) entity;
            PoseStack matrixstack = new PoseStack();
            matrixstack.mulPose(renderInfo.rotation());
            if (fromLeft) {
                matrixstack.mulPose(Axis.YN.rotationDegrees(150.0F * f - 60.0F));
                matrixstack.mulPose(Axis.ZN.rotationDegrees(150.0F * f - 60.0F));

            } else {
                matrixstack.mulPose(Axis.YP.rotationDegrees(150.0F * f - 60.0F));
                matrixstack.mulPose(Axis.ZP.rotationDegrees(150.0F * f - 60.0F));
            }
            matrixstack.scale(-1.0F, -1.0F, 1.0F);
            matrixstack.translate(0.0D, 0.3F, 1.25D);

            VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(IafRenderType.getGhost(RenderGhost.getGhostOverlayForType(ghostEntity.getColor())));
            this.model.setupAnim(ghostEntity, 0, 0, entity.tickCount + partialTicks, 0, 0);
            com.github.alexthe666.iceandfire.client.render.IafModels.render(this.model, matrixstack, ivertexbuilder, 240, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(f1, 1.0F, 1.0F, 1.0F));
        }

    }
}

