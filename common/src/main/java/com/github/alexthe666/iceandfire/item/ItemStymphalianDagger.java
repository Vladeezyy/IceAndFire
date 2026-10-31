package com.github.alexthe666.iceandfire.item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStymphalianDagger extends SwordItem {

    public ItemStymphalianDagger() {
        super(IafItemRegistry.STYMHALIAN_SWORD_TOOL_MATERIAL, dev.vladeezyy.iceandfire.platform.Registrar.itemProps().attributes(SwordItem.createAttributes(IafItemRegistry.STYMHALIAN_SWORD_TOOL_MATERIAL, 3, -1.0F))/*.tab(IceAndFire.TAB_ITEMS)*/);
    }


    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity targetEntity, @NotNull LivingEntity attacker) {
        super.hurtEnemy(stack, targetEntity, attacker);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.stymphalian_bird_dagger.desc_0").withStyle(ChatFormatting.GRAY));
    }
}