package com.github.alexthe666.iceandfire.entity.props;

import com.github.alexthe666.iceandfire.IceAndFire;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import dev.vladeezyy.iceandfire.platform.PacketContext;

public record SyncEntityData(int entityId, CompoundTag tag) implements CustomPacketPayload {
    public static final Type<SyncEntityData> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "sync_entity_data"));
    public static final StreamCodec<ByteBuf, SyncEntityData> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, SyncEntityData::entityId,
            ByteBufCodecs.COMPOUND_TAG, SyncEntityData::tag,
            SyncEntityData::new);

    @Override
    public Type<SyncEntityData> type() {
        return TYPE;
    }

    public static void handle(final SyncEntityData message, final PacketContext context) {
        context.enqueueWork(() -> {
            Player localPlayer = context.player();
            EntityDataProvider.getCapability(localPlayer.level().getEntity(message.entityId)).ifPresent(data -> data.deserialize(message.tag));
        });
    }
}
