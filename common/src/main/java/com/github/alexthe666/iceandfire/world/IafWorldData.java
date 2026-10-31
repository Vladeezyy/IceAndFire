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
import net.minecraft.world.level.storage.SavedDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class IafWorldData extends SavedData {
    public enum FeatureType {
        SURFACE,
        UNDERGROUND,
        OCEAN
    }

    private static final String IDENTIFIER = IceAndFire.MODID + "_general";
    private static final EnumMap<FeatureType, ArrayList<Map.Entry<String, BlockPos>>> LAST_GENERATED = new EnumMap<>(FeatureType.class);
    static {
        LAST_GENERATED.put(FeatureType.SURFACE, new ArrayList<>());
        LAST_GENERATED.put(FeatureType.UNDERGROUND, new ArrayList<>());
        LAST_GENERATED.put(FeatureType.OCEAN, new ArrayList<>());
    }

    public IafWorldData() { /* Nothing to do */ }

    public IafWorldData(final CompoundTag tag) {
        this.load(tag);
    }

    public static IafWorldData get(final Level world) {
        if (world instanceof ServerLevel) {
            ServerLevel overworld = world.getServer().getLevel(world.dimension());
            SavedDataStorage storage = overworld.getDataStorage();
            IafWorldData data = storage.computeIfAbsent(new SavedData.Factory<>(IafWorldData::new, (tag, registries) -> new IafWorldData(tag), null), IDENTIFIER);
            data.setDirty();

            return data;
        }

        return null;
    }

    public boolean check(final TypedFeature feature, final BlockPos position, final String id) {
        return check(feature.getFeatureType(), position, id);
    }

    public boolean check(final FeatureType type, final BlockPos position, final String id) {
        ArrayList<Map.Entry<String, BlockPos>> entries = LAST_GENERATED.get(type);

        boolean canGenerate = true;
        List<Map.Entry<String, BlockPos>> toRemove = null;

        for (Map.Entry<String, BlockPos> entry : entries) {
            if (entry.getKey().equals(id)) {
                if (toRemove == null) toRemove = new ArrayList<>();
                toRemove.add(entry);
            }

            canGenerate = position.distSqr(entry.getValue()) > IafConfig.dangerousWorldGenSeparationLimit * IafConfig.dangerousWorldGenSeparationLimit;
        }

        if (toRemove != null) {
            entries.removeAll(toRemove);
        }

        if (entries.size() > 5_000) {
            IceAndFire.LOGGER.debug("Too many BlockPos entries for feature type {} tracked, removing oldest ones", type);
            entries.subList(0, 1_000).clear();
            entries.trimToSize();
        }

        entries.add(Map.entry(id, position));

        return canGenerate;
    }

    public IafWorldData load(final CompoundTag tag) {
        FeatureType[] types = FeatureType.values();

        for (FeatureType type : types) {
            ListTag list = tag.getList(type.toString(), ListTag.TAG_COMPOUND);

            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompoundOrEmpty(i);
                String id = entry.getStringOr("id", "");
                BlockPos position = readPosition(entry);
                LAST_GENERATED.get(type).add(Map.entry(id, position));
            }
        }

        return this;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull final CompoundTag tag, @NotNull HolderLookup.Provider registries) {
        for (var e : LAST_GENERATED.entrySet()) {
            ListTag listTag = new ListTag();

            for (Map.Entry<String, BlockPos> entry : e.getValue()) {
                CompoundTag subTag = new CompoundTag();
                subTag.putString("id", entry.getKey());
                subTag.put("position", NbtUtils.writeBlockPos(entry.getValue()));

                listTag.add(subTag);
            }

            tag.put(e.getKey().toString(), listTag);
        }

        return tag;
    }

    /** 1.21 writes positions as int arrays, worlds of 1.20 have compounds with X, Y and Z. */
    private static BlockPos readPosition(CompoundTag entry) {
        if (entry.contains("position", Tag.TAG_COMPOUND)) {
            CompoundTag old = entry.getCompoundOrEmpty("position");
            return new BlockPos(old.getIntOr("X", 0), old.getIntOr("Y", 0), old.getIntOr("Z", 0));
        }
        return NbtUtils.readBlockPos(entry, "position").orElse(BlockPos.ZERO);
    }
}
