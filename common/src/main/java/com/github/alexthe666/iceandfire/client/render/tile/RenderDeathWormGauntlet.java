package com.github.alexthe666.iceandfire.client.render.tile;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelDeathWormGauntlet;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDeathWorm;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderDeathWormGauntlet extends BlockEntityWithoutLevelRenderer {
    private static final ModelDeathWormGauntlet MODEL = new ModelDeathWormGauntlet();

    public RenderDeathWormGauntlet(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext type, @NotNull PoseStack stackIn, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        RenderType texture;

        if (stack.getItem() == IafItemRegistry.DEATHWORM_GAUNTLET_RED.get()) {
            texture = RenderTypes.entityCutoutCull(RenderDeathWorm.TEXTURE_RED);
        } else if (stack.getItem() == IafItemRegistry.DEATHWORM_GAUNTLET_WHITE.get()) {
            texture = RenderTypes.entityCutoutCull(RenderDeathWorm.TEXTURE_WHITE);
        } else {
            texture = RenderTypes.entityCutoutCull(RenderDeathWorm.TEXTURE_YELLOW);
        }
        stackIn.pushPose();
        stackIn.translate(0.5F, 0.5F, 0.5F);
        stackIn.pushPose();
        stackIn.pushPose();
        MODEL.animate(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        MODEL.renderToBuffer(stackIn, bufferIn.getBuffer(texture), combinedLightIn, combinedOverlayIn, -1);
        stackIn.popPose();
        stackIn.popPose();
        stackIn.popPose();
    }
}
