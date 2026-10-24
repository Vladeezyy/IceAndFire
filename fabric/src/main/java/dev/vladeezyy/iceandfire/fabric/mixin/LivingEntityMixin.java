package dev.vladeezyy.iceandfire.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.vladeezyy.iceandfire.bus.EntityTickEvent;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.bus.LivingDropsEvent;
import dev.vladeezyy.iceandfire.bus.LivingEntityUseItemEvent;
import dev.vladeezyy.iceandfire.bus.LivingFallEvent;
import dev.vladeezyy.iceandfire.bus.LivingIncomingDamageEvent;
import dev.vladeezyy.iceandfire.fabric.DropCapture;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/** NeoForge's living entity events: tick, incoming damage, fall, item use tick and death drops */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    protected int useItemRemaining;

    @Shadow
    protected int lastHurtByPlayerTime;

    @Inject(method = "tick", at = @At("HEAD"))
    private void iceandfire$tick(CallbackInfo ci) {
        IafBus.post(new EntityTickEvent.Pre((LivingEntity) (Object) this));
    }

    @WrapMethod(method = "hurt")
    private boolean iceandfire$hurt(DamageSource source, float amount, Operation<Boolean> original) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level() instanceof ServerLevel && !self.isInvulnerableTo(source) && !self.isDeadOrDying()) {
            LivingIncomingDamageEvent event = new LivingIncomingDamageEvent(self, source, amount);
            if (IafBus.post(event)) {
                return false;
            }
            amount = event.getAmount();
        }
        return original.call(source, amount);
    }

    @WrapMethod(method = "causeFallDamage")
    private boolean iceandfire$fall(float distance, float multiplier, DamageSource source, Operation<Boolean> original) {
        LivingFallEvent event = new LivingFallEvent((LivingEntity) (Object) this, distance, multiplier);
        if (IafBus.post(event)) {
            return false;
        }
        return original.call(event.getDistance(), event.getDamageMultiplier(), source);
    }

    @Inject(method = "updateUsingItem", at = @At("HEAD"), cancellable = true)
    private void iceandfire$useTick(ItemStack stack, CallbackInfo ci) {
        LivingEntityUseItemEvent.Tick event = new LivingEntityUseItemEvent.Tick((LivingEntity) (Object) this, stack, this.useItemRemaining);
        if (IafBus.post(event)) {
            this.useItemRemaining = -1;
            ci.cancel();
            return;
        }
        this.useItemRemaining = event.getDuration();
        if (this.useItemRemaining <= 0) {
            ci.cancel();
        }
    }

    @Inject(method = "dropAllDeathLoot", at = @At("HEAD"))
    private void iceandfire$captureDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        DropCapture.start((LivingEntity) (Object) this);
    }

    @Inject(method = "dropAllDeathLoot", at = @At("TAIL"))
    private void iceandfire$dropDrops(ServerLevel level, DamageSource source, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        List<ItemEntity> drops = DropCapture.stop(self);
        if (drops != null && !IafBus.post(new LivingDropsEvent(self, source, drops, this.lastHurtByPlayerTime > 0))) {
            drops.forEach(level::addFreshEntity);
        }
    }
}
