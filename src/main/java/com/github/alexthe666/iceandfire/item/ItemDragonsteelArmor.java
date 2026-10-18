package com.github.alexthe666.iceandfire.item;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.resources.ResourceLocation;
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
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

import static com.github.alexthe666.iceandfire.item.IafItemRegistry.*;

public class ItemDragonsteelArmor extends ArmorItem implements IProtectAgainstDragonItem, IafClientItem{

    private final IafArmorMaterial material;

    public ItemDragonsteelArmor(IafArmorMaterial material, int renderIndex, ArmorItem.Type slot) {
        super(material.holder(), slot, new Item.Properties().durability(material.getDurabilityForType(slot))/*.tab(IceAndFire.TAB_ITEMS)*/);
        this.material = material;
    }

    @Override
    public void registerClientExtensions(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity LivingEntity, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
                boolean inner = armorSlot == EquipmentSlot.LEGS || armorSlot == EquipmentSlot.HEAD;
                if (itemStack.getItem() instanceof ArmorItem) {
                    if (material == DRAGONSTEEL_FIRE_ARMOR_MATERIAL)
                        return new ModelDragonsteelFireArmor(inner);
                    if (material == DRAGONSTEEL_ICE_ARMOR_MATERIAL)
                        return new ModelDragonsteelIceArmor(inner);
                    if (material == DRAGONSTEEL_LIGHTNING_ARMOR_MATERIAL)
                        return new ModelDragonsteelLightningArmor(inner);
                }
                return _default;

            }
        });
    }


    // The defense comes from the config, which loads after the items are registered
    @Override
    public @NotNull ItemAttributeModifiers getDefaultAttributeModifiers(@NotNull ItemStack stack) {
        EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(this.type.getSlot());
        ResourceLocation id = ResourceLocation.withDefaultNamespace("armor." + this.type.getName());
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
        return super.getMaxDamage(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getDefense() {
        return this.material.getDefenseForType(this.getType());
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        if (material == DRAGONSTEEL_FIRE_ARMOR_MATERIAL) {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_dragonsteel_fire" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else if (material == DRAGONSTEEL_ICE_ARMOR_MATERIAL) {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_dragonsteel_ice" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        } else {
            return ResourceLocation.parse("iceandfire:textures/models/armor/armor_dragonsteel_lightning" + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
        }
    }
}
