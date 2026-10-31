package com.github.alexthe666.iceandfire.client.render.entity;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.minecraft.util.ARGB;
import com.github.alexthe666.iceandfire.client.model.ModelChainTie;
import com.github.alexthe666.iceandfire.entity.EntityChainTie;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;


public class RenderChainTie extends IafEntityRenderer<EntityChainTie> {
    private static final Identifier TEXTURE = Identifier.parse("iceandfire:textures/models/misc/chain_tie.png");
    private final ModelChainTie leashKnotModel = new ModelChainTie();

    public RenderChainTie(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderLegacy(@NotNull EntityChainTie entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        matrixStackIn.pushPose();
        matrixStackIn.translate(0, 0.5F, 0);
        matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
        this.leashKnotModel.setupAnim(entityIn, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
        VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.entityCutoutNoCull(TEXTURE));
        com.github.alexthe666.iceandfire.client.render.IafModels.render(this.leashKnotModel, matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, -1);
        matrixStackIn.popPose();
            }

    /**
     * Returns the location of an entity's texture.
     */
    public @NotNull Identifier getTextureLocation(@NotNull EntityChainTie entity) {
        return TEXTURE;
    }
}