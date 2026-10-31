package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 1.21.1's BlockEntityWithoutLevelRenderer.renderByItem of the items the mod draws in code (26.x: IafItemSpecialRenderer) */
public interface IafLegacyItemRenderer {
    void renderByItem(ItemStack stack, ItemDisplayContext display, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay);
}
