package dev.vladeezyy.iceandfire.bus;

import net.minecraft.world.entity.Mob;

/** A mob finishes spawning (natural, spawner, egg...), before it joins the level */
public class FinalizeSpawnEvent extends LivingEvent {
    public FinalizeSpawnEvent(Mob mob) {
        super(mob);
    }

    @Override
    public Mob getEntity() {
        return (Mob) super.getEntity();
    }
}
