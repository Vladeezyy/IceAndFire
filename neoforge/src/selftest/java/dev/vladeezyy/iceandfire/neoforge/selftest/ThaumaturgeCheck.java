package dev.vladeezyy.iceandfire.neoforge.selftest;

import com.github.alexthe666.iceandfire.IceAndFire;
import dev.vladeezyy.iceandfire.neoforge.compat.ThaumaturgeCompat;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.RegistryManager;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Thaumaturge compat (dev runs only). Without Thaumaturge: the compat pack is not there. With it
 * ({@code -Pwith_thaumaturge}): the pack is on, Draco and Fulgur are registered, the data map has entries for the
 * scales and the dragons, and Thaumaturge's aspect index gives the scales Draco, the lightning dragon's heart Fulgur and
 * a dragonbone sword (crafted, no entry of its own) Draco from its recipe. Lists the Ice and Fire items with no aspects
 * at all and the mobs without an entry, and writes every item's and mob's aspects to run/thaumaturge_aspects.txt.
 * Thaumaturge is read by reflection: no compile dependency on it.
 */
final class ThaumaturgeCheck {
    private static final String PREFIX = "[selftest] ";
    /** entity types that are no creature: parts, projectiles, eggs, skulls, statues, the chain knot */
    private static final java.util.regex.Pattern NOT_MOBS = java.util.regex.Pattern.compile(
        "multipart|charge|arrow|_egg$|skull|bubbles|breath|chain_tie|stone_statue|^player$|trident|feather|ghost_sword");

    private ThaumaturgeCheck() {
    }

    static boolean run(MinecraftServer server) {
        boolean available = server.getPackRepository().getAvailableIds().contains(ThaumaturgeCompat.PACK_ID);
        if (!ModList.get().isLoaded(ThaumaturgeCompat.MOD_ID)) {
            IceAndFire.LOGGER.info(PREFIX + "Thaumaturge not loaded, compat pack available: {}", available);
            return !available;
        }
        boolean selected = server.getPackRepository().getSelectedIds().contains(ThaumaturgeCompat.PACK_ID);
        ResourceKey<Registry<Object>> aspectRegistry = ResourceKey.createRegistryKey(rl("thaumaturge", "aspect"));
        Registry<Object> aspects = server.registryAccess().lookup(aspectRegistry).orElse(null);
        boolean draco = aspects != null && aspects.containsKey(rl(IceAndFire.MODID, "draco"));
        boolean fulgur = aspects != null && aspects.containsKey(rl(IceAndFire.MODID, "fulgur"));
        DataMapType<Item, ?> baseAspects = RegistryManager.getDataMap(Registries.ITEM, rl("thaumaturge", "base_aspects"));
        DataMapType<EntityType<?>, ?> entityAspects = RegistryManager.getDataMap(Registries.ENTITY_TYPE, rl("thaumaturge", "entity_aspects"));
        Object scalesEntry = baseAspects == null ? null : BuiltInRegistries.ITEM.wrapAsHolder(item("dragonscales_red")).getData(baseAspects);
        Object dragonEntry = entityAspects == null ? null : BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entity("fire_dragon")).getData(entityAspects);
        IceAndFire.LOGGER.info(PREFIX + "Thaumaturge: pack on {}, draco {}, fulgur {}, red scales entry {}, fire dragon entry {}", selected, draco, fulgur, scalesEntry != null, dragonEntry != null);

        Map<String, Map<String, Integer>> index = new TreeMap<>();
        List<String> without = new ArrayList<>();
        try {
            Method of = Class.forName("com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess").getMethod("of", ItemStack.class);
            for (Item item : BuiltInRegistries.ITEM) {
                Identifier id = BuiltInRegistries.ITEM.getKey(item);
                if (!id.getNamespace().equals(IceAndFire.MODID)) {
                    continue;
                }
                Map<String, Integer> list = amounts(of.invoke(null, new ItemStack(item)));
                index.put(id.getPath(), list);
                // spawn eggs have no aspects in Thaumaturge either
                if (list.isEmpty() && !(item instanceof SpawnEggItem)) {
                    without.add(id.getPath());
                }
            }
        } catch (ReflectiveOperationException e) {
            IceAndFire.LOGGER.error(PREFIX + "Thaumaturge API not found", e);
            return false;
        }
        List<String> mobsWithout = new ArrayList<>();
        StringBuilder mobs = new StringBuilder();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
            if (!id.getNamespace().equals(IceAndFire.MODID) || NOT_MOBS.matcher(id.getPath()).find()) {
                continue;
            }
            Object entry = entityAspects == null ? null : BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type).getData(entityAspects);
            mobs.append(id.getPath()).append(" = ").append(entry).append('\n');
            if (entry == null) {
                mobsWithout.add(id.getPath());
            }
        }
        StringBuilder dump = new StringBuilder();
        index.forEach((id, list) -> dump.append(id).append(" = ").append(list).append('\n'));
        // every item and entity id, for tools/gen_thaumaturge.py
        StringBuilder ids = new StringBuilder();
        index.keySet().forEach(id -> ids.append("item ").append(id).append('\n'));
        BuiltInRegistries.ENTITY_TYPE.keySet().stream().filter(id -> id.getNamespace().equals(IceAndFire.MODID)).sorted()
            .forEach(id -> ids.append("entity ").append(id.getPath()).append('\n'));
        try {
            Files.writeString(Path.of("thaumaturge_aspects.txt"), dump + "\n# mobs\n" + mobs);
            Files.writeString(Path.of("thaumaturge_ids.txt"), ids);
        } catch (IOException e) {
            IceAndFire.LOGGER.warn(PREFIX + "could not write thaumaturge_aspects.txt", e);
        }
        Map<String, Integer> scales = index.getOrDefault("dragonscales_red", Map.of());
        Map<String, Integer> heart = index.getOrDefault("lightning_dragon_heart", Map.of());
        Map<String, Integer> sword = index.getOrDefault("dragonbone_sword", Map.of());
        IceAndFire.LOGGER.info(PREFIX + "Thaumaturge index: red scales {}, lightning heart {}, dragonbone sword {}; {}/{} items have aspects; without: {}; mobs without an entry: {}",
            scales, heart, sword, index.size() - without.size(), index.size(), without, mobsWithout);
        String d = IceAndFire.MODID + ":draco";
        return selected && draco && fulgur && scalesEntry != null && dragonEntry != null && scales.containsKey(d)
            && heart.containsKey(IceAndFire.MODID + ":fulgur") && sword.containsKey(d) && without.isEmpty() && mobsWithout.isEmpty();
    }

    /** aspect id -> amount of an AspectList */
    @SuppressWarnings("unchecked")
    private static Map<String, Integer> amounts(Object aspectList) throws ReflectiveOperationException {
        Map<String, Integer> out = new TreeMap<>();
        Method amountOf = aspectList.getClass().getMethod("amountOf", Holder.class);
        for (Holder<?> holder : (Iterable<Holder<?>>) aspectList.getClass().getMethod("aspects").invoke(aspectList)) {
            out.put(holder.unwrapKey().map(k -> k.identifier().toString()).orElse("?"), (Integer) amountOf.invoke(aspectList, holder));
        }
        return out;
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.getValue(rl(IceAndFire.MODID, path));
    }

    private static EntityType<?> entity(String path) {
        return BuiltInRegistries.ENTITY_TYPE.getValue(rl(IceAndFire.MODID, path));
    }

    private static Identifier rl(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
}
