package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.equipment.ArmorType;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemDragonArmor extends Item {

    public DragonArmorType type;
    public int dragonSlot;
    public String name;
    private Pattern baseName = Pattern.compile("[a-z]+_[a-z]+");

    public ItemDragonArmor(DragonArmorType type, int dragonSlot) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
        this.type = type;
        this.dragonSlot = dragonSlot;
        if (type == DragonArmorType.FIRE || type == DragonArmorType.ICE || type == DragonArmorType.LIGHTNING)
            baseName = Pattern.compile("[a-z]+_[a-z]+_[a-z]+");
    }

    @Override
    public @NotNull String getDescriptionId() {
        String fullName = BuiltInRegistries.ITEM.getKey(this).getPath();
        Matcher matcher = baseName.matcher(fullName);
        name = matcher.find() ? matcher.group() : fullName;
        return "item.iceandfire." + name;
    }

    static String getNameForSlot(int slot){
        return switch (slot) {
            case 0 -> "head";
            case 1 -> "neck";
            case 2 -> "body";
            case 3 -> "tail";
            default -> "";
        };
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        String words = switch (dragonSlot) {
            case 1 -> "dragon.armor_neck";
            case 2 -> "dragon.armor_body";
            case 3 -> "dragon.armor_tail";
            default -> "dragon.armor_head";
        };
        tooltip.accept(Component.translatable(words).withStyle(ChatFormatting.GRAY));
    }

    public enum DragonArmorType {
        IRON,
        GOLD,
        DIAMOND,
        SILVER,
        FIRE,
        ICE,
        COPPER,
        LIGHTNING
    }

}
