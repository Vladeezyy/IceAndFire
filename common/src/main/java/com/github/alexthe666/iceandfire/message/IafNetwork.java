package com.github.alexthe666.iceandfire.message;

import dev.vladeezyy.iceandfire.platform.PacketContext;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.function.BiConsumer;

/** The mod's packets; each loader registers them with its network API (version "1") */
public final class IafNetwork {
    public static final String PROTOCOL_VERSION = "1";

    private IafNetwork() {
    }

    /** Receives the packets: one direction per message (the original accepted every message from both sides) */
    public interface PacketRegistrar {
        <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler);

        <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler);

        default <T extends CustomPacketPayload> void both(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
            this.toClient(type, codec, handler);
            this.toServer(type, codec, handler);
        }
    }

    public static void register(PacketRegistrar registrar) {
        registrar.toClient(MessageDeathWormHitbox.TYPE, MessageDeathWormHitbox.CODEC, MessageDeathWormHitbox.Handler::handle);
        registrar.toClient(MessageDragonSetBurnBlock.TYPE, MessageDragonSetBurnBlock.CODEC, MessageDragonSetBurnBlock.Handler::handle);
        registrar.toClient(MessageSetMyrmexHiveNull.TYPE, MessageSetMyrmexHiveNull.CODEC, MessageSetMyrmexHiveNull.Handler::handle);
        registrar.toClient(MessageSirenSong.TYPE, MessageSirenSong.CODEC, MessageSirenSong.Handler::handle);
        registrar.toClient(MessageSpawnParticleAt.TYPE, MessageSpawnParticleAt.CODEC, MessageSpawnParticleAt.Handler::handle);
        registrar.toClient(MessageUpdateDragonforge.TYPE, MessageUpdateDragonforge.CODEC, MessageUpdateDragonforge.Handler::handle);
        registrar.toClient(MessageUpdatePixieHouse.TYPE, MessageUpdatePixieHouse.CODEC, MessageUpdatePixieHouse.Handler::handle);
        registrar.toClient(MessageUpdatePixieHouseModel.TYPE, MessageUpdatePixieHouseModel.CODEC, MessageUpdatePixieHouseModel.Handler::handle);
        registrar.toClient(MessageUpdatePixieJar.TYPE, MessageUpdatePixieJar.CODEC, MessageUpdatePixieJar.Handler::handle);
        registrar.toClient(MessageUpdatePodium.TYPE, MessageUpdatePodium.CODEC, MessageUpdatePodium.Handler::handle);
        registrar.toServer(MessageDragonControl.TYPE, MessageDragonControl.CODEC, MessageDragonControl.Handler::handle);
        registrar.toServer(MessageHippogryphArmor.TYPE, MessageHippogryphArmor.CODEC, MessageHippogryphArmor.Handler::handle);
        registrar.toServer(MessageSwingArm.TYPE, MessageSwingArm.CODEC, MessageSwingArm.Handler::handle);
        registrar.toClient(MessageDragonSyncFire.TYPE, MessageDragonSyncFire.CODEC, MessageDragonSyncFire.Handler::handle);
        registrar.both(MessageGetMyrmexHive.TYPE, MessageGetMyrmexHive.CODEC, MessageGetMyrmexHive.Handler::handle);
        registrar.both(MessageStartRidingMob.TYPE, MessageStartRidingMob.CODEC, MessageStartRidingMob.Handler::handle);
        registrar.toClient(MessageUpdateLectern.TYPE, MessageUpdateLectern.CODEC, MessageUpdateLectern.Handler::handle);
        registrar.toClient(MessageSyncPath.TYPE, MessageSyncPath.CODEC, MessageSyncPath::handle);
        registrar.toClient(MessageSyncPathReached.TYPE, MessageSyncPathReached.CODEC, MessageSyncPathReached::handle);
        registrar.toClient(SyncEntityData.TYPE, SyncEntityData.CODEC, SyncEntityData::handle);
    }
}
