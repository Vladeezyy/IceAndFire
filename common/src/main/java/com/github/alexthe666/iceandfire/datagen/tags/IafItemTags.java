package com.github.alexthe666.iceandfire.datagen.tags;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.ItemMobSkull;
import com.github.alexthe666.iceandfire.item.ItemSeaSerpentScales;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import java.util.concurrent.CompletableFuture;

/** The tags of the data provider IafItemTagsProvider (NeoForge module), for the game code of both loaders */
public final class IafItemTags {
    private IafItemTags() {
    }

    // the c: tag folders (NeoForge's ItemTagsProvider constants before the split)
    public static final String ORES_PATH = "ores";
    public static final String STORAGE_BLOCK_PATH = "storage_blocks";
    public static final String GEMS = "gems";
    public static final String INGOTS_PATH = "ingots";
    public static final String NUGGETS_PATH = "nuggets";
    public static final String BONES_PATH = "bones";

    public static TagKey<Item> CHARRED_BLOCKS = createKey("charred_blocks");
    public static TagKey<Item> FROZEN_BLOCKS = createKey("frozen_blocks");
    public static TagKey<Item> CRACKLED_BLOCKS = createKey("crackled_blocks");
    public static TagKey<Item> DRAGON_SKULLS = createKey("dragon_skulls");
    public static TagKey<Item> MOB_SKULLS = createKey("mob_skulls");
    public static TagKey<Item> SCALES_DRAGON_FIRE = createKey("scales/dragon/fire");
    public static TagKey<Item> SCALES_DRAGON_ICE = createKey("scales/dragon/ice");
    public static TagKey<Item> SCALES_DRAGON_LIGHTNING = createKey("scales/dragon/lightning");
    public static TagKey<Item> SCALES_SEA_SERPENT = createKey("scales/sea_serpent");
    public static TagKey<Item> DRAGON_FOOD_MEAT = createKey("dragon_food_meat");
    public static TagKey<Item> STORAGE_BLOCKS_SCALES_DRAGON_FIRE = createForgeKey(STORAGE_BLOCK_PATH + "/scales/dragon/fire");
    public static TagKey<Item> STORAGE_BLOCKS_SCALES_DRAGON_ICE = createForgeKey(STORAGE_BLOCK_PATH + "/scales/dragon/ice");
    public static TagKey<Item> STORAGE_BLOCKS_SCALES_DRAGON_LIGHTNING = createForgeKey(STORAGE_BLOCK_PATH + "/scales/dragon/lightning");
    public static TagKey<Item> STORAGE_BLOCKS_SILVER = createForgeKey(STORAGE_BLOCK_PATH + "/silver");
    public static TagKey<Item> GEMS_SAPPHIRE = createForgeKey(GEMS + "/sapphire");
    public static TagKey<Item> INGOTS_SILVER = createForgeKey(INGOTS_PATH + "/silver");
    public static TagKey<Item> NUGGETS_COPPER = createForgeKey(NUGGETS_PATH + "/copper");
    public static TagKey<Item> NUGGETS_SILVER = createForgeKey(NUGGETS_PATH + "/silver");
    public static TagKey<Item> BONES_WITHER = createForgeKey(BONES_PATH + "/wither");
    public static TagKey<Item> MAKE_ITEM_DROPS_FIREIMMUNE = createKey("make_item_drops_fireimmune");
    public static TagKey<Item> DRAGON_ARROWS = createKey("dragon_arrows");
    public static TagKey<Item> DRAGON_BLOODS = createKey("dragon_bloods");
    public static TagKey<Item> DRAGON_HEARTS = createKey("dragon_hearts");
    public static TagKey<Item> BREED_AMPITHERE = createKey("breed_ampithere");
    public static TagKey<Item> BREED_HIPPOCAMPUS = createKey("breed_hippocampus");
    public static TagKey<Item> BREED_HIPPOGRYPH = createKey("breed_hippogryph");
    public static TagKey<Item> HEAL_AMPITHERE = createKey("heal_ampithere");
    public static TagKey<Item> HEAL_COCKATRICE = createKey("heal_cockatrice");
    public static TagKey<Item> HEAL_HIPPOCAMPUS = createKey("heal_hippocampus");
    public static TagKey<Item> HEAL_PIXIE = createKey("heal_pixie");
    public static TagKey<Item> TAME_HIPPOGRYPH = createKey("tame_hippogryph");
    public static TagKey<Item> TAME_PIXIE = createKey("tame_pixie");
    public static TagKey<Item> TEMPT_DRAGON = createKey("tempt_dragon");
    public static TagKey<Item> TEMPT_HIPPOCAMPUS = createKey("tempt_hippocampus");
    public static TagKey<Item> TEMPT_HIPPOGRYPH = createKey("tempt_hippogryph");

    public static TagKey<Item> createKey(final String name) {
        return TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.fromNamespaceAndPath(IceAndFire.MODID, name));
    }

    public static TagKey<Item> createForgeKey(final String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", name));
    }
}
