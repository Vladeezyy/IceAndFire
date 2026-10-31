package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;

import net.minecraft.resources.Identifier;
import com.github.alexthe666.iceandfire.client.model.armor.ModelCopperArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemCopperArmor extends IafArmorItem implements IafArmorTextureItem {

    public ItemCopperArmor(IafArmorMaterial material, ArmorType slot) {
        super(material, slot);
    }


    @Override
    @Nullable
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        return Identifier.parse("iceandfire:textures/models/armor/" + (slot == EquipmentSlot.LEGS ? "armor_copper_metal_layer_2" : "armor_copper_metal_layer_1") + ".png");
    }


}