package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessagePlayerHitMultipart implements CustomPacketPayload {
    public static final Type<MessagePlayerHitMultipart> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "player_hit_multipart"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessagePlayerHitMultipart> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessagePlayerHitMultipart::read);

    @Override
    public Type<MessagePlayerHitMultipart> type() {
        return TYPE;
    }
    public int creatureID;
    public int extraData;

    public MessagePlayerHitMultipart(int creatureID) {
        this.creatureID = creatureID;
        this.extraData = 0;
    }

    public MessagePlayerHitMultipart(int creatureID, int extraData) {
        this.creatureID = creatureID;
        this.extraData = extraData;
    }

    public MessagePlayerHitMultipart() {
    }

    public static MessagePlayerHitMultipart read(FriendlyByteBuf buf) {
        return new MessagePlayerHitMultipart(buf.readInt(), buf.readInt());
    }

    public static void write(MessagePlayerHitMultipart message, FriendlyByteBuf buf) {
        buf.writeInt(message.creatureID);
        buf.writeInt(message.extraData);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessagePlayerHitMultipart message, final IPayloadContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow().getReceptionSide() == LogicalSide.CLIENT) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.creatureID);

                    if (entity instanceof LivingEntity livingEntity) {
                        double dist = player.distanceTo(livingEntity);

                        if (dist < 100) {
                            player.attack(livingEntity);

                            if (livingEntity instanceof EntityHydra hydra) {
                                hydra.triggerHeadFlags(message.extraData);
                            }
                        }
                    }
                }
            });

        }
    }
}
