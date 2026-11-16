package com.github.alexthe666.iceandfire.client.particle;

import net.minecraft.util.FastColor;
import com.github.alexthe666.iceandfire.client.model.ModelSiren;
import com.github.alexthe666.iceandfire.client.render.entity.RenderSiren;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ParticleSirenAppearance extends Particle {
    private final Model model = new ModelSiren();
    private final int sirenType;

    public ParticleSirenAppearance(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, int sirenType) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.gravity = 0.0F;
        this.lifetime = 30;
        this.sirenType = sirenType;
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        float f = ((float) this.age + partialTicks) / (float) this.lifetime;
        float f1 = 0.05F + 0.5F * Mth.sin(f * (float) Math.PI);
        PoseStack matrixstack = new PoseStack();
        matrixstack.mulPose(renderInfo.rotation());
        matrixstack.mulPose(Axis.XP.rotationDegrees(150.0F * f - 60.0F));
        matrixstack.scale(-1.0F, -1.0F, 1.0F);
        matrixstack.translate(0.0D, -1.101F, 1.5D);
        MultiBufferSource.BufferSource irendertypebuffer$impl = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderType.entityTranslucent(RenderSiren.getSirenOverlayTexture(sirenType)));
        this.model.renderToBuffer(matrixstack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(f1, 1.0F, 1.0F, 1.0F));
        irendertypebuffer$impl.endBatch();
        restoreParticleState();
    }

    /**
     * endBatch leaves the state of the model's render type (entity shader, no depth test, no lightmap): the particles of
     * the CUSTOM pass drawn after an appearance (all the mod's own) drew with it and were not seen. Back to what
     * ParticleEngine.render and CUSTOM.begin set.
     */
    static void restoreParticleState() {
        com.mojang.blaze3d.systems.RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getParticleShader);
        Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
        com.mojang.blaze3d.systems.RenderSystem.enableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.depthMask(true);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }
}

