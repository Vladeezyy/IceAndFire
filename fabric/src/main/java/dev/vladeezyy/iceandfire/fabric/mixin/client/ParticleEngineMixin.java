package dev.vladeezyy.iceandfire.fabric.mixin.client;

import com.github.alexthe666.iceandfire.client.particle.IafAppearanceParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/** NeoForge's RegisterParticleGroupsEvent: the mod's appearance particles (ghost, siren) get their group, drawn last */
@Mixin(ParticleEngine.class)
public abstract class ParticleEngineMixin {
    @Shadow
    @Final
    @Mutable
    private static List<ParticleRenderType> RENDER_ORDER;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void iceandfire$order(CallbackInfo ci) {
        List<ParticleRenderType> order = new ArrayList<>(RENDER_ORDER);
        order.add(IafAppearanceParticle.GROUP);
        RENDER_ORDER = List.copyOf(order);
    }

    @Inject(method = "createParticleGroup", at = @At("HEAD"), cancellable = true)
    private void iceandfire$group(ParticleRenderType type, CallbackInfoReturnable<ParticleGroup<?>> cir) {
        if (type == IafAppearanceParticle.GROUP) {
            cir.setReturnValue(new IafAppearanceParticle.Group((ParticleEngine) (Object) this));
        }
    }
}
