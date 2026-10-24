package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public abstract class PlayerEvent extends LivingEvent {
    protected PlayerEvent(Player player) {
        super(player);
    }

    @Override
    public Player getEntity() {
        return (Player) super.getEntity();
    }

    public static class PlayerLoggedOutEvent extends PlayerEvent {
        public PlayerLoggedOutEvent(Player player) {
            super(player);
        }
    }

    /** A player starts tracking (seeing) an entity (server) */
    public static class StartTracking extends PlayerEvent {
        private final Entity target;

        public StartTracking(Player player, Entity target) {
            super(player);
            this.target = target;
        }

        public Entity getTarget() {
            return this.target;
        }
    }
}
