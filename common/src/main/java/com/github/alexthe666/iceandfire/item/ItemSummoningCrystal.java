package com.github.alexthe666.iceandfire.item;
import net.minecraft.core.UUIDUtil;
import java.util.function.Consumer;
import net.minecraft.world.item.component.TooltipDisplay;

import com.github.alexthe666.iceandfire.util.ItemNbt;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.world.DragonPosWorldData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemSummoningCrystal extends Item {
    /** Keeps the chunk of a summoned dragon loaded until it arrived (NeoForge's TicketController before; a vanilla ticket as forced chunks use) */
    /**
     * Summons waiting for their dragon to load, by dragon UUID. The original kept the state of a summon in fields
     * of the item, which exists once for the whole game: two players summoning at the same time overwrote each other.
     */
    public static final Map<UUID, PendingSummon> DELAYED_SUMMONS = new HashMap<>();

    public ItemSummoningCrystal() {
        super(dev.vladeezyy.iceandfire.platform.Registrar.itemProps()/*.tab(IceAndFire.TAB_ITEMS)*/.stacksTo(1));
    }

    private static CompoundTag getDragonTag(ItemStack stack) {
        if (stack.getItem() instanceof ItemSummoningCrystal && ItemNbt.hasTag(stack)) {
            for (String tagInfo : ItemNbt.view(stack).keySet()) {
                if (tagInfo.contains("Dragon")) {
                    return ItemNbt.view(stack).getCompoundOrEmpty(tagInfo);
                }
            }
        }
        return null;
    }

    public static boolean hasDragon(ItemStack stack) {
        return getDragonTag(stack) != null;
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Player player) {
        ItemNbt.setTag(itemStack, new CompoundTag());
    }

    public ItemStack onItemUseFinish(Level worldIn, LivingEntity LivingEntity) {
        return new ItemStack(this);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Item.TooltipContext worldIn, TooltipDisplay display, Consumer<Component> tooltip, @NotNull TooltipFlag flagIn) {

        boolean flag = false;
        String desc = "entity.iceandfire.fire_dragon";
        if (stack.getItem() == IafItemRegistry.SUMMONING_CRYSTAL_ICE.get()) {
            desc = "entity.iceandfire.ice_dragon";
        }
        if (stack.getItem() == IafItemRegistry.SUMMONING_CRYSTAL_LIGHTNING.get()) {
            desc = "entity.iceandfire.lightning_dragon";
        }
        if (ItemNbt.hasTag(stack)) {
            for (String tagInfo : ItemNbt.view(stack).keySet()) {
                if (tagInfo.contains("Dragon")) {
                    CompoundTag dragonTag = ItemNbt.view(stack).getCompoundOrEmpty(tagInfo);
                    Component dragonName = Component.translatable(desc);
                    if (!dragonTag.getStringOr("CustomName", "").isEmpty()) {
                        dragonName = Component.literal(dragonTag.getStringOr("CustomName", ""));
                    }
                    tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.bound", dragonName).withStyle(ChatFormatting.GRAY));
                    flag = true;
                }
            }
        }
        if (!flag) {
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_0").withStyle(ChatFormatting.GRAY));
            tooltip.accept(Component.translatable("item.iceandfire.summoning_crystal.desc_1").withStyle(ChatFormatting.GRAY));

        }

    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide() || context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        ServerLevel serverWorld = (ServerLevel) context.getLevel();
        if (serverWorld.dimension() != Level.OVERWORLD) {
            return InteractionResult.PASS;
        }
        Player summoningPlayer = context.getPlayer();
        InteractionHand summoningHand = context.getHand();
        ItemStack stack = summoningPlayer.getItemInHand(summoningHand);
        float dragonTargetYaw = summoningPlayer.getYRot();
        BlockPos dragonTargetPosition = context.getClickedPos().relative(context.getClickedFace());

        if (stack.getItem() != this) {
            return InteractionResult.PASS;
        }

        CompoundTag dragonTag = getDragonTag(stack);
        if (dragonTag == null) {
            return InteractionResult.PASS;
        }

        UUID dragonUuid = dragonTag.read("DragonUUID", UUIDUtil.CODEC).orElse(null);

        IceAndFire.LOGGER.info("Trying to summon dragon {} {}", dragonUuid, dragonTag.getString("CustomName"));
        Entity entity = serverWorld.getEntity(dragonUuid);
        // If the dragon is already loaded, summon it immediately
        if (entity != null) {
            summonEntity(entity, serverWorld, dragonTargetPosition, dragonTargetYaw, summoningPlayer, summoningHand, stack);
            return InteractionResult.PASS;
        }

        // early exit if we are not allowed to load chunks to summon
        if (!IafConfig.chunkLoadSummonCrystal) {
            IceAndFire.LOGGER.info("Dragon entity {} not loaded, and chunk loading is disabled", dragonUuid);
            displayClientError(summoningPlayer);
            return InteractionResult.PASS;
        }

        DragonPosWorldData data = DragonPosWorldData.get(serverWorld);
        if (data == null) {
            IceAndFire.LOGGER.warn("Unable to load DragonPosWorldData for world {}", serverWorld);
            displayClientError(summoningPlayer);
            return InteractionResult.PASS;
        }

        BlockPos dragonOriginPosition = data.getDragonPos(dragonUuid);

        if (dragonOriginPosition == null) {
            IceAndFire.LOGGER.warn("Summoning dragon origin position unknown for dragon {}", dragonUuid);
            displayClientError(summoningPlayer);
            return InteractionResult.PASS;
        }

        ChunkPos pos = new ChunkPos(dragonOriginPosition);
        IceAndFire.LOGGER.info("Dragon entity not loaded, loading chunk {}", pos);

        // try to load the chunk, and mark the entity to be summoned as soon as it gets added to the world
        serverWorld.getChunkSource().addTicketWithRadius(com.github.alexthe666.iceandfire.world.IafTicketTypes.SUMMONING_CRYSTAL.get(), pos, 2);
        DELAYED_SUMMONS.put(dragonUuid, new PendingSummon(this, dragonUuid, dragonTargetPosition, dragonTargetYaw, summoningPlayer, summoningHand, serverWorld, dragonOriginPosition, stack, serverWorld.getGameTime()));

        return InteractionResult.PASS;
    }

    public void summonEntity(Entity entity, Level worldIn, BlockPos offsetPos, float yaw, Player summoningPlayer, InteractionHand summoningHand, ItemStack stack) {
        entity.snapTo(offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D, yaw, 0);
        if (entity instanceof EntityDragonBase) {
            ((EntityDragonBase) entity).setCrystalBound(false);
        }
        if (IafConfig.chunkLoadSummonCrystal) {
            DragonPosWorldData data = DragonPosWorldData.get(worldIn);
            if (data != null) {
                data.removeDragon(entity.getUUID());
            }
        }
        summoningPlayer.playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
        summoningPlayer.playSound(SoundEvents.GLASS_BREAK, 1, 1);
        summoningPlayer.swing(summoningHand);
        summoningPlayer.displayClientMessage(Component.translatable("message.iceandfire.dragonTeleport"), true);
        ItemNbt.setTag(stack, new CompoundTag());
    }

    /** Called when the dragon of a pending summon was added to the level. */
    public static void delayedSummon(PendingSummon summon) {
        DELAYED_SUMMONS.remove(summon.dragonUuid());
        ChunkPos pos = new ChunkPos(summon.dragonOriginPosition());

        // make delayed summons expire if for some reason the chunk loading, or entity loading takes too long
        // wouldn't want the dragon to summon minutes later at the original position
        final long SUMMON_DELAY_TOLERANCE = 2_000;
        if (summon.serverWorld().getGameTime() - SUMMON_DELAY_TOLERANCE > summon.summoningTime()) {
            IceAndFire.LOGGER.info("Dragon summon timed out for dragon {}; unloading chunk {}", summon.dragonUuid(), pos);
        } else {
            Entity entity = summon.serverWorld().getEntity(summon.dragonUuid());
            if (entity != null) {
                if (summon.summoningPlayer().isAlive()) {
                    summon.item().summonEntity(entity, summon.serverWorld(), summon.dragonTargetPosition(), summon.dragonTargetYaw(), summon.summoningPlayer(), summon.summoningHand(), summon.stack());
                    IceAndFire.LOGGER.info("Summoned dragon {} and unloading chunk {}", summon.dragonUuid(), pos);
                } else {
                    IceAndFire.LOGGER.info("Player died since summoning {}; unloading chunk {}", summon.dragonUuid(), pos);
                }
            }
        }

        summon.serverWorld().getChunkSource().removeTicketWithRadius(com.github.alexthe666.iceandfire.world.IafTicketTypes.SUMMONING_CRYSTAL.get(), pos, 2);
    }

    public record PendingSummon(ItemSummoningCrystal item, UUID dragonUuid, BlockPos dragonTargetPosition, float dragonTargetYaw, Player summoningPlayer,
                                InteractionHand summoningHand, ServerLevel serverWorld, BlockPos dragonOriginPosition, ItemStack stack, long summoningTime) {
    }

    private static void displayClientError(Player summoningPlayer) {
        summoningPlayer.displayClientMessage(Component.translatable("message.iceandfire.noDragonTeleport"), true);
    }
}
