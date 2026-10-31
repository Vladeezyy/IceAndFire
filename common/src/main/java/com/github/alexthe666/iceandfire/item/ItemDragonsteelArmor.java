package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.resources.Identifier;
import com.github.alexthe666.iceandfire.client.model.armor.ModelDragonsteelFireArmor;
import com.github.alexthe666.iceandfire.client.model.armor.ModelDragonsteelIceArmor;
import com.github.alexthe666.iceandfire.client.model.armor.ModelDragonsteelLightningArmor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.github.alexthe666.iceandfire.item.IafItemRegistry.*;

public class ItemDragonsteelArmor extends IafArmorItem implements IProtectAgainstDragonItem, IafArmorTextureItem, IafStackStatsItem {

    private final IafArmorMaterial material;

    public ItemDragonsteelArmor(IafArmorMaterial material, int renderIndex, ArmorType slot) {
        super(material, slot);
        this.material = material;
    }

    public IafArmorMaterial iafMaterial() {
        return material;
    }



    // The defense comes from the config, which loads after the items are registered
    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(this.type.getSlot());
        Identifier id = Identifier.withDefaultNamespace("armor." + this.type.getName());
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(id, material.getDefenseForType(this.type), AttributeModifier.Operation.ADD_VALUE), group)
                .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(id, material.getToughness(), AttributeModifier.Operation.ADD_VALUE), group)
                .build();
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        if (this.type != null) {
            return material.getDurabilityForType(this.type);
        }
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.MAX_DAMAGE, 0);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getDefense() {
        return this.material.getDefenseForType(this.getType());
    }

    @Override
    public Identifier getArmorTexture(ItemStack stack, EquipmentSlot slot) {
        if (material == DRAGONSTEEL_FIRE_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/armor_dragonsteel_fire" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else if (material == DRAGONSTEEL_ICE_ARMOR_MATERIAL) {
            return Identifier.parse("iceandfire:textures/models/armor/armor_dragonsteel_ice" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else {
            return Identifier.parse("iceandfire:textures/models/armor/armor_dragonsteel_lightning" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        }
    }
}
