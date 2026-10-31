package dev.vladeezyy.iceandfire.bus;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;

import java.util.List;

/** The trades of a profession by level, filled once at startup; listeners add to the lists */
public class VillagerTradesEvent extends Event {
    private final Int2ObjectMap<List<VillagerTrades.ItemListing>> trades;
    private final ResourceKey<VillagerProfession> type;

    public VillagerTradesEvent(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, ResourceKey<VillagerProfession> type) {
        this.trades = trades;
        this.type = type;
    }

    public Int2ObjectMap<List<VillagerTrades.ItemListing>> getTrades() {
        return this.trades;
    }

    public ResourceKey<VillagerProfession> getType() {
        return this.type;
    }
}
