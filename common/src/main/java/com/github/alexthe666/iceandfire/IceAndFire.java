package com.github.alexthe666.iceandfire;

import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.client.ClientProxy;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.IafVillagerRegistry;
import com.github.alexthe666.iceandfire.entity.props.IafAttachments;
import com.github.alexthe666.iceandfire.entity.props.SyncEntityData;
import com.github.alexthe666.iceandfire.entity.tile.IafTileEntityRegistry;
import com.github.alexthe666.iceandfire.inventory.IafContainerRegistry;
import com.github.alexthe666.iceandfire.item.IafArmorMaterials;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.item.IafTabRegistry;
import com.github.alexthe666.iceandfire.loot.IafLootRegistry;
import com.github.alexthe666.iceandfire.message.*;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.recipe.IafRecipeSerializers;
import com.github.alexthe666.iceandfire.world.*;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(IceAndFire.MODID)
public class IceAndFire {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MODID = "iceandfire";
    private static final String PROTOCOL_VERSION = Integer.toString(1);
    public static boolean DEBUG = true;
    public static String VERSION = "UNKNOWN";
    public static CommonProxy PROXY = makeProxy();

    public IceAndFire(IEventBus modBus, ModContainer mod) {
        VERSION = mod.getModInfo().getVersion().toString();

        mod.registerConfig(ModConfig.Type.CLIENT, ConfigHolder.CLIENT_SPEC);
        mod.registerConfig(ModConfig.Type.COMMON, ConfigHolder.SERVER_SPEC);
        PROXY.init();

        NeoForge.EVENT_BUS.addListener(IceAndFire::onServerStarted);

        final Registrar<MapCodec<? extends BiomeModifier>> biomeModifiers = Registrar.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, IceAndFire.MODID);
        biomeModifiers.register(modBus);
        biomeModifiers.register("iaf_mob_spawns", IafMobSpawnBiomeModifier::makeCodec);
        biomeModifiers.register("iaf_features", IafFeatureBiomeModifier::makeCodec);

        IafArmorMaterials.MATERIALS.register(modBus);
        IafAttachments.ATTACHMENTS.register(modBus);
        IafItemRegistry.ITEMS.register(modBus);
        IafBlockRegistry.BLOCKS.register(modBus);
        IafTabRegistry.TAB_REGISTER.register(modBus);
        IafEntityRegistry.ENTITIES.register(modBus);
        IafTileEntityRegistry.TYPES.register(modBus);
        IafPlacementFilterRegistry.PLACEMENT_MODIFIER_TYPES.register(modBus);
        IafWorldRegistry.FEATURES.register(modBus);
        IafRecipeRegistry.RECIPE_TYPE.register(modBus);
        IafStructureTypes.STRUCTURE_TYPES.register(modBus);
        IafContainerRegistry.CONTAINERS.register(modBus);
        IafRecipeSerializers.SERIALIZERS.register(modBus);
        IafProcessors.PROCESSORS.register(modBus);
        IafLootRegistry.LOOT_FUNCTIONS.register(modBus);

        IafVillagerRegistry.POI_TYPES.register(modBus);
        IafVillagerRegistry.PROFESSIONS.register(modBus);

        modBus.addListener(IceAndFire::setup);
        modBus.addListener(IceAndFire::setupComplete);
        modBus.addListener(IceAndFire::setupClient);
        modBus.addListener(IceAndFire::registerPayloads);
    }

    private static CommonProxy makeProxy() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return new ClientProxy();
        }
        return new CommonProxy();
    }

    public static void onServerStarted(ServerStartedEvent event) {
        LOGGER.info(IafWorldRegistry.LOADED_FEATURES);
        LOGGER.info(IafEntityRegistry.LOADED_ENTITIES);
        IafWorldRegistry.LOADED_FEATURES.clear();
    }

    public static void sendMSGToServer(CustomPacketPayload message) {
        PacketDistributor.sendToServer(message);
    }

    public static void sendMSGToAll(CustomPacketPayload message) {
        PacketDistributor.sendToAllPlayers(message);
    }

    /** Sends the message to the players that see the entity; most entity updates do not need to go further. */
    public static void sendMSGToTracking(Entity entity, CustomPacketPayload message) {
        if (!entity.level().isClientSide) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, message);
        }
    }

    /**
     * Sends the message to the players that have the chunk of the block, in its level. sendMSGToAll sent block updates
     * to every player of every dimension: a player in the Nether got the podium of the same position in the Overworld.
     */
    public static void sendMSGToTracking(Level level, BlockPos pos, CustomPacketPayload message) {
        if (level instanceof ServerLevel serverLevel) {
            PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(pos), message);
        }
    }

    public static void sendMSGToPlayer(CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    private static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MODID).versioned(PROTOCOL_VERSION);
        // One direction per message: the original accepted every message from both sides, so a client could, for
        // example, put any item on any podium with MessageUpdatePodium
        registrar.playToClient(MessageDeathWormHitbox.TYPE, MessageDeathWormHitbox.CODEC, MessageDeathWormHitbox.Handler::handle);
        registrar.playToClient(MessageDragonSetBurnBlock.TYPE, MessageDragonSetBurnBlock.CODEC, MessageDragonSetBurnBlock.Handler::handle);
        registrar.playToClient(MessageSetMyrmexHiveNull.TYPE, MessageSetMyrmexHiveNull.CODEC, MessageSetMyrmexHiveNull.Handler::handle);
        registrar.playToClient(MessageSirenSong.TYPE, MessageSirenSong.CODEC, MessageSirenSong.Handler::handle);
        registrar.playToClient(MessageSpawnParticleAt.TYPE, MessageSpawnParticleAt.CODEC, MessageSpawnParticleAt.Handler::handle);
        registrar.playToClient(MessageUpdateDragonforge.TYPE, MessageUpdateDragonforge.CODEC, MessageUpdateDragonforge.Handler::handle);
        registrar.playToClient(MessageUpdatePixieHouse.TYPE, MessageUpdatePixieHouse.CODEC, MessageUpdatePixieHouse.Handler::handle);
        registrar.playToClient(MessageUpdatePixieHouseModel.TYPE, MessageUpdatePixieHouseModel.CODEC, MessageUpdatePixieHouseModel.Handler::handle);
        registrar.playToClient(MessageUpdatePixieJar.TYPE, MessageUpdatePixieJar.CODEC, MessageUpdatePixieJar.Handler::handle);
        registrar.playToClient(MessageUpdatePodium.TYPE, MessageUpdatePodium.CODEC, MessageUpdatePodium.Handler::handle);
        registrar.playToServer(MessageDragonControl.TYPE, MessageDragonControl.CODEC, MessageDragonControl.Handler::handle);
        registrar.playToServer(MessageHippogryphArmor.TYPE, MessageHippogryphArmor.CODEC, MessageHippogryphArmor.Handler::handle);
        registrar.playToServer(MessageSwingArm.TYPE, MessageSwingArm.CODEC, MessageSwingArm.Handler::handle);
        registrar.playToClient(MessageDragonSyncFire.TYPE, MessageDragonSyncFire.CODEC, MessageDragonSyncFire.Handler::handle);
        registrar.playBidirectional(MessageGetMyrmexHive.TYPE, MessageGetMyrmexHive.CODEC, MessageGetMyrmexHive.Handler::handle);
        registrar.playBidirectional(MessageStartRidingMob.TYPE, MessageStartRidingMob.CODEC, MessageStartRidingMob.Handler::handle);
        registrar.playToClient(MessageUpdateLectern.TYPE, MessageUpdateLectern.CODEC, MessageUpdateLectern.Handler::handle);
        registrar.playToClient(MessageSyncPath.TYPE, MessageSyncPath.CODEC, MessageSyncPath::handle);
        registrar.playToClient(MessageSyncPathReached.TYPE, MessageSyncPathReached.CODEC, MessageSyncPathReached::handle);
        registrar.playToClient(SyncEntityData.TYPE, SyncEntityData.CODEC, SyncEntityData::handle);
    }

    private static void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> PROXY.setup());
    }

    private static void setupClient(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> PROXY.clientInit());
    }

    private static void setupComplete(final FMLLoadCompleteEvent event) {
        PROXY.postInit();
    }

}
