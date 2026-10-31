package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemModArmor extends IafArmorItem implements IafArmorTextureItem {

    public ItemModArmor(IafArmorMaterial material, ArmorType slot) {
        super(material, slot);
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        if (this == IafItemRegistry.EARPLUGS.get()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            if (calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) == 1) {
                return "item.iceandfire.air_pods";
            }
        }
        return super.getDescriptionId(stack);
    }

    @Override
    @Nullable
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        if (this.material == IafItemRegistry.MYRMEX_DESERT_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/" + (slot == EquipmentSlot.LEGS ? "myrmex_desert_layer_2" : "myrmex_desert_layer_1") + ".png");
        }
        if (this.material == IafItemRegistry.MYRMEX_JUNGLE_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/" + (slot == EquipmentSlot.LEGS ? "myrmex_jungle_layer_2" : "myrmex_jungle_layer_1") + ".png");
        }
        if (this.material == IafItemRegistry.SHEEP_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/" + (slot == EquipmentSlot.LEGS ? "sheep_disguise_layer_2" : "sheep_disguise_layer_1") + ".png");
        }
        if (this.material == IafItemRegistry.EARPLUGS_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/earplugs_layer_1.png");
        }
        return null;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (this == IafItemRegistry.EARPLUGS.get()) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            if (calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) == 1) {
                tooltip.accept(Component.translatable("item.iceandfire.air_pods.desc").withStyle(ChatFormatting.GREEN));
            }
        }
        super.appendHoverText(stack, worldIn, display, tooltip, flagIn);
    }
}
