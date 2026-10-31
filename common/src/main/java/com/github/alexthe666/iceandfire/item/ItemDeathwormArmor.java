package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.resources.Identifier;
import com.github.alexthe666.iceandfire.client.model.armor.ModelDeathWormArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ItemDeathwormArmor extends IafArmorItem implements IafArmorTextureItem {

    public ItemDeathwormArmor(IafArmorMaterial material, ArmorType slot) {
        super(material, slot);
    }




    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        if (this.getMaterial() == IafItemRegistry.DEATHWORM_2_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/armor_deathworm_red" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else if (this.getMaterial() == IafItemRegistry.DEATHWORM_1_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/armor_deathworm_white" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else {
            return Identifier.parse("iceandfire:textures/models/armor/armor_deathworm_yellow" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        }
    }
}
