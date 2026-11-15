package com.github.alexthe666.iceandfire.mixin;

import dev.vladeezyy.rampart.server.entity.collision.ICustomCollisions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * A ridden mob that passes through some blocks (the death worm in sand) moves there on the client, but the server's
 * vehicle check saw those blocks as new collisions and sent the mob back every tick (rubber-banding, no log line).
 * Here the blocks the mob passes through do not count. Only runs for such mobs, once per vehicle move packet, over the
 * blocks of the mob's box.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class VehiclePassThroughMixin {
    @Inject(method = "isEntityCollidingWithAnythingNew", at = @At("HEAD"), cancellable = true)
    private void iceandfire$passThrough(LevelReader level, Entity entity, AABB oldAABB, double newX, double newY, double newZ, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof ICustomCollisions mob)) {
            return;
        }
        AABB newBox = entity.getBoundingBox().move(newX - entity.getX(), newY - entity.getY(), newZ - entity.getZ()).deflate(1.0E-5F);
        VoxelShape newShape = Shapes.create(newBox);
        VoxelShape oldShape = Shapes.create(oldAABB.deflate(1.0E-5F));
        CollisionContext context = CollisionContext.of(entity);
        BlockPos min = BlockPos.containing(newBox.minX, newBox.minY, newBox.minZ);
        BlockPos max = BlockPos.containing(newBox.maxX, newBox.maxY, newBox.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos, context);
            if (shape.isEmpty() || mob.canPassThrough(pos, state, shape)) {
                continue;
            }
            VoxelShape placed = shape.move(pos);
            if (Shapes.joinIsNotEmpty(placed, newShape, BooleanOp.AND) && !Shapes.joinIsNotEmpty(placed, oldShape, BooleanOp.AND)) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }
}
