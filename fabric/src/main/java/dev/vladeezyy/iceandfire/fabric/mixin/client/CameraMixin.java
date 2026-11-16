package dev.vladeezyy.iceandfire.fabric.mixin.client;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.ViewportEvent;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's ViewportEvent.ComputeCameraAngles, right after the camera is placed; roll as NeoForge's setRotation(yaw, pitch, roll) */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Shadow
    @Final
    private static Vector3f FORWARDS;
    @Shadow
    @Final
    private static Vector3f UP;
    @Shadow
    @Final
    private static Vector3f LEFT;
    @Shadow
    @Final
    private Quaternionf rotation;
    @Shadow
    @Final
    private Vector3f forwards;
    @Shadow
    @Final
    private Vector3f up;
    @Shadow
    @Final
    private Vector3f left;

    @Shadow
    protected abstract void setRotation(float yRot, float xRot);

    @Shadow
    public abstract float getYRot();

    @Shadow
    public abstract float getXRot();

    @Inject(method = "setup", at = @At("TAIL"))
    private void iceandfire$angles(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        ViewportEvent.ComputeCameraAngles event = new ViewportEvent.ComputeCameraAngles((Camera) (Object) this, partialTick, this.getYRot(), this.getXRot(), 0);
        IafBus.post(event);
        if (event.getYaw() != this.getYRot() || event.getPitch() != this.getXRot()) {
            this.setRotation(event.getYaw(), event.getPitch());
        }
        if (event.getRoll() != 0) {
            // GameRenderer builds the view matrix from rotation(), so the roll goes into it and the three vectors
            this.rotation.rotationYXZ((float) Math.PI - event.getYaw() * ((float) Math.PI / 180F), -event.getPitch() * ((float) Math.PI / 180F),
                -event.getRoll() * ((float) Math.PI / 180F));
            FORWARDS.rotate(this.rotation, this.forwards);
            UP.rotate(this.rotation, this.up);
            LEFT.rotate(this.rotation, this.left);
        }
    }
}
