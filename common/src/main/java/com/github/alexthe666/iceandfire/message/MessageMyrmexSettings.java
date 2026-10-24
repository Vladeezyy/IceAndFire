package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageMyrmexSettings implements CustomPacketPayload {
    public static final Type<MessageMyrmexSettings> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "myrmex_settings"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMyrmexSettings> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageMyrmexSettings::read);

    @Override
    public Type<MessageMyrmexSettings> type() {
        return TYPE;
    }

    public int queenID;
    public boolean reproduces;
    public boolean deleteRoom;
    public long roomToDelete;

    public MessageMyrmexSettings(int queenID, boolean repoduces, boolean deleteRoom, long roomToDelete) {
        this.queenID = queenID;
        this.reproduces = repoduces;
        this.deleteRoom = deleteRoom;
        this.roomToDelete = roomToDelete;
    }

    public static MessageMyrmexSettings read(FriendlyByteBuf buf) {
        return new MessageMyrmexSettings(buf.readInt(), buf.readBoolean(), buf.readBoolean(), buf.readLong());
    }

    public static void write(MessageMyrmexSettings message, FriendlyByteBuf buf) {
        buf.writeInt(message.queenID);
        buf.writeBoolean(message.reproduces);
        buf.writeBoolean(message.deleteRoom);
        buf.writeLong(message.roomToDelete);

    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageMyrmexSettings message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.queenID);

                    if (entity instanceof EntityMyrmexBase myrmex) {
                        MyrmexHive hive = myrmex.getHive();

                        if (hive != null) {
                            hive.reproduces = message.reproduces;

                            if (message.deleteRoom) {
                                hive.removeRoom(BlockPos.of(message.roomToDelete));
                            }
                        }
                    }
                }
            });

        }
    }
}