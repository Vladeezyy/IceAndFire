package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * Armor with its own texture: the signature of NeoForge's IItemExtension#getArmorTexture, so NeoForge calls it as before;
 * the Fabric armor renderer calls it through this interface.
 */
public interface IafArmorTextureItem {
    ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel);
}
