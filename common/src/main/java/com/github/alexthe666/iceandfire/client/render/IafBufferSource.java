package com.github.alexthe666.iceandfire.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
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
    private static final net.minecraft.client.renderer.block.model.BlockDisplayContext BLOCK_DISPLAY = net.minecraft.client.renderer.block.model.BlockDisplayContext.create();
    // made on first use: most sources (the render events of every mob each frame) stay empty
    private Map<RenderType, Recorder> buffers;
    private java.util.List<java.util.function.Consumer<SubmitNodeCollector>> later;

    /** 1.21.1's ItemRenderer.renderStatic into this source: the model is resolved now, submitted with the rest */
    public void item(net.minecraft.world.item.ItemStack stack, net.minecraft.world.item.ItemDisplayContext display, int light, int overlay, PoseStack poseStack, @org.jetbrains.annotations.Nullable net.minecraft.world.level.Level level, @org.jetbrains.annotations.Nullable net.minecraft.world.entity.LivingEntity holder, int seed) {
        if (stack.isEmpty()) {
            return;
        }
        net.minecraft.client.renderer.item.ItemStackRenderState state = new net.minecraft.client.renderer.item.ItemStackRenderState();
        net.minecraft.client.Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, display, level, holder, seed);
        PoseStack.Pose pose = poseStack.last().copy();
        this.later().add(collector -> {
            PoseStack stackAt = new PoseStack();
            stackAt.last().set(pose);
            state.submit(stackAt, collector, light, overlay, 0);
        });
    }

    /** 1.21.1's BlockRenderDispatcher.renderSingleBlock into this source (the block from 0,0,0 to 1,1,1) */
    public void block(net.minecraft.world.level.block.state.BlockState blockState, PoseStack poseStack, int light, int overlay) {
        net.minecraft.client.renderer.block.BlockModelRenderState state = new net.minecraft.client.renderer.block.BlockModelRenderState();
        new net.minecraft.client.renderer.block.BlockModelResolver(net.minecraft.client.Minecraft.getInstance().getModelManager()).update(state, blockState, BLOCK_DISPLAY);
        PoseStack.Pose pose = poseStack.last().copy();
        this.later().add(collector -> {
            PoseStack stackAt = new PoseStack();
            stackAt.last().set(pose);
            state.submit(stackAt, collector, light, overlay, 0);
        });
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        if (this.buffers == null) {
            this.buffers = new LinkedHashMap<>();
        }
        return this.buffers.computeIfAbsent(type, t -> new Recorder());
    }

    private java.util.List<java.util.function.Consumer<SubmitNodeCollector>> later() {
        if (this.later == null) {
            this.later = new java.util.ArrayList<>();
        }
        return this.later;
    }

    /** Submits everything recorded and clears it */
    public void submit(SubmitNodeCollector collector) {
        if (this.buffers == null && this.later == null) {
            return;
        }
        if (this.buffers != null) for (Map.Entry<RenderType, Recorder> entry : this.buffers.entrySet()) {
            Recorder recorder = entry.getValue();
            if (recorder.count > 0) {
                collector.submitCustomGeometry(IDENTITY, entry.getKey(), (pose, consumer) -> recorder.replay(consumer));
            }
        }
        this.buffers = null;
        if (this.later != null) {
            for (java.util.function.Consumer<SubmitNodeCollector> action : this.later) {
                action.accept(collector);
            }
        }
        this.later = null;
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

        @Override
        public VertexConsumer setUv3(float u, float v) {
            // 26.3: the glint formats' decal coordinates (vanilla's sheeted decal sets them); replayed as 0, 0
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
                    .setUv3(0, 0)
                    .setNormal(this.floats.getFloat(fo + 5), this.floats.getFloat(fo + 6), this.floats.getFloat(fo + 7));
            }
        }
    }
}
