package com.github.alexthe666.iceandfire.client.render.entity;

import dev.vladeezyy.rampart.client.render.RampartLivingRenderState;
import net.minecraft.resources.Identifier;

/** Render state of the mod's mobs: their texture, picked from the entity when the state is filled */
public class IafMobRenderState extends RampartLivingRenderState {
	public Identifier texture;
	/** {@code IafEntityLayer.poseParent}: the pose count when a layer last posed the parent model for this state; 0 = never */
	public long parentPosedAt;
}
