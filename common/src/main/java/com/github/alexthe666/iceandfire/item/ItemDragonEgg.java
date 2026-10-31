package com.github.alexthe666.iceandfire.item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import net.minecraft.core.component.DataComponents;
import com.github.alexthe666.iceandfire.util.ItemNbt;
import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDragonEgg extends Item {
    public EnumDragonEgg type;

    public ItemDragonEgg(EnumDragonEgg type) {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
        this.type = type;
    }

    @Override
    public @NotNull String getDescriptionId() {
        return "item.iceandfire.dragonegg";
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Player player) {
        ItemNbt.setTag(itemStack, new CompoundTag());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("dragon." + type.toString().toLowerCase()).withStyle(type.color));
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        ItemStack itemstack = context.getPlayer().getItemInHand(context.getHand());
        BlockPos offset = context.getClickedPos().relative(context.getClickedFace());
        EntityDragonEgg egg = new EntityDragonEgg(IafEntityRegistry.DRAGON_EGG.get(), context.getLevel());
        egg.setEggType(type);
        egg.moveTo(offset.getX() + 0.5, offset.getY(), offset.getZ() + 0.5, 0, 0);
        egg.onPlayerPlace(context.getPlayer());
        if (itemstack.has(DataComponents.CUSTOM_NAME)) {
            egg.setCustomName(itemstack.getHoverName());
        }
        if (!context.getLevel().isClientSide()) {
            context.getLevel().addFreshEntity(egg);
        }
        itemstack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
