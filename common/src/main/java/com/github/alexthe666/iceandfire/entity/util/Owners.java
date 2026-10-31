package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/** 1.21.1's {@code getOwnerUUID()}: since 1.21.5 the owner is an {@link EntityReference} */
public final class Owners {
    private Owners() {}

    @Nullable
    public static UUID uuid(OwnableEntity entity) {
        EntityReference<LivingEntity> owner = entity.getOwnerReference();
        return owner == null ? null : owner.getUUID();
    }

    /** 1.21.1's {@code setOwnerUUID(uuid)} */
    public static void set(net.minecraft.world.entity.TamableAnimal entity, @Nullable UUID uuid) {
        entity.setOwnerReference(uuid == null ? null : EntityReference.of(uuid));
    }
}
