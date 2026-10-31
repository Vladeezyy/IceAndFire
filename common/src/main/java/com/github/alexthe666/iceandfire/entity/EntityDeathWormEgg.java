package com.github.alexthe666.iceandfire.entity;

import net.minecraft.network.RegistryFriendlyByteBuf;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

public class EntityDeathWormEgg extends ThrowableItemProjectile {

    // A synced flag instead of NeoForge's spawn data (IEntityWithComplexSpawn): the same on both loaders
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> GIANT = net.minecraft.network.syncher.SynchedEntityData.defineId(EntityDeathWormEgg.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);

    public EntityDeathWormEgg(EntityType<? extends ThrowableItemProjectile> type, Level worldIn) {
        super(type, worldIn);
    }

    public EntityDeathWormEgg(EntityType<? extends ThrowableItemProjectile> type, LivingEntity throwerIn, Level worldIn,
                              boolean giant) {
        super(type, throwerIn, worldIn);
        this.entityData.set(GIANT, giant);
    }

    public EntityDeathWormEgg(EntityType<? extends ThrowableItemProjectile> type, double x, double y, double z,
                              Level worldIn, boolean giant) {
        super(type, x, y, z, worldIn);
        this.entityData.set(GIANT, giant);
    }


    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.@NotNull Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GIANT, false);
    }

    private boolean isGiant() {
        // getDefaultItem runs while the entity data is built (false then, as the field of the original)
        return this.entityData != null && this.entityData.get(GIANT);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            for (int i = 0; i < 8; ++i) {
                this.level().addParticle(new ItemParticleOption(ParticleTypes.ITEM, this.getItem()), this.getX(), this.getY(), this.getZ(), (this.random.nextFloat() - 0.5D) * 0.08D, (this.random.nextFloat() - 0.5D) * 0.08D, (this.random.nextFloat() - 0.5D) * 0.08D);
            }
        }
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    @Override
    protected void onHit(HitResult result) {
        Entity thrower = getOwner();
        if (result.getType() == HitResult.Type.ENTITY) {
            ((EntityHitResult) result).getEntity().hurt(level().damageSources().thrown(this, thrower), 0.0F);
        }

        if (!this.level().isClientSide()) {
            float wormSize = 0.25F + (float) (Math.random() * 0.35F);

            EntityDeathWorm deathworm = new EntityDeathWorm(IafEntityRegistry.DEATH_WORM.get(), this.level());
            deathworm.setVariant(random.nextInt(3));
            deathworm.setTame(true, true);
            deathworm.setWormHome(blockPosition());
            deathworm.setWormAge(1);
            deathworm.setDeathWormScale(isGiant() ? (wormSize * 4) : wormSize);
            deathworm.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
            if (thrower instanceof Player) {
                deathworm.setOwnerUUID(thrower.getUUID());
            }
            this.level().addFreshEntity(deathworm);

            this.level().broadcastEntityEvent(this, (byte) 3);
            this.remove(RemovalReason.DISCARDED);
        }
    }

    @Override
    protected @NotNull Item getDefaultItem() {
        return isGiant() ? IafItemRegistry.DEATHWORM_EGG_GIGANTIC.get() : IafItemRegistry.DEATHWORM_EGG.get();
    }
}
