package com.github.alexthe666.iceandfire.item;
import net.minecraft.core.component.DataComponents;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.Item;
import com.github.alexthe666.iceandfire.IafConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemModHoe extends HoeItem implements DragonSteelOverrides<ItemModHoe>, IafStackStatsItem, IafTieredItem {

    private final CustomToolMaterial tier;

    private ItemAttributeModifiers dragonsteelModifiers;
    private double dragonsteelDamage;

    public ItemModHoe(CustomToolMaterial toolmaterial) {
        super(IafToolMaterials.vanilla(toolmaterial), 5, -3.0F, dev.vladeezyy.iceandfire.platform.Registrar.itemProps());
        this.tier = toolmaterial;
    }

    @Override
    @Deprecated
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        return isDragonsteel(getTier()) ? this.bakeDragonsteel() : this.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
    }

    @Override
    @Deprecated
    public ItemAttributeModifiers bakeDragonsteel() {
        if (dragonsteelModifiers == null || dragonsteelDamage != IafConfig.dragonsteelBaseDamage) {
            dragonsteelDamage = IafConfig.dragonsteelBaseDamage;
            dragonsteelModifiers = ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 1F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build();
        }
        return dragonsteelModifiers;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return isDragonsteel(getTier()) ? IafConfig.dragonsteelBaseDurability : getTier().getUses();
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        hurtEnemy(this, stack, target, attacker);
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, display, tooltip, flagIn);
        appendHoverText(getTier(), stack, worldIn, tooltip, flagIn);
    }

    @Override
    public CustomToolMaterial getTier() {
        return tier;
    }
}
