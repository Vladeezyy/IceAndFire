package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemBlindfold extends IafArmorItem implements IafArmorTextureItem {

    public ItemBlindfold() {
        super(IafItemRegistry.BLINDFOLD_ARMOR_MATERIAL, ArmorType.HELMET);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot);
        if (!(entity instanceof Player player) || player.getItemBySlot(this.getEquipmentSlot()) != stack) {
            return;
        }
        
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 50, 0, false, false));
    }

    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        return Identifier.parse("iceandfire:textures/models/armor/blindfold_layer_1.png");
    }
}
