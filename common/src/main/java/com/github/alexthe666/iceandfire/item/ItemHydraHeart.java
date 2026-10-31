package com.github.alexthe666.iceandfire.item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHydraHeart extends Item {

    public ItemHydraHeart() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
    }

    // NeoForge extension method (no vanilla override)
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        // 26.x passes the equipment slot only: the hotbar index of the stack, as 1.21.1's itemSlot 0 - 8
        int itemSlot = -1;
        if (entity instanceof Player holder) {
            for (int i = 0; i < 9; i++) {
                if (holder.getInventory().getItem(i) == stack) {
                    itemSlot = i;
                    break;
                }
            }
        }
        if (entity instanceof Player && itemSlot >= 0 && itemSlot <= 8) {
            double healthPercentage = ((Player) entity).getHealth() / Math.max(1, ((Player) entity).getMaxHealth());
            if (healthPercentage < 1.0D) {
                int level = 0;
                if (healthPercentage < 0.25D) {
                    level = 3;
                } else if (healthPercentage < 0.5D) {
                    level = 2;
                } else if (healthPercentage < 0.75D) {
                    level = 1;
                }
                //Consider using EffectInstance.combine
                if (!((Player) entity).hasEffect(MobEffects.REGENERATION) || ((Player) entity).getEffect(MobEffects.REGENERATION).getAmplifier() < level)
                    ((Player) entity).addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, level, true, false));
            }
            //In hotbar
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hydra_heart.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hydra_heart.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
