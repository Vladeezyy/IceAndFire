package com.github.alexthe666.iceandfire.item;

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
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class ItemDeathwormGauntlet extends Item {

    // The lunge state lives in each holder's MiscData (gauntletReceded/Launched/Wear): fields here were shared by all players
    public ItemDeathwormGauntlet() {
        super(new Item.Properties().durability(500)/*.tab(IceAndFire.TAB_ITEMS)*/);
    }


    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
        return 1;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.PASS, itemStackIn);
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
        // The server keeps the state: inventoryTick runs only there
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        EntityDataProvider.getCapability(player).ifPresent(data -> {
            if (data.miscData.gauntletReceded || data.miscData.gauntletLaunched) {
                return;
            }
            CompoundTag tag = ItemNbt.getOrEmpty(stack);

            if (tag.getInt("HolderID") != player.getId()) {
                tag.putInt("HolderID", player.getId());
                ItemNbt.setTag(stack, tag);
            }

            if (player.getCooldowns().getCooldownPercent(this, 0.0F) == 0) {
                player.getCooldowns().addCooldown(this, 10);
                // no excluded player: a sound the server plays from the player skips that player
                level.playSound(null, player.getX(), player.getY(), player.getZ(), IafSoundRegistry.DEATHWORM_ATTACK, player.getSoundSource(), 1F, 1F);
                data.miscData.gauntletLaunched = true;
            }
        });
    }

    @Override
    public void releaseUsing(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity LivingEntity, int timeLeft) {
        EntityDataProvider.getExisting(LivingEntity).ifPresent(data -> {
            if (data.miscData.gauntletWear > 0) {
                stack.hurtAndBreak(data.miscData.gauntletWear, LivingEntity, LivingEntity.getSlotForHand(LivingEntity.getUsedItemHand()));
                data.miscData.gauntletWear = 0;
            }
        });

        CompoundTag tag = ItemNbt.getOrEmpty(stack);

        if (tag.getInt("HolderID") != -1) {
            tag.putInt("HolderID", -1);
            ItemNbt.setTag(stack, tag);
        }
    }

    // NeoForge extension method (no vanilla override)
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level world, @NotNull Entity entity, int itemSlot, boolean isSelected) {
        // 1.21.1 ticks inventories on the client too: the server keeps the lunge state, the client gets lungeTicks synced
        if (world.isClientSide || !(entity instanceof LivingEntity)) {
            return;
        }

        EntityDataProvider.getCapability(entity).ifPresent(data -> {
            int tempLungeTicks = data.miscData.lungeTicks;

            if (data.miscData.gauntletReceded) {
                if (tempLungeTicks > 0) {
                    tempLungeTicks = tempLungeTicks - 4;
                }

                if (tempLungeTicks <= 0) {
                    tempLungeTicks = 0;
                    data.miscData.gauntletReceded = false;
                    data.miscData.gauntletLaunched = false;
                }
            } else if (data.miscData.gauntletLaunched) {
                tempLungeTicks = 4 + tempLungeTicks;

                if (tempLungeTicks > 20) {
                    data.miscData.gauntletReceded = true;
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
                            data.miscData.gauntletWear++;
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
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.iceandfire.deathworm_gauntlet.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
