package com.github.alexthe666.iceandfire.datagen.tags;

import static com.github.alexthe666.iceandfire.datagen.tags.BannerPatternTagGenerator.*;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.recipe.IafBannerPatterns;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class BannerPatternTagGeneratorProvider extends TagsProvider<BannerPattern> {

    public BannerPatternTagGeneratorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.BANNER_PATTERN, provider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(FIRE_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_FIRE);
        this.tag(ICE_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_ICE);
        this.tag(LIGHTNING_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_LIGHTNING);
        this.tag(FIRE_HEAD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_FIRE_HEAD);
        this.tag(ICE_HEAD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_ICE_HEAD);
        this.tag(LIGHTNING_HEAD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_LIGHTNING_HEAD);
        this.tag(AMPHITHERE_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_AMPHITHERE);
        this.tag(BIRD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_BIRD);
        this.tag(EYE_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_EYE);
        this.tag(FAE_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_FAE);
        this.tag(FEATHER_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_FEATHER);
        this.tag(GORGON_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_GORGON);
        this.tag(HIPPOCAMPUS_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_HIPPOCAMPUS);
        this.tag(HIPPOGRYPH_HEAD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_HIPPOGRYPH_HEAD);
        this.tag(MERMAID_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_MERMAID);
        this.tag(SEA_SERPENT_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_SEA_SERPENT);
        this.tag(TROLL_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_TROLL);
        this.tag(WEEZER_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_WEEZER);
        this.tag(DREAD_BANNER_PATTERN).add(IafBannerPatterns.PATTERN_DREAD);
    }


    @Override
    public String getName() {
        return "Ice and Fire Banner Pattern Tags";
    }
}

