package dev.vladeezyy.iceandfire.fabric.mixin;

import com.github.alexthe666.iceandfire.item.IafStackStatsItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
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

    @Redirect(method = "forEachModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers()Lnet/minecraft/world/item/component/ItemAttributeModifiers;"))
    private ItemAttributeModifiers iceandfire$modifiers(Item item) {
        return item instanceof IafStackStatsItem stats ? stats.getDefaultAttributeModifiers((ItemStack) (Object) this) : item.getDefaultAttributeModifiers();
    }
}
