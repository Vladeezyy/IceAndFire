package dev.vladeezyy.iceandfire.bus;

import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexthe666.iceandfire.client.render.MultiBufferSource;
import net.minecraft.world.entity.player.Player;

public abstract class RenderPlayerEvent extends PlayerEvent {
    private final float partialTick;
    private final PoseStack poseStack;
    private final MultiBufferSource multiBufferSource;
    private final int packedLight;

    protected RenderPlayerEvent(Player player, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        super(player);
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

    /** Before a player is drawn; canceling it skips it */
    public static class Pre extends RenderPlayerEvent implements ICancellableEvent {
        public Pre(Player player, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
            super(player, partialTick, poseStack, multiBufferSource, packedLight);
        }
    }
}
