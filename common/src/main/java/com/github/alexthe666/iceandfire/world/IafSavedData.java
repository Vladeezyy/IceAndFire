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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 26.x saved data: a {@link SavedDataType} with a codec, stored as {@code data/iceandfire/<path>.dat}. The NBT is the
 * 1.21.1 port's (a pass-through codec over the old load/save), and {@link #get} first moves the 1.21.1 file
 * {@code data/<legacy name>.dat} to the new place, so the hives, dragon positions and structure spacing of old worlds stay.
 */
public final class IafSavedData {
    private static final Set<Path> MIGRATED = Collections.synchronizedSet(new HashSet<>());

    private IafSavedData() {}

    public static <T extends SavedData> SavedDataType<T> type(String path, Supplier<T> constructor, Function<CompoundTag, T> load, BiFunction<T, CompoundTag, CompoundTag> save) {
        return new SavedDataType<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, path), constructor,
                CompoundTag.CODEC.xmap(load, data -> save.apply(data, new CompoundTag())), DataFixTypes.SAVED_DATA_WANDERING_TRADER);
    }

    public static <T extends SavedData> T get(ServerLevel level, SavedDataType<T> type, String legacyName) {
        SavedDataStorage storage = level.getDataStorage();
        Path legacy = storage.dataFolder.resolve(legacyName + ".dat");
        if (MIGRATED.add(legacy) && Files.exists(legacy)) {
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
        return storage.computeIfAbsent(type);
    }
}
