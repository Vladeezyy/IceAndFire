package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.ResourceLocation;
import com.github.alexthe666.iceandfire.client.model.armor.ModelDeathWormArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ItemDeathwormArmor extends ArmorItem {

    public ItemDeathwormArmor(IafArmorMaterial material, ArmorItem.Type slot) {
        super(material.holder(), slot, new Item.Properties().durability(material.getDurabilityForType(slot))/*.tab(IceAndFire.TAB_ITEMS)*/);
    }




    // NeoForge extension method (no vanilla override)
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (this.getMaterial() == IafItemRegistry.DEATHWORM_2_ARMOR_MATERIAL.holder()) {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_deathworm_red" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else if (this.getMaterial() == IafItemRegistry.DEATHWORM_1_ARMOR_MATERIAL.holder()) {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_deathworm_white" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_deathworm_yellow" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        }
    }
}
