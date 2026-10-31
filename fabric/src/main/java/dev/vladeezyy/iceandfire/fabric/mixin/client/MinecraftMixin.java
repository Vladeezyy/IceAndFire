package dev.vladeezyy.iceandfire.fabric.mixin.client;

import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.PlayerInteractEvent;
import dev.vladeezyy.iceandfire.bus.ScreenEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's PlayerInteractEvent.LeftClickEmpty (26.2: the screens are opened by Gui, see GuiMixin) */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    public LocalPlayer player;

    @Inject(method = "startAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;resetAttackStrengthTicker()V"))
    private void iceandfire$leftClickEmpty(CallbackInfoReturnable<Boolean> cir) {
        IafBus.post(new PlayerInteractEvent.LeftClickEmpty(this.player));
    }
}
