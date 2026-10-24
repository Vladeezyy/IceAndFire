package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** A player attacks an entity; canceling it stops the attack */
public class AttackEntityEvent extends PlayerEvent implements ICancellableEvent {
    private final Entity target;

    public AttackEntityEvent(Player player, Entity target) {
        super(player);
        this.target = target;
    }

    public Entity getTarget() {
        return this.target;
    }
}
