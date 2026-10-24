package dev.vladeezyy.iceandfire.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.RenderPlayerEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderPlayerEvent.Pre */
@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    private void iceandfire$pre(AbstractClientPlayer player, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (IafBus.post(new RenderPlayerEvent.Pre(player, partialTick, poseStack, buffers, light))) {
            ci.cancel();
        }
    }
}
