package dev.vladeezyy.iceandfire.bus;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;

import java.util.List;

/** The trades of a profession by level, filled once at startup; listeners add to the lists */
public class VillagerTradesEvent extends Event {
    private final Int2ObjectMap<List<VillagerTrades.ItemListing>> trades;
    private final VillagerProfession type;

    public VillagerTradesEvent(Int2ObjectMap<List<VillagerTrades.ItemListing>> trades, VillagerProfession type) {
        this.trades = trades;
        this.type = type;
    }

    public Int2ObjectMap<List<VillagerTrades.ItemListing>> getTrades() {
        return this.trades;
    }

    public VillagerProfession getType() {
        return this.type;
    }
}
