package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.vladeezyy.rampart.client.model.basic.BasicEntityModel;
import net.minecraft.client.model.Model;

/**
 * 1.21.1's {@code model.renderToBuffer} for code that poses a model itself (layers, item and block renderers,
 * particles, statues). Rampart's models draw vanilla parts that only take the pose of the boxes in {@code syncPose()}
 * (the game's {@code setupAnim(state)} does it on the submit path); {@code renderToBuffer} is final on 26.x.
 */
public final class IafModels {
    private IafModels() {
    }

    public static void render(Model<?> model, PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
        if (model instanceof BasicEntityModel<?> rampart) {
            rampart.syncPose();
        }
        model.renderToBuffer(poseStack, buffer, light, overlay, color);
    }

    public static void render(Model<?> model, PoseStack poseStack, VertexConsumer buffer, int light, int overlay) {
        render(model, poseStack, buffer, light, overlay, -1);
    }
}
