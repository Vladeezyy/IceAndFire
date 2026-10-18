package com.github.alexthe666.iceandfire.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import org.jetbrains.annotations.NotNull;

/**
 * What the dragon forge matches recipes against: the input and blood slots and the dragon type of the forge.
 */
public record DragonForgeInput(ItemStack input, ItemStack blood, String dragonType) implements RecipeInput {

    @Override
    public @NotNull ItemStack getItem(int index) {
        return switch (index) {
            case 0 -> input;
            case 1 -> blood;
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public int size() {
        return 2;
    }
}
