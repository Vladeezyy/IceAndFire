package dev.vladeezyy.iceandfire.fabric.mixin;

import com.github.alexthe666.iceandfire.item.ItemDragonArrow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** NeoForge's ArrowItem#isInfinite: dragonbone arrows are not used up with Infinity (vanilla's Infinity takes only plain arrows) */
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
    @WrapOperation(method = "useAmmo", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;processAmmoUse(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;I)I"))
    private static int iceandfire$infiniteArrow(ServerLevel level, ItemStack weapon, ItemStack ammo, int count, Operation<Integer> original,
                                                @Local(argsOnly = true) LivingEntity shooter) {
        if (ammo.getItem() instanceof ItemDragonArrow arrow && arrow.isInfinite(ammo, weapon, shooter)) {
            return 0;
        }
        return original.call(level, weapon, ammo, count);
    }
}
