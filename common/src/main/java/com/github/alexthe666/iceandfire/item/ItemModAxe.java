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

public class ItemModAxe extends AxeItem implements DragonSteelOverrides<ItemModAxe>, IafStackStatsItem, IafTieredItem {

    private final CustomToolMaterial tier;

    private ItemAttributeModifiers dragonsteelModifiers;
    private double dragonsteelDamage;

    public ItemModAxe(CustomToolMaterial toolmaterial) {
        super(IafToolMaterials.vanilla(toolmaterial), 5.0F, -3.0F, dev.vladeezyy.iceandfire.platform.Registrar.itemProps());
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
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, IafConfig.dragonsteelBaseDamage - 1F + 5F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -3.0F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
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
        appendHoverText(tier, stack, worldIn, tooltip, flagIn);
    }

    // NeoForge extension method (no vanilla override)
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @Override
    public CustomToolMaterial getTier() {
        return tier;
    }
}
