package com.github.alexthe666.iceandfire.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class ItemGenericFood extends Item {
    private final int healAmount;
    private final float saturation;

    public ItemGenericFood(int amount, float saturation, boolean isWolfFood, boolean eatFast, boolean alwaysEdible) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().food(createFood(amount, saturation, isWolfFood, eatFast, alwaysEdible, null), consumable(eatFast, null)));
        this.healAmount = amount;
        this.saturation = saturation;
    }

    public ItemGenericFood(int amount, float saturation, boolean isWolfFood, boolean eatFast, boolean alwaysEdible, int stackSize) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().food(createFood(amount, saturation, isWolfFood, eatFast, alwaysEdible, null), consumable(eatFast, null)).stacksTo(stackSize));
        this.healAmount = amount;
        this.saturation = saturation;
    }

    public static final FoodProperties createFood(int amount, float saturation, boolean isWolfFood, boolean eatFast, boolean alwaysEdible, MobEffectInstance potion) {
        FoodProperties.Builder builder = new FoodProperties.Builder();
        builder.nutrition(amount);
        builder.saturationModifier(saturation);
        // Meat is the minecraft:meat item tag since 1.20.5; eating speed and effects are the consumable (below)
        if (alwaysEdible) {
            builder.alwaysEdible();
        }
        return builder.build();
    }

    /** 1.21.2+: how the food is eaten (fast: 0.8 s as 1.21.1's fast food, else 1.6 s) and its effect */
    public static net.minecraft.world.item.component.Consumable consumable(boolean eatFast, MobEffectInstance potion) {
        net.minecraft.world.item.component.Consumable.Builder builder = net.minecraft.world.item.component.Consumables.defaultFood().consumeSeconds(eatFast ? 0.8F : 1.6F);
        if (potion != null) {
            builder.onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(potion));
        }
        return builder.build();
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity LivingEntity) {
        this.onFoodEaten(stack, worldIn, LivingEntity);
        return super.finishUsingItem(stack, worldIn, LivingEntity);
    }

    public void onFoodEaten(ItemStack stack, Level worldIn, LivingEntity livingEntity) {
    }
}
