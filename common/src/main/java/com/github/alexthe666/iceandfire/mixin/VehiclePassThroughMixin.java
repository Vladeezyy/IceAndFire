package com.github.alexthe666.iceandfire.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.vladeezyy.rampart.server.entity.collision.ICustomCollisions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * A ridden mob that passes through some blocks (the death worm in sand) moves there on the client, but the server's
 * vehicle check saw those blocks as collisions and sent the mob back (rubber-banding, no log line). Here the blocks the
 * mob passes through do not count. Only runs for such mobs, in the vehicle move packet, over the blocks of the mob's box.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class VehiclePassThroughMixin {
    @WrapOperation(method = "handleMoveVehicle", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;noCollision(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Z"))
    private boolean iceandfire$passThrough(ServerLevel level, Entity entity, AABB box, Operation<Boolean> original) {
        if (!(entity instanceof ICustomCollisions mob)) {
            return original.call(level, entity, box);
        }
        VoxelShape boxShape = Shapes.create(box);
        CollisionContext context = CollisionContext.of(entity);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getCollisionShape(level, pos, context);
            if (!shape.isEmpty() && !mob.canPassThrough(pos, state, shape)
                && Shapes.joinIsNotEmpty(shape.move(pos.getX(), pos.getY(), pos.getZ()), boxShape, BooleanOp.AND)) {
                return false;
            }
        }
        return true;
    }
}
