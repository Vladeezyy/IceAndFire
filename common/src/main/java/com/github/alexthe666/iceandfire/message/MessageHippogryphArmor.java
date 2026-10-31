package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;


public class MessageHippogryphArmor implements CustomPacketPayload {
    public static final Type<MessageHippogryphArmor> TYPE = new Type<>(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "hippogryph_armor"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageHippogryphArmor> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageHippogryphArmor::read);

    @Override
    public Type<MessageHippogryphArmor> type() {
        return TYPE;
    }

    public int dragonId;
    public int slot_index;
    public int armor_type;

    public MessageHippogryphArmor(int dragonId, int slot_index, int armor_type) {
        this.dragonId = dragonId;
        this.slot_index = slot_index;
        this.armor_type = armor_type;
    }

    public MessageHippogryphArmor() {
    }

    public static MessageHippogryphArmor read(FriendlyByteBuf buf) {
        return new MessageHippogryphArmor(buf.readInt(), buf.readInt(), buf.readInt());
    }

    public static void write(MessageHippogryphArmor message, FriendlyByteBuf buf) {
        buf.writeInt(message.dragonId);
        buf.writeInt(message.slot_index);
        buf.writeInt(message.armor_type);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageHippogryphArmor message, final PacketContext context) {

            context.enqueueWork(() -> {
                Player player = context.player();

                if (player != null) {
                    Entity entity = player.level().getEntity(message.dragonId);

                    // The values of the client are not trusted (any client could saddle and armor any hippogryph
                    // without the items): the server reads its own inventory of the creature
                    if (entity == null || player.distanceToSqr(entity) > 64 * 64) {
                        return;
                    }
                    if (entity instanceof EntityHippogryph hippogryph) {
                        hippogryph.refreshInventory();
                    } else if (entity instanceof EntityHippocampus hippo) {
                        hippo.updateContainerEquipment();
                    }
                }
            });

        }
    }
}