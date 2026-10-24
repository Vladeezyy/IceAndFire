package dev.vladeezyy.iceandfire.fabric.mixin;

import dev.vladeezyy.iceandfire.bus.EntityMountEvent;
import dev.vladeezyy.iceandfire.bus.EntityStruckByLightningEvent;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.fabric.DropCapture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's entity events: struck by lightning, mount and dismount, and the death drops captured for LivingDropsEvent */
@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow
    public abstract Entity getVehicle();

    @Inject(method = "thunderHit", at = @At("HEAD"), cancellable = true)
    private void iceandfire$thunderHit(ServerLevel level, LightningBolt lightning, CallbackInfo ci) {
        if (IafBus.post(new EntityStruckByLightningEvent((Entity) (Object) this, lightning))) {
            ci.cancel();
        }
    }

    @Inject(method = "startRiding(Lnet/minecraft/world/entity/Entity;Z)Z", at = @At("RETURN"))
    private void iceandfire$mount(Entity vehicle, boolean force, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            IafBus.post(new EntityMountEvent((Entity) (Object) this, vehicle, true));
        }
    }

    @Inject(method = "removeVehicle", at = @At("HEAD"))
    private void iceandfire$dismount(CallbackInfo ci) {
        Entity vehicle = this.getVehicle();
        if (vehicle != null) {
            IafBus.post(new EntityMountEvent((Entity) (Object) this, vehicle, false));
        }
    }

    @Redirect(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean iceandfire$captureDrop(Level level, Entity drop) {
        return DropCapture.capture((Entity) (Object) this, drop) || level.addFreshEntity(drop);
    }
}
