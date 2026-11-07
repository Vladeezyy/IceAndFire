package com.github.alexthe666.iceandfire.entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.phys.Vec3;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class EntityMutlipartPart extends Entity {

    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> PARENT_UUID = SynchedEntityData.defineId(EntityMutlipartPart.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Float> SCALE_WIDTH = SynchedEntityData.defineId(EntityMutlipartPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> SCALE_HEIGHT = SynchedEntityData.defineId(EntityMutlipartPart.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> PART_YAW = SynchedEntityData.defineId(EntityMutlipartPart.class, EntityDataSerializers.FLOAT);
    /** Ids of the parts made on the client (see the constructor); far from the statues' copies (RenderStoneStatue, -1 - n) */
    private static final AtomicInteger CLIENT_IDS = new AtomicInteger(-1_000_000);
    public EntityDimensions multipartSize;
    protected float radius;
    protected float angleYaw;
    protected float offsetY;
    protected float damageMultiplier;
    /** Set when the parent added this part; parts are not saved (noSave), a part loaded from an old world has no owner. */
    private boolean addedByParent;
    /** In a ticking section of its level (ServerLevelEntityCallbacksMixin; NeoForge's isAddedToLevel before) */
    private boolean trackedByLevel;

    protected EntityMutlipartPart(EntityType<?> t, Level world) {
        super(t, world);
        multipartSize = t.getDimensions();
    }

    @Override
    protected void readAdditionalSaveData(ValueInput compound) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput compound) {

    }

    @Override
    protected void doWaterSplashEffect() {

    }

    public EntityMutlipartPart(EntityType<?> t, Entity parent, float radius, float angleYaw, float offsetY, float sizeX,
        float sizeY, float damageMultiplier) {
        super(t, parent.level());
        if (parent.level().isClientSide()) {
            // The parent makes its parts on the client as well (resetParts in its constructor, again on its first tick and when
            // it grows), but those are never added to the client level: the server's parts come in their own spawn packets.
            // From 26.2 an entity takes its id from the level, and the client level gives 0, so getId(), equals() and hashCode()
            // threw: mods that hook Entity.remove and look the entity up (LambDynLights) crashed the client on the first tick of
            // a dragon (CurseForge report 2026-11-08). The server never sends a negative id.
            this.setId(CLIENT_IDS.getAndDecrement());
        }
        this.setParent(parent);
        this.setScaleX(sizeX);
        this.setScaleY(sizeY);
        this.radius = radius;
        this.angleYaw = (angleYaw + 90.0F) * ((float) Math.PI / 180.0F);
        this.offsetY = offsetY;

        this.damageMultiplier = damageMultiplier;
    }


    public static AttributeSupplier.Builder bakeAttributes() {
        return Mob.createMobAttributes()
            //HEALTH
            .add(Attributes.MAX_HEALTH, 2D)
            //SPEED
            .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    @Override
    public @NotNull EntityDimensions getDimensions(@NotNull Pose poseIn) {
        return EntityDimensions.scalable(getScaleX(), getScaleY());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PARENT_UUID, Optional.empty());
        builder.define(SCALE_WIDTH, 0.5F);
        builder.define(SCALE_HEIGHT, 0.5F);
        builder.define(PART_YAW, 0F);
    }

    @Nullable
    public UUID getParentId() {
        return this.entityData.get(PARENT_UUID).map(EntityReference::getUUID).orElse(null);
    }

    public void setParentId(@Nullable UUID uniqueId) {
        this.entityData.set(PARENT_UUID, (Optional.ofNullable(uniqueId)).map(EntityReference::of));
    }

    private float getScaleX() {
        return this.entityData.get(SCALE_WIDTH);
    }

    private void setScaleX(float scale) {
        this.entityData.set(SCALE_WIDTH, scale);
    }

    private float getScaleY() {
        return this.entityData.get(SCALE_HEIGHT);
    }

    private void setScaleY(float scale) {
        this.entityData.set(SCALE_HEIGHT, scale);
    }

    public float getPartYaw() {
        return this.entityData.get(PART_YAW);
    }

    private void setPartYaw(float yaw) {
        this.entityData.set(PART_YAW, yaw % 360);
    }

    /** A hitbox has no use for water: this was a block scan for every part of every dragon, every tick */
    @Override
    protected boolean updateFluidInteraction() {
        return false;
    }

    @Override
    public void tick() {
        if (!this.addedByParent && !this.level().isClientSide()) {
            // Saved with the chunk by 1.20.1: the parent creates its own parts, this one only doubled the hitbox
            this.discard();
            return;
        }
        wasTouchingWater = false;
        if (this.tickCount > 10) {
            Entity parent = getParent();
            refreshDimensions();
            if (parent != null && !level().isClientSide()) {
                float renderYawOffset = parent.getYRot();
                if (parent instanceof LivingEntity) {
                    renderYawOffset = ((LivingEntity) parent).yBodyRot;
                }
                if (isSlowFollow()) {
                    this.setPos(parent.xo + this.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)), parent.yo + this.offsetY, parent.zo + this.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)));
                    double d0 = parent.getX() - this.getX();
                    double d1 = parent.getY() - this.getY();
                    double d2 = parent.getZ() - this.getZ();
                    float f2 = -((float) (Mth.atan2(d1, Mth.sqrt((float) (d0 * d0 + d2 * d2))) * (180F / (float) Math.PI)));
                    this.setXRot(this.limitAngle(this.getXRot(), f2, 5.0F));
                    this.setYRot(renderYawOffset);
                    this.setPartYaw(getYRot());
                } else {
                    this.setPos(parent.getX() + this.radius * Mth.cos((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)), parent.getY() + this.offsetY, parent.getZ() + this.radius * Mth.sin((float) (renderYawOffset * (Math.PI / 180.0F) + this.angleYaw)));
                }
                // No markHurt() here: it sent a motion packet for every part every tick to every player tracking it
                // (200 packets per second per dragon); the position goes with the usual position updates
                if (!this.level().isClientSide()) {
                    this.collideWithNearbyEntities();
                }
                if (parent.isRemoved() && !level().isClientSide()) {
                    this.remove(RemovalReason.DISCARDED);
                }
            } else if (tickCount > 20 && !level().isClientSide()) {
                remove(RemovalReason.DISCARDED);
            }
        }
        super.tick();
    }

    protected boolean isSlowFollow(){
        return false;
    }

    /** Source: {@link net.minecraft.world.entity.ai.control.MoveControl#rotlerp(float, float, float)} */
    protected float limitAngle(float sourceAngle, float targetAngle, float maximumChange) {
        float f = Mth.wrapDegrees(targetAngle - sourceAngle);
        if (f > maximumChange) {
            f = maximumChange;
        }

        if (f < -maximumChange) {
            f = -maximumChange;
        }

        float f1 = sourceAngle + f;
        if (f1 < 0.0F) {
            f1 += 360.0F;
        } else if (f1 > 360.0F) {
            f1 -= 360.0F;
        }

        return f1;
    }


    @Override
    public void remove(@NotNull RemovalReason reason) {
        super.remove(RemovalReason.DISCARDED);
    }

    public Entity getParent() {
        UUID id = getParentId();

        if (id != null && level() instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(id);
        }

        return null;
    }

    public void setParent(Entity entity) {
        this.setParentId(entity.getUUID());
    }

    @Override
    public boolean is(@NotNull Entity entity) {
        return this == entity || this.getParent() == entity;
    }

    @Override
    public boolean isPickable() {
        return true;
    }


    public void collideWithNearbyEntities() {
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().expandTowards(0.20000000298023224D, 0.0D, 0.20000000298023224D));
        Entity parent = this.getParent();
        if (parent != null) {
            entities.stream().filter(entity -> entity != parent && !sharesRider(parent, entity) && !(entity instanceof EntityMutlipartPart) && entity.isPushable()).forEach(entity -> entity.push(parent));

        }
    }

    public static boolean sharesRider(Entity parent, Entity entityIn) {
        for (Entity entity : parent.getPassengers()) {
            if (entity.equals(entityIn)) {
                return true;
            }

            if (sharesRider(entity, entityIn)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NotNull InteractionResult interact(Player player, InteractionHand hand, Vec3 location) {
        // Server only: the client has no parent (the interact packet of the vanilla client reaches this on the server)
        Entity parent = getParent();
        if (parent == null) {
            return InteractionResult.PASS;
        }
        InteractionResult result = parent.interact(player, hand, location);
        // Player.interactOn offers the held item (dragon horn, lead...) only to living entities and a part is not one:
        // a click on a part never picked the dragon up with the horn (#5196)
        if (!result.consumesAction() && parent instanceof LivingEntity living) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.isEmpty()) {
                ItemStack copy = stack.copy();
                if (player.getAbilities().instabuild) {
                    stack = copy;
                }
                result = stack.interactLivingEntity(player, living, hand);
                if (result.consumesAction()) {
                    this.level().gameEvent(GameEvent.ENTITY_INTERACT, living.position(), GameEvent.Context.of(player));
                    if (stack.isEmpty() && !player.getAbilities().instabuild) {
                        dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.onPlayerDestroyItem(player, copy, hand);
                        player.setItemInHand(hand, ItemStack.EMPTY);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity parent = getParent();
        return parent != null && !this.isInvulnerableTo(source) && parent.hurtOrSimulate(source, damage * this.damageMultiplier);
    }

    // Entity has no isInvulnerableTo on 26.x (LivingEntity's only); hurtServer checks it
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.DROWN) || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.LAVA) || source.is(DamageTypeTags.IS_FIRE) || this.isInvulnerableToBase(source);
    }

    public boolean isAddedByParent() {
        return this.addedByParent;
    }

    public boolean isTrackedByLevel() {
        return trackedByLevel;
    }

    public void setTrackedByLevel(boolean tracked) {
        this.trackedByLevel = tracked;
    }

    public void setAddedByParent() {
        this.addedByParent = true;
    }
}
