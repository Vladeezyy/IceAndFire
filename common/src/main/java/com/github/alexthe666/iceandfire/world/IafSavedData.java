package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.SavedDataStorage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 26.x saved data: a {@link SavedDataType} with a codec, stored as {@code data/iceandfire/<path>.dat}. The NBT is the
 * 1.21.1 port's (a pass-through codec over the old load/save), and the first load moves the 1.21.1 file
 * {@code data/<legacy name>.dat} to the new place, so the hives, dragon positions and structure spacing of old worlds stay.
 * <p>
 * The worldgen threads use the data too, but the storage's map is a plain HashMap that the autosave walks on the server
 * thread: the data is taken from it on the server thread when the level loads, everyone else reads this cache.
 */
public final class IafSavedData {
    private static final Map<ServerLevel, Map<SavedDataType<?>, SavedData>> LOADED = new ConcurrentHashMap<>();
    private static final Set<String> WARNED = ConcurrentHashMap.newKeySet();

    private IafSavedData() {}

    public static <T extends SavedData> SavedDataType<T> type(String path, Supplier<T> constructor, Function<CompoundTag, T> load, BiFunction<T, CompoundTag, CompoundTag> save) {
        return new SavedDataType<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, path), constructor,
                CompoundTag.CODEC.xmap(load, data -> save.apply(data, new CompoundTag())), DataFixTypes.SAVED_DATA_WANDERING_TRADER);
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
        if (!level.getServer().isSameThread() && WARNED.add(level.dimension().identifier() + " " + type.id())) {
            // No level load event for this level (or another mod generated chunks in it first): the old way, under a lock
            IceAndFire.LOGGER.warn("Saved data {} of {} first asked off the server thread, before the level load event", type.id(), level.dimension().identifier());
        }
        return load(level, type, legacyName);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SavedData> T cached(ServerLevel level, SavedDataType<T> type) {
        Map<SavedDataType<?>, SavedData> loaded = LOADED.get(level);
        return loaded == null ? null : (T) loaded.get(type);
    }

    @SuppressWarnings("unchecked")
    private static synchronized <T extends SavedData> T load(ServerLevel level, SavedDataType<T> type, String legacyName) {
        Map<SavedDataType<?>, SavedData> loaded = LOADED.computeIfAbsent(level, key -> new ConcurrentHashMap<>(4));
        SavedData data = loaded.get(type);
        if (data == null) {
            SavedDataStorage storage = level.getDataStorage();
            migrate(storage, type, legacyName);
            data = storage.computeIfAbsent(type);
            loaded.put(type, data);
        }
        return (T) data;
    }

    private static void migrate(SavedDataStorage storage, SavedDataType<?> type, String legacyName) {
        Path legacy = storage.dataFolder.resolve(legacyName + ".dat");
        if (!Files.exists(legacy)) {
            return;
        }
        Path target = type.id().withSuffix(".dat").resolveAgainst(storage.dataFolder);
        try {
            if (!Files.exists(target)) {
                Files.createDirectories(target.getParent());
                Files.move(legacy, target);
                IceAndFire.LOGGER.info("Moved the 1.21.1 saved data {} to {}", legacy, target);
            }
        } catch (IOException e) {
            IceAndFire.LOGGER.error("Could not move the saved data {} to {}", legacy, target, e);
        }
    }
}
