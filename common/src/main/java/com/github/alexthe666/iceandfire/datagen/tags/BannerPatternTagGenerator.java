package com.github.alexthe666.iceandfire.datagen.tags;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.recipe.IafBannerPatterns;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

/** The tags of the data provider BannerPatternTagGeneratorProvider (NeoForge module), for the game code of both loaders */
public final class BannerPatternTagGenerator {
    private BannerPatternTagGenerator() {
    }

    public static final TagKey<BannerPattern> FIRE_BANNER_PATTERN = create("pattern_item/fire");
    public static final TagKey<BannerPattern> ICE_BANNER_PATTERN = create("pattern_item/ice");
    public static final TagKey<BannerPattern> LIGHTNING_BANNER_PATTERN = create("pattern_item/lightning");
    public static final TagKey<BannerPattern> FIRE_HEAD_BANNER_PATTERN = create("pattern_item/fire_head");
    public static final TagKey<BannerPattern> ICE_HEAD_BANNER_PATTERN = create("pattern_item/ice_head");
    public static final TagKey<BannerPattern> LIGHTNING_HEAD_BANNER_PATTERN = create("pattern_item/lightning_head");
    public static final TagKey<BannerPattern> AMPHITHERE_BANNER_PATTERN = create("pattern_item/amphithere");
    public static final TagKey<BannerPattern> BIRD_BANNER_PATTERN = create("pattern_item/bird");
    public static final TagKey<BannerPattern> EYE_BANNER_PATTERN = create("pattern_item/eye");
    public static final TagKey<BannerPattern> FAE_BANNER_PATTERN = create("pattern_item/fae");
    public static final TagKey<BannerPattern> FEATHER_BANNER_PATTERN = create("pattern_item/feather");
    public static final TagKey<BannerPattern> GORGON_BANNER_PATTERN = create("pattern_item/gorgon");
    public static final TagKey<BannerPattern> HIPPOCAMPUS_BANNER_PATTERN = create("pattern_item/hippocampus");
    public static final TagKey<BannerPattern> HIPPOGRYPH_HEAD_BANNER_PATTERN = create("pattern_item/hippogryph_head");
    public static final TagKey<BannerPattern> MERMAID_BANNER_PATTERN = create("pattern_item/mermaid");
    public static final TagKey<BannerPattern> SEA_SERPENT_BANNER_PATTERN = create("pattern_item/sea_serpent");
    public static final TagKey<BannerPattern> TROLL_BANNER_PATTERN = create("pattern_item/troll");
    public static final TagKey<BannerPattern> WEEZER_BANNER_PATTERN = create("pattern_item/weezer");
    public static final TagKey<BannerPattern> DREAD_BANNER_PATTERN = create("pattern_item/dread");

    private static TagKey<BannerPattern> create(String name) {
        return TagKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }
}
