package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

/** The common (c:) tags of NeoForge's {@code Tags} the game code uses, with the same names and ids (Fabric has them too) */
public final class Tags {
    private Tags() {
    }

    public static final class Blocks {
        public static final TagKey<Block> COBBLESTONES = tag("cobblestones");
        public static final TagKey<Block> GRAVELS = tag("gravels");
        public static final TagKey<Block> STONES = tag("stones");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static final class Items {
        public static final TagKey<Item> STONES = tag("stones");
        public static final TagKey<Item> STRINGS = tag("strings");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> IS_AQUATIC = tag("is_aquatic");
        public static final TagKey<Biome> IS_BADLANDS = tag("is_badlands");
        public static final TagKey<Biome> IS_BEACH = tag("is_beach");
        public static final TagKey<Biome> IS_COLD = tag("is_cold");
        public static final TagKey<Biome> IS_COLD_OVERWORLD = tag("is_cold/overworld");
        public static final TagKey<Biome> IS_DENSE_VEGETATION = tag("is_dense_vegetation");
        public static final TagKey<Biome> IS_DRY = tag("is_dry");
        public static final TagKey<Biome> IS_DRY_OVERWORLD = tag("is_dry/overworld");
        public static final TagKey<Biome> IS_FOREST = tag("is_forest");
        public static final TagKey<Biome> IS_HILL = tag("is_hill");
        public static final TagKey<Biome> IS_HOT = tag("is_hot");
        public static final TagKey<Biome> IS_HOT_OVERWORLD = tag("is_hot/overworld");
        public static final TagKey<Biome> IS_JUNGLE = tag("is_jungle");
        public static final TagKey<Biome> IS_MAGICAL = tag("is_magical");
        public static final TagKey<Biome> IS_MOUNTAIN = tag("is_mountain");
        public static final TagKey<Biome> IS_MOUNTAIN_PEAK = tag("is_mountain/peak");
        public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = tag("is_mountain/slope");
        public static final TagKey<Biome> IS_OCEAN = tag("is_ocean");
        public static final TagKey<Biome> IS_OVERWORLD = tag("is_overworld");
        public static final TagKey<Biome> IS_PLAINS = tag("is_plains");
        public static final TagKey<Biome> IS_RARE = tag("is_rare");
        public static final TagKey<Biome> IS_SANDY = tag("is_sandy");
        public static final TagKey<Biome> IS_SAVANNA = tag("is_savanna");
        public static final TagKey<Biome> IS_SNOWY = tag("is_snowy");
        public static final TagKey<Biome> IS_SPARSE_VEGETATION = tag("is_sparse_vegetation");
        public static final TagKey<Biome> IS_SWAMP = tag("is_swamp");
        public static final TagKey<Biome> IS_TAIGA = tag("is_taiga");
        public static final TagKey<Biome> IS_UNDERGROUND = tag("is_underground");
        public static final TagKey<Biome> IS_WET_OVERWORLD = tag("is_wet/overworld");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", name));
        }
    }
}
