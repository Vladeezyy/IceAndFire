package com.github.alexthe666.iceandfire.entity.props;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

/**
 * Access to the Ice and Fire data of living entities.
 * <p>
 * The original cached the capabilities in static maps by entity id, which were read and written from several threads
 * and crashed servers (issue #5495). The data is attached to the entity (NeoForge attachments, Fabric data attachments), so no cache is needed.
 */
public class EntityDataProvider {

    public static Optional<EntityData> getCapability(final Entity entity) {
        if (entity instanceof LivingEntity living) {
            return Optional.of(dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.entityData(living));
        }
        return Optional.empty();
    }

    /** Only the data that already exists, without creating it. */
    public static Optional<EntityData> getExisting(final Entity entity) {
        if (entity instanceof LivingEntity living) {
            return dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.existingEntityData(living);
        }
        return Optional.empty();
    }
}
