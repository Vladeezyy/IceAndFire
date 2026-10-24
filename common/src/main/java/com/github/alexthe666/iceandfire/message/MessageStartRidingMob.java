package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.ISyncMount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageStartRidingMob implements CustomPacketPayload {
    public static final Type<MessageStartRidingMob> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "start_riding_mob"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageStartRidingMob> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageStartRidingMob::read);

    @Override
    public Type<MessageStartRidingMob> type() {
        return TYPE;
    }

    public int dragonId;
    public boolean ride;
    public boolean baby;

    public MessageStartRidingMob(int dragonId, boolean ride, boolean baby) {
        this.dragonId = dragonId;
        this.ride = ride;
        this.baby = baby;
    }

    public MessageStartRidingMob() {
    }

    public static MessageStartRidingMob read(FriendlyByteBuf buf) {
        return new MessageStartRidingMob(buf.readInt(), buf.readBoolean(), buf.readBoolean());
    }

    public static void write(MessageStartRidingMob message, FriendlyByteBuf buf) {
        buf.writeInt(message.dragonId);
        buf.writeBoolean(message.ride);
        buf.writeBoolean(message.baby);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageStartRidingMob message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    if (entity instanceof ISyncMount && entity instanceof TamableAnimal tamable) {
                        if (tamable.isOwnedBy(player) && tamable.distanceTo(player) < 14) {
                            if (message.ride) {
                                if (message.baby) {
                                    tamable.startRiding(player, true);
                                } else {
                                    player.startRiding(tamable, true);
                                }
                            } else {
                                if (message.baby) {
                                    tamable.stopRiding();
                                } else {
                                    player.stopRiding();
                                }
                            }
                        }
                    }
                }
            });

        }
    }
}