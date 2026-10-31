package com.github.alexthe666.iceandfire.world;

import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.HashMap;

public class IafFeatureBiomeModifier implements BiomeModifier {
    private static final net.neoforged.neoforge.registries.DeferredHolder<MapCodec<? extends BiomeModifier>, MapCodec<? extends BiomeModifier>> SERIALIZER = net.neoforged.neoforge.registries.DeferredHolder.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "iaf_features"));
    private final HolderSet<PlacedFeature> features;
    public final HashMap<String, Holder<PlacedFeature>> featureMap = new HashMap<>();
    public IafFeatureBiomeModifier(HolderSet<PlacedFeature> features) {
        this.features = features;
        this.features.forEach(feature -> featureMap.put(feature.unwrapKey().get().location().toString(), feature));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD) {
            IafWorldRegistry.addFeatures(biome, featureMap, (step, feature) -> builder.getGenerationSettings().getFeatures(step).add(feature));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static MapCodec<IafFeatureBiomeModifier> makeCodec() {
        return RecordCodecBuilder.mapCodec(config -> config.group(PlacedFeature.LIST_CODEC.fieldOf("features").forGetter((otherConfig) -> otherConfig.features)).apply(config, IafFeatureBiomeModifier::new));
    }
}