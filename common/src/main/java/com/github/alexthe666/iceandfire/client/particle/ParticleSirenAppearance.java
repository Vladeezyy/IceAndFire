package com.github.alexthe666.iceandfire.client.particle;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelSiren;
import com.github.alexthe666.iceandfire.client.render.entity.RenderSiren;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class ParticleSirenAppearance extends IafAppearanceParticle {
    private final ModelSiren model = new ModelSiren();
    private final int sirenType;

    public ParticleSirenAppearance(ClientLevel worldIn, double xCoordIn, double yCoordIn, double zCoordIn, int sirenType) {
        super(worldIn, xCoordIn, yCoordIn, zCoordIn);
        this.gravity = 0.0F;
        this.lifetime = 30;
        this.sirenType = sirenType;
    }

    @Override
    protected void renderAppearance(IafBufferSource irendertypebuffer$impl, Camera renderInfo, float partialTicks) {
        float f = ((float) this.age + partialTicks) / (float) this.lifetime;
        float f1 = 0.05F + 0.5F * Mth.sin(f * (float) Math.PI);
        PoseStack matrixstack = new PoseStack();
        matrixstack.rotate(renderInfo.rotation());
        matrixstack.rotate(Axis.XP.rotationDegrees(150.0F * f - 60.0F));
        matrixstack.scale(-1.0F, -1.0F, 1.0F);
        matrixstack.translate(0.0D, -1.101F, 1.5D);
        VertexConsumer ivertexbuilder = irendertypebuffer$impl.getBuffer(RenderTypes.entityTranslucent(RenderSiren.getSirenOverlayTexture(sirenType)));
        com.github.alexthe666.iceandfire.client.render.IafModels.render(this.model, matrixstack, ivertexbuilder, 15728880, OverlayTexture.NO_OVERLAY, ARGB.colorFromFloat(f1, 1.0F, 1.0F, 1.0F));
    }
}

