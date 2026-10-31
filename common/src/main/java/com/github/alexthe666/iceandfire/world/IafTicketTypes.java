package com.github.alexthe666.iceandfire.world;

import com.github.alexthe666.iceandfire.IceAndFire;
import dev.vladeezyy.iceandfire.platform.Registrar;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.TicketType;

/** Chunk tickets of the mod: 26.x ticket types are registry entries (1.21.1: TicketType.create) */
public final class IafTicketTypes {
    public static final Registrar<TicketType> TICKET_TYPES = Registrar.create(Registries.TICKET_TYPE, IceAndFire.MODID);
    /** Keeps the chunk of a summoned dragon loaded and ticking until it arrived; not saved, never expires by itself */
    public static final RegistryEntry<TicketType, TicketType> SUMMONING_CRYSTAL = TICKET_TYPES.register("summoning_crystal", () -> new TicketType(0L, TicketType.FLAG_LOADING | TicketType.FLAG_SIMULATION));

    private IafTicketTypes() {}
}
