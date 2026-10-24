package dev.vladeezyy.iceandfire.fabric.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.vladeezyy.iceandfire.platform.ClientPlatform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** NeoForge's RenderLivingEvent.Pre/Post around every living entity renderer */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void iceandfire$pre(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        if (ClientPlatform.INSTANCE.renderLivingPre(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTick, poseStack, buffers, light)) {
            ci.cancel();
        }
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("TAIL"))
    @SuppressWarnings("unchecked")
    private void iceandfire$post(T entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, CallbackInfo ci) {
        ClientPlatform.INSTANCE.renderLivingPost(entity, (LivingEntityRenderer<T, M>) (Object) this, partialTick, poseStack, buffers, light);
    }
}
