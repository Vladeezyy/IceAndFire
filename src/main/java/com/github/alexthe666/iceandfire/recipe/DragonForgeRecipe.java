package com.github.alexthe666.iceandfire.recipe;

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

    @Override
    public @NotNull ItemStack getResultItem(@NotNull HolderLookup.Provider registries) {
        return result;
    }

    public @NotNull ItemStack getResultItem() {
        return result;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull DragonForgeInput dragonforge, @NotNull HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return new ItemStack(IafBlockRegistry.DRAGONFORGE_FIRE_CORE.get());
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return IafRecipeSerializers.DRAGONFORGE_SERIALIZER.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return IafRecipeRegistry.DRAGON_FORGE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DragonForgeRecipe> {
        private static final MapCodec<DragonForgeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.fieldOf("input").forGetter(DragonForgeRecipe::getInput),
                Ingredient.CODEC_NONEMPTY.fieldOf("blood").forGetter(DragonForgeRecipe::getBlood),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DragonForgeRecipe::getResultItem),
                com.mojang.serialization.Codec.STRING.fieldOf("dragon_type").forGetter(DragonForgeRecipe::getDragonType),
                com.mojang.serialization.Codec.INT.fieldOf("cook_time").forGetter(DragonForgeRecipe::getCookTime)
        ).apply(instance, DragonForgeRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getInput,
                Ingredient.CONTENTS_STREAM_CODEC, DragonForgeRecipe::getBlood,
                ItemStack.STREAM_CODEC, DragonForgeRecipe::getResultItem,
                ByteBufCodecs.STRING_UTF8, DragonForgeRecipe::getDragonType,
                ByteBufCodecs.VAR_INT, DragonForgeRecipe::getCookTime,
                DragonForgeRecipe::new);

        @Override
        public @NotNull MapCodec<DragonForgeRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, DragonForgeRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

}
