package com.github.alexthe666.iceandfire.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.HumanoidArm;

/** A Citadel-style model with hands (1.21.1's vanilla ArmedModel; 26.x's takes a render state) */
public interface IafArmedModel {
    void translateToHand(HumanoidArm side, PoseStack poseStack);
}
