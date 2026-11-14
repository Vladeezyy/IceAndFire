package dev.vladeezyy.iceandfire.fabric;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * The death drops of a living entity, held back for LivingDropsEvent as NeoForge does. A stack of captures: with one
 * slot, a mob that died while another one dropped its loot took over the capture and the first mob's drops were lost.
 */
public final class DropCapture {
    private record Capture(LivingEntity entity, List<ItemEntity> drops) {
    }

    // per thread: death loot drops on the server thread, but nothing here may leak across threads
    private static final ThreadLocal<ArrayDeque<Capture>> CAPTURES = ThreadLocal.withInitial(ArrayDeque::new);

    private DropCapture() {
    }

    public static void start(LivingEntity dying) {
        CAPTURES.get().push(new Capture(dying, new ArrayList<>()));
    }

    /** True if the drop was held back */
    public static boolean capture(Entity dropper, Entity drop) {
        Capture top = CAPTURES.get().peek();
        if (top != null && top.entity() == dropper && drop instanceof ItemEntity item) {
            top.drops().add(item);
            return true;
        }
        return false;
    }

    @Nullable
    public static List<ItemEntity> stop(LivingEntity dying) {
        ArrayDeque<Capture> captures = CAPTURES.get();
        Capture top = captures.peek();
        if (top == null || top.entity() != dying) {
            return null;
        }
        captures.pop();
        return top.drops();
    }
}
