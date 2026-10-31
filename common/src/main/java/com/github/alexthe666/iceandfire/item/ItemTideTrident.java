package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.client.render.entity.RenderTideTridentItem;
import com.github.alexthe666.iceandfire.entity.EntityTideTrident;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemTideTrident extends TridentItem {

    public ItemTideTrident() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.durability(400).attributes(createAttributes()).component(DataComponents.TOOL, TridentItem.createToolProperties()));
    }


    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving, int timeLeft) {
        if (entityLiving instanceof Player player) {
            int useTicks = this.getUseDuration(stack, entityLiving) - timeLeft;
            if (useTicks >= 10) {
                float riptide = EnchantmentHelper.getTridentSpinAttackStrength(stack, player);
                if (!(riptide > 0.0F) || player.isInWaterOrRain()) {
                    if (stack.getDamageValue() >= stack.getMaxDamage() - 1) {
                        return false;
                    }
                    Holder<SoundEvent> sound = EnchantmentHelper.pickHighestLevel(stack, EnchantmentEffectComponents.TRIDENT_SOUND).orElse(SoundEvents.TRIDENT_THROW);
                    if (!worldIn.isClientSide()) {
                        stack.hurtAndBreak(1, player, entityLiving.getUsedItemHand().asEquipmentSlot());
                        if (riptide == 0.0F) {
                            EntityTideTrident trident = new EntityTideTrident(worldIn, player, stack);
                            trident.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 1.0F);
                            if (player.hasInfiniteMaterials()) {
                                trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                            }

                            worldIn.addFreshEntity(trident);
                            worldIn.playSound(null, trident, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                            if (!player.hasInfiniteMaterials()) {
                                player.getInventory().removeItem(stack);
                            }
                        }
                    }

                    player.awardStat(Stats.ITEM_USED.get(this));
                    if (riptide > 0.0F) {
                        float yRot = player.getYRot();
                        float xRot = player.getXRot();
                        float x = -Mth.sin(yRot * 0.017453292F) * Mth.cos(xRot * 0.017453292F);
                        float y = -Mth.sin(xRot * 0.017453292F);
                        float z = Mth.cos(yRot * 0.017453292F) * Mth.cos(xRot * 0.017453292F);
                        float length = Mth.sqrt(x * x + y * y + z * z);
                        x *= riptide / length;
                        y *= riptide / length;
                        z *= riptide / length;
                        player.push(x, y, z);
                        player.startAutoSpinAttack(20, 8.0F, stack);
                        if (player.onGround()) {
                            player.move(MoverType.SELF, new Vec3(0.0D, 1.1999999284744263D, 0.0D));
                        }

                        worldIn.playSound(null, player, sound.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    }
                }
            }
        }
        return true;
    }

    public static ItemAttributeModifiers createAttributes() {
        return ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, 12.0D, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, -2.9F, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .build();
    }

    // Piercing on top of the trident enchantments, as in the original
    // NeoForge extension method (no vanilla override)
    public boolean supportsEnchantment(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.PIERCING) || enchantment.value().isSupportedItem(stack);
    }

    // NeoForge extension method (no vanilla override)
    public boolean isPrimaryItemFor(@NotNull ItemStack stack, @NotNull Holder<Enchantment> enchantment) {
        return enchantment.is(Enchantments.PIERCING) || enchantment.value().isPrimaryItem(stack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {

        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.tide_trident.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.tide_trident.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
