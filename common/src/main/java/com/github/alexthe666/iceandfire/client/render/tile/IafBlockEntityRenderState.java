package com.github.alexthe666.iceandfire.client.render.tile;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Render state of the mod's block entity renderers: the block entity itself (the 1.21.1 renderers read it while drawing) */
public class IafBlockEntityRenderState extends BlockEntityRenderState {
    public BlockEntity blockEntity;
    public float partialTick;
}
