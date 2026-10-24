package com.github.alexthe666.iceandfire.datagen;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static com.github.alexthe666.iceandfire.misc.IafDamageRegistry.GORGON_DMG_TYPE;

public class IafDamageTypeTagsProvider extends DamageTypeTagsProvider {

    public IafDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper existingFileHelper) {
        super(output, provider, IceAndFire.MODID, existingFileHelper);
    }

    @Override
    public void addTags(HolderLookup.Provider pProvider) {
        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(GORGON_DMG_TYPE);
        this.tag(DamageTypeTags.BYPASSES_EFFECTS).add(GORGON_DMG_TYPE);
    }
}
