package com.github.alexthe666.iceandfire.message;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import com.github.alexthe666.iceandfire.item.ItemMyrmexStaff;
import com.github.alexthe666.iceandfire.util.ItemNbt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import java.util.UUID;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;


public class MessageGetMyrmexHive implements CustomPacketPayload {
    public static final Type<MessageGetMyrmexHive> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "get_myrmex_hive"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MessageGetMyrmexHive> CODEC = StreamCodec.of((buf, message) -> write(message, buf), MessageGetMyrmexHive::read);

    @Override
    public Type<MessageGetMyrmexHive> type() {
        return TYPE;
    }

    public CompoundTag hive;

    public MessageGetMyrmexHive(CompoundTag hive) {
        this.hive = hive;
    }

    public MessageGetMyrmexHive() {
    }

    public static MessageGetMyrmexHive read(FriendlyByteBuf buf) {
        return new MessageGetMyrmexHive(buf.readNbt());
    }

    public static void write(MessageGetMyrmexHive message, FriendlyByteBuf buf) {
        buf.writeNbt(message.hive);
    }

    public static class Handler {
        public Handler() {
        }

        public static void handle(final MessageGetMyrmexHive message, final PacketContext context) {

            context.enqueueWork(() -> {
                MyrmexHive receivedHive = MyrmexHive.fromNBT(message.hive);
                if (context.flow() == net.minecraft.network.protocol.PacketFlow.CLIENTBOUND) {
                    CompoundTag tag = new CompoundTag();
                    receivedHive.writeVillageDataToNBT(tag);
                    receivedHive.readVillageDataFromNBT(tag);
                    IceAndFire.PROXY.setReferencedHive(receivedHive);
                    return;
                }

                // Edits of the staff screens: only from a player who holds the staff of this hive and may command it
                Player player = context.player();
                MyrmexHive realHive = receivedHive.hiveUUID == null ? null : MyrmexWorldData.get(player.level()).getHiveFromUUID(receivedHive.hiveUUID);
                if (realHive == null || !holdsStaffOf(player, realHive.hiveUUID)) {
                    return;
                }
                if (player.isCreative() || realHive.canPlayerCommandHive(player.getUUID())) {
                    realHive.applyStaffEdits(receivedHive, player);
                }
            });

        }
    }

    private static boolean holdsStaffOf(Player player, UUID hive) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof ItemMyrmexStaff && ItemNbt.view(stack).hasUUID("HiveUUID") && hive.equals(ItemNbt.view(stack).getUUID("HiveUUID"))) {
                return true;
            }
        }
        return false;
    }
}
