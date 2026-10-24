package com.github.alexthe666.iceandfire.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/**
 * Enchantments are data since 1.21: their level on a stack is read through the holder of the world's registry.
 */
public final class EnchantmentLevels {

    private EnchantmentLevels() {
    }

    public static int get(Level level, ResourceKey<Enchantment> enchantment, ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(enchantment)
                .map(stack::getEnchantmentLevel)
                .orElse(0);
    }

    public static boolean has(Level level, ResourceKey<Enchantment> enchantment, ItemStack stack) {
        return get(level, enchantment, stack) > 0;
    }
}
