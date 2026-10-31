package com.github.alexthe666.iceandfire.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorType;

/**
 * 1.21.1's {@code ArmorItem} for Ice and Fire's armor: since 1.21.5 armor is a plain item with
 * {@code Properties.humanoidArmor}; this keeps the material and the piece the original's code asks for.
 */
public class IafArmorItem extends Item {
    protected final IafArmorMaterial material;
    protected final ArmorType type;

    public IafArmorItem(IafArmorMaterial material, ArmorType type) {
        this(material, type, material.properties(type));
    }

    public IafArmorItem(IafArmorMaterial material, ArmorType type, Item.Properties properties) {
        super(properties);
        this.material = material;
        this.type = type;
    }

    public IafArmorMaterial getMaterial() {
        return material;
    }

    public ArmorType getType() {
        return type;
    }

    public EquipmentSlot getEquipmentSlot() {
        return type.getSlot();
    }
}
