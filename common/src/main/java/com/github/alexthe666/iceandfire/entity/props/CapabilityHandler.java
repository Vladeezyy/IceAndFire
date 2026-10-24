package com.github.alexthe666.iceandfire.entity.props;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import dev.vladeezyy.iceandfire.bus.EntityJoinLevelEvent;
import dev.vladeezyy.iceandfire.bus.PlayerEvent;
import dev.vladeezyy.iceandfire.bus.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = IceAndFire.MODID)
public class CapabilityHandler {

    @SubscribeEvent
    public static void handleInitialSync(final EntityJoinLevelEvent event) {
        // Only entities that have data: syncing the defaults of every mob that joins is wasted traffic
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity entity) {
            EntityDataProvider.getExisting(entity).ifPresent(data -> syncEntityData(entity));
        }
    }

    @SubscribeEvent
    public static void onPlayerStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity target && event.getEntity() instanceof ServerPlayer serverPlayer) {
            EntityDataProvider.getExisting(target).ifPresent(data -> IceAndFire.sendMSGToPlayer(new SyncEntityData(target.getId(), data.serialize()), serverPlayer));
        }
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity living) {
            EntityDataProvider.getExisting(living).ifPresent(data -> data.tick(living));
        }
    }

    public static void syncEntityData(final Entity entity) {
        if (entity.level().isClientSide() || !(entity instanceof LivingEntity)) {
            return;
        }
        EntityDataProvider.getCapability(entity).ifPresent(data -> dev.vladeezyy.iceandfire.platform.Platform.INSTANCE.sendToTrackingAndSelf(entity, new SyncEntityData(entity.getId(), data.serialize())));
    }

    public static @Nullable Player getLocalPlayer() {
        return IceAndFire.PROXY.getClientSidePlayer();
    }
}
