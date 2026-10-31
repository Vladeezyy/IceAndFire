package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
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

    /** 1.21.1's ItemRenderer.getFoilBuffer(Direct): the glint is a render type of its own on 26.x, drawn with the same vertices */
    public static com.mojang.blaze3d.vertex.VertexConsumer foilBuffer(MultiBufferSource buffers, net.minecraft.client.renderer.rendertype.RenderType type, boolean foil) {
        com.mojang.blaze3d.vertex.VertexConsumer base = buffers.getBuffer(type);
        return foil ? new Both(buffers.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityGlint()), base) : base;
    }

    /** Every vertex to both consumers (26.2 removed VertexMultiConsumer) */
    private record Both(com.mojang.blaze3d.vertex.VertexConsumer a, com.mojang.blaze3d.vertex.VertexConsumer b) implements com.mojang.blaze3d.vertex.VertexConsumer {
        @Override public com.mojang.blaze3d.vertex.VertexConsumer addVertex(float x, float y, float z) { a.addVertex(x, y, z); b.addVertex(x, y, z); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setColor(int r, int g, int bl, int al) { a.setColor(r, g, bl, al); b.setColor(r, g, bl, al); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setColor(int color) { a.setColor(color); b.setColor(color); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv(float u, float v) { a.setUv(u, v); b.setUv(u, v); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv1(int u, int v) { a.setUv1(u, v); b.setUv1(u, v); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setUv2(int u, int v) { a.setUv2(u, v); b.setUv2(u, v); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setNormal(float x, float y, float z) { a.setNormal(x, y, z); b.setNormal(x, y, z); return this; }
        @Override public com.mojang.blaze3d.vertex.VertexConsumer setLineWidth(float width) { a.setLineWidth(width); b.setLineWidth(width); return this; }
    }
}
