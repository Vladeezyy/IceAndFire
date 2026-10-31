package dev.vladeezyy.iceandfire.fabric.mixin.client;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.ViewportEvent;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeCameraAngles, right after the camera is placed (roll is not supported) */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract float yRot();

    @Shadow
    public abstract float xRot();

    @Inject(method = "setup", at = @At("TAIL"))
    private void iceandfire$angles(net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles((Camera) (Object) this, partialTick, this.yRot(), this.xRot(), 0);
        IafBus.post(event);
        if (event.getYaw() != this.yRot() || event.getPitch() != this.xRot()) {
            this.setRotation(event.getYaw(), event.getPitch());
        }
    }
}
