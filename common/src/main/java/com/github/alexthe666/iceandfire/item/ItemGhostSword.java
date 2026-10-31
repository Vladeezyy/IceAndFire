package com.github.alexthe666.iceandfire.item;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.alexthe666.iceandfire.entity.EntityGhostSword;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemGhostSword extends Item implements IafTieredItem {

    private final CustomToolMaterial tier;

    public ItemGhostSword() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().sword(IafItemRegistry.GHOST_SWORD_TOOL_MATERIAL.toolMaterial(), 5, -1.0F));
        this.tier = IafItemRegistry.GHOST_SWORD_TOOL_MATERIAL;
    }

    public static void spawnGhostSwordEntity(ItemStack stack, Player playerEntity) {
        if (playerEntity.getCooldowns().isOnCooldown(stack))
            return;
        if (playerEntity.getItemInHand(InteractionHand.MAIN_HAND) != stack)
            return;
        double totalDmg = 0D;
        for (ItemAttributeModifiers.Entry entry : dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.attributeModifiers(stack).modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND)) {
                totalDmg += entry.modifier().amount();
            }
        }
        playerEntity.playSound(SoundEvents.ZOMBIE_INFECT, 1, 1);
        EntityGhostSword shot = new EntityGhostSword(IafEntityRegistry.GHOST_SWORD.get(), playerEntity.level(), playerEntity, totalDmg * 0.5F);
        shot.shootFromRotation(playerEntity, playerEntity.getXRot(), playerEntity.getYRot(), 0.0F, 1, 0.5f);
        playerEntity.level().addFreshEntity(shot);
        stack.hurtAndBreak(1, playerEntity, EquipmentSlot.MAINHAND);
        playerEntity.getCooldowns().addCooldown(stack, 10);
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity targetEntity, @NotNull LivingEntity attacker) {
        super.hurtEnemy(stack, targetEntity, attacker);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.ghost_sword.desc_0").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public CustomToolMaterial getTier() {
        return tier;
    }
}
