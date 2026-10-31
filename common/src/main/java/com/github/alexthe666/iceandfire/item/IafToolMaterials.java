package com.github.alexthe666.iceandfire.item;

import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * The vanilla tool material of a mod tool material. 26.x refuses an enchantment value of 0, which 1.21.1 read as "not
 * enchantable at the table" (the dread sword): the material gets 1 and {@link #properties} drops the enchantable
 * component again, as 1.21.1 behaved.
 */
public final class IafToolMaterials {
    private static final Map<CustomToolMaterial, ToolMaterial> VANILLA = new IdentityHashMap<>();

    private IafToolMaterials() {
    }

    public static synchronized ToolMaterial vanilla(CustomToolMaterial material) {
        if (material.getEnchantmentValue() > 0) {
            return material.toolMaterial();
        }
        return VANILLA.computeIfAbsent(material, m -> new ToolMaterial(m.toolMaterial().incorrectBlocksForDrops(), m.getUses(), m.getSpeed(), m.getAttackDamageBonus(), 1, m.toolMaterial().repairItems()));
    }

    /** Item properties made with {@link #vanilla}: not enchantable at the table when the material says 0 */
    public static Item.Properties properties(Item.Properties properties, CustomToolMaterial material) {
        return material.getEnchantmentValue() > 0 ? properties : properties.component(DataComponents.ENCHANTABLE, null);
    }
}
