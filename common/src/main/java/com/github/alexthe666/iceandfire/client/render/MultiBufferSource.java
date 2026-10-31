package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;

/**
 * 1.21.1's {@code MultiBufferSource} (removed in 26.2): the drawing code of the mod's block entity, item and layer
 * renderers asks it for buffers; {@link IafBufferSource} records them and submits them to the collector.
 */
public interface MultiBufferSource {
    VertexConsumer getBuffer(RenderType type);
}
