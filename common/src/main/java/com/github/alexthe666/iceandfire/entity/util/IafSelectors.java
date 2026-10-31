package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import javax.annotation.Nullable;
import java.util.function.Predicate;

/** 1.21.2+: target goals take a {@link TargetingConditions.Selector} (target and server level) instead of a predicate */
public final class IafSelectors {
    private IafSelectors() {}

    @Nullable
    public static TargetingConditions.Selector of(@Nullable Predicate<? super LivingEntity> predicate) {
        return predicate == null ? null : (target, level) -> predicate.test(target);
    }
}
