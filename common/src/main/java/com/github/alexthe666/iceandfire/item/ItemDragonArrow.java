package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.util.EnchantmentLevels;
import org.jetbrains.annotations.Nullable;
import com.github.alexthe666.iceandfire.entity.EntityDragonArrow;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemDragonArrow extends ArrowItem {
    public ItemDragonArrow() {
        super(new Properties());
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack arrow, @NotNull LivingEntity shooter, @Nullable ItemStack weapon) {
        return new EntityDragonArrow(IafEntityRegistry.DRAGON_ARROW.get(), shooter, level);
    }

    // NeoForge extension method (no vanilla override)
    public boolean isInfinite(@NotNull final ItemStack arrow, @NotNull final ItemStack bow, @NotNull final LivingEntity player) {
        // Technically this would always return false - it's more a compat layer for Apotheosis' Endless Quiver enchantment
        boolean isInfinite = false;

        if (!isInfinite) {
            isInfinite = EnchantmentLevels.has(player.level(), Enchantments.INFINITY, bow) && getClass() == ItemDragonArrow.class;
        }

        return isInfinite;
    }
}
