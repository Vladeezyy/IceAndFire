package com.github.alexthe666.iceandfire.pathfinding.raycoms;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

/**
 * Gives every vertex the same color. 1.21 removed BufferBuilder#defaultColor, which the debug renderer relied on.
 */
public class DefaultColorVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public DefaultColorVertexConsumer(VertexConsumer delegate, int red, int green, int blue, int alpha) {
        this.delegate = delegate;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x, y, z).setColor(red, green, blue, alpha);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        delegate.setUv(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        delegate.setUv1(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        delegate.setUv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        delegate.setNormal(normalX, normalY, normalZ);
        return this;
    }
}
