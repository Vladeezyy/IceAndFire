package com.github.alexthe666.iceandfire.item;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import com.github.alexthe666.iceandfire.message.MessageGetMyrmexHive;
import com.github.alexthe666.iceandfire.message.MessageSetMyrmexHiveNull;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ItemMyrmexStaff extends Item {

    public ItemMyrmexStaff(boolean jungle) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps().stacksTo(1));
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Player player) {
        ItemNbt.setTag(itemStack, new CompoundTag());
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!ItemNbt.hasTag(stack)) {
            ItemNbt.setTag(stack, new CompoundTag());
            ItemNbt.update(stack, tag -> tag.store("HiveUUID", UUIDUtil.CODEC, new UUID(0, 0)));
        }
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        if (playerIn.isShiftKeyDown()) {
            return super.use(worldIn, playerIn, hand);
        }
        if (ItemNbt.hasTag(itemStackIn) && ItemNbt.view(itemStackIn).read("HiveUUID", UUIDUtil.CODEC).isPresent()) {
            UUID id = ItemNbt.view(itemStackIn).read("HiveUUID", UUIDUtil.CODEC).orElse(null);
            if (!worldIn.isClientSide()) {
                MyrmexHive hive = MyrmexWorldData.get(worldIn).getHiveFromUUID(id);
                if (hive != null) {
                    IceAndFire.sendMSGToPlayer(new MessageGetMyrmexHive(hive.toNBT()), (ServerPlayer) playerIn);
                } else {
                    IceAndFire.sendMSGToPlayer(new MessageSetMyrmexHiveNull(), (ServerPlayer) playerIn);
                }
            } else if (id != null && !id.equals(new UUID(0, 0))) {
                IceAndFire.PROXY.openMyrmexStaffGui(itemStackIn);
            }
        }
        playerIn.swing(hand, net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
        return InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (!context.getPlayer().isShiftKeyDown()) {
            return super.useOn(context);
        } else {
            CompoundTag tag = ItemNbt.getTag(context.getPlayer().getItemInHand(context.getHand()));
            if (tag != null && tag.read("HiveUUID", UUIDUtil.CODEC).isPresent()) {
                UUID id = tag.read("HiveUUID", UUIDUtil.CODEC).orElse(null);
                if (!context.getLevel().isClientSide()) {
                    MyrmexHive hive = MyrmexWorldData.get(context.getLevel()).getHiveFromUUID(id);
                    if (hive != null) {
                        IceAndFire.sendMSGToPlayer(new MessageGetMyrmexHive(hive.toNBT()), (ServerPlayer) context.getPlayer());
                    } else {
                        IceAndFire.sendMSGToPlayer(new MessageSetMyrmexHiveNull(), (ServerPlayer) context.getPlayer());
                    }
                } else if (id != null && !id.equals(new UUID(0, 0))) {
                    IceAndFire.PROXY.openMyrmexAddRoomGui(context.getPlayer().getItemInHand(context.getHand()), context.getClickedPos(), context.getPlayer().getDirection());
                }
            }
            context.getPlayer().swing(context.getHand(), net.minecraft.world.item.component.SwingAnimation.DEFAULT, true);
            return InteractionResult.SUCCESS;
        }
    }
}
