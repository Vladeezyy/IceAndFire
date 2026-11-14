package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The mod's saved data of each level, stored as {@code data/<name>.dat} in the level's own data storage.
 * <p>
 * The worldgen threads use the data too, but the storage's map is a plain HashMap that the autosave walks on the server
 * thread (a lock of our own around computeIfAbsent does not stop that walk): the data is taken from it on the server
 * thread when the level loads, everyone else reads this cache.
 */
public final class IafSavedData {
    private static final Map<ServerLevel, Map<String, SavedData>> LOADED = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private IafSavedData() {}

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

    public static <T extends SavedData> T get(ServerLevel level, SavedData.Factory<T> factory, String name) {
        T data = cached(level, name);
        if (data != null) {
            return data;
        }
        // as before the cache: a level object the server does not list for its dimension (a mod's copy) uses the listed one's
        ServerLevel listed = level.getServer().getLevel(level.dimension());
        if (listed != null && listed != level) {
            level = listed;
            data = cached(level, name);
            if (data != null) {
                return data;
            }
        }
        if (!level.getServer().isSameThread() && WARNED.add(level.dimension() + " " + name)) {
            // No level load event for this level (or another mod generated chunks in it first): the old way, under a lock
            IceAndFire.LOGGER.warn("Saved data {} of {} first asked off the server thread, before the level load event", name, level.dimension());
        }
        return load(level, factory, name);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SavedData> T cached(ServerLevel level, String name) {
        Map<String, SavedData> loaded = LOADED.get(level);
        return loaded == null ? null : (T) loaded.get(name);
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T extends SavedData> T load(ServerLevel level, SavedData.Factory<T> factory, String name) {
        Map<String, SavedData> loaded = LOADED.computeIfAbsent(level, key -> new ConcurrentHashMap<>(4));
        SavedData data = loaded.get(name);
        if (data == null) {
            data = level.getDataStorage().computeIfAbsent(factory, name);
            loaded.put(name, data);
        }
        return (T) data;
    }
}
