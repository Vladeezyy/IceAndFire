package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.entity.EntitySpawnReason;
import dev.vladeezyy.rampart.server.item.CustomToolMaterial;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.event.ServerEvents;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;


public interface DragonSteelOverrides<T extends Item & IafTieredItem> {

    /**
     * Kept for compatibility
     *
     * @deprecated use data pack overrides instead
     */
    @Deprecated
    ItemAttributeModifiers bakeDragonsteel();

    default float getAttackDamage(T item) {
        double damage = 0;
        for (ItemAttributeModifiers.Entry entry : item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE)) {
                damage += entry.modifier().amount();
            }
        }
        return (float) damage;
    }

    default boolean isDragonsteel(CustomToolMaterial tier) {
        return DragonSteelTier.isDragonsteel(tier);
    }

    default boolean isDragonsteelFire(CustomToolMaterial tier) {
        return tier == DragonSteelTier.DRAGONSTEEL_TIER_FIRE;
    }

    default boolean isDragonsteelIce(CustomToolMaterial tier) {
        return tier == DragonSteelTier.DRAGONSTEEL_TIER_ICE;
    }

    default boolean isDragonsteelLightning(CustomToolMaterial tier) {
        return tier == DragonSteelTier.DRAGONSTEEL_TIER_LIGHTNING;
    }

    default void hurtEnemy(T item, ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (item.getTier() == IafItemRegistry.SILVER_TOOL_MATERIAL) {
            if (target.is(EntityTypeTags.UNDEAD)) {
                target.hurt(attacker.level().damageSources().magic(), getAttackDamage(item) + 3.0F);
            }
        }

        if (item.getTier() == IafItemRegistry.MYRMEX_CHITIN_TOOL_MATERIAL) {
            if (!target.is(EntityTypeTags.ARTHROPOD)) {
                target.hurt(attacker.level().damageSources().generic(), getAttackDamage(item) + 5.0F);
            }
            if (target instanceof EntityDeathWorm) {
                target.hurt(attacker.level().damageSources().generic(), getAttackDamage(item) + 5.0F);
            }
        }
        if (isDragonsteelFire(item.getTier()) && IafConfig.dragonWeaponFireAbility) {
            target.igniteForSeconds(15);
            target.knockback(1F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ(), attacker.damageSources().mobAttack(attacker), 0.0F);
        }
        if (isDragonsteelIce(item.getTier()) && IafConfig.dragonWeaponIceAbility) {
            EntityDataProvider.getCapability(target).ifPresent(data -> data.frozenData.setFrozen(target, 300));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 300, 2));
            target.knockback(1F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ(), attacker.damageSources().mobAttack(attacker), 0.0F);
        }
        if (isDragonsteelLightning(item.getTier()) && IafConfig.dragonWeaponLightningAbility) {
            boolean flag = true;
            if (attacker instanceof Player) {
                if (attacker.getSwingAnimation(0) > 0.2) {
                    flag = false;
                }
            }
            if (!attacker.level().isClientSide() && flag) {
                LightningBolt lightningboltentity = EntityTypes.LIGHTNING_BOLT.create(target.level(), EntitySpawnReason.MOB_SUMMONED);
                lightningboltentity.entityTags().add(ServerEvents.BOLT_DONT_DESTROY_LOOT);
                lightningboltentity.entityTags().add(attacker.getStringUUID());
                lightningboltentity.snapTo(target.position());
                if (!target.level().isClientSide()) {
                    target.level().addFreshEntity(lightningboltentity);
                }
            }
            target.knockback(1F, attacker.getX() - target.getX(), attacker.getZ() - target.getZ(), attacker.damageSources().mobAttack(attacker), 0.0F);
        }

    }

    default void appendHoverText(CustomToolMaterial tier, ItemStack stack, @Nullable Item.TooltipContext worldIn, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        if (tier == IafItemRegistry.SILVER_TOOL_MATERIAL) {
            tooltip.accept(Component.translatable("silvertools.hurt").withStyle(ChatFormatting.GREEN));
        }
        if (tier == IafItemRegistry.MYRMEX_CHITIN_TOOL_MATERIAL) {
            tooltip.accept(Component.translatable("myrmextools.hurt").withStyle(ChatFormatting.GREEN));
        }
        if (isDragonsteelFire(tier) && IafConfig.dragonWeaponFireAbility) {
            tooltip.accept(Component.translatable("dragon_sword_fire.hurt2").withStyle(ChatFormatting.DARK_RED));
        }
        if (isDragonsteelIce(tier) && IafConfig.dragonWeaponIceAbility) {
            tooltip.accept(Component.translatable("dragon_sword_ice.hurt2").withStyle(ChatFormatting.AQUA));
        }
        if (isDragonsteelLightning(tier) && IafConfig.dragonWeaponLightningAbility) {
            tooltip.accept(Component.translatable("dragon_sword_lightning.hurt2").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}
