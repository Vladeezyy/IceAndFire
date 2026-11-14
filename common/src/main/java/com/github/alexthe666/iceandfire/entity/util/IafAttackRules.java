package com.github.alexthe666.iceandfire.entity.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Whether a player's item may hit a mob: the gorgon head, the cockatrice scepter and the chain worked through PvP off,
 * claims and other players' pets (they deal no player damage, so neither vanilla nor claim mods saw them)
 */
public final class IafAttackRules {
    private IafAttackRules() {
    }

    /** A tamed mob with an owner other than the player (tamables, horses, dragons: OwnableEntity) */
    public static boolean isForeignPet(Entity target, Player player) {
        if (!(target instanceof OwnableEntity ownable)) {
            return false;
        }
        EntityReference<LivingEntity> owner = ownable.getOwnerReference();
        return owner != null && !owner.getUUID().equals(player.getUUID());
    }

    /**
     * Server side: not someone else's pet, PvP allows hitting a player target, and the loader's attack event (claims)
     * does not refuse. The client knows only the first part.
     */
    public static boolean mayHarm(Player player, Entity target) {
        if (target == player) {
            return true;
        }
        if (isForeignPet(target, player)) {
            return false;
        }
        if (player.level().isClientSide()) {
            return true;
        }
        if (target instanceof Player other && !player.canHarmPlayer(other)) {
            return false;
        }
        return dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.canPlayerAttack(player, target);
    }
}
