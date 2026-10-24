package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityJar;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageUpdatePixieJar implements CustomPacketPayload {
    public static final Type<MessageUpdatePixieJar> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "update_pixie_jar"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageUpdatePixieJar> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageUpdatePixieJar::read);

    @Override
    public Type<MessageUpdatePixieJar> type() {
        return TYPE;
    }

    public long blockPos;
    public boolean isProducing;

    public MessageUpdatePixieJar(long blockPos, boolean isProducing) {
        this.blockPos = blockPos;
        this.isProducing = isProducing;

    }

    public MessageUpdatePixieJar() {
    }

    public static MessageUpdatePixieJar read(FriendlyByteBuf buf) {
        return new MessageUpdatePixieJar(buf.readLong(), buf.readBoolean());
    }

    public static void write(MessageUpdatePixieJar message, FriendlyByteBuf buf) {
        buf.writeLong(message.blockPos);
        buf.writeBoolean(message.isProducing);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageUpdatePixieJar message, final IPayloadContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow().getReceptionSide() == LogicalSide.CLIENT) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    BlockPos pos = BlockPos.of(message.blockPos);

                    if (player.level().getBlockEntity(pos) instanceof TileEntityJar jar) {
                        jar.hasProduced = message.isProducing;
                    }
                }
            });

        }
    }
}