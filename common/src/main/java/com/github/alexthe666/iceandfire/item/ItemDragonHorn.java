package com.github.alexthe666.iceandfire.item;
import com.github.alexthe666.iceandfire.entity.util.Values;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.core.UUIDUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.core.registries.BuiltInRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ItemDragonHorn extends Item {

    public ItemDragonHorn() {
        super((dev.vladeezyy.iceandfire.platform.Registrar.itemProps())/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
    }

    public static int getDragonType(ItemStack stack) {
        if (ItemNbt.hasTag(stack)) {
            String id = ItemNbt.view(stack).getStringOr("DragonHornEntityID", "");
            if (EntityType.byString(id).isPresent()) {
                EntityType entityType = EntityType.byString(id).get();
                if (entityType == IafEntityRegistry.FIRE_DRAGON.get())
                    return 1;

                if (entityType == IafEntityRegistry.ICE_DRAGON.get())
                    return 2;

                if (entityType == IafEntityRegistry.LIGHTNING_DRAGON.get())
                    return 3;
            }
        }

        return 0;
    }


    @Override
    public void onCraftedBy(ItemStack itemStack, Player player) {
        ItemNbt.setTag(itemStack, null);
    }


    @Override
    public @NotNull InteractionResult interactLivingEntity(@NotNull ItemStack stack, Player playerIn, @NotNull LivingEntity target, @NotNull InteractionHand hand) {
        ItemStack trueStack = playerIn.getItemInHand(hand);
        if (!playerIn.level().isClientSide() && hand == InteractionHand.MAIN_HAND && target instanceof EntityDragonBase && ((EntityDragonBase) target).isOwnedBy(playerIn) && getEntityTag(trueStack).isEmpty()) {
            CompoundTag newTag = new CompoundTag();

            CompoundTag entityTag = Values.write(target.registryAccess(), target::save);
            newTag.put("EntityTag", entityTag);

            newTag.putString("DragonHornEntityID", BuiltInRegistries.ENTITY_TYPE.getKey(target.getType()).toString());
            ItemNbt.setTag(trueStack, newTag);

            playerIn.swing(hand);
            playerIn.level().playSound(playerIn, playerIn.blockPosition(), SoundEvents.ZOMBIE_VILLAGER_CONVERTED, SoundSource.NEUTRAL, 3.0F, 0.75F);
            target.remove(Entity.RemovalReason.DISCARDED);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }


    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP)
            return InteractionResult.FAIL;
        ItemStack stack = context.getItemInHand();
        CompoundTag stackTag = ItemNbt.getOrEmpty(stack);
        if (!stackTag.getStringOr("DragonHornEntityID", "").isEmpty()) {
            Level world = context.getLevel();
            String id = stackTag.getStringOr("DragonHornEntityID", "");
            EntityType type = EntityType.byString(id).orElse(null);
            if (type != null) {
                Entity entity = type.create(world, EntitySpawnReason.MOB_SUMMONED);
                if (entity instanceof EntityDragonBase) {
                    EntityDragonBase dragon = (EntityDragonBase) entity;
                    Values.load(dragon, getEntityTag(stack));
                }
                //Still needed to allow for intercompatibility
                if (stackTag.contains("EntityUUID"))
                    entity.setUUID(stackTag.read("EntityUUID", UUIDUtil.CODEC).orElse(null));

                entity.absSnapTo(context.getClickedPos().getX() + 0.5D, (context.getClickedPos().getY() + 1), context.getClickedPos().getZ() + 0.5D, 180 + (context.getHorizontalDirection()).toYRot(), 0.0F);
                if (world.addFreshEntity(entity)) {
                    stackTag.remove("DragonHornEntityID");
                    stackTag.remove("EntityTag");
                    stackTag.remove("EntityUUID");
                    ItemNbt.setTag(stack, stackTag);
                    stack.remove(DataComponents.ENTITY_DATA);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {
        if (ItemNbt.hasTag(stack)) {
            CompoundTag entityTag = getEntityTag(stack);
            if (!entityTag.isEmpty()) {
                String id = ItemNbt.view(stack).getStringOr("DragonHornEntityID", "");
                if (EntityType.byString(id).isPresent()) {
                    EntityType type = EntityType.byString(id).get();
                    tooltip.accept((Component.translatable(type.getDescriptionId())).withStyle(getTextColorForEntityType(type)));
                    String name = (Component.translatable("dragon.unnamed")).getString();
                    if (worldIn.registries() != null) {
                        // 26.x saves the name as a component (1.21.1: a JSON string)
                        Component component = entityTag.read("CustomName", net.minecraft.network.chat.ComponentSerialization.CODEC, worldIn.registries().createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE)).orElse(null);
                        if (component != null)
                            name = component.getString();
                    }

                    tooltip.accept((Component.literal(name)).withStyle(ChatFormatting.GRAY));
                    String gender = (Component.translatable("dragon.gender")).getString() + " " + (Component.translatable(entityTag.getBooleanOr("Gender", false) ? "dragon.gender.male" : "dragon.gender.female")).getString();
                    tooltip.accept((Component.literal(gender)).withStyle(ChatFormatting.GRAY));
                    int stagenumber = entityTag.getIntOr("AgeTicks", 0) / 24000;
                    int stage1 = 0;
                    if (stagenumber >= 100) {
                        stage1 = 5;
                    } else if (stagenumber >= 75) {
                        stage1 = 4;
                    } else if (stagenumber >= 50) {
                        stage1 = 3;
                    } else if (stagenumber >= 25) {
                        stage1 = 2;
                    } else {
                        stage1 = 1;
                    }
                    String stage = (Component.translatable("dragon.stage")).getString() + " " + stage1 + " " + (Component.translatable("dragon.days.front")).getString() + stagenumber + " " + (Component.translatable("dragon.days.back")).getString();
                    tooltip.accept((Component.literal(stage)).withStyle(ChatFormatting.GRAY));
                }
            }

        }
    }

    /**
     * The stored dragon. Horns of 1.20.1 worlds have it in minecraft:entity_data after the world upgrade.
     */
    private static CompoundTag getEntityTag(ItemStack stack) {
        CompoundTag tag = ItemNbt.view(stack).getCompoundOrEmpty("EntityTag");
        if (tag.isEmpty()) {
            net.minecraft.world.item.component.TypedEntityData<EntityType<?>> entityData = stack.get(DataComponents.ENTITY_DATA);
            if (entityData != null) {
                return entityData.copyTagWithoutId();
            }
        }
        return tag.copy();
    }

    private ChatFormatting getTextColorForEntityType(EntityType type) {
        if (type == IafEntityRegistry.FIRE_DRAGON.get())
            return ChatFormatting.DARK_RED;

        if (type == IafEntityRegistry.ICE_DRAGON.get())
            return ChatFormatting.BLUE;

        if (type == IafEntityRegistry.LIGHTNING_DRAGON.get())
            return ChatFormatting.DARK_PURPLE;

        return ChatFormatting.GRAY;
    }
}
