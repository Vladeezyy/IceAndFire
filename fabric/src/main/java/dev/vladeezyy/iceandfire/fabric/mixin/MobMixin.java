package dev.vladeezyy.iceandfire.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.vladeezyy.iceandfire.bus.FinalizeSpawnEvent;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.LivingChangeTargetEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** NeoForge's mob events: target change and the first spawn of a mob */
@Mixin(Mob.class)
public abstract class MobMixin {
    @WrapMethod(method = "setTarget")
    private void iceandfire$setTarget(LivingEntity target, Operation<Void> original) {
        LivingChangeTargetEvent event = new LivingChangeTargetEvent((Mob) (Object) this, target);
        if (!IafBus.post(event)) {
            original.call(event.getNewAboutToBeSetTarget());
        }
    }

    @Inject(method = "finalizeSpawn", at = @At("HEAD"))
    private void iceandfire$finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnType, SpawnGroupData data, CallbackInfoReturnable<SpawnGroupData> cir) {
        IafBus.post(new FinalizeSpawnEvent((Mob) (Object) this));
    }
}
