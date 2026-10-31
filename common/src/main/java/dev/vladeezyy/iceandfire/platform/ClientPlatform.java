package dev.vladeezyy.iceandfire.platform;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.material.Fluid;

import java.util.ServiceLoader;

/**
 * What the client code of Ice and Fire needs from the mod loader. Only touched on the client (render code), so the
 * implementation may use client-only loader classes. The defaults are what vanilla does.
 */
public interface ClientPlatform {
    ClientPlatform INSTANCE = ServiceLoader.load(ClientPlatform.class, ClientPlatform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Ice and Fire client platform for this mod loader"));

    /** The model an armor item is drawn with (other mods' items may bring their own) */
    Model armorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original);




    /** The still texture of a fluid on the block atlas */
    Identifier fluidStillTexture(Fluid fluid);

    /** Whether a name tag at this squared distance is drawn (NeoForge: the name tag distance attribute) */
    default boolean isNameplateInRenderDistance(Entity entity, double distanceSq) {
        return distanceSq <= 4096.0D;
    }


    /** What the loader adds to the title screen (NeoForge: the mod count and warnings) */
    default void renderMainMenu(net.minecraft.client.gui.screens.Screen screen, net.minecraft.client.gui.GuiGraphicsExtractor graphics, net.minecraft.client.gui.Font font, int width, int height, int alpha) {
    }


    /** Adds a key binding to the controls */
    void registerKeyMapping(net.minecraft.client.KeyMapping mapping);
}
