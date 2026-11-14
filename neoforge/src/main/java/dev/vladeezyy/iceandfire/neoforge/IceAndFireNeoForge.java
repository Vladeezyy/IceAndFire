package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.CommonProxy;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.message.IafNetwork;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.world.IafFeatureBiomeModifier;
import com.github.alexthe666.iceandfire.world.IafMobSpawnBiomeModifier;
import com.mojang.serialization.MapCodec;
import dev.vladeezyy.iceandfire.bus.EventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.BiConsumer;

/** Ice and Fire on NeoForge: the common init, NeoForge's registration events and its game events posted on {@link IafBus} */
@Mod(IceAndFire.MODID)
public final class IceAndFireNeoForge {
    private static final DeferredRegister<MapCodec<? extends BiomeModifier>> BIOME_MODIFIERS = DeferredRegister.create(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, IceAndFire.MODID);
    private static ModConfigSpec clientSpec;
    private static ModConfigSpec serverSpec;

    static {
        BIOME_MODIFIERS.register("iaf_mob_spawns", IafMobSpawnBiomeModifier::makeCodec);
        BIOME_MODIFIERS.register("iaf_features", IafFeatureBiomeModifier::makeCodec);
    }

    public IceAndFireNeoForge(IEventBus modBus, ModContainer mod) {
        clientSpec = NeoForgeConfig.build(ConfigHolder.CLIENT_SPEC);
        serverSpec = NeoForgeConfig.build(ConfigHolder.SERVER_SPEC);
        mod.registerConfig(ModConfig.Type.CLIENT, clientSpec);
        mod.registerConfig(ModConfig.Type.COMMON, serverSpec);

        IceAndFire.init();
        NeoForgePlatform.registerAll(modBus);
        BIOME_MODIFIERS.register(modBus);
        IafAttachments.ATTACHMENTS.register(modBus);
        EventIndex.register();

        modBus.addListener(IceAndFireNeoForge::onConfig);
        modBus.addListener(IceAndFireNeoForge::onConfigReload);
        modBus.addListener(IceAndFireNeoForge::registerPayloads);
        modBus.addListener(IafCapabilities::register);
        modBus.addListener((EntityAttributeCreationEvent event) -> IafEntityRegistry.bakeAttributes(event::put));
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> IafEntityRegistry.registerSpawnPlacements(new IafEntityRegistry.SpawnPlacementSink() {
            @Override
            public <T extends Mob> void register(net.minecraft.world.entity.EntityType<T> type, net.minecraft.world.entity.SpawnPlacementType placement, net.minecraft.world.level.levelgen.Heightmap.Types heightmap, net.minecraft.world.entity.SpawnPlacements.SpawnPredicate<T> predicate) {
                event.register(type, placement, heightmap, predicate, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        }));
        modBus.addListener(EventPriority.LOW, (RegisterEvent event) -> {
            if (event.getRegistryKey() == Registries.ITEM) {
                IafItemRegistry.setRepairMaterials();
            }
        });
        modBus.addListener((FMLCommonSetupEvent event) -> event.enqueueWork(() -> {
            IceAndFire.PROXY.setup();
            IafBus.post(new dev.vladeezyy.iceandfire.bus.FMLCommonSetupEvent());
        }));
        modBus.addListener((FMLLoadCompleteEvent event) -> IceAndFire.PROXY.postInit());

        registerGameEvents(NeoForge.EVENT_BUS);
    }

    private static void onConfig(ModConfigEvent.Loading event) {
        bake(event.getConfig());
    }

    private static void onConfigReload(ModConfigEvent.Reloading event) {
        bake(event.getConfig());
    }

    private static void bake(ModConfig config) {
        if (config.getSpec() == clientSpec) {
            CommonProxy.onConfigLoaded(true);
        } else if (config.getSpec() == serverSpec) {
            CommonProxy.onConfigLoaded(false);
        }
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(IceAndFire.MODID).versioned(IafNetwork.PROTOCOL_VERSION);
        IafNetwork.register(new IafNetwork.PacketRegistrar() {
            @Override
            public <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                registrar.playToClient(type, codec, (payload, context) -> handler.accept(payload, context(context)));
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                registrar.playToServer(type, codec, (payload, context) -> handler.accept(payload, context(context)));
            }

            @Override
            public <T extends CustomPacketPayload> void both(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                registrar.playBidirectional(type, codec, (payload, context) -> handler.accept(payload, context(context)));
            }
        });
    }

    private static PacketContext context(IPayloadContext context) {
        return new PacketContext() {
            @Override
            public Player player() {
                return context.player();
            }

            @Override
            public void enqueueWork(Runnable work) {
                context.enqueueWork(work);
            }

            @Override
            public PacketFlow flow() {
                return context.flow();
            }
        };
    }

    /** The game events the common listeners ({@link EventIndex}) want, as the mod's own events */
    private static void registerGameEvents(IEventBus bus) {
        bus.addListener((ServerAboutToStartEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.ServerAboutToStartEvent(event.getServer())));
        bus.addListener((ServerStartedEvent event) -> IceAndFire.onServerStarted());
        bus.addListener((net.neoforged.neoforge.event.server.ServerStoppedEvent event) -> IceAndFire.onServerStopped());
        // saved data taken on the server thread before the level generates (the worldgen threads read IafSavedData's cache);
        // first, as another listener may already generate chunks
        bus.addListener(EventPriority.HIGHEST, (net.neoforged.neoforge.event.level.LevelEvent.Load event) -> {
            if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
                com.github.alexthe666.iceandfire.world.IafSavedData.onLevelLoad(level);
            }
        });
        bus.addListener((net.neoforged.neoforge.event.level.LevelEvent.Unload event) -> {
            if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level) {
                com.github.alexthe666.iceandfire.world.IafSavedData.onLevelUnload(level);
            }
        });
        bus.addListener((RegisterBrewingRecipesEvent event) -> IafRecipeRegistry.registerBrewing(event.getBuilder()));
        // every listener wants living entities only
        bus.addListener((EntityTickEvent.Pre event) -> {
            if (event.getEntity() instanceof LivingEntity) {
                IafBus.post(new dev.vladeezyy.iceandfire.bus.EntityTickEvent.Pre(event.getEntity()));
            }
        });
        bus.addListener((EntityJoinLevelEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.EntityJoinLevelEvent(event.getEntity(), event.getLevel())));
        bus.addListener((EntityMountEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.EntityMountEvent(event.getEntityMounting(), event.getEntityBeingMounted(), event.isMounting())));
        bus.addListener((EntityStruckByLightningEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.EntityStruckByLightningEvent(event.getEntity(), event.getLightning()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((ProjectileImpactEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.ProjectileImpactEvent(event.getProjectile(), event.getRayTraceResult()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((FinalizeSpawnEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.FinalizeSpawnEvent(event.getEntity())));
        bus.addListener((LivingChangeTargetEvent event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.LivingChangeTargetEvent(event.getEntity(), event.getNewAboutToBeSetTarget());
            if (IafBus.post(ours)) {
                event.setCanceled(true);
            } else if (ours.getNewAboutToBeSetTarget() != event.getNewAboutToBeSetTarget()) {
                event.setNewAboutToBeSetTarget(ours.getNewAboutToBeSetTarget());
            }
        });
        bus.addListener((LivingDeathEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.LivingDeathEvent(event.getEntity(), event.getSource()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((LivingDropsEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.LivingDropsEvent(event.getEntity(), event.getSource(), event.getDrops(), event.isRecentlyHit()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((LivingEntityUseItemEvent.Tick event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.LivingEntityUseItemEvent.Tick(event.getEntity(), event.getItem(), event.getDuration());
            if (IafBus.post(ours)) {
                event.setCanceled(true);
            } else {
                event.setDuration(ours.getDuration());
            }
        });
        bus.addListener((LivingFallEvent event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.LivingFallEvent(event.getEntity(), event.getDistance(), event.getDamageMultiplier());
            if (IafBus.post(ours)) {
                event.setCanceled(true);
            } else {
                event.setDistance(ours.getDistance());
                event.setDamageMultiplier(ours.getDamageMultiplier());
            }
        });
        bus.addListener((LivingIncomingDamageEvent event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.LivingIncomingDamageEvent(event.getEntity(), event.getSource(), event.getAmount());
            if (IafBus.post(ours)) {
                event.setCanceled(true);
            } else if (ours.getAmount() != event.getAmount()) {
                event.setAmount(ours.getAmount());
            }
        });
        bus.addListener((AttackEntityEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.AttackEntityEvent(event.getEntity(), event.getTarget()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((PlayerEvent.StartTracking event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.PlayerEvent.StartTracking(event.getEntity(), event.getTarget())));
        bus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.PlayerEvent.PlayerLoggedOutEvent(event.getEntity())));
        bus.addListener((PlayerInteractEvent.EntityInteract event) -> interact(event, new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.EntityInteract(event.getEntity(), event.getHand(), event.getTarget())));
        bus.addListener((PlayerInteractEvent.RightClickBlock event) -> interact(event, new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.RightClickBlock(event.getEntity(), event.getHand(), event.getPos(), event.getFace())));
        bus.addListener((PlayerInteractEvent.RightClickItem event) -> interact(event, new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.RightClickItem(event.getEntity(), event.getHand())));
        bus.addListener((PlayerInteractEvent.LeftClickEmpty event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.LeftClickEmpty(event.getEntity())));
        bus.addListener((BlockEvent.BreakEvent event) -> {
            if (IafBus.post(new dev.vladeezyy.iceandfire.bus.BlockEvent.BreakEvent(event.getLevel(), event.getPos(), event.getState(), event.getPlayer()))) {
                event.setCanceled(true);
            }
        });
        bus.addListener((VillagerTradesEvent event) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.VillagerTradesEvent(event.getTrades(), event.getType())));
        bus.addListener((LootTableLoadEvent event) -> {
            var ours = new dev.vladeezyy.iceandfire.bus.LootTableLoadEvent(event.getName(), event.getTable());
            IafBus.post(ours);
            if (ours.getTable() != event.getTable()) {
                event.setTable(ours.getTable());
            }
            for (var pool : ours.getAddedPools()) {
                event.getTable().addPool(pool.getValue().name(pool.getKey()).build());
            }
        });
    }

    private static <E extends PlayerInteractEvent & net.neoforged.bus.api.ICancellableEvent> void interact(E event, dev.vladeezyy.iceandfire.bus.PlayerInteractEvent ours) {
        if (IafBus.post(ours)) {
            event.setCanceled(true);
            if (event instanceof PlayerInteractEvent.EntityInteract entity) {
                entity.setCancellationResult(ours.getCancellationResult());
            } else if (event instanceof PlayerInteractEvent.RightClickBlock block) {
                block.setCancellationResult(ours.getCancellationResult());
            } else if (event instanceof PlayerInteractEvent.RightClickItem item) {
                item.setCancellationResult(ours.getCancellationResult());
            }
        }
    }
}
