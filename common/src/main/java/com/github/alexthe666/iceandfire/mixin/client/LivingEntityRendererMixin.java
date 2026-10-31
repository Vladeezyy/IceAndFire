package com.github.alexthe666.iceandfire.mixin.client;

import com.github.alexthe666.iceandfire.client.render.IafBufferSource;
import com.github.alexthe666.iceandfire.client.render.IafEntityHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.RenderLivingEvent;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The mod's RenderLivingEvent on 26.x (1.21.1: NeoForge's and the Fabric mixin's event around LivingEntityRenderer.render):
 * the state remembers its entity, submit asks Pre (riders hidden on dragons) and gives Post the entity's pose (ice,
 * chains, the cockatrice scepter beam).
 */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void iceandfire$keepEntity(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        ((IafEntityHolder) state).iceandfire$set(entity, partialTicks);
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void iceandfire$pre(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        IafEntityHolder holder = (IafEntityHolder) state;
        if (holder.iceandfire$entity() instanceof LivingEntity entity) {
            IafBufferSource buffers = new IafBufferSource();
            // players: RenderPlayerEvent first, as 1.21.1's PlayerRenderer did before LivingEntityRenderer's event
            boolean canceled = entity instanceof net.minecraft.world.entity.player.Player player
                && IafBus.post(new dev.vladeezyy.iceandfire.bus.RenderPlayerEvent.Pre(player, holder.iceandfire$partialTick(), poseStack, buffers, state.lightCoords));
            canceled = canceled || IafBus.post(new RenderLivingEvent.Pre(entity, holder.iceandfire$partialTick(), poseStack, buffers, state.lightCoords));
            buffers.submit(collector);
            if (canceled) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("RETURN"))
    private void iceandfire$post(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        IafEntityHolder holder = (IafEntityHolder) state;
        if (holder.iceandfire$entity() instanceof LivingEntity entity) {
            IafBufferSource buffers = new IafBufferSource();
            IafBus.post(new RenderLivingEvent.Post(entity, holder.iceandfire$partialTick(), poseStack, buffers, state.lightCoords));
            buffers.submit(collector);
        }
    }
}
