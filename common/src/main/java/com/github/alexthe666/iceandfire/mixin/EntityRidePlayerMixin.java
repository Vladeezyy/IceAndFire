package com.github.alexthe666.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Since 1.21.11 the server seats nothing on a vehicle whose type is not saved (players), while the clients still seat a
 * baby dragon on its owner's shoulder: the dragon stayed on the ground on the server. Players eject their passengers
 * when they leave (ServerPlayer.disconnect), so the dragon is not lost with them.
 */
@Mixin(Entity.class)
public abstract class EntityRidePlayerMixin {
    @WrapOperation(method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z"))
    private boolean iceandfire$dragonOnShoulder(EntityType<?> type, Operation<Boolean> original, @Local(argsOnly = true) Entity vehicle) {
        return original.call(type) || (Object) this instanceof EntityDragonBase && vehicle instanceof Player;
    }
}
