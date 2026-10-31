package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

/**
 * Armor values of Ice and Fire. Since 1.21.2 the vanilla {@link ArmorMaterial} is a record that repairs from an item
 * tag ({@code data/iceandfire/tags/item/repairs/<id>.json}, the original's repair ingredients) and is drawn from an
 * equipment asset ({@code assets/iceandfire/equipment/<id>.json}); items take {@link #properties(ArmorType)}.
 */
public class IafArmorMaterial {

    protected static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final String name;
    private final String id;
    private final int maxDamageFactor;
    private final int[] damageReduction;
    private final int enchantability;
    private final Holder<SoundEvent> sound;
    private final float toughness;
    private ArmorMaterial vanilla;

    public IafArmorMaterial(String name, int durability, int[] damageReduction, int encantability, Holder<SoundEvent> sound, float toughness) {
        this.name = name;
        this.maxDamageFactor = durability;
        this.damageReduction = damageReduction;
        this.enchantability = encantability;
        this.sound = sound;
        this.toughness = toughness;
        this.id = name.substring(name.indexOf(':') + 1).toLowerCase(Locale.ROOT).replace(' ', '_');
    }

    /** The vanilla material with this material's values (built once: the dragonsteel values are read from the config then) */
    public ArmorMaterial vanilla() {
        if (vanilla == null) {
            Map<ArmorType, Integer> defense = new EnumMap<>(ArmorType.class);
            for (ArmorType type : ArmorType.values()) {
                defense.put(type, getDefenseForType(type));
            }
            ResourceKey<EquipmentAsset> asset = ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(IceAndFire.MODID, id));
            vanilla = new ArmorMaterial(maxDamageFactor, defense, getEnchantmentValue(), sound, getToughness(), getKnockbackResistance(), repairItems(), asset);
        }
        return vanilla;
    }

    /** Properties of an armor piece of this material: durability, attributes, repair items, equippable */
    public Item.Properties properties(ArmorType type) {
        return dev.vladeezyy.iceandfire.platform.Registrar.itemProps().humanoidArmor(vanilla(), type).durability(getDurabilityForType(type));
    }

    public TagKey<Item> repairItems() {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(IceAndFire.MODID, "repairs/" + id));
    }

    public String getName() {
        return name;
    }

    /** Index of the armor piece in the arrays of the original: feet, legs, chest, head. */
    protected static int index(ArmorType type) {
        return switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE, BODY -> 2;
            case HELMET -> 3;
        };
    }

    public int getDurabilityForType(ArmorType slotIn) {
        return MAX_DAMAGE_ARRAY[index(slotIn)] * this.maxDamageFactor;
    }

    public int getDefenseForType(ArmorType slotIn) {
        return damageReduction[index(slotIn)];
    }

    public int getEnchantmentValue() {
        return enchantability;
    }

    public float getToughness() {
        return toughness;
    }

    public float getKnockbackResistance() {
        return 0;
    }
}
