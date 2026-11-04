package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;

/** Chunk tickets of the mod: 26.x ticket types are registry entries (1.21.1: TicketType.create) */
public final class IafTicketTypes {
    public static final Registrar<TicketType> TICKET_TYPES = Registrar.create(Registries.TICKET_TYPE, IceAndFire.MODID);
    /** Ticks a summon waits for its dragon: it may be gone from the last known position */
    public static final long SUMMON_TIMEOUT = 2_000L;

    /** Keeps the chunk of a summoned dragon loaded and ticking until it arrived; not saved, expires if the dragon never comes */
    public static final RegistryEntry<TicketType, TicketType> SUMMONING_CRYSTAL = TICKET_TYPES.register("summoning_crystal", () -> new TicketType(SUMMON_TIMEOUT, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));

    private IafTicketTypes() {}
}
