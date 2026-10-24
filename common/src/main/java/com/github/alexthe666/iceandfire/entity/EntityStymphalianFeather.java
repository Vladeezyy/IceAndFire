package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jetbrains.annotations.NotNull;

public class EntityStymphalianFeather extends AbstractArrow {

    public EntityStymphalianFeather(EntityType<? extends AbstractArrow> t, Level worldIn) {
        super(t, worldIn);
    }

    public EntityStymphalianFeather(EntityType<? extends AbstractArrow> t, Level worldIn, LivingEntity shooter) {
        super(t, shooter, worldIn, new ItemStack(IafItemRegistry.STYMPHALIAN_BIRD_FEATHER.get()), null);
        this.setBaseDamage(IafConfig.stymphalianBirdFeatherAttackStength);
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(reason);
        if (IafConfig.stymphalianBirdFeatherDropChance > 0) {
            if (this.level().isClientSide && this.random.nextInt(IafConfig.stymphalianBirdFeatherDropChance) == 0) {
                this.spawnAtLocation(getPickupItem(), 0.1F);
            }
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (this.tickCount > 100) {
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHit) {
        Entity shootingEntity = this.getOwner();
        if (shootingEntity instanceof EntityStymphalianBird && entityHit.getEntity() != null && entityHit.getEntity() instanceof EntityStymphalianBird) {
            return;
        } else {
            super.onHitEntity(entityHit);
            if (entityHit.getEntity() != null && entityHit.getEntity() instanceof EntityStymphalianBird) {
                LivingEntity LivingEntity = (LivingEntity) entityHit.getEntity();
                LivingEntity.setArrowCount(LivingEntity.getArrowCount() - 1);
                ItemStack itemstack1 = LivingEntity.isUsingItem() ? LivingEntity.getUseItem() : ItemStack.EMPTY;
                if (itemstack1.getItem().canPerformAction(itemstack1, ItemAbilities.SHIELD_BLOCK)) {
                    damageShield(LivingEntity, 1.0F);
                }
            }

        }
    }

    protected void damageShield(LivingEntity entity, float damage) {
        if (damage >= 3.0F && entity.getUseItem().getItem().canPerformAction(entity.getUseItem(), ItemAbilities.SHIELD_BLOCK)) {
            ItemStack copyBeforeUse = entity.getUseItem().copy();
            int i = 1 + Mth.floor(damage);
            InteractionHand Hand = entity.getUsedItemHand();
            entity.getUseItem().hurtAndBreak(i, entity, LivingEntity.getSlotForHand(Hand));
            if (entity.getUseItem().isEmpty()) {
                if (entity instanceof Player) {
                    net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem((Player) entity, copyBeforeUse, Hand);
                }

                if (Hand == net.minecraft.world.InteractionHand.MAIN_HAND) {
                    entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                } else {
                    entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                }
                this.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
            }
        }
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(IafItemRegistry.STYMPHALIAN_BIRD_FEATHER.get());
    }
}
