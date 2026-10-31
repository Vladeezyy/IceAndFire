package com.github.alexthe666.iceandfire.item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDragonScales extends Item {
    EnumDragonEgg type;

    public ItemDragonScales(EnumDragonEgg type) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/);
        this.type = type;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack iafStack) {
        return Component.translatable(this.iafDescriptionId());
    }

    private String iafDescriptionId() {
        return "item.iceandfire.dragonscales";
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("dragon." + type.toString().toLowerCase()).withStyle(type.color));
    }

}
