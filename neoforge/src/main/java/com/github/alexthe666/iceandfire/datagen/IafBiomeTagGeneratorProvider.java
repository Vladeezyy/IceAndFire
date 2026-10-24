package com.github.alexthe666.iceandfire.datagen;

import static com.github.alexthe666.iceandfire.datagen.IafBiomeTagGenerator.*;

import net.minecraft.core.registries.Registries;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IafBiomeTagGeneratorProvider extends BiomeTagsProvider {


    public IafBiomeTagGeneratorProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(HAS_GRAVEYARD).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_MAUSOLEUM).addTag(BiomeTags.IS_OVERWORLD);
        tag(HAS_GORGON_TEMPLE).addTag(BiomeTags.IS_OVERWORLD);
    }

    @Override
    public String getName() {
        return "Ice and Fire Biome Tags";
    }
}
