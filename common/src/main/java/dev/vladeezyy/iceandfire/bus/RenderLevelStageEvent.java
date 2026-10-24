package dev.vladeezyy.iceandfire.bus;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;

/** A stage of the level rendering; only the stages the mod draws in */
public class RenderLevelStageEvent extends Event {
    private final Stage stage;
    private final PoseStack poseStack;
    private final DeltaTracker partialTick;

    public RenderLevelStageEvent(Stage stage, PoseStack poseStack, DeltaTracker partialTick) {
        this.stage = stage;
        this.poseStack = poseStack;
        this.partialTick = partialTick;
    }

    public Stage getStage() {
        return this.stage;
    }

    public PoseStack getPoseStack() {
        return this.poseStack;
    }

    public DeltaTracker getPartialTick() {
        return this.partialTick;
    }

    public enum Stage {
        AFTER_CUTOUT_MIPPED_BLOCKS_BLOCKS,
        AFTER_TRIPWIRE_BLOCKS
    }
}
