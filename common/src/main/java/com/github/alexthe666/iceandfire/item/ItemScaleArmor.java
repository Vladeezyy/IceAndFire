package com.github.alexthe666.iceandfire.item;

import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.resources.ResourceLocation;
import com.github.alexthe666.iceandfire.client.model.armor.ModelFireDragonScaleArmor;
import com.github.alexthe666.iceandfire.client.model.armor.ModelIceDragonScaleArmor;
import com.github.alexthe666.iceandfire.client.model.armor.ModelLightningDragonScaleArmor;
import com.github.alexthe666.iceandfire.entity.DragonType;
import com.github.alexthe666.iceandfire.enums.EnumDragonArmor;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemScaleArmor extends ArmorItem implements IProtectAgainstDragonItem, IafArmorTextureItem {

    public EnumDragonArmor armor_type;
    public EnumDragonEgg eggType;

    public ItemScaleArmor(EnumDragonEgg eggType, EnumDragonArmor armorType, IafArmorMaterial material, ArmorItem.Type slot) {
        super(material.holder(), slot, new Item.Properties().durability(material.getDurabilityForType(slot))/*.tab(IceAndFire.TAB_ITEMS)*/);
        this.armor_type = armorType;
        this.eggType = eggType;
    }

    @Override
    public @NotNull String getDescriptionId() {
        switch (this.type) {
            case HELMET:
                return "item.iceandfire.dragon_helmet";
            case CHESTPLATE:
                return "item.iceandfire.dragon_chestplate";
            case LEGGINGS:
                return "item.iceandfire.dragon_leggings";
            case BOOTS:
                return "item.iceandfire.dragon_boots";
        }
        return "item.iceandfire.dragon_helmet";
    }


    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, ArmorMaterial.Layer layer, boolean innerModel) {
        return ResourceLocation.parse("iceandfire:textures/models/armor/" + armor_type.name() + (slot == EquipmentSlot.LEGS ? "_legs.png" : ".png"));
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("dragon." + eggType.toString().toLowerCase()).withStyle(eggType.color));
        tooltip.add(Component.translatable("item.dragonscales_armor.desc").withStyle(ChatFormatting.GRAY));
    }
}
