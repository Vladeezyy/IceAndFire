package com.github.alexthe666.iceandfire.datagen.tags;

import static com.github.alexthe666.iceandfire.datagen.tags.IafEntityTags.*;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class IafEntityTagsProvider extends EntityTypeTagsProvider {

    public IafEntityTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper existingFileHelper) {
        super(output, provider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(IMMUNE_TO_GORGON_STONE)
                .addTag(Tags.EntityTypes.BOSSES)
                .add(EntityType.WARDEN);
    }

}
