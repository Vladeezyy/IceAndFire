package com.github.alexthe666.iceandfire.selftest;
import com.github.alexthe666.iceandfire.item.IafArmorItem;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Writes the vanilla item tags that make the gear of Ice and Fire enchantable in 1.21 (swords, head_armor,
 * enchantable/durability...), from the item classes. Dev only: gradle runSelftest -Diceandfire.dumptags=<dir>.
 */
public class TagDump {

    public static void onStarted(net.minecraft.server.MinecraftServer server) {
        String dir = System.getProperty("iceandfire.dumptags");
        if (dir == null) {
            return;
        }
        Map<String, Set<String>> tags = new TreeMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!id.getNamespace().equals(IceAndFire.MODID)) {
                continue;
            }
            String name = id.toString();
            if (item instanceof com.github.alexthe666.iceandfire.item.ItemModSword) {
                add(tags, "swords", name);
            } else if (item instanceof com.github.alexthe666.iceandfire.item.ItemModAxe) {
                add(tags, "axes", name);
            } else if (item instanceof com.github.alexthe666.iceandfire.item.ItemModPickaxe) {
                add(tags, "pickaxes", name);
            } else if (item instanceof com.github.alexthe666.iceandfire.item.ItemModShovel) {
                add(tags, "shovels", name);
            } else if (item instanceof com.github.alexthe666.iceandfire.item.ItemModHoe) {
                add(tags, "hoes", name);
            } else if (item instanceof IafArmorItem armor) {
                EquipmentSlot slot = armor.getEquipmentSlot();
                add(tags, switch (slot) {
                    case HEAD -> "head_armor";
                    case CHEST -> "chest_armor";
                    case LEGS -> "leg_armor";
                    default -> "foot_armor";
                }, name);
            } else if (item instanceof TridentItem) {
                add(tags, "enchantable/trident", name);
            } else if (item instanceof BowItem) {
                add(tags, "enchantable/bow", name);
            } else if (new ItemStack(item).isDamageableItem()) {
                // Scepters, staffs, flutes, gauntlets...: Unbreaking and Mending as in 1.20.1 (category BREAKABLE)
                add(tags, "enchantable/durability", name);
            }
        }
        for (Map.Entry<String, Set<String>> entry : tags.entrySet()) {
            JsonObject json = new JsonObject();
            json.addProperty("replace", false);
            JsonArray values = new JsonArray();
            entry.getValue().forEach(values::add);
            json.add("values", values);
            Path file = Path.of(dir, "data/minecraft/tags/item/" + entry.getKey() + ".json");
            try {
                Files.createDirectories(file.getParent());
                Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(json) + "\n");
            } catch (IOException e) {
                IceAndFire.LOGGER.error("[tagdump] {}", file, e);
            }
            IceAndFire.LOGGER.info("[tagdump] {} {}", entry.getKey(), entry.getValue().size());
        }
        server.halt(false);
    }

    private static void add(Map<String, Set<String>> tags, String tag, String item) {
        tags.computeIfAbsent(tag, k -> new TreeSet<>()).add(item);
    }
}
