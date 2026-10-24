package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.ResourceLocation;
import com.github.alexthe666.iceandfire.client.model.armor.ModelSilverArmor;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class ItemSilverArmor extends ArmorItem implements IafArmorTextureItem {

    public ItemSilverArmor(IafArmorMaterial material, ArmorItem.Type slot) {
        super(material.holder(), slot, new Item.Properties().durability(material.getDurabilityForType(slot))/*.tab(IceAndFire.TAB_ITEMS)*/);
    }


    @Override
    @Nullable
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ResourceLocation.parse("iceandfire:textures/models/armor/" + (slot == EquipmentSlot.LEGS ? "armor_silver_metal_layer_2" : "armor_silver_metal_layer_1") + ".png");
    }


}