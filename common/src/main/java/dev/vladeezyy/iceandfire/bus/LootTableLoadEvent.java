package dev.vladeezyy.iceandfire.bus;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootTable;

/** A loot table is loaded; listeners change it in place or replace it */
public class LootTableLoadEvent extends Event {
    private final Identifier name;
    private LootTable table;

    public LootTableLoadEvent(Identifier name, LootTable table) {
        this.name = name;
        this.table = table;
    }

    public Identifier getName() {
        return this.name;
    }

    public LootTable getTable() {
        return this.table;
    }

    public void setTable(LootTable table) {
        this.table = table;
    }

    private final java.util.List<java.util.Map.Entry<String, net.minecraft.world.level.storage.loot.LootPool.Builder>> addedPools = new java.util.ArrayList<>();

    /** Adds a pool (the loader adds it: NeoForge to the table with this name, Fabric to the table builder) */
    public void addPool(String name, net.minecraft.world.level.storage.loot.LootPool.Builder pool) {
        this.addedPools.add(java.util.Map.entry(name, pool));
    }

    public java.util.List<java.util.Map.Entry<String, net.minecraft.world.level.storage.loot.LootPool.Builder>> getAddedPools() {
        return this.addedPools;
    }
}
