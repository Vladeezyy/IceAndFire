package com.github.alexthe666.iceandfire.client;

import com.github.alexthe666.iceandfire.client.model.armor.*;
import com.github.alexthe666.iceandfire.client.render.entity.RenderTideTridentItem;
import com.github.alexthe666.iceandfire.client.render.tile.*;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.item.*;
import com.google.common.base.Suppliers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
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
    private static final Supplier<BlockEntityWithoutLevelRenderer> BLOCK_ITEMS = Suppliers.memoize(() -> new IceAndFireTEISR(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
    private static final Supplier<BlockEntityWithoutLevelRenderer> GAUNTLET = Suppliers.memoize(() -> new RenderDeathWormGauntlet(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
    private static final Supplier<BlockEntityWithoutLevelRenderer> GORGON_HEAD = Suppliers.memoize(() -> new RenderGorgonHead(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
    private static final Supplier<BlockEntityWithoutLevelRenderer> TIDE_TRIDENT = Suppliers.memoize(() -> new RenderTideTridentItem(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));
    private static final Supplier<BlockEntityWithoutLevelRenderer> TROLL_WEAPON = Suppliers.memoize(() -> new RenderTrollWeapon(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

    private IafItemRenderers() {
    }

    /** The renderer of an item drawn in code (builtin/entity model), or null */
    @Nullable
    public static Supplier<BlockEntityWithoutLevelRenderer> itemRenderer(Item item) {
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
