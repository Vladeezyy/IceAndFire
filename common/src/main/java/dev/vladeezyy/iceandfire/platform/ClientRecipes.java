package dev.vladeezyy.iceandfire.platform;

import com.github.alexthe666.iceandfire.recipe.DragonForgeRecipe;

import java.util.List;

/**
 * 26.x clients get no recipes: the server sends the dragon forge recipes (loader recipe sync) and the loader stores
 * them here, for the forge screen's slot checks and JEI.
 */
public final class ClientRecipes {
    private static volatile List<DragonForgeRecipe> dragonForge = List.of();

    private ClientRecipes() {}

    public static List<DragonForgeRecipe> dragonForge() {
        return dragonForge;
    }

    public static void setDragonForge(List<DragonForgeRecipe> recipes) {
        dragonForge = List.copyOf(recipes);
    }
}
