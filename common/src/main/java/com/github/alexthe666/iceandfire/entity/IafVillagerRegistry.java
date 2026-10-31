package com.github.alexthe666.iceandfire.entity;

import java.util.Optional;
import net.minecraft.world.item.trading.ItemCost;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.datagen.IafProcessorLists;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class IafVillagerRegistry {

    public static final Registrar<PoiType> POI_TYPES = Registrar.create(BuiltInRegistries.POINT_OF_INTEREST_TYPE, IceAndFire.MODID);
    public static final RegistryEntry<PoiType, PoiType> SCRIBE_POI = POI_TYPES.register("scribe", () -> new PoiType(ImmutableSet.copyOf(IafBlockRegistry.LECTERN.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final Registrar<VillagerProfession> PROFESSIONS = Registrar.create(BuiltInRegistries.VILLAGER_PROFESSION, IceAndFire.MODID);
    public static final RegistryEntry<VillagerProfession, VillagerProfession> SCRIBE = PROFESSIONS.register("scribe", ()-> new VillagerProfession(net.minecraft.network.chat.Component.translatable("entity.minecraft.villager.iceandfire.scribe"), (entry) -> entry.value().equals(SCRIBE_POI.get()), (entry) -> entry.value().equals(SCRIBE_POI.get()), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_LIBRARIAN, scribeTrades()));

    // 26.x: the trades are data (data/iceandfire/trade_set/scribe/level_n.json, from tools/port26/gen_trades_26.py)
    private static it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraft.resources.ResourceKey<net.minecraft.world.item.trading.TradeSet>> scribeTrades() {
        it.unimi.dsi.fastutil.ints.Int2ObjectMap<net.minecraft.resources.ResourceKey<net.minecraft.world.item.trading.TradeSet>> trades = new it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap<>();
        for (int level = 1; level <= 5; level++) {
            trades.put(level, net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.TRADE_SET, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "scribe/level_" + level)));
        }
        return trades;
    }

    public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry,
                                         Registry<StructureProcessorList> processorListRegistry,
                                         Identifier poolRL,
                                         String nbtPieceRL,
                                         int weight) {

        Holder<StructureProcessorList> villageHouseProcessorList = processorListRegistry.getHolderOrThrow(IafProcessorLists.HOUSE_PROCESSOR);

        // Grab the pool we want to add to
        StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
        if (pool == null) return;

        // Grabs the nbt piece and creates a SinglePoolElement of it that we can add to a structure's pool.
        // Use .legacy( for villages/outposts and .single( for everything else
        SinglePoolElement piece = SinglePoolElement.legacy(nbtPieceRL, villageHouseProcessorList).apply(StructureTemplatePool.Projection.RIGID);

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's templates field public for us to see.
        // Weight is handled by how many times the entry appears in this list.
        // We do not need to worry about immutability as this field is created using Lists.newArrayList(); which makes a mutable list.
        for (int i = 0; i < weight; i++) {
            pool.templates.add(piece);
        }

        // Use AccessTransformer or Accessor Mixin to make StructureTemplatePool's rawTemplates field public for us to see.
        // This list of pairs of pieces and weights is not used by vanilla by default but another mod may need it for efficiency.
        // So lets add to this list for completeness. We need to make a copy of the array as it can be an immutable list.
        List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
        listOfPieceEntries.add(new Pair<>(piece, weight));
        pool.rawTemplates = listOfPieceEntries;
    }


}
