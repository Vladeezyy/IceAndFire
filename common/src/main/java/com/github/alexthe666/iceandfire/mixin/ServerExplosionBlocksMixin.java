package com.github.alexthe666.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.util.ExplodedBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.List;

/**
 * The blocks {@code explode()} computed, for the ground blast ({@code BlockLaunchExplosion}), which runs a vanilla
 * explosion that keeps the blocks and then removes them itself. Optional: a mod that replaces {@code explode()} leaves
 * the blast without blocks, not the game without a start.
 */
@Mixin(ServerExplosion.class)
public abstract class ServerExplosionBlocksMixin implements ExplodedBlocks {
    @Unique
    private List<BlockPos> iceandfire$explodedBlocks;

    @ModifyVariable(method = "explode", at = @At("STORE"), ordinal = 0, require = 0)
    private List<BlockPos> iceandfire$rememberBlocks(List<BlockPos> blocks) {
        this.iceandfire$explodedBlocks = blocks;
        return blocks;
    }

    @Override
    public List<BlockPos> iceandfire$explodedBlocks() {
        return this.iceandfire$explodedBlocks;
    }
}
