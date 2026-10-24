package dev.vladeezyy.iceandfire.bus;

import net.minecraft.server.MinecraftServer;

public class ServerAboutToStartEvent extends Event {
    private final MinecraftServer server;

    public ServerAboutToStartEvent(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {
        return this.server;
    }
}
