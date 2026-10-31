package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public class EntityTideTrident extends ThrownTrident {

    private static final int ADDITIONALPIERCING = 2;
    private int entitiesHit = 0;

    public EntityTideTrident(EntityType<? extends ThrownTrident> type, Level worldIn) {
        super(type, worldIn);
    }

    public EntityTideTrident(Level worldIn, LivingEntity thrower, ItemStack thrownStackIn) {
        this(IafEntityRegistry.TIDE_TRIDENT.get(), worldIn);
        this.setPos(thrower.getX(), thrower.getEyeY() - 0.1F, thrower.getZ());
        this.setOwner(thrower);
        this.setPickupItemStack(thrownStackIn.copy());
        this.entityData.set(ID_LOYALTY, this.getLoyaltyFromItem(thrownStackIn));
        this.entityData.set(ID_FOIL, thrownStackIn.hasFoil());
        if (worldIn instanceof ServerLevel serverLevel) {
            this.setPierceLevel((byte) EnchantmentHelper.getPiercingCount(serverLevel, thrownStackIn, thrownStackIn));
        }
    }

    @Override
    protected @NotNull ItemStack getDefaultPickupItem() {
        return new ItemStack(IafItemRegistry.TIDE_TRIDENT.get());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity entity = result.getEntity();
        float f = 12.0F;
        Entity entity1 = this.getOwner();
        DamageSource damagesource = level().damageSources().trident(this, entity1 == null ? this : entity1);
        if (this.level() instanceof ServerLevel serverLevel) {
            f = EnchantmentHelper.modifyDamage(serverLevel, this.getWeaponItem(), entity, damagesource, f);
        }
        entitiesHit++;
        if (entitiesHit >= getMaxPiercing())
            this.dealtDamage = true;
        if (entity.hurtOrSimulate(damagesource, f)) {
            if (entity.getType() == EntityTypes.ENDERMAN) {
                return;
            }

            // Channeling and the other enchantments of 1.21 act here
            if (this.level() instanceof ServerLevel serverLevel) {
                EnchantmentHelper.doPostAttackEffectsWithItemSource(serverLevel, entity, damagesource, this.getWeaponItem());
            }

            if (entity instanceof LivingEntity livingentity1) {
                this.doKnockback(livingentity1, damagesource);
                this.doPostHurtEffects(livingentity1);
            }
        }

        this.playSound(SoundEvents.TRIDENT_HIT, 1.0F, 1.0F);
    }

    private int getMaxPiercing() {
        return ADDITIONALPIERCING + getPierceLevel();
    }

}
