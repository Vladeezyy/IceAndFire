package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 1.21.11 saved data: a {@link SavedDataType} with a codec, stored as {@code data/<name>.dat} as on 1.21.1 (the same
 * file and NBT: a pass-through codec over the old load/save), so the hives, dragon positions and structure spacing of
 * old worlds stay. The data fixer type is one without fixes for the mod's keys (the storage always runs one).
 * <p>
 * The worldgen threads use the data too, but the storage's map is a plain HashMap that the autosave walks on the server
 * thread: the data is taken from it on the server thread when the level loads, everyone else reads this cache.
 */
public final class IafSavedData {
    private static final Map<ServerLevel, Map<SavedDataType<?>, SavedData>> LOADED = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private IafSavedData() {}

    public static <T extends SavedData> SavedDataType<T> type(String name, Supplier<T> constructor, Function<CompoundTag, T> load, BiFunction<T, CompoundTag, CompoundTag> save) {
        return new SavedDataType<>(name, constructor,
                CompoundTag.CODEC.xmap(load, data -> save.apply(data, new CompoundTag())), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    }

    /** Level load event (server thread), before any chunk of the level generates. */
    public static void onLevelLoad(ServerLevel level) {
        // levels of a server that stopped without unload events (crash at start) would stay in memory
        LOADED.keySet().removeIf(old -> old.getServer() != level.getServer());
        IafWorldData.get(level);
        MyrmexWorldData.get(level);
        DragonPosWorldData.get(level);
    }

    public static void onLevelUnload(ServerLevel level) {
        LOADED.remove(level);
    }

    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type, String legacyName) {
        T data = cached(level, type);
        if (data != null) {
            return data;
        }
        // as before the cache: a level object the server does not list for its dimension (a mod's copy) uses the listed one's
        ServerLevel listed = level.getServer().getLevel(level.dimension());
        if (listed != null && listed != level) {
            level = listed;
            data = cached(level, type);
            if (data != null) {
                return data;
            }
        }
        if (!level.getServer().isSameThread() && WARNED.add(level.dimension() + " " + type.id())) {
            // No level load event for this level (or another mod generated chunks in it first): the old way, under a lock
            IceAndFire.LOGGER.warn("Saved data {} of {} first asked off the server thread, before the level load event", type.id(), level.dimension());
        }
        return load(level, type);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SavedData> T cached(ServerLevel level, SavedDataType<T> type) {
        Map<SavedDataType<?>, SavedData> loaded = LOADED.get(level);
        return loaded == null ? null : (T) loaded.get(type);
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T extends SavedData> T load(ServerLevel level, SavedDataType<T> type) {
        Map<SavedDataType<?>, SavedData> loaded = LOADED.computeIfAbsent(level, key -> new ConcurrentHashMap<>(4));
        SavedData data = loaded.get(type);
        if (data == null) {
            data = level.getDataStorage().computeIfAbsent(type);
            loaded.put(type, data);
        }
        return (T) data;
    }
}
