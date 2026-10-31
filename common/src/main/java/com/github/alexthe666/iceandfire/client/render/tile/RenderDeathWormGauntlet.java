package com.github.alexthe666.iceandfire.client.render.tile;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import com.github.alexthe666.iceandfire.client.model.ModelDeathWormGauntlet;
import com.github.alexthe666.iceandfire.client.render.entity.RenderDeathWorm;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RenderDeathWormGauntlet implements com.github.alexthe666.iceandfire.client.render.IafLegacyItemRenderer {
    private static final ModelDeathWormGauntlet MODEL = new ModelDeathWormGauntlet();

    public RenderDeathWormGauntlet() {
        EntityModelSet p_172551_ = net.minecraft.client.Minecraft.getInstance().getEntityModels();
    }

    @Override
    public void renderByItem(ItemStack stack, @NotNull ItemDisplayContext type, @NotNull PoseStack stackIn, @NotNull MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        RenderType texture;

        if (stack.getItem() == IafItemRegistry.DEATHWORM_GAUNTLET_RED.get()) {
            texture = RenderTypes.entityCutout(RenderDeathWorm.TEXTURE_RED);
        } else if (stack.getItem() == IafItemRegistry.DEATHWORM_GAUNTLET_WHITE.get()) {
            texture = RenderTypes.entityCutout(RenderDeathWorm.TEXTURE_WHITE);
        } else {
            texture = RenderTypes.entityCutout(RenderDeathWorm.TEXTURE_YELLOW);
        }
        stackIn.pushPose();
        stackIn.translate(0.5F, 0.5F, 0.5F);
        stackIn.pushPose();
        stackIn.pushPose();
        MODEL.animate(stack, Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false));
        com.github.alexthe666.iceandfire.client.render.IafModels.render(MODEL, stackIn, bufferIn.getBuffer(texture), combinedLightIn, combinedOverlayIn, -1);
        stackIn.popPose();
        stackIn.popPose();
        stackIn.popPose();
    }
}
