package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.client.render.tile.RenderTrollWeapon;
import com.github.alexthe666.iceandfire.enums.EnumTroll;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemTrollWeapon extends Item implements IafTieredItem {

    public EnumTroll.Weapon weapon = EnumTroll.Weapon.AXE;

    public ItemTrollWeapon(EnumTroll.Weapon weapon) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().sword(IafItemRegistry.TROLL_WEAPON_TOOL_MATERIAL.toolMaterial(), 15, -3.5F));
        this.weapon = weapon;
    }


    // NeoForge extension method (no vanilla override)
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return player.getAttackStrengthScale(0) < 0.95 || player.attackAnim != 0;
    }

    public boolean onEntitySwing(LivingEntity LivingEntity, ItemStack stack) {
        if (LivingEntity instanceof Player) {
            Player player = (Player) LivingEntity;
            if (player.getAttackStrengthScale(0) < 1 && player.attackAnim > 0) {
                return true;
            } else {
                player.swingTime = -1;
            }
        }
        return false;
    }

    public void onUpdate(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof Player && isSelected) {
            Player player = (Player) entityIn;
            if (player.getAttackStrengthScale(0) < 0.95 && player.attackAnim > 0) {
                player.swingTime--;
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
    }


    @Override
    public CustomToolMaterial getTier() {
        return IafItemRegistry.TROLL_WEAPON_TOOL_MATERIAL;
    }
}
