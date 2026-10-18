package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforge;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;


public class MessageUpdateDragonforge implements CustomPacketPayload {
    public static final Type<MessageUpdateDragonforge> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "update_dragonforge"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageUpdateDragonforge> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageUpdateDragonforge::read);

    @Override
    public Type<MessageUpdateDragonforge> type() {
        return TYPE;
    }

    public long blockPos;
    public int cookTime;

    public MessageUpdateDragonforge(long blockPos, int houseType) {
        this.blockPos = blockPos;
        this.cookTime = houseType;

    }

    public MessageUpdateDragonforge() {
    }

    public static MessageUpdateDragonforge read(FriendlyByteBuf buf) {
        return new MessageUpdateDragonforge(buf.readLong(), buf.readInt());
    }

    public static void write(MessageUpdateDragonforge message, FriendlyByteBuf buf) {
        buf.writeLong(message.blockPos);
        buf.writeInt(message.cookTime);
    }


    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageUpdateDragonforge message, final IPayloadContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow().getReceptionSide() == LogicalSide.CLIENT) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    BlockPos pos = BlockPos.of(message.blockPos);

                    if (player.level().getBlockEntity(pos) instanceof TileEntityDragonforge forge) {
                        forge.cookTime = message.cookTime;

                        if (message.cookTime > 0) {
                            forge.lastDragonFlameTimer = 40;
                        }
                    }
                }
            });

        }
    }
}