package com.github.alexthe666.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** The dragon's 1.21.1 override of handleRelativeFrictionAndCalculateMovement (private in 26.x): a ridden flight keeps its motion */
@Mixin(LivingEntity.class)
public abstract class LivingEntityFrictionMixin {
    @Inject(method = "handleRelativeFrictionAndCalculateMovement", at = @At("HEAD"), cancellable = true)
    private void iceandfire$dragonFlight(Vec3 input, float friction, CallbackInfoReturnable<Vec3> cir) {
        if ((Object) this instanceof EntityDragonBase dragon && dragon.skipsRelativeFriction()) {
            cir.setReturnValue(input);
        }
    }
}
