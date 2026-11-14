package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.ISyncMount;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;


public class MessageStartRidingMob implements CustomPacketPayload {
    public static final Type<MessageStartRidingMob> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "start_riding_mob"));
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
                boolean fromClient = context.flow() != net.minecraft.network.protocol.PacketFlow.CLIENTBOUND;

                if (!fromClient) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                } else if (message.ride) {
                    // A client may only get off: it seated forced riders past the saddle, the passenger limit and walls.
                    // Mounting goes through the click on the mob (mobInteract).
                    return;
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    if (entity instanceof ISyncMount && entity instanceof TamableAnimal tamable) {
                        if (tamable.isOwnedBy(player) && tamable.distanceTo(player) < 14) {
                            if (message.ride) {
                                if (message.baby) {
                                    tamable.startRiding(player, true, true);
                                } else {
                                    player.startRiding(tamable, true, true);
                                }
                            } else {
                                if (message.baby) {
                                    if (!fromClient || tamable.getVehicle() == player) {
                                        tamable.stopRiding();
                                    }
                                } else if (!fromClient || player.getVehicle() == tamable) {
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