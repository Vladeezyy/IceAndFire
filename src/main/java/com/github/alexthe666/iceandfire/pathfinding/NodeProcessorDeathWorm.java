package com.github.alexthe666.iceandfire.pathfinding;

import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.Target;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class NodeProcessorDeathWorm extends NodeEvaluator {

    @Override
    public @NotNull Node getStart() {
        return this.getNode(Mth.floor(this.mob.getBoundingBox().minX), Mth.floor(this.mob.getBoundingBox().minY + 0.5D), Mth.floor(this.mob.getBoundingBox().minZ));
    }

    @Override
    public @NotNull Target getTarget(double x, double y, double z) {
        return new Target(this.getNode(Mth.floor(x - 0.4), Mth.floor(y + 0.5D), Mth.floor(z - 0.4)));
    }

    @Override
    public @NotNull PathType getPathTypeOfMob(@NotNull PathfindingContext context, int x, int y, int z, @NotNull Mob entitylivingIn) {
        return this.getPathType(context, x, y, z);
    }

    @Override
    public @NotNull PathType getPathType(@NotNull PathfindingContext context, int x, int y, int z) {
        BlockGetter worldIn = context.level();
        BlockPos blockpos = new BlockPos(x, y, z);
        BlockState blockstate = worldIn.getBlockState(blockpos);
        if (!isPassable(worldIn, blockpos.below()) && (blockstate.isAir() || isPassable(worldIn, blockpos))) {
            return PathType.BREACH;
        } else {
            return isPassable(worldIn, blockpos) ? PathType.WATER : PathType.BLOCKED;
        }
    }

    @Override
    public int getNeighbors(Node @NotNull [] p_222859_1_, @NotNull Node p_222859_2_) {
        int i = 0;

        for (Direction direction : Direction.values()) {
            Node pathpoint = this.getSandNode(p_222859_2_.x + direction.getStepX(), p_222859_2_.y + direction.getStepY(), p_222859_2_.z + direction.getStepZ());
            if (pathpoint != null && !pathpoint.closed) {
                p_222859_1_[i++] = pathpoint;
            }
        }

        return i;
    }

    @Nullable
    private Node getSandNode(int p_186328_1_, int p_186328_2_, int p_186328_3_) {
        PathType pathnodetype = this.isFree(p_186328_1_, p_186328_2_, p_186328_3_);
        return pathnodetype != PathType.BREACH && pathnodetype != PathType.WATER ? null : this.getNode(p_186328_1_, p_186328_2_, p_186328_3_);
    }

    private PathType isFree(int p_186327_1_, int p_186327_2_, int p_186327_3_) {
        BlockPos.MutableBlockPos blockpos$mutable = new BlockPos.MutableBlockPos();
        for (int i = p_186327_1_; i < p_186327_1_ + this.entityWidth; ++i) {
            for (int j = p_186327_2_; j < p_186327_2_ + this.entityHeight; ++j) {
                for (int k = p_186327_3_; k < p_186327_3_ + this.entityDepth; ++k) {
                    BlockState blockstate = this.currentContext.level().getBlockState(blockpos$mutable.set(i, j, k));
                    if (!isPassable(this.currentContext.level(), blockpos$mutable.below()) && (blockstate.isAir() || isPassable(this.currentContext.level(), blockpos$mutable))) {
                        return PathType.BREACH;
                    }

                }
            }
        }

        BlockState blockstate1 = this.currentContext.level().getBlockState(blockpos$mutable);
        return isPassable(blockstate1) ? PathType.WATER : PathType.BLOCKED;
    }


    private boolean isPassable(BlockGetter world, BlockPos pos) {
        return world.getBlockState(pos).is(BlockTags.SAND) || world.getBlockState(pos).isAir();
    }

    private boolean isPassable(BlockState state) {
        return state.is(BlockTags.SAND) || state.isAir();
    }
}