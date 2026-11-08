package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;


public class MessageSetMyrmexHiveNull implements CustomPacketPayload {
    public static final Type<MessageSetMyrmexHiveNull> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "set_myrmex_hive_null"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSetMyrmexHiveNull> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageSetMyrmexHiveNull::read);

    @Override
    public Type<MessageSetMyrmexHiveNull> type() {
        return TYPE;
    }

    public MessageSetMyrmexHiveNull() {
    }

    public static MessageSetMyrmexHiveNull read(FriendlyByteBuf buf) {
        return new MessageSetMyrmexHiveNull();
    }

    public static void write(MessageSetMyrmexHiveNull message, FriendlyByteBuf buf) {
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(MessageSetMyrmexHiveNull message, PacketContext context) {
            // on the client thread, as MessageGetMyrmexHive: the staff screens read the hive while they draw
            context.enqueueWork(() -> {
                Player player = context.player();
                if (player != null) {
                    IceAndFire.PROXY.setReferencedHive(null);
                }
            });
        }
    }
}