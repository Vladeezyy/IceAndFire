package com.github.alexthe666.iceandfire.datagen.tags;


import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.datagen.IafPOITypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.PoiTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public class POITagGeneratorProvider extends PoiTypeTagsProvider {

    public POITagGeneratorProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).add(IafPOITypes.SCRIBE_POI);
    }


    @Override
    public String getName() {
        return "Ice and Fire POI Type Tags";
    }
}

