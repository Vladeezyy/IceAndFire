package com.github.alexthe666.iceandfire;

import com.github.alexthe666.iceandfire.misc.IafSoundRegistry;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.client.ClientProxy;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.IafVillagerRegistry;
import com.github.alexthe666.iceandfire.entity.props.SyncEntityData;
import com.github.alexthe666.iceandfire.entity.tile.IafTileEntityRegistry;
import com.github.alexthe666.iceandfire.inventory.IafContainerRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.IafTabRegistry;
import com.github.alexthe666.iceandfire.loot.IafLootRegistry;
import com.github.alexthe666.iceandfire.message.*;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.recipe.IafRecipeSerializers;
import com.github.alexthe666.iceandfire.world.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.vladeezyy.iceandfire.platform.Platform;

/** The mod's common init; the NeoForge and Fabric entry points call {@link #init()} first */
public class IceAndFire {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "iceandfire";
    public static boolean DEBUG = true;
    public static String VERSION = "UNKNOWN";
    public static CommonProxy PROXY;

    private static boolean initialized;

    public static void init() {
        if (initialized)
            return;
        initialized = true;
        VERSION = Platform.INSTANCE.modVersion();
        PROXY = makeProxy();
        PROXY.init();
        // the registry classes queue their entries (Registrar) when loaded, in the original's order
        IafItemRegistry.ITEMS.getRegistryKey();
        IafBlockRegistry.BLOCKS.getRegistryKey();
        IafItemRegistry.registerSpawnEggs();
        IafSoundRegistry.registerSoundEvents();
        com.github.alexthe666.iceandfire.entity.IafDataSerializers.register();
        com.github.alexthe666.iceandfire.world.IafTicketTypes.TICKET_TYPES.getRegistryKey();
        IafTabRegistry.TAB_REGISTER.getRegistryKey();
        IafEntityRegistry.ENTITIES.getRegistryKey();
        IafTileEntityRegistry.TYPES.getRegistryKey();
        IafPlacementFilterRegistry.PLACEMENT_MODIFIER_TYPES.getRegistryKey();
        IafWorldRegistry.FEATURES.getRegistryKey();
        IafRecipeRegistry.RECIPE_TYPE.getRegistryKey();
        IafStructureTypes.STRUCTURE_TYPES.getRegistryKey();
        IafContainerRegistry.CONTAINERS.getRegistryKey();
        IafRecipeSerializers.SERIALIZERS.getRegistryKey();
        IafProcessors.PROCESSORS.getRegistryKey();
        IafLootRegistry.LOOT_FUNCTIONS.getRegistryKey();
        IafVillagerRegistry.POI_TYPES.getRegistryKey();
        IafVillagerRegistry.PROFESSIONS.getRegistryKey();
    }

    private static CommonProxy makeProxy() {
        if (Platform.INSTANCE.isClient()) {
            return new ClientProxy();
        }
        return new CommonProxy();
    }

    public static void onServerStarted() {
        LOGGER.info(IafWorldRegistry.LOADED_FEATURES);
        LOGGER.info(IafEntityRegistry.LOADED_ENTITIES);
        IafWorldRegistry.LOADED_FEATURES.clear();
    }

    /**
     * Static state that holds levels and players: kept past the server, the next world of the same game got a summon
     * of the old one and the old players stayed in memory.
     */
    public static void onServerStopped() {
        com.github.alexthe666.iceandfire.item.ItemSummoningCrystal.DELAYED_SUMMONS.clear();
        com.github.alexthe666.iceandfire.pathfinding.raycoms.pathjobs.AbstractPathJob.trackingMap.clear();
    }

    public static void sendMSGToServer(CustomPacketPayload message) {
        Platform.INSTANCE.sendToServer(message);
    }

    public static void sendMSGToAll(CustomPacketPayload message) {
        Platform.INSTANCE.sendToAll(message);
    }

    /** Sends the message to the players that see the entity; most entity updates do not need to go further. */
    public static void sendMSGToTracking(Entity entity, CustomPacketPayload message) {
        if (!entity.level().isClientSide()) {
            Platform.INSTANCE.sendToTrackingAndSelf(entity, message);
        }
    }

    /**
     * Sends the message to the players that have the chunk of the block, in its level. sendMSGToAll sent block updates
     * to every player of every dimension: a player in the Nether got the podium of the same position in the Overworld.
     */
    public static void sendMSGToTracking(Level level, BlockPos pos, CustomPacketPayload message) {
        if (level instanceof ServerLevel serverLevel) {
            Platform.INSTANCE.sendToTrackingChunk(serverLevel, pos, message);
        }
    }

    public static void sendMSGToPlayer(CustomPacketPayload message, ServerPlayer player) {
        Platform.INSTANCE.sendToPlayer(player, message);
    }
}
