package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityPixieHouse;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;


public class MessageUpdatePixieHouse implements CustomPacketPayload {
    public static final Type<MessageUpdatePixieHouse> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "update_pixie_house"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageUpdatePixieHouse> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageUpdatePixieHouse::read);

    @Override
    public Type<MessageUpdatePixieHouse> type() {
        return TYPE;
    }

    public long blockPos;
    public boolean hasPixie;
    public int pixieType;

    public MessageUpdatePixieHouse(long blockPos, boolean hasPixie, int pixieType) {
        this.blockPos = blockPos;
        this.hasPixie = hasPixie;
        this.pixieType = pixieType;

    }

    public MessageUpdatePixieHouse() {
    }

    public static MessageUpdatePixieHouse read(FriendlyByteBuf buf) {
        return new MessageUpdatePixieHouse(buf.readLong(), buf.readBoolean(), buf.readInt());
    }

    public static void write(MessageUpdatePixieHouse message, FriendlyByteBuf buf) {
        buf.writeLong(message.blockPos);
        buf.writeBoolean(message.hasPixie);
        buf.writeInt(message.pixieType);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageUpdatePixieHouse message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    BlockPos pos = BlockPos.of(message.blockPos);
                    BlockEntity blockEntity = player.level().getBlockEntity(pos);

                    if (blockEntity instanceof TileEntityPixieHouse house) {
                        house.hasPixie = message.hasPixie;
                        house.pixieType = message.pixieType;
                    } else if (blockEntity instanceof TileEntityJar jar) {
                        jar.hasPixie = message.hasPixie;
                        jar.pixieType = message.pixieType;
                    }
                }
            });

        }
    }

}