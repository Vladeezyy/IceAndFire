package com.github.alexthe666.iceandfire.recipe;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.world.item.crafting.RecipeSerializer;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

public class IafRecipeSerializers {
    public static final Registrar<RecipeSerializer<?>> SERIALIZERS = Registrar.create(BuiltInRegistries.RECIPE_SERIALIZER, IceAndFire.MODID);
    public static final RegistryEntry<RecipeSerializer<?>, RecipeSerializer<?>> DRAGONFORGE_SERIALIZER = SERIALIZERS.register("dragonforge", DragonForgeRecipe.Serializer::new);

}
