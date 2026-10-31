package com.github.alexthe666.iceandfire.item;

import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class DragonSteelTier {

    // Tiers are ordered by tags since 1.20.5: dragonsteel mines everything netherite does
    public static final TagKey<Block> DRAGONSTEEL_TIER_TAG = TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.parse("iceandfire:needs_dragonsteel"));
    public static final CustomToolMaterial DRAGONSTEEL_TIER_FIRE = create("dragonsteel_fire");
    public static final CustomToolMaterial DRAGONSTEEL_TIER_ICE = create("dragonsteel_ice");
    public static final CustomToolMaterial DRAGONSTEEL_TIER_LIGHTNING = create("dragonsteel_lightning");
    //FIXME: Probably shouldn't be called dragonsteel
    public static final CustomToolMaterial DRAGONSTEEL_TIER_DREAD_QUEEN = create("dread_queen");

    private static CustomToolMaterial create(String name) {
        // NeoForge's SimpleTier(incorrect blocks, uses 8000, speed 10, damage 21, enchantability 10, repair item); the
        // repair items are the tag data/iceandfire/tags/item/repairs/<name>.json (the dread queen's is empty)
        return new CustomToolMaterial(name, 4, 8000, 21, 10, 10, BlockTags.INCORRECT_FOR_NETHERITE_TOOL, IafItemRegistry.repairs(name));
    }

    public static boolean isDragonsteel(CustomToolMaterial tier) {
        return tier == DRAGONSTEEL_TIER_FIRE || tier == DRAGONSTEEL_TIER_ICE || tier == DRAGONSTEEL_TIER_LIGHTNING || tier == DRAGONSTEEL_TIER_DREAD_QUEEN;
    }
}
