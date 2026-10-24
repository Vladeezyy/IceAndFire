package com.github.alexthe666.iceandfire.entity.util;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityMutlipartPart;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EntityUtil {
    /**
     * Adds the part to the level once and keeps it attached to its parent.
     *
     * @return false when the part is gone (its chunk was unloaded or it was killed) and the parent has to create a new one
     */
    public static boolean updatePart(@Nullable final EntityMutlipartPart part, @NotNull final LivingEntity parent) {
        if (part == null || !(parent.level() instanceof ServerLevel serverLevel) || parent.isRemoved()) {
            return true;
        }

        if (part.isRemoved()) {
            return false;
        }

        // Added once: a part in a section without entity access (isAddedToLevel() == false) is still in the level,
        // adding it again every tick spammed "UUID of added entity already exists" (#5089, #5360)
        if (!part.isAddedByParent()) {
            UUID uuid = part.getUUID();
            Entity existing = serverLevel.getEntity(uuid);

            // Update UUID if a different entity with the same UUID exists already
            if (existing != null && existing != part) {
                while (serverLevel.getEntity(uuid) != null) {
                    uuid = Mth.createInsecureUUID(parent.getRandom());
                }

                IceAndFire.LOGGER.debug("Updated the UUID of [{}] due to a clash with [{}]", part, existing);
            }

            part.setUUID(uuid);
            part.setParent(parent);
            if (!serverLevel.addFreshEntity(part)) {
                part.discard();
                return false;
            }
            part.setAddedByParent();
        } else if (!part.isTrackedByLevel()) {
            // Left in a section without entity access while the parent still ticks: it does not tick there, bring it back
            part.setPos(parent.getX(), parent.getY(), parent.getZ());
        }

        part.setParent(parent);
        return true;
    }
}
