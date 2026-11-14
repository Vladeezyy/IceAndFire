package dev.vladeezyy.iceandfire.fabric.mixin;

import com.github.alexthe666.iceandfire.item.IafStackStatsItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Durability and attributes that depend on the config (dragonsteel), as NeoForge asks the item for them */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @Inject(method = "getMaxDamage", at = @At("HEAD"), cancellable = true)
    private void iceandfire$maxDamage(CallbackInfoReturnable<Integer> cir) {
        if (this.getItem() instanceof IafStackStatsItem item) {
            cir.setReturnValue(item.getMaxDamage((ItemStack) (Object) this));
        }
    }

    // Both overloads by descriptor: the name alone took only one of them. WrapOperation, not Redirect: two redirects of
    // one call (another mod) fail the game at start
    @WrapOperation(method = {
        "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlotGroup;Ljava/util/function/BiConsumer;)V",
        "forEachModifier(Lnet/minecraft/world/entity/EquipmentSlot;Ljava/util/function/BiConsumer;)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers()Lnet/minecraft/world/item/component/ItemAttributeModifiers;"))
    private ItemAttributeModifiers iceandfire$modifiers(Item item, Operation<ItemAttributeModifiers> original) {
        return item instanceof IafStackStatsItem stats ? stats.getDefaultAttributeModifiers((ItemStack) (Object) this) : original.call(item);
    }
}
