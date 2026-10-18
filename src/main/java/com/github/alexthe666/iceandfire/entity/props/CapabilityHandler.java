package com.github.alexthe666.iceandfire.entity.props;

import net.neoforged.fml.common.EventBusSubscriber;
import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class CapabilityHandler {
    public static final Capability<EntityData> ENTITY_DATA_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});
    public static final ResourceLocation ENTITY_DATA = ResourceLocation.fromNamespaceAndPath(IceAndFire.MODID, "entity_data");

    @SubscribeEvent
    public static void attachCapability(final AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(ENTITY_DATA, new EntityDataProvider());
        }
    }

    @SubscribeEvent
    public static void handleInitialSync(final EntityJoinLevelEvent event) {
        // TODO :: only sync if values are not the default values?
        syncEntityData(event.getEntity());
    }

    @SubscribeEvent
    public static void removeCachedEntry(final EntityLeaveLevelEvent event) {
        EntityDataProvider.removeCachedEntry(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity target && event.getEntity() instanceof ServerPlayer serverPlayer) {
            EntityDataProvider.getCapability(target).ifPresent(data -> IceAndFire.sendMSGToPlayer(new SyncEntityData(target.getId(), data.serialize()), serverPlayer));
        }
    }

    /* Currently there is no data which would need to be kept after death
    @SubscribeEvent
    public static void handleDeath(final PlayerEvent.Clone event) {
        if (event.isWasDeath()) { // TODO :: entering the end portal also triggers this but with this flag set as false
            Player oldPlayer = event.getOriginal();
            Player newPlayer = event.getEntity();

            oldPlayer.reviveCaps();

            // Unsure but at this point the cached entry might already be removed, no need to re-add it (since it will not be removed again)
            oldPlayer.getCapability(ENTITY_DATA_CAPABILITY).ifPresent(oldData -> {
                EntityDataProvider.getCapability(newPlayer).ifPresent(newData -> {
                    newData.deserialize(oldData.serialize());
                    // Sync to client is handled by 'EntityJoinLevelEvent'
                });
            });

            oldPlayer.invalidateCaps();
        }
    }
    */

    @SubscribeEvent
    public static void tickData(final LivingEvent.LivingTickEvent event) {
        EntityDataProvider.getCapability(event.getEntity()).ifPresent(data -> data.tick(event.getEntity()));
    }

    public static void syncEntityData(final Entity entity) {
        if (entity.level().isClientSide() || !(entity instanceof LivingEntity)) {
            return;
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            EntityDataProvider.getCapability(entity).ifPresent(data -> IceAndFire.NETWORK_WRAPPER.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> serverPlayer), new SyncEntityData(entity.getId(), data.serialize())));
        } else {
            EntityDataProvider.getCapability(entity).ifPresent(data -> IceAndFire.NETWORK_WRAPPER.send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), new SyncEntityData(entity.getId(), data.serialize())));
        }
    }

    public static @Nullable Player getLocalPlayer() {
        return IceAndFire.PROXY.getClientSidePlayer();
    }
}
