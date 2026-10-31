package com.github.alexthe666.iceandfire.item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import com.github.alexthe666.iceandfire.client.model.armor.ModelSeaSerpentArmor;
import com.github.alexthe666.iceandfire.enums.EnumSeaSerpent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemSeaSerpentArmor extends IafArmorItem implements IafArmorTextureItem {

    public EnumSeaSerpent armor_type;

    public ItemSeaSerpentArmor(EnumSeaSerpent armorType, IafArmorMaterial material, ArmorType slot) {
        super(material, slot);
        this.armor_type = armorType;
    }

    @Override
    public @NotNull String getDescriptionId() {
        switch (this.type) {
            case HELMET:
                return "item.iceandfire.sea_serpent_helmet";
            case CHESTPLATE:
                return "item.iceandfire.sea_serpent_chestplate";
            case LEGGINGS:
                return "item.iceandfire.sea_serpent_leggings";
            case BOOTS:
                return "item.iceandfire.sea_serpent_boots";
        }
        return "item.iceandfire.sea_serpent_helmet";
    }


    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        return Identifier.parse("iceandfire:textures/models/armor/armor_tide_" + armor_type.resourceName + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, world, entity, slot, (slot == EquipmentSlot.MAINHAND));
        if (!(entity instanceof Player player) || player.getItemBySlot(this.getEquipmentSlot()) != stack) {
            return;
        }
        
        player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 50, 0, false, false));
        if (player.isInWaterOrRain()) {
            int headMod = player.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ItemSeaSerpentArmor ? 1 : 0;
            int chestMod = player.getItemBySlot(EquipmentSlot.CHEST).getItem() instanceof ItemSeaSerpentArmor ? 1 : 0;
            int legMod = player.getItemBySlot(EquipmentSlot.LEGS).getItem() instanceof ItemSeaSerpentArmor ? 1 : 0;
            int footMod = player.getItemBySlot(EquipmentSlot.FEET).getItem() instanceof ItemSeaSerpentArmor ? 1 : 0;
            player.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 50, headMod + chestMod + legMod + footMod - 1, false, false));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {

        tooltip.accept(Component.translatable("sea_serpent." + armor_type.resourceName).withStyle(armor_type.color));
        tooltip.accept(Component.translatable("item.iceandfire.sea_serpent_armor.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.sea_serpent_armor.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
