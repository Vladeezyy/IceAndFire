package dev.vladeezyy.iceandfire.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** The death drops of a living entity, held back for LivingDropsEvent as NeoForge does (server thread only) */
public final class DropCapture {
    @Nullable
    private static LivingEntity entity;
    @Nullable
    private static List<ItemEntity> drops;

    private DropCapture() {
    }

    public static void start(LivingEntity dying) {
        entity = dying;
        drops = new ArrayList<>();
    }

    /** True if the drop was held back */
    public static boolean capture(Entity dropper, Entity drop) {
        if (dropper == entity && drops != null && drop instanceof ItemEntity item) {
            drops.add(item);
            return true;
        }
        return false;
    }

    @Nullable
    public static List<ItemEntity> stop(LivingEntity dying) {
        if (dying != entity) {
            return null;
        }
        List<ItemEntity> captured = drops;
        entity = null;
        drops = null;
        return captured;
    }
}
