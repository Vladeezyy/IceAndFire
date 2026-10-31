package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.21.1-style drawing on 26.x: a {@link MultiBufferSource} that records what the mod's renderers draw (positions and
 * normals already transformed by their pose stack, as 1.21.1 put them in the buffer) and hands the vertices to the
 * frame's {@link SubmitNodeCollector} as custom geometry with an identity pose, so the result is the same quads in
 * the same order per render type. For the code that draws by hand (projectiles, skulls, beams, overlays); the mobs'
 * bodies go through vanilla's model submits.
 */
public final class IafBufferSource implements MultiBufferSource {
    private static final PoseStack IDENTITY = new PoseStack();
    private final Map<RenderType, Recorder> buffers = new LinkedHashMap<>();

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return this.buffers.computeIfAbsent(type, t -> new Recorder());
    }

    /** Submits everything recorded and clears it */
    public void submit(SubmitNodeCollector collector) {
        for (Map.Entry<RenderType, Recorder> entry : this.buffers.entrySet()) {
            Recorder recorder = entry.getValue();
            if (recorder.count > 0) {
                collector.submitCustomGeometry(IDENTITY, entry.getKey(), (pose, consumer) -> recorder.replay(consumer));
            }
        }
        this.buffers.clear();
    }

    /** One render type's vertices: position, color, uv, overlay, light and normal per vertex */
    private static final class Recorder implements VertexConsumer {
        private final FloatArrayList floats = new FloatArrayList();
        private final IntArrayList ints = new IntArrayList();
        private int count;

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.count++;
            this.floats.add(x);
            this.floats.add(y);
            this.floats.add(z);
            // u, v, nx, ny, nz
            this.floats.add(0);
            this.floats.add(0);
            this.floats.add(0);
            this.floats.add(1);
            this.floats.add(0);
            // color, overlay, light
            this.ints.add(-1);
            this.ints.add(0);
            this.ints.add(0);
            return this;
        }

        private int f(int offset) {
            return (this.count - 1) * 8 + offset;
        }

        private int i(int offset) {
            return (this.count - 1) * 3 + offset;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.ints.set(i(0), (a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            this.ints.set(i(0), color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.floats.set(f(3), u);
            this.floats.set(f(4), v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.ints.set(i(1), (v & 0xFFFF) << 16 | (u & 0xFFFF));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.ints.set(i(2), (v & 0xFFFF) << 16 | (u & 0xFFFF));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.floats.set(f(5), x);
            this.floats.set(f(6), y);
            this.floats.set(f(7), z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            return this;
        }

        void replay(VertexConsumer consumer) {
            for (int v = 0; v < this.count; v++) {
                int fo = v * 8, io = v * 3;
                int overlay = this.ints.getInt(io + 1), light = this.ints.getInt(io + 2);
                consumer.addVertex(this.floats.getFloat(fo), this.floats.getFloat(fo + 1), this.floats.getFloat(fo + 2))
                    .setColor(this.ints.getInt(io))
                    .setUv(this.floats.getFloat(fo + 3), this.floats.getFloat(fo + 4))
                    .setUv1(overlay & 0xFFFF, overlay >>> 16)
                    .setUv2(light & 0xFFFF, light >>> 16)
                    .setNormal(this.floats.getFloat(fo + 5), this.floats.getFloat(fo + 6), this.floats.getFloat(fo + 7));
            }
        }
    }
}
