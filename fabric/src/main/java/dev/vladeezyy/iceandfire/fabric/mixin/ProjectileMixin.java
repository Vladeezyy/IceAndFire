package dev.vladeezyy.iceandfire.fabric.mixin;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.ProjectileImpactEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's ProjectileImpactEvent: a canceled impact neither hits nor deflects */
@Mixin(Projectile.class)
public abstract class ProjectileMixin {
    @Inject(method = "hitTargetOrDeflectSelf", at = @At("HEAD"), cancellable = true)
    private void iceandfire$impact(HitResult hit, CallbackInfoReturnable<ProjectileDeflection> cir) {
        if (hit.getType() != HitResult.Type.MISS && IafBus.post(new ProjectileImpactEvent((Projectile) (Object) this, hit))) {
            cir.setReturnValue(ProjectileDeflection.NONE);
        }
    }
}
