package com.github.alexthe666.iceandfire.item;
import net.minecraft.world.entity.EquipmentSlot;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCyclopsEye extends Item {

    public ItemCyclopsEye() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.durability(500));
    }

    // NeoForge extension method (no vanilla override)
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
        if (!ItemNbt.hasTag(stack)) {
            ItemNbt.setTag(stack, new CompoundTag());
        } else {
            if (entity instanceof LivingEntity) {
                LivingEntity living = (LivingEntity) entity;
                if (living.getMainHandItem() == stack || living.getOffhandItem() == stack) {
                    double range = 15;
                    boolean inflictedDamage = false;
                    for (Mob LivingEntity : world.getEntitiesOfClass(Mob.class, new AABB(living.getX() - range, living.getY() - range, living.getZ() - range, living.getX() + range, living.getY() + range, living.getZ() + range))) {
                        if (!LivingEntity.is(living) && !LivingEntity.isAlliedTo(living) && (LivingEntity.getTarget() == living || LivingEntity.getLastHurtByMob() == living || LivingEntity instanceof Enemy)) {
                            LivingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10, 1));
                            inflictedDamage = true;
                        }
                    }
                    if (inflictedDamage) {
                        ItemNbt.update(stack, tag -> tag.putInt("HurtingTicks", ItemNbt.view(stack).getInt("HurtingTicks") + 1));
                    }
                }
                if (ItemNbt.view(stack).getInt("HurtingTicks") > 120) {
                    if (world instanceof ServerLevel serverLevel) {
                        stack.hurtAndBreak(1, serverLevel, entity instanceof net.minecraft.server.level.ServerPlayer player ? player : null, item -> {
                        });
                    }
                    ItemNbt.update(stack, tag -> tag.putInt("HurtingTicks", 0));
                }
            }

        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.accept(Component.translatable("item.iceandfire.legendary_weapon.desc").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.cyclops_eye.desc_0").withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("item.iceandfire.cyclops_eye.desc_1").withStyle(ChatFormatting.GRAY));
    }
}
