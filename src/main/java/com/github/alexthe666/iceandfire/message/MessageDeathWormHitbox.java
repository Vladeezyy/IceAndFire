package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.LogicalSide;



public class MessageDeathWormHitbox implements CustomPacketPayload {
    public static final Type<MessageDeathWormHitbox> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "death_worm_hitbox"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageDeathWormHitbox> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageDeathWormHitbox::read);

    @Override
    public Type<MessageDeathWormHitbox> type() {
        return TYPE;
    }

    public int deathWormId;
    public float scale;

    public MessageDeathWormHitbox(int deathWormId, float scale) {
        this.deathWormId = deathWormId;
        this.scale = scale;
    }

    public MessageDeathWormHitbox() {
    }

    public static MessageDeathWormHitbox read(FriendlyByteBuf buf) {
        return new MessageDeathWormHitbox(buf.readInt(), buf.readFloat());
    }

    public static void write(MessageDeathWormHitbox message, FriendlyByteBuf buf) {
        buf.writeInt(message.deathWormId);
        buf.writeFloat(message.scale);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageDeathWormHitbox message, final IPayloadContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow().getReceptionSide() == LogicalSide.CLIENT) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    Entity entity = player.level().getEntity(message.deathWormId);

                    if (entity instanceof EntityDeathWorm deathWorm) {
                        deathWorm.initSegments(message.scale);
                    }
                }
            });

        }
    }
}