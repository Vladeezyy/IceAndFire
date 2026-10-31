package com.github.alexthe666.iceandfire.item;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import com.github.alexthe666.iceandfire.entity.EntityHippogryphEgg;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.enums.EnumHippogryphTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ItemHippogryphEgg extends Item {

    public ItemHippogryphEgg() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
    }

    public static ItemStack createEggStack(EnumHippogryphTypes parent1, EnumHippogryphTypes parent2) {
        EnumHippogryphTypes eggType = ThreadLocalRandom.current().nextBoolean() ? parent1 : parent2;
        ItemStack stack = new ItemStack(IafItemRegistry.HIPPOGRYPH_EGG.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("EggOrdinal", eggType.ordinal());
        ItemNbt.setTag(stack, tag);
        return stack;
    }


/*    @Override
    public void fillItemCategory(@NotNull CreativeModeTab group, @NotNull NonNullList<ItemStack> items) {
        if (this.allowdedIn(group)) {
            for (EnumHippogryphTypes type : EnumHippogryphTypes.values()) {
                ItemStack stack = new ItemStack(this);
                CompoundTag tag = new CompoundTag();
                tag.putInt("EggOrdinal", type.ordinal());
                ItemNbt.setTag(stack, tag);
                items.add(stack);

            }
        }

    }*/

    @Override
    public @NotNull InteractionResult use(@NotNull Level worldIn, Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (!playerIn.isCreative()) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (worldIn.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isClientSide()) {
            EntityHippogryphEgg entityegg = new EntityHippogryphEgg(IafEntityRegistry.HIPPOGRYPH_EGG.get(), worldIn,
                playerIn, itemstack);
            entityegg.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F, 1.0F);
            worldIn.addFreshEntity(entityegg);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        CompoundTag tag = ItemNbt.getTag(stack);
        int eggOrdinal = 0;
        if (tag != null) {
            eggOrdinal = tag.getIntOr("EggOrdinal", 0);
        }

        String type = EnumHippogryphTypes.values()[Mth.clamp(eggOrdinal, 0, EnumHippogryphTypes.values().length - 1)].name().toLowerCase();
        tooltip.accept(Component.translatable("entity.iceandfire.hippogryph." + type).withStyle(ChatFormatting.GRAY));
    }
}
