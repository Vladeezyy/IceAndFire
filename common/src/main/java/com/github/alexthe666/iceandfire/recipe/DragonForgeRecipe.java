package com.github.alexthe666.iceandfire.recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.PlacementInfo;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DragonForgeRecipe implements Recipe<DragonForgeInput> {
    private static final MapCodec<DragonForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(DragonForgeRecipe::getInput),
            Ingredient.CODEC.fieldOf("blood").forGetter(DragonForgeRecipe::getBlood),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DragonForgeRecipe::getResult),
            com.mojang.serialization.Codec.STRING.fieldOf("dragon_type").forGetter(DragonForgeRecipe::getDragonType),
            com.mojang.serialization.Codec.INT.fieldOf("cook_time").forGetter(DragonForgeRecipe::getCookTime)
    ).apply(instance, DragonForgeRecipe::new));
    private static final StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getInput,
            Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getBlood,
            ItemStack.STREAM_CODEC, DragonForgeRecipe::getResult,
            ByteBufCodecs.STRING_UTF8, DragonForgeRecipe::getDragonType,
            ByteBufCodecs.VAR_INT, DragonForgeRecipe::getCookTime,
            DragonForgeRecipe::new);
    public static final RecipeSerializer<DragonForgeRecipe> SERIALIZER = new RecipeSerializer<>() {
        @Override
        public @NotNull MapCodec<DragonForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    };

    private final Ingredient input;
    private final Ingredient blood;
    private final ItemStack result;
    private final String dragonType;
    private final int cookTime;

    public DragonForgeRecipe(Ingredient input, Ingredient blood, ItemStack result, String dragonType, int cookTime) {
        this.input = input;
        this.blood = blood;
        this.result = result;
        this.dragonType = dragonType;
        this.cookTime = cookTime;
    }

    public Ingredient getInput() {
        return input;
    }

    public Ingredient getBlood() {
        return blood;
    }

    public int getCookTime() {
        return cookTime;
    }

    public String getDragonType() {
        return dragonType;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public boolean matches(DragonForgeInput inv, @NotNull Level worldIn) {
        return this.input.test(inv.input()) && this.blood.test(inv.blood()) && this.dragonType.equals(inv.dragonType());
    }

    public boolean isValidInput(ItemStack stack) {
        return this.input.test(stack);
    }

    public boolean isValidBlood(ItemStack blood) {
        return this.blood.test(blood);
    }

    public @NotNull ItemStack getResultItem() {
        return result.copy();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull DragonForgeInput dragonforge, HolderLookup.@NotNull Provider registries) {
        return result.copy();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String group() {
        return "";
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public @NotNull RecipeSerializer<DragonForgeRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public @NotNull RecipeType<DragonForgeRecipe> getType() {
        return IafRecipeRegistry.DRAGON_FORGE_TYPE.get();
    }
}
