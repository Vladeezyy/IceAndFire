package com.github.alexthe666.iceandfire.entity.util;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * 26.x saves through {@link ValueOutput} / {@link ValueInput} instead of a {@code CompoundTag}. The values the original
 * kept as NBT (lists, block states, item handlers, encoded positions) go through these as the same NBT, under the same
 * keys, so worlds saved by the 1.21.1 port load unchanged.
 */
public final class Values {
	private Values() {}

	public static void put(ValueOutput output, String key, Tag tag) {
		output.store(key, ExtraCodecs.NBT, tag);
	}

	@Nullable
	public static Tag get(ValueInput input, String key) {
		return input.read(key, ExtraCodecs.NBT).orElse(null);
	}

	public static boolean contains(ValueInput input, String key) {
		return input.read(key, ExtraCodecs.NBT).isPresent();
	}

	/** The compound under the key, or an empty one */
	public static CompoundTag compound(ValueInput input, String key) {
		return input.read(key, CompoundTag.CODEC).orElseGet(CompoundTag::new);
	}

	/** The list under the key, or an empty one */
	public static ListTag list(ValueInput input, String key) {
		return get(input, key) instanceof ListTag list ? list : new ListTag();
	}

	/** An entity's save data as NBT (statues, dragon eggs, saved passengers) */
	public static CompoundTag save(Entity entity) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, entity.registryAccess());
		entity.saveWithoutId(output);
		return output.buildResult();
	}

	/** Loads NBT saved by {@link #save} into an entity */
	public static void load(Entity entity, CompoundTag tag) {
		entity.load(TagValueInput.create(ProblemReporter.DISCARDING, entity.registryAccess(), tag));
	}

	/** 1.21.1's {@code ItemStack.save(registries, tag)}: the stack merged into the tag (same NBT) */
	public static Tag saveItem(HolderLookup.Provider registries, ItemStack stack, CompoundTag into) {
		return ItemStack.CODEC.encode(stack, registries.createSerializationContext(NbtOps.INSTANCE), into).getOrThrow();
	}

	/** 1.21.1's {@code ItemStack.parseOptional(registries, tag)} */
	public static ItemStack parseItem(HolderLookup.Provider registries, CompoundTag tag) {
		return ItemStack.OPTIONAL_CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result().orElse(ItemStack.EMPTY);
	}

	/** NBT written by a save method that takes a value output (an entity's additional data) */
	public static CompoundTag write(HolderLookup.Provider registries, java.util.function.Consumer<ValueOutput> writer) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
		writer.accept(output);
		return output.buildResult();
	}

	/** NBT as a value input, for code that reads a compound the way it reads a save */
	public static ValueInput input(HolderLookup.Provider registries, CompoundTag tag) {
		return TagValueInput.create(ProblemReporter.DISCARDING, registries, tag);
	}
}
