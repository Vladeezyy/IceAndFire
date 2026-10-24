package com.github.alexthe666.iceandfire.item;

import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemBlindfold extends ArmorItem implements IafArmorTextureItem {

    public ItemBlindfold() {
        super(IafItemRegistry.BLINDFOLD_ARMOR_MATERIAL.holder(), Type.HELMET, new Item.Properties().durability(IafItemRegistry.BLINDFOLD_ARMOR_MATERIAL.getDurabilityForType(Type.HELMET)));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!(entity instanceof Player player) || player.getItemBySlot(this.getEquipmentSlot()) != stack) {
            return;
        }
        
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ResourceLocation.parse("iceandfire:textures/models/armor/blindfold_layer_1.png");
    }
}
