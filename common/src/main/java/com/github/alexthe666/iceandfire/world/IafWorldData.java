package com.github.alexthe666.iceandfire.world;

import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.world.gen.TypedFeature;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class IafWorldData extends SavedData {
    public enum FeatureType {
        SURFACE,
        UNDERGROUND,
        OCEAN
    }

    private static final String IDENTIFIER = IceAndFire.MODID + "_general";
    private static final SavedData.Factory<IafWorldData> FACTORY = new SavedData.Factory<>(IafWorldData::new, (tag, registries) -> new IafWorldData(tag), null);
    /**
     * Per level (it was static: shared by all dimensions and, in singleplayer, by every world opened since the start).
     * The last checked position of each id, oldest first: the original's list, where a check dropped the older entries of
     * its id, so the order of the last entry of each id is all it kept. Also the lock of this data.
     */
    private final EnumMap<FeatureType, LinkedHashMap<String, BlockPos>> lastGenerated = new EnumMap<>(FeatureType.class);
    /**
     * An entry was added since the last save. Not {@link #setDirty()}: the storage clears that flag after encoding, which
     * would drop an entry a worldgen thread adds in between.
     */
    private volatile boolean changed;

    {
        for (FeatureType type : FeatureType.values()) {
            lastGenerated.put(type, new LinkedHashMap<>());
        }
    }

    public IafWorldData() { /* Nothing to do */ }

    public IafWorldData(final CompoundTag tag) {
        this.load(tag);
    }

    public static IafWorldData get(final Level world) {
        if (world instanceof ServerLevel level) {
            // marked dirty only when check() adds an entry
            return IafSavedData.get(level, FACTORY, IDENTIFIER);
        }

        return null;
    }

    public boolean check(final TypedFeature feature, final BlockPos position, final String id) {
        return check(feature.getFeatureType(), position, id);
    }

    public boolean check(final FeatureType type, final BlockPos position, final String id) {
        // Called by the worldgen threads at the same time, saved by the server thread
        synchronized (lastGenerated) {
            LinkedHashMap<String, BlockPos> entries = lastGenerated.get(type);
            // As the original: only the last checked position counts, whatever its id (the config: "from the last
            // generated structure"); the original went through the whole list and kept the result of the last entry
            Map.Entry<String, BlockPos> last = entries.lastEntry();
            double limit = IafConfig.dangerousWorldGenSeparationLimit;
            boolean canGenerate = last == null || position.distSqr(last.getValue()) > limit * limit;

            entries.remove(id);
            if (entries.size() > 5_000) {
                IceAndFire.LOGGER.debug("Too many BlockPos entries for feature type {} tracked, removing oldest ones", type);
                Iterator<String> oldest = entries.keySet().iterator();
                for (int i = 0; i < 1_000; i++) {
                    oldest.next();
                    oldest.remove();
                }
            }
            entries.put(id, position.immutable());
            changed = true;

            return canGenerate;
        }
    }

    @Override
    public boolean isDirty() {
        return changed || super.isDirty();
    }

    public IafWorldData load(final CompoundTag tag) {
        synchronized (lastGenerated) {
            for (FeatureType type : FeatureType.values()) {
                LinkedHashMap<String, BlockPos> entries = lastGenerated.get(type);
                ListTag list = tag.getList(type.toString(), Tag.TAG_COMPOUND);

                for (int i = 0; i < list.size(); i++) {
                    CompoundTag entry = list.getCompound(i);
                    String id = entry.getString("id");
                    // old saves repeat ids (the shared list): the later one moves to the end, as in the original's list
                    entries.remove(id);
                    entries.put(id, readPosition(entry));
                }
            }
        }

        return this;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull final CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        synchronized (lastGenerated) {
            // under the lock, before encoding: an entry added after this sets it again
            changed = false;
            for (var e : lastGenerated.entrySet()) {
                ListTag listTag = new ListTag();

                for (Map.Entry<String, BlockPos> entry : e.getValue().entrySet()) {
                    CompoundTag subTag = new CompoundTag();
                    subTag.putString("id", entry.getKey());
                    subTag.put("position", NbtUtils.writeBlockPos(entry.getValue()));

                    listTag.add(subTag);
                }

                tag.put(e.getKey().toString(), listTag);
            }
        }

        return tag;
    }

    /** 1.21 writes positions as int arrays, worlds of 1.20 have compounds with X, Y and Z. */
    private static BlockPos readPosition(CompoundTag entry) {
        if (entry.contains("position", Tag.TAG_COMPOUND)) {
            CompoundTag old = entry.getCompound("position");
            return new BlockPos(old.getInt("X"), old.getInt("Y"), old.getInt("Z"));
        }
        return NbtUtils.readBlockPos(entry, "position").orElse(BlockPos.ZERO);
    }
}
