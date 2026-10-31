package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 1.21.1's item and block drawing calls for the mod's hand-drawn renderers, on an {@link IafBufferSource} (26.x has
 * no ItemRenderer or BlockRenderDispatcher: items and blocks are resolved to render states and submitted).
 */
public final class IafLegacyRender {
    private IafLegacyRender() {
    }

    /** ItemRenderer.renderStatic(stack, display, light, overlay, poseStack, buffers, level, seed) */
    public static void renderStatic(ItemStack stack, ItemDisplayContext display, int light, int overlay, PoseStack poseStack, MultiBufferSource buffers, @Nullable Level level, int seed) {
        if (buffers instanceof IafBufferSource source) {
            source.item(stack, display, light, overlay, poseStack, level, null, seed);
        }
    }

    /** ItemRenderer.renderStatic(holder, stack, display, leftHand, poseStack, buffers, level, light, overlay, seed) */
    public static void renderStatic(@Nullable LivingEntity holder, ItemStack stack, ItemDisplayContext display, boolean leftHand, PoseStack poseStack, MultiBufferSource buffers, @Nullable Level level, int light, int overlay, int seed) {
        if (buffers instanceof IafBufferSource source) {
            source.item(stack, display, light, overlay, poseStack, level, holder, seed);
        }
    }

    /** BlockRenderDispatcher.renderSingleBlock(state, poseStack, buffers, light, overlay) */
    public static void renderSingleBlock(BlockState state, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        if (buffers instanceof IafBufferSource source) {
            source.block(state, poseStack, light, overlay);
        }
    }
}
