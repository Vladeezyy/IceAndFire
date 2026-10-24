package com.github.alexthe666.iceandfire.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.SimpleTier;

import java.util.function.Supplier;

public class DragonSteelTier {

    // Tiers are ordered by tags since 1.20.5: dragonsteel mines everything netherite does
    public static final TagKey<Block> DRAGONSTEEL_TIER_TAG = BlockTags.create(ResourceLocation.parse("iceandfire:needs_dragonsteel"));
    public static final Tier DRAGONSTEEL_TIER_FIRE = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_FIRE_INGOT.get()));
    public static final Tier DRAGONSTEEL_TIER_ICE = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_ICE_INGOT.get()));
    public static final Tier DRAGONSTEEL_TIER_LIGHTNING = createTierWithRepairItem(() -> Ingredient.of(IafItemRegistry.DRAGONSTEEL_LIGHTNING_INGOT.get()));
    //FIXME: Probably shouldn't be called dragonsteel
    public static final Tier DRAGONSTEEL_TIER_DREAD_QUEEN = createTierWithRepairItem(() -> Ingredient.EMPTY);

    private static Tier createTierWithRepairItem(Supplier<Ingredient> ingredient) {
        return new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 8000, 10, 21, 10, ingredient);
    }

    public static boolean isDragonsteel(Tier tier) {
        return tier == DRAGONSTEEL_TIER_FIRE || tier == DRAGONSTEEL_TIER_ICE || tier == DRAGONSTEEL_TIER_LIGHTNING || tier == DRAGONSTEEL_TIER_DREAD_QUEEN;
    }
}
