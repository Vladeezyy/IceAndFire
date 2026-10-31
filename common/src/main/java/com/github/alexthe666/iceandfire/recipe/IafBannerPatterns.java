package com.github.alexthe666.iceandfire.recipe;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BannerPattern;

/**
 * Banner patterns are data since 1.20.5: data/iceandfire/banner_pattern/*.json. These are their keys.
 */
public class IafBannerPatterns {
    public static final ResourceKey<BannerPattern> PATTERN_FIRE = key("fire");
    public static final ResourceKey<BannerPattern> PATTERN_ICE = key("ice");
    public static final ResourceKey<BannerPattern> PATTERN_LIGHTNING = key("lightning");
    public static final ResourceKey<BannerPattern> PATTERN_FIRE_HEAD = key("fire_head");
    public static final ResourceKey<BannerPattern> PATTERN_ICE_HEAD = key("ice_head");
    public static final ResourceKey<BannerPattern> PATTERN_LIGHTNING_HEAD = key("lightning_head");
    public static final ResourceKey<BannerPattern> PATTERN_AMPHITHERE = key("amphithere");
    public static final ResourceKey<BannerPattern> PATTERN_BIRD = key("bird");
    public static final ResourceKey<BannerPattern> PATTERN_EYE = key("eye");
    public static final ResourceKey<BannerPattern> PATTERN_FAE = key("fae");
    public static final ResourceKey<BannerPattern> PATTERN_FEATHER = key("feather");
    public static final ResourceKey<BannerPattern> PATTERN_GORGON = key("gorgon");
    public static final ResourceKey<BannerPattern> PATTERN_HIPPOCAMPUS = key("hippocampus");
    public static final ResourceKey<BannerPattern> PATTERN_HIPPOGRYPH_HEAD = key("hippogryph_head");
    public static final ResourceKey<BannerPattern> PATTERN_MERMAID = key("mermaid");
    public static final ResourceKey<BannerPattern> PATTERN_SEA_SERPENT = key("sea_serpent");
    public static final ResourceKey<BannerPattern> PATTERN_TROLL = key("troll");
    public static final ResourceKey<BannerPattern> PATTERN_WEEZER = key("weezer");
    public static final ResourceKey<BannerPattern> PATTERN_DREAD = key("dread");

    private static ResourceKey<BannerPattern> key(String name) {
        return ResourceKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }
}
