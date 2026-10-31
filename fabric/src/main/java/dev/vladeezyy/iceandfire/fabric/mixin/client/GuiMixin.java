package dev.vladeezyy.iceandfire.fabric.mixin.client;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.ScreenEvent;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.jetbrains.annotations.Nullable;

/** NeoForge's ScreenEvent.Opening (26.2: Minecraft.setScreen moved to Minecraft.gui) */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow
    public abstract @Nullable Screen screen();

    @Unique
    private boolean iceandfire$canceled;

    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen iceandfire$opening(Screen screen) {
        this.iceandfire$canceled = false;
        if (screen == null) {
            return null;
        }
        ScreenEvent.Opening event = new ScreenEvent.Opening(this.screen(), screen);
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
}
