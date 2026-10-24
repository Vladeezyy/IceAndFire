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
        public static final TagKey<Biome> IS_MOUNTAIN = TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath("c", "is_mountain"));
    }
}
