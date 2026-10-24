package com.github.alexthe666.iceandfire.datagen;

import net.minecraft.core.HolderLookup;
import java.util.concurrent.CompletableFuture;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

import java.util.Optional;

import static com.github.alexthe666.iceandfire.client.IafClientSetup.*;

public class AtlasGenerator extends SpriteSourceProvider {

    public AtlasGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ExistingFileHelper helper) {
        super(output, lookupProvider, IceAndFire.MODID, helper);
    }
    @Override
    protected void gather() {
        this.atlas(CHESTS_ATLAS).addSource(new SingleFile(GHOST_CHEST_LOCATION, Optional.empty()));
        this.atlas(CHESTS_ATLAS).addSource(new SingleFile(GHOST_CHEST_LEFT_LOCATION, Optional.empty()));
        this.atlas(CHESTS_ATLAS).addSource(new SingleFile(GHOST_CHEST_RIGHT_LOCATION, Optional.empty()));

        //this.atlas(SHIELD_PATTERNS_ATLAS).addSource(new SingleFile(TwilightForestMod.prefix("model/knightmetal_shield"), Optional.empty()));


    }
}
