package dev.vladeezyy.iceandfire.fabric;

import com.github.alexthe666.iceandfire.CommonProxy;
import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.config.ConfigHolder;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.tile.IafTileEntityRegistry;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityDragonforge;
import com.github.alexthe666.iceandfire.item.IafItemRegistry;
import com.github.alexthe666.iceandfire.message.IafNetwork;
import com.github.alexthe666.iceandfire.recipe.IafRecipeRegistry;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import dev.vladeezyy.iceandfire.bus.EventIndex;
import dev.vladeezyy.iceandfire.bus.IafBus;
import dev.vladeezyy.iceandfire.platform.PacketContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;

/** Ice and Fire on Fabric: the common code wired to Fabric API (the NeoForge module does the same with NeoForge's events) */
public final class IceAndFireFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FabricConfig.load(ConfigHolder.SERVER_SPEC, "common");
        CommonProxy.onConfigLoaded(false);

        IceAndFire.init();
        FabricPlatform.registerAll();
        EventIndex.register();

        registerPackets();
        IafEntityRegistry.bakeAttributes(FabricDefaultAttributeRegistry::register);
        IafEntityRegistry.registerSpawnPlacements(new IafEntityRegistry.SpawnPlacementSink() {
            @Override
            public <T extends Mob> void register(EntityType<T> type, SpawnPlacementType placement, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
                SpawnPlacements.register(type, placement, heightmap, predicate);
            }
        });
        registerBiomeModifications();
        FabricBrewingRecipeRegistryBuilder.BUILD.register(IafRecipeRegistry::registerBrewing);
        // 26.x: the client gets only the recipe serializers the server syncs (JEI shows the dragon forge recipes)
        net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization.synchronizeRecipeSerializer(com.github.alexthe666.iceandfire.recipe.DragonForgeRecipe.SERIALIZER);
        registerStorages();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            FabricPlatform.server = server;
            IafBus.post(new dev.vladeezyy.iceandfire.bus.ServerAboutToStartEvent(server));
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> IceAndFire.onServerStarted());
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            FabricPlatform.server = null;
            IceAndFire.onServerStopped();
        });
        // saved data taken on the server thread before the level generates (the worldgen threads read IafSavedData's cache);
        // in a phase before the default one, as another listener may already generate chunks
        Identifier savedDataPhase = Identifier.fromNamespaceAndPath(IceAndFire.MODID, "saved_data");
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.LOAD.addPhaseOrdering(savedDataPhase, net.fabricmc.fabric.api.event.Event.DEFAULT_PHASE);
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.LOAD.register(savedDataPhase, (server, level) -> com.github.alexthe666.iceandfire.world.IafSavedData.onLevelLoad(level));
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents.UNLOAD.register((server, level) -> com.github.alexthe666.iceandfire.world.IafSavedData.onLevelUnload(level));
        registerGameEvents();
        registerTrades();

        IceAndFire.PROXY.setup();
        IafBus.post(new dev.vladeezyy.iceandfire.bus.FMLCommonSetupEvent());
        IceAndFire.PROXY.postInit();
    }

    private static void registerPackets() {
        IafNetwork.register(new IafNetwork.PacketRegistrar() {
            @Override
            public <T extends CustomPacketPayload> void toClient(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                PayloadTypeRegistry.playS2C().register(type, codec);
                FabricNetwork.CLIENT_HANDLERS.put(type, handler);
            }

            @Override
            public <T extends CustomPacketPayload> void toServer(CustomPacketPayload.Type<T> type, StreamCodec<? super RegistryFriendlyByteBuf, T> codec, BiConsumer<T, PacketContext> handler) {
                PayloadTypeRegistry.playC2S().register(type, codec);
                ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> handler.accept(payload, new PacketContext() {
                    @Override
                    public Player player() {
                        return context.player();
                    }

                    @Override
                    public void enqueueWork(Runnable work) {
                        context.server().execute(work);
                    }

                    @Override
                    public PacketFlow flow() {
                        return PacketFlow.SERVERBOUND;
                    }
                }));
            }
        });
    }

    /** The two biome modifiers of the NeoForge module (iceandfire:iaf_features, iceandfire:iaf_mob_spawns) */
    private static void registerBiomeModifications() {
        HashMap<String, Holder<PlacedFeature>> features = new HashMap<>();
        HolderOwner<PlacedFeature> owner = new HolderOwner<>() {
        };
        for (String id : FabricFeatures.IDS) {
            features.put(id, Holder.Reference.createStandAlone(owner, ResourceKey.create(Registries.PLACED_FEATURE, Identifier.parse(id))));
        }
        BiomeModifications.create(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "iaf_features")).add(ModificationPhase.ADDITIONS, BiomeSelectors.all(),
            (selection, context) -> IafWorldRegistry.addFeatures(selection.getBiomeRegistryEntry(), features,
                (step, feature) -> context.getGenerationSettings().addFeature(step, feature.unwrapKey().orElseThrow())));
        BiomeModifications.create(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "iaf_mob_spawns")).add(ModificationPhase.ADDITIONS, BiomeSelectors.all(),
            (selection, context) -> IafEntityRegistry.addSpawners(selection.getBiomeRegistryEntry(),
                (category, spawner, weight) -> context.getSpawnSettings().addSpawn(category, spawner, weight)));
    }

    /** Hoppers and pipes: the forge bricks and inputs give the core's slots (NeoForge: IafCapabilities) */
    private static void registerStorages() {
        ItemStorage.SIDED.registerForBlockEntity((brick, side) -> {
            TileEntityDragonforge core = brick.getConnectedTileEntity();
            return core != null ? InventoryStorage.of(core, side) : null;
        }, IafTileEntityRegistry.DRAGONFORGE_BRICK.get());
        ItemStorage.SIDED.registerForBlockEntity((input, side) -> {
            TileEntityDragonforge core = input.getCore();
            return core != null ? InventoryStorage.of(core, side) : null;
        }, IafTileEntityRegistry.DRAGONFORGE_INPUT.get());
    }

    /** VillagerTradesEvent per level: the listener fills one map, only the list of the level Fabric asks for is kept */
    private static void registerTrades() {
        for (var profession : BuiltInRegistries.VILLAGER_PROFESSION.registryKeySet()) {
            if (!profession.identifier().getNamespace().equals(IceAndFire.MODID)) {
                continue;
            }
            for (int level = 1; level <= 5; level++) {
                int tradeLevel = level;
                net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper.registerVillagerOffers(profession, level, factories -> {
                    Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = new Int2ObjectOpenHashMap<>();
                    for (int other = 1; other <= 5; other++) {
                        trades.put(other, other == tradeLevel ? factories : new ArrayList<>());
                    }
                    IafBus.post(new dev.vladeezyy.iceandfire.bus.VillagerTradesEvent(trades, profession));
                });
            }
        }
    }

    /** The game events with a Fabric API callback; the others are mixins (dev.vladeezyy.iceandfire.fabric.mixin) */
    private static void registerGameEvents() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.EntityJoinLevelEvent(entity, level)));
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, source, amount) -> !IafBus.post(new dev.vladeezyy.iceandfire.bus.LivingDeathEvent(entity, source)));
        AttackEntityCallback.EVENT.register((player, level, hand, entity, hit) ->
            IafBus.post(new dev.vladeezyy.iceandfire.bus.AttackEntityEvent(player, entity)) ? InteractionResult.FAIL : InteractionResult.PASS);
        PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, blockEntity) ->
            !IafBus.post(new dev.vladeezyy.iceandfire.bus.BlockEvent.BreakEvent(level, pos, state, player)));
        EntityTrackingEvents.START_TRACKING.register((target, player) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.PlayerEvent.StartTracking(player, target)));
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> IafBus.post(new dev.vladeezyy.iceandfire.bus.PlayerEvent.PlayerLoggedOutEvent(handler.player)));
        UseEntityCallback.EVENT.register((player, level, hand, entity, hit) -> {
            // NeoForge's EntityInteract is the plain interaction, not the one at a point (hit != null)
            if (hit != null) {
                return InteractionResult.PASS;
            }
            var event = new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.EntityInteract(player, hand, entity);
            return IafBus.post(event) ? event.getCancellationResult() : InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            var event = new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.RightClickBlock(player, hand, hit.getBlockPos(), hit.getDirection());
            return IafBus.post(event) ? event.getCancellationResult() : InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, level, hand) -> {
            var event = new dev.vladeezyy.iceandfire.bus.PlayerInteractEvent.RightClickItem(player, hand);
            return IafBus.post(event) ? event.getCancellationResult() : InteractionResult.PASS;
        });
    }
}
