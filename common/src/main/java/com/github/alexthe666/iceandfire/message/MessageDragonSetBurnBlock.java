package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public class MessageDragonSetBurnBlock implements CustomPacketPayload {
    public static final Type<MessageDragonSetBurnBlock> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "dragon_set_burn_block"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDragonSetBurnBlock> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageDragonSetBurnBlock::read);

    @Override
    public Type<MessageDragonSetBurnBlock> type() {
        return TYPE;
    }
    public int dragonId;
    public boolean breathingFire;
    public int posX;
    public int posY;
    public int posZ;

    public MessageDragonSetBurnBlock(int dragonId, boolean breathingFire, BlockPos pos) {
        this.dragonId = dragonId;
        this.breathingFire = breathingFire;
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();
    }

    public static MessageDragonSetBurnBlock read(FriendlyByteBuf buf) {
        return new MessageDragonSetBurnBlock(buf.readInt(), buf.readBoolean(), new BlockPos(buf.readInt(), buf.readInt(), buf.readInt()));
    }

    public static void write(MessageDragonSetBurnBlock message, FriendlyByteBuf buf) {
        buf.writeInt(message.dragonId);
        buf.writeBoolean(message.breathingFire);
        buf.writeInt(message.posX);
        buf.writeInt(message.posY);
        buf.writeInt(message.posZ);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageDragonSetBurnBlock message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    if (entity instanceof EntityDragonBase dragon) {
                        dragon.setBreathingFire(message.breathingFire);
                        dragon.burningTarget = new BlockPos(message.posX, message.posY, message.posZ);
                    }
                }
            });

        }
    }
}