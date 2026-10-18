package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageMultipartInteract implements CustomPacketPayload {
    public static final Type<MessageMultipartInteract> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "multipart_interact"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageMultipartInteract> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageMultipartInteract::read);

    @Override
    public Type<MessageMultipartInteract> type() {
        return TYPE;
    }

    public int creatureID;
    public float dmg;

    public MessageMultipartInteract(int creatureID, float dmg) {
        this.creatureID = creatureID;
        this.dmg = dmg;
    }

    public MessageMultipartInteract() {
    }

    public static MessageMultipartInteract read(FriendlyByteBuf buf) {
        return new MessageMultipartInteract(buf.readInt(), buf.readFloat());
    }

    public static void write(MessageMultipartInteract message, FriendlyByteBuf buf) {
        buf.writeInt(message.creatureID);
        buf.writeFloat(message.dmg);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageMultipartInteract message, final IPayloadContext context) {

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
                            if (message.dmg > 0F) {
                                livingEntity.hurt(player.level().damageSources().mobAttack(player), message.dmg);
                            } else {
                                livingEntity.interact(player, InteractionHand.MAIN_HAND);
                            }
                        }
                    }
                }
            });

        }
    }
}