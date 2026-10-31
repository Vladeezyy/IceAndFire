package com.github.alexthe666.iceandfire.entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import com.github.alexthe666.iceandfire.entity.util.IDreadMob;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class EntityDreadHorse extends SkeletonHorse implements IDreadMob {

    protected static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> COMMANDER_UNIQUE_ID = SynchedEntityData.defineId(EntityDreadHorse.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

    public EntityDreadHorse(EntityType type, Level worldIn) {
        super(type, worldIn);
    }


    public static AttributeSupplier.Builder bakeAttributes() {
        return createBaseHorseAttributes()
            //HEALTH
            .add(Attributes.MAX_HEALTH, 25.0D)
            //SPEED
            .add(Attributes.MOVEMENT_SPEED, 0.3D)
            //ARMOR
            .add(Attributes.ARMOR, 4.0D);
    }


    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(COMMANDER_UNIQUE_ID, Optional.empty());
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        if (this.getCommanderId() != null) {
            compound.store("CommanderUUID", UUIDUtil.CODEC, this.getCommanderId());
        }
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        UUID uuid;
        if (compound.read("CommanderUUID", UUIDUtil.CODEC).isPresent()) {
            uuid = compound.read("CommanderUUID", UUIDUtil.CODEC).orElseThrow();
        } else {
            String s = compound.getStringOr("CommanderUUID", "");
            uuid = OldUsersConverter.convertMobOwnerIfNecessary(this.level().getServer(), s);
        }

        if (uuid != null) {
            try {
                this.setCommanderId(uuid);
            } catch (Throwable throwable) {
            }
        }

    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        SpawnGroupData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
        this.setAge(24000);
        return data;
    }

    @Override
    protected boolean considersEntityAsAlly(@NotNull Entity entityIn) {
        return entityIn instanceof IDreadMob || super.considersEntityAsAlly(entityIn);
    }

    @Nullable
    public UUID getCommanderId() {
        return this.entityData.get(COMMANDER_UNIQUE_ID).map(EntityReference::getUUID).orElse(null);
    }

    public void setCommanderId(@Nullable UUID uuid) {
        this.entityData.set(COMMANDER_UNIQUE_ID, (Optional.ofNullable(uuid)).map(EntityReference::of));
    }

    @Override
    public Entity getCommander() {
        try {
            UUID uuid = this.getCommanderId();
            return uuid == null ? null : this.level().getPlayerByUUID(uuid);
        } catch (IllegalArgumentException var2) {
            return null;
        }
    }
}
