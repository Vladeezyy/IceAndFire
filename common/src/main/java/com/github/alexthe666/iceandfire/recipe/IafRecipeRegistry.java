package com.github.alexthe666.iceandfire.recipe;

import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.core.Position;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;
import dev.vladeezyy.iceandfire.bus.FMLCommonSetupEvent;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber(modid = IceAndFire.MODID, bus = EventBusSubscriber.Bus.MOD)
public class IafRecipeRegistry {
    public static final Registrar<RecipeType<?>> RECIPE_TYPE = Registrar.create(Registries.RECIPE_TYPE, IceAndFire.MODID);
    public static final RegistryEntry<RecipeType<?>, RecipeType<DragonForgeRecipe>> DRAGON_FORGE_TYPE = RECIPE_TYPE.register("dragonforge", () -> dev.vladeezyy.iceandfire.platform.Registrar.recipeType(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "dragonforge")));

    @SubscribeEvent
    public static void preInit(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DispenserBlock.registerBehavior(IafItemRegistry.STYMPHALIAN_ARROW.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    EntityStymphalianArrow entityarrow = new EntityStymphalianArrow(
                        IafEntityRegistry.STYMPHALIAN_ARROW.get(), worldIn, position.x(), position.y(),
                        position.z());
                    entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return entityarrow;
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.AMPHITHERE_ARROW.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    EntityAmphithereArrow entityarrow = new EntityAmphithereArrow(IafEntityRegistry.AMPHITHERE_ARROW.get(),
                        worldIn, position.x(), position.y(), position.z());
                    entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return entityarrow;
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.SEA_SERPENT_ARROW.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    EntitySeaSerpentArrow entityarrow = new EntitySeaSerpentArrow(IafEntityRegistry.SEA_SERPENT_ARROW.get(),
                        worldIn, position.x(), position.y(), position.z());
                    entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return entityarrow;
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.DRAGONBONE_ARROW.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    EntityDragonArrow entityarrow = new EntityDragonArrow(IafEntityRegistry.DRAGON_ARROW.get(),
                        position.x(), position.y(), position.z(), worldIn);
                    entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return entityarrow;
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.HYDRA_ARROW.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    EntityHydraArrow entityarrow = new EntityHydraArrow(IafEntityRegistry.HYDRA_ARROW.get(), worldIn,
                        position.x(), position.y(), position.z());
                    entityarrow.pickup = AbstractArrow.Pickup.ALLOWED;
                    return entityarrow;
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.HIPPOGRYPH_EGG.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    return new EntityHippogryphEgg(IafEntityRegistry.HIPPOGRYPH_EGG.get(), worldIn, position.x(),
                        position.y(), position.z(), stackIn);
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.ROTTEN_EGG.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    return new EntityCockatriceEgg(IafEntityRegistry.COCKATRICE_EGG.get(), position.x(), position.y(),
                        position.z(), worldIn);
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.DEATHWORM_EGG.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    return new EntityDeathWormEgg(IafEntityRegistry.DEATH_WORM_EGG.get(), position.x(), position.y(),
                        position.z(), worldIn, false);
                }
            });
            DispenserBlock.registerBehavior(IafItemRegistry.DEATHWORM_EGG_GIGANTIC.get(), new IafProjectileDispenseBehavior() {
                /**
                 * Return the projectile entity spawned by this dispense behavior.
                 */
                @Override
                protected @NotNull Projectile getProjectile(@NotNull Level worldIn, @NotNull Position position, @NotNull ItemStack stackIn) {
                    return new EntityDeathWormEgg(IafEntityRegistry.DEATH_WORM_EGG.get(), position.x(), position.y(),
                        position.z(), worldIn, true);
                }
            });
        });
    }

    // 26.3: brewing recipes are data (data/iceandfire/recipe/brewing: water + shiny scales -> water breathing)
}
