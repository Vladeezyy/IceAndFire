package com.github.alexthe666.iceandfire.item;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

/**
 * Armor values of Ice and Fire. In 1.21 the vanilla {@link ArmorMaterial} is a registry entry, so every material
 * registers one on creation and items use {@link #holder()}.
 */
public class IafArmorMaterial {

    protected static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
    private final String name;
    private final int maxDamageFactor;
    private final int[] damageReduction;
    private final int enchantability;
    private final Holder<SoundEvent> sound;
    private final float toughness;
    private Ingredient repairMaterial = Ingredient.EMPTY;
    private final Holder<ArmorMaterial> holder;

    public IafArmorMaterial(String name, int durability, int[] damageReduction, int encantability, Holder<SoundEvent> sound, float toughness) {
        this.name = name;
        this.maxDamageFactor = durability;
        this.damageReduction = damageReduction;
        this.enchantability = encantability;
        this.sound = sound;
        this.toughness = toughness;
        String id = name.substring(name.indexOf(':') + 1).toLowerCase(Locale.ROOT).replace(' ', '_');
        this.holder = IafArmorMaterials.MATERIALS.register(id, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                    for (ArmorItem.Type type : ArmorItem.Type.values()) {
                        map.put(type, getDefenseForType(type));
                    }
                }),
                getEnchantmentValue(), this.sound, this::getRepairIngredient,
                List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, id))),
                getToughness(), getKnockbackResistance()));
    }

    public Holder<ArmorMaterial> holder() {
        return holder;
    }

    public String getName() {
        return name;
    }

    /** Index of the armor piece in the arrays of the original: feet, legs, chest, head. */
    protected static int index(ArmorItem.Type type) {
        return switch (type) {
            case BOOTS -> 0;
            case LEGGINGS -> 1;
            case CHESTPLATE, BODY -> 2;
            case HELMET -> 3;
        };
    }

    public int getDurabilityForType(ArmorItem.Type slotIn) {
        return MAX_DAMAGE_ARRAY[index(slotIn)] * this.maxDamageFactor;
    }

    public int getDefenseForType(ArmorItem.Type slotIn) {
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

    public Ingredient getRepairIngredient() {
        return repairMaterial;
    }

    public void setRepairMaterial(Ingredient repairMaterial) {
        this.repairMaterial = repairMaterial;
    }
}
