package dev.vladeezyy.iceandfire.bus;

import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

/**
 * A living entity drawn by its renderer (26.x: fired by LivingEntityRendererMixin around LivingEntityRenderer.submit;
 * the buffers record into the frame's submits, see IafBufferSource). The renderer and its model are not part of it.
 */
public abstract class RenderLivingEvent extends LivingEvent {
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    protected RenderLivingEvent(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        super(entity);
        this.partialTick = partialTick;
        this.poseStack = poseStack;
        this.multiBufferSource = multiBufferSource;
        this.packedLight = packedLight;
    }

    public float getPartialTick() {
        return this.partialTick;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public MultiBufferSource getMultiBufferSource() {
        return this.multiBufferSource;
    }

    public int getPackedLight() {
        return this.packedLight;
    }

    /** Before a living entity is drawn; canceling it skips it */
    public static class Pre extends RenderLivingEvent implements ICancellableEvent {
        public Pre(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }

    /** After a living entity was drawn */
    public static class Post extends RenderLivingEvent {
        public Post(LivingEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(entity, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
