package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageDragonSyncFire implements CustomPacketPayload {
    public static final Type<MessageDragonSyncFire> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "dragon_sync_fire"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDragonSyncFire> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageDragonSyncFire::read);

    @Override
    public Type<MessageDragonSyncFire> type() {
        return TYPE;
    }

    public int dragonId;
    public double posX;
    public double posY;
    public double posZ;
    public int syncType;

    public MessageDragonSyncFire(int dragonId, double posX, double posY, double posZ, int syncType) {
        this.dragonId = dragonId;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.syncType = syncType;
    }

    public MessageDragonSyncFire() {
    }

    public static MessageDragonSyncFire read(FriendlyByteBuf buf) {
        return new MessageDragonSyncFire(buf.readInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void write(MessageDragonSyncFire message, FriendlyByteBuf buf) {
        buf.writeInt(message.dragonId);
        buf.writeDouble(message.posX);
        buf.writeDouble(message.posY);
        buf.writeDouble(message.posZ);
        buf.writeInt(message.syncType);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageDragonSyncFire message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    if (entity instanceof EntityDragonBase dragon) {
                        dragon.stimulateFire(message.posX, message.posY, message.posZ, message.syncType);
                    }
                }
            });

        }
    }

}
