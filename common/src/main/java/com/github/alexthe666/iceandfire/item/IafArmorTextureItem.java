package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Armor with its own texture for the slot (26.x: NeoForge calls it from the item's IClientItemExtensions#getArmorTexture,
 * the Fabric armor renderer directly).
 */
public interface IafArmorTextureItem {
    Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot);
}
