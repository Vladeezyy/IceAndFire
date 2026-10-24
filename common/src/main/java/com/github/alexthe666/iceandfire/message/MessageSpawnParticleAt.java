package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;


public class MessageSpawnParticleAt implements CustomPacketPayload {
    public static final Type<MessageSpawnParticleAt> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "spawn_particle_at"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageSpawnParticleAt> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageSpawnParticleAt::read);

    @Override
    public Type<MessageSpawnParticleAt> type() {
        return TYPE;
    }
    private double x;
    private double y;
    private double z;
    private int particleType;

    public MessageSpawnParticleAt() {
    }

    public MessageSpawnParticleAt(double x, double y, double z, int particleType) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.particleType = particleType;
    }

    public static MessageSpawnParticleAt read(FriendlyByteBuf buf) {
        return new MessageSpawnParticleAt(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt());
    }

    public static void write(MessageSpawnParticleAt message, FriendlyByteBuf buf) {
        buf.writeDouble(message.x);
        buf.writeDouble(message.y);
        buf.writeDouble(message.z);
        buf.writeInt(message.particleType);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageSpawnParticleAt message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    player = IceAndFire.PROXY.getClientSidePlayer();
                }

                if (player != null) {
                    ItemStack mainHand = player.getMainHandItem();

                    if (!mainHand.isEmpty() && mainHand.getItem() == IafItemRegistry.DRAGON_DEBUG_STICK.get()) {
                        player.level().addParticle(ParticleTypes.SMOKE, message.x, message.y, message.z, 0, 0, 0);
                    }
                }
            });

        }
    }
}
