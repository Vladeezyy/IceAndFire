package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.event.ServerEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;


public class MessageSwingArm implements CustomPacketPayload {
    public static final Type<MessageSwingArm> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "swing_arm"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSwingArm> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageSwingArm::read);

    @Override
    public Type<MessageSwingArm> type() {
        return TYPE;
    }

    public MessageSwingArm() {

    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(MessageSwingArm message, PacketContext context) {
            Player player = context.player();
            if (player != null) {
                ServerEvents.onLeftClick(player, player.getItemInHand(InteractionHand.MAIN_HAND));
            }
        }
    }


    public static MessageSwingArm read(FriendlyByteBuf buf) {
        return new MessageSwingArm();
    }

    public static void write(MessageSwingArm message, FriendlyByteBuf buf) {
    }

}
