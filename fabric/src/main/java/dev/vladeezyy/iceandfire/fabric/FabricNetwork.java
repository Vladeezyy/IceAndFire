package dev.vladeezyy.iceandfire.fabric;

import dev.vladeezyy.iceandfire.platform.PacketContext;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/** The handlers of the packets to the client, registered with Fabric's client networking by the client entry point */
final class FabricNetwork {
    static final Map<CustomPacketPayload.Type<?>, BiConsumer<?, PacketContext>> CLIENT_HANDLERS = new LinkedHashMap<>();

    private FabricNetwork() {
    }
}
