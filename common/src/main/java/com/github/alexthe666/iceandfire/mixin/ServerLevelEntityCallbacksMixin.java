package com.github.alexthe666.iceandfire.mixin;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Dragons bound to a summoning crystal remember where they were unloaded and get summoned once loaded; multipart parts
 * know whether they tick (both loaders, server levels)
 */
@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
public class ServerLevelEntityCallbacksMixin {
    @Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void iceandfire$onTrackingStart(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityDragonBase dragon) {
            dragon.onAddedToServerLevel();
        } else if (entity instanceof EntityMutlipartPart part) {
            part.setTrackedByLevel(true);
        }
    }

    @Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
    private void iceandfire$onTrackingEnd(Entity entity, CallbackInfo ci) {
        if (entity instanceof EntityDragonBase dragon) {
            dragon.onRemovedFromServerLevel();
        } else if (entity instanceof EntityMutlipartPart part) {
            part.setTrackedByLevel(false);
        }
    }
}
