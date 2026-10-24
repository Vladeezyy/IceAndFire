package dev.vladeezyy.iceandfire.fabric.mixin.client;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.PlayerInteractEvent;
import dev.vladeezyy.iceandfire.bus.ScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's ScreenEvent.Opening and PlayerInteractEvent.LeftClickEmpty */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public Screen screen;

    @Shadow
    public LocalPlayer player;

    @Unique
    private boolean iceandfire$canceled;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen iceandfire$opening(Screen screen) {
        this.iceandfire$canceled = false;
        if (screen == null) {
            return null;
        }
        ScreenEvent.Opening event = new ScreenEvent.Opening(this.screen, screen);
        if (IafBus.post(event)) {
            this.iceandfire$canceled = true;
            return screen;
        }
        return event.getNewScreen();
    }

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void iceandfire$openingCanceled(Screen screen, CallbackInfo ci) {
        if (this.iceandfire$canceled) {
            this.iceandfire$canceled = false;
            ci.cancel();
        }
    }

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
    private void iceandfire$leftClickEmpty(CallbackInfoReturnable<Boolean> cir) {
        IafBus.post(new PlayerInteractEvent.LeftClickEmpty(this.player));
    }
}
