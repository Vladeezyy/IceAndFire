package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.model.armor.*;
import com.github.alexthe666.iceandfire.client.render.entity.RenderTideTridentItem;
import com.github.alexthe666.iceandfire.client.render.tile.*;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.item.*;
import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import com.github.alexthe666.iceandfire.client.render.IafLegacyItemRenderer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import javax.annotation.Nullable;
import java.util.function.Supplier;

import static com.github.alexthe666.iceandfire.item.IafItemRegistry.*;

/**
 * The armor models and item renderers of the items, formerly their NeoForge client extensions. NeoForge registers them
 * as IClientItemExtensions, Fabric as armor and item renderers.
 */
public final class IafItemRenderers {
    private static final Supplier<IafLegacyItemRenderer> BLOCK_ITEMS = Suppliers.memoize(IceAndFireTEISR::new);
    private static final Supplier<IafLegacyItemRenderer> GAUNTLET = Suppliers.memoize(RenderDeathWormGauntlet::new);
    private static final Supplier<IafLegacyItemRenderer> GORGON_HEAD = Suppliers.memoize(RenderGorgonHead::new);
    private static final Supplier<IafLegacyItemRenderer> TIDE_TRIDENT = Suppliers.memoize(RenderTideTridentItem::new);
    private static final Supplier<IafLegacyItemRenderer> TROLL_WEAPON = Suppliers.memoize(RenderTrollWeapon::new);

    private IafItemRenderers() {
    }

    /** The renderer of a special item model (assets/iceandfire/items: iceandfire:item_renderer) */
    public static IafLegacyItemRenderer renderer(com.github.alexthe666.iceandfire.client.render.IafItemSpecialRenderer.Kind kind) {
        return switch (kind) {
            case BLOCK_ITEMS -> BLOCK_ITEMS.get();
            case GAUNTLET -> GAUNTLET.get();
            case GORGON_HEAD -> GORGON_HEAD.get();
            case TIDE_TRIDENT -> TIDE_TRIDENT.get();
            case TROLL_WEAPON -> TROLL_WEAPON.get();
        };
    }

    /** The renderer of an item drawn in code (builtin/entity model), or null */
    @Nullable
    public static Supplier<IafLegacyItemRenderer> itemRenderer(Item item) {
        if (item instanceof BlockItemWithRender) return BLOCK_ITEMS;
        if (item instanceof ItemDeathwormGauntlet) return GAUNTLET;
        if (item instanceof ItemGorgonHead) return GORGON_HEAD;
        if (item instanceof ItemTideTrident) return TIDE_TRIDENT;
        if (item instanceof ItemTrollWeapon) return TROLL_WEAPON;
        return null;
    }

    /** Whether the item has its own armor model */
    public static boolean hasArmorModel(Item item) {
        return item instanceof ItemCopperArmor || item instanceof ItemSilverArmor || item instanceof ItemDeathwormArmor || item instanceof ItemSeaSerpentArmor
            || item instanceof ItemTrollArmor || item instanceof ItemDragonsteelArmor || item instanceof ItemScaleArmor;
    }

    private static final java.util.Map<Item, HumanoidModel<?>[]> ARMOR_MODELS = new java.util.IdentityHashMap<>();

    /**
     * The armor model of a worn stack, or {@code original}. Built once per item and slot (1.21.1 baked a new model every
     * frame); 26.x poses it from the wearer's render state when it is drawn, so one instance serves every wearer.
     */
    public static net.minecraft.client.model.Model<?> armorModel(net.minecraft.world.item.ItemStack stack, net.minecraft.client.model.Model<?> original) {
        Item item = stack.getItem();
        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable == null || !hasArmorModel(item)) {
            return original;
        }
        EquipmentSlot slot = equippable.slot();
        HumanoidModel<?>[] models = ARMOR_MODELS.computeIfAbsent(item, i -> new HumanoidModel<?>[EquipmentSlot.values().length]);
        HumanoidModel<?> model = models[slot.ordinal()];
        if (model == null) {
            model = armorModel(item, slot, null);
            models[slot.ordinal()] = model;
        }
        return model == null ? original : model;
    }

    /** The texture an armor stack is drawn with: the item's own (IafArmorTextureItem), or {@code fallback} */
    public static net.minecraft.resources.Identifier armorTexture(net.minecraft.world.item.ItemStack stack, net.minecraft.resources.Identifier fallback) {
        net.minecraft.world.item.equipment.Equippable equippable = stack.get(net.minecraft.core.component.DataComponents.EQUIPPABLE);
        if (equippable != null && stack.getItem() instanceof com.github.alexthe666.iceandfire.item.IafArmorTextureItem textured) {
            return textured.getArmorTexture(stack, equippable.slot());
        }
        return fallback;
    }

    /** The armor model of an item, or {@code original} */
    public static HumanoidModel<?> armorModel(Item item, EquipmentSlot slot, HumanoidModel<?> original) {
        if (item instanceof ItemCopperArmor) return new ModelCopperArmor(slot);
        if (item instanceof ItemSilverArmor) return new ModelSilverArmor(slot);
        if (item instanceof ItemDeathwormArmor) return new ModelDeathWormArmor(ModelDeathWormArmor.getBakedModel(slot));
        if (item instanceof ItemSeaSerpentArmor) return new ModelSeaSerpentArmor(slot);
        if (item instanceof ItemTrollArmor) return new ModelTrollArmor(slot);
        if (item instanceof ItemDragonsteelArmor armor) {
            if (armor.iafMaterial() == DRAGONSTEEL_FIRE_ARMOR_MATERIAL) return new ModelDragonsteelFireArmor(slot);
            if (armor.iafMaterial() == DRAGONSTEEL_ICE_ARMOR_MATERIAL) return new ModelDragonsteelIceArmor(slot);
            if (armor.iafMaterial() == DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL) return new ModelDragonsteelLightningArmor(slot);
        }
        if (item instanceof ItemScaleArmor armor) {
            DragonType dragonType = armor.armor_type.eggType.dragonType;
            if (dragonType == DragonType.FIRE) return new ModelFireDragonScaleArmor(slot);
            if (dragonType == DragonType.ICE) return new ModelIceDragonScaleArmor(slot);
            if (dragonType == DragonType.LIGHTNING) return new ModelLightningDragonScaleArmor(slot);
        }
        return original;
    }
}
