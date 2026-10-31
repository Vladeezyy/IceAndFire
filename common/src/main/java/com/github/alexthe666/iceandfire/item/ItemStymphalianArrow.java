package com.github.alexthe666.iceandfire.item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.entity.EntityStymphalianArrow;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemStymphalianArrow extends ArrowItem {

    public ItemStymphalianArrow() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/);
    }

    @Override
    public @NotNull AbstractArrow createArrow(@NotNull Level worldIn, @NotNull ItemStack stack, @NotNull LivingEntity shooter, @Nullable ItemStack weapon) {
        return new EntityStymphalianArrow(IafEntityRegistry.STYMPHALIAN_ARROW.get(), worldIn, shooter);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.stymphalian_arrow.desc").withStyle(ChatFormatting.GRAY));
    }

}
