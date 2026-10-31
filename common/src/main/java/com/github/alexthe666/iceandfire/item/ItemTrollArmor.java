package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import com.github.alexthe666.iceandfire.client.model.armor.ModelTrollArmor;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class ItemTrollArmor extends IafArmorItem implements IafArmorTextureItem {

    public EnumTroll troll;

    public ItemTrollArmor(EnumTroll troll, IafArmorMaterial material, ArmorType slot) {
        super(material, slot);
        this.troll = troll;
    }

    public static String getName(EnumTroll troll, EquipmentSlot slot) {
        return "%s_troll_leather_%s".formatted(troll.name().toLowerCase(Locale.ROOT), getArmorPart(slot));
    }


    private static String getArmorPart(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helmet";
            case CHEST -> "chestplate";
            case LEGS -> "leggings";
            case FEET -> "boots";
            default -> "";
        };
    }


    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        return Identifier.parse("iceandfire:textures/models/armor/armor_troll_" + troll.name().toLowerCase(Locale.ROOT) + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.troll_leather_armor_" + getArmorPart(type.getSlot()) + ".desc").withStyle(ChatFormatting.GREEN));
    }
}
