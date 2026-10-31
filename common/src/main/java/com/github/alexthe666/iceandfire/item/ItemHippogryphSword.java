package com.github.alexthe666.iceandfire.item;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHippogryphSword extends Item implements IafTieredItem {

    private final CustomToolMaterial tier;

    public ItemHippogryphSword() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().sword(IafItemRegistry.HIPPOGRYPH_SWORD_TOOL_MATERIAL.toolMaterial(), 3, -2.4F));
        this.tier = IafItemRegistry.HIPPOGRYPH_SWORD_TOOL_MATERIAL;
    }

    @Override
    public void hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity targetEntity, LivingEntity attacker) {
        float f = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
        float f3 = 1.0F + (float) attacker.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) * f;
        if (attacker instanceof Player) {
            Player player = (Player) attacker;
            for (LivingEntity LivingEntity : attacker.level().getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(1.0D, 0.25D, 1.0D))) {
                if (LivingEntity != player && LivingEntity != targetEntity && !attacker.isAlliedTo(LivingEntity) && attacker.distanceToSqr(LivingEntity) < 9.0D) {
                    LivingEntity.knockback(0.4F, Mth.sin(attacker.getYRot() * 0.017453292F), -Mth.cos(attacker.getYRot() * 0.017453292F));
                    LivingEntity.hurt(attacker.level().damageSources().playerAttack(player), f3);
                }
            }
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, player.getSoundSource(), 1.0F, 1.0F);
            // 1.21.1's Player.sweepAttack (private in 26.x): the sweep particle in front of the player
            if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                double d0 = -Mth.sin(player.getYRot() * ((float) Math.PI / 180F));
                double d1 = Mth.cos(player.getYRot() * ((float) Math.PI / 180F));
                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, player.getX() + d0, player.getY(0.5D), player.getZ() + d1, 0, d0, 0.0D, d1, 0.0D);
            }
        }
        super.hurtEnemy(stack, targetEntity, attacker);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hippogryph_sword.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.hippogryph_sword.desc_1").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public CustomToolMaterial getTier() {
        return tier;
    }
}
