package com.github.alexthe666.iceandfire.world;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 1.21.11 saved data: a {@link SavedDataType} with a codec, stored as {@code data/<name>.dat} as on 1.21.1 (the same
 * file and NBT: a pass-through codec over the old load/save), so the hives, dragon positions and structure spacing of
 * old worlds stay. The data fixer type is one without fixes for the mod's keys (the storage always runs one).
 */
public final class IafSavedData {
    private IafSavedData() {}

    public static <T extends SavedData> SavedDataType<T> type(String name, Supplier<T> constructor, Function<CompoundTag, T> load, BiFunction<T, CompoundTag, CompoundTag> save) {
        return new SavedDataType<>(name, constructor,
                CompoundTag.CODEC.xmap(load, data -> save.apply(data, new CompoundTag())), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type, String legacyName) {
        return level.getDataStorage().computeIfAbsent(type);
    }
}
