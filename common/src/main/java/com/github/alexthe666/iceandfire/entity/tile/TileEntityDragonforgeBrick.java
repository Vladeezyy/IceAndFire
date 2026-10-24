package com.github.alexthe666.iceandfire.entity.tile;

import dev.vladeezyy.iceandfire.platform.transfer.ItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class TileEntityDragonforgeBrick extends BlockEntity {

    public TileEntityDragonforgeBrick(BlockPos pos, BlockState state) {
        super(IafTileEntityRegistry.DRAGONFORGE_BRICK.get(), pos, state);
    }

    /** The item handler of the forge core this brick belongs to; registered in IafCapabilities. */
    @Nullable
    public ItemHandler getItemHandler(@Nullable Direction facing) {
        TileEntityDragonforge core = getConnectedTileEntity();
        return core != null ? core.getItemHandler(facing) : null;
    }

    private TileEntityDragonforge getConnectedTileEntity() {
        for (Direction facing : Direction.values()) {
            if (level.getBlockEntity(worldPosition.relative(facing)) instanceof TileEntityDragonforge forge) {
                return forge;
            }
        }
        return null;
    }

}
