package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class DragonSteelTier {

    // Tiers are ordered by tags since 1.20.5: dragonsteel mines everything netherite does
    public static final TagKey<Block> DRAGONSTEEL_TIER_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, ResourceLocation.parse("iceandfire:needs_dragonsteel"));
    public static final Tier DRAGONSTEEL_TIER_FIRE = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_FIRE_INGOT.get()));
    public static final Tier DRAGONSTEEL_TIER_ICE = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_ICE_INGOT.get()));
    public static final Tier DRAGONSTEEL_TIER_LIGHTNING = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_LIGHTNING_INGOT.get()));
    //FIXME: Probably shouldn't be called dragonsteel
    public static final Tier DRAGONSTEEL_TIER_DREAD_QUEEN = createTierWithRepairItem(() -> Ingredient.EMPTY);

    private static Tier createTierWithRepairItem(Supplier<Ingredient> ingredient) {
        // NeoForge's SimpleTier(incorrect blocks, uses 8000, speed 10, damage 21, enchantability 10, repair item)
        Supplier<Ingredient> repair = com.google.common.base.Suppliers.memoize(ingredient::get);
        return new Tier() {
            @Override
            public int getUses() {
                return 8000;
            }

            @Override
            public float getSpeed() {
                return 10;
            }

            @Override
            public float getAttackDamageBonus() {
                return 21;
            }

            @Override
            public TagKey<Block> getIncorrectBlocksForDrops() {
                return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;
            }

            @Override
            public int getEnchantmentValue() {
                return 10;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return repair.get();
            }
        };
    }

    public static boolean isDragonsteel(Tier tier) {
        return tier == DRAGONSTEEL_TIER_FIRE || tier == DRAGONSTEEL_TIER_ICE || tier == DRAGONSTEEL_TIER_LIGHTNING || tier == DRAGONSTEEL_TIER_DREAD_QUEEN;
    }
}
