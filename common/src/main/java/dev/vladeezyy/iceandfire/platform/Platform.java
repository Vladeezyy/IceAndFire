package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/** What the common code of Ice and Fire needs from the mod loader (NeoForge or Fabric), found by {@link ServiceLoader} */
public interface Platform {
    Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Ice and Fire platform for this mod loader"));

    /** "neoforge" or "fabric" */
    String name();

    Path configDir();

    boolean isModLoaded(String modId);

    /** True in a client (also while it runs the integrated server) */
    boolean isClient();

    /** The mod's version as the loader read it from the jar */
    String modVersion();

    // --- registration ---

    /** Registers {@code factory}'s value under {@code id} (NeoForge: on its register event, Fabric: in registry order at init) */
    <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, Supplier<? extends T> factory);

    // --- network ---

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToAll(CustomPacketPayload payload);

    void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);

    void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload);
}
