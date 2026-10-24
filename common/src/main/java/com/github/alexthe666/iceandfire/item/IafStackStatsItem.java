package com.github.alexthe666.iceandfire.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * Durability and attributes from the config (dragonsteel): the signatures of NeoForge's IItemExtension, which calls
 * them as before; Fabric calls them through this interface from its ItemStack mixin.
 */
public interface IafStackStatsItem {
    int getMaxDamage(ItemStack stack);

    ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack);
}
