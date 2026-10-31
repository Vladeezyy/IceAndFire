package com.github.alexthe666.iceandfire.item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import com.github.alexthe666.iceandfire.client.render.tile.RenderDeathWormGauntlet;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemDeathwormGauntlet extends Item {

    private boolean deathwormReceded = true;
    private boolean deathwormLaunched = false;
    private int specialDamage = 0;

    public ItemDeathwormGauntlet() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().durability(500)/*.tab(IceAndFire.TAB_ITEMS)*/);
    }


    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return InteractionResult.PASS;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
        if (!deathwormReceded && !deathwormLaunched) {
            if (entity instanceof Player player) {
                CompoundTag tag = ItemNbt.getOrEmpty(stack);

                if (tag.getIntOr("HolderID", 0) != player.getId()) {
                    tag.putInt("HolderID", player.getId());
                    ItemNbt.setTag(stack, tag);
                }

                if (player.getCooldowns().getCooldownPercent(stack, 0.0F) == 0) {
                    player.getCooldowns().addCooldown(stack, 10);
                    player.playSound(IafSoundRegistry.DEATHWORM_ATTACK, 1F, 1F);
                    deathwormReceded = false;
                    deathwormLaunched = true;
                }
            }
        }
    }

    @Override
    public boolean releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity LivingEntity, int timeLeft) {
        if (specialDamage > 0) {
            stack.hurtAndBreak(specialDamage, LivingEntity, LivingEntity.getUsedItemHand().asEquipmentSlot());
            specialDamage = 0;
        }

        CompoundTag tag = ItemNbt.getOrEmpty(stack);

        if (tag.getIntOr("HolderID", 0) != -1) {
            tag.putInt("HolderID", -1);
            ItemNbt.setTag(stack, tag);
        }
        return true;
    }

    // NeoForge extension method (no vanilla override)
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        EntityDataProvider.getCapability(entity).ifPresent(data -> {
            int tempLungeTicks = data.miscData.lungeTicks;

            if (deathwormReceded) {
                if (tempLungeTicks > 0) {
                    tempLungeTicks = tempLungeTicks - 4;
                }

                if (tempLungeTicks <= 0) {
                    tempLungeTicks = 0;
                    deathwormReceded = false;
                    deathwormLaunched = false;
                }
            } else if (deathwormLaunched) {
                tempLungeTicks = 4 + tempLungeTicks;

                if (tempLungeTicks > 20) {
                    deathwormReceded = true;
                }
            }

            if (data.miscData.lungeTicks == 20) {
                if (entity instanceof Player player) {
                    Vec3 Vector3d = player.getViewVector(1.0F).normalize();
                    double range = 5;

                    for (LivingEntity livingEntity : world.getEntitiesOfClass(LivingEntity.class, new AABB(player.getX() - range, player.getY() - range, player.getZ() - range, player.getX() + range, player.getY() + range, player.getZ() + range))) {
                        //Let's not pull/hit ourselves
                        if (livingEntity == entity) {
                            continue;
                        }

                        Vec3 Vector3d1 = new Vec3(livingEntity.getX() - player.getX(), livingEntity.getY() - player.getY(), livingEntity.getZ() - player.getZ());
                        double d0 = Vector3d1.length();
                        Vector3d1 = Vector3d1.normalize();
                        double d1 = Vector3d.dot(Vector3d1);
                        boolean canSee = d1 > 1.0D - 0.5D / d0 && player.hasLineOfSight(livingEntity);

                        if (canSee) {
                            specialDamage++;
                            livingEntity.hurt(entity.level().damageSources().playerAttack((Player) entity), 3F);
                            livingEntity.knockback(0.5F, livingEntity.getX() - player.getX(), livingEntity.getZ() - player.getZ());
                        }
                    }
                }
            }

            data.miscData.setLungeTicks(tempLungeTicks);
        });
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
