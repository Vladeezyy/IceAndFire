package com.github.alexthe666.iceandfire.block;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.Item;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;


public class BlockSeaSerpentScales extends Block {
    ChatFormatting color;
    String name;

    public BlockSeaSerpentScales(String name, ChatFormatting color) {
        super(
            dev.vladeezyy.iceandfire.platform.Registrar.blockProps()
                .mapColor(MapColor.STONE)
                .strength(30F, 500F)
                .sound(SoundType.STONE)
                .requiresCorrectToolForDrops()
        );

        this.color = color;
        this.name = name;
    }

    // 26.x: blocks have no tooltips (kept for the item)
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("sea_serpent." + name).withStyle(color));
    }
}
