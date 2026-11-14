package com.github.alexthe666.iceandfire.world;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.core.UUIDUtil;

import net.minecraft.core.HolderLookup;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DragonPosWorldData extends SavedData {

    private static final String IDENTIFIER = "iceandfire_dragonPositions";
    private static final SavedDataType<DragonPosWorldData> TYPE = IafSavedData.type(IDENTIFIER, DragonPosWorldData::new, DragonPosWorldData::new, (data, tag) -> data.save(tag, null));
    protected final Map<UUID, BlockPos> lastDragonPositions = new HashMap<>();
    private Level world;
    private int tickCounter;

    public DragonPosWorldData() {
    }

    public DragonPosWorldData(Level world) {
        this.world = world;
        this.setDirty();
    }

    public DragonPosWorldData(CompoundTag compoundTag) {
        this.load(compoundTag);
    }

    public static DragonPosWorldData get(Level world) {
        if (world instanceof ServerLevel level) {
            // the level's cached data (IafSavedData)
            DragonPosWorldData data = IafSavedData.get(level, TYPE, IDENTIFIER);
            data.world = world;
            data.setDirty();
            return data;
        }
        return null;
    }

    public void addDragon(UUID uuid, BlockPos pos) {
        lastDragonPositions.put(uuid, pos);
        this.setDirty();
    }

    public void removeDragon(UUID uuid) {
        lastDragonPositions.remove(uuid);
        this.setDirty();
    }

    public BlockPos getDragonPos(UUID uuid) {
        return lastDragonPositions.get(uuid);
    }

    public void debug() {
        IceAndFire.LOGGER.warn(lastDragonPositions.toString());
    }


    public void tick() {
        ++this.tickCounter;
    }

    public DragonPosWorldData load(CompoundTag nbt) {
        this.tickCounter = nbt.getIntOr("Tick", 0);
        ListTag nbttaglist = nbt.getListOrEmpty("DragonMap");
        this.lastDragonPositions.clear();
        for (int i = 0; i < nbttaglist.size(); ++i) {
            CompoundTag CompoundNBT = nbttaglist.getCompoundOrEmpty(i);
            // an entry without its dragon is dropped: a null key threw on every autosave
            UUID uuid = CompoundNBT.read("DragonUUID", UUIDUtil.CODEC).orElse(null);
            if (uuid == null) {
                continue;
            }
            BlockPos pos = new BlockPos(CompoundNBT.getIntOr("DragonPosX", 0), CompoundNBT.getIntOr("DragonPosY", 0), CompoundNBT.getIntOr("DragonPosZ", 0));
            this.lastDragonPositions.put(uuid, pos);
        }
        return this;
    }

    // 26.x: through IafSavedData (the 1.21.1 NBT)
    public @NotNull CompoundTag save(CompoundTag compound, @NotNull HolderLookup.Provider registries) {
        compound.putInt("Tick", this.tickCounter);
        ListTag nbttaglist = new ListTag();
        for (Map.Entry<UUID, BlockPos> pair : lastDragonPositions.entrySet()) {
            if (pair.getKey() == null || pair.getValue() == null) {
                continue;
            }
            CompoundTag CompoundNBT = new CompoundTag();
            CompoundNBT.store("DragonUUID", UUIDUtil.CODEC, pair.getKey());
            CompoundNBT.putInt("DragonPosX", pair.getValue().getX());
            CompoundNBT.putInt("DragonPosY", pair.getValue().getY());
            CompoundNBT.putInt("DragonPosZ", pair.getValue().getZ());
            nbttaglist.add(CompoundNBT);
        }
        compound.put("DragonMap", nbttaglist);
        return compound;
    }
}
