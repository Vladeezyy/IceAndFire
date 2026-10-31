package dev.vladeezyy.iceandfire.fabric;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexSentinel;
import com.github.alexthe666.iceandfire.entity.props.EntityData;
import com.github.alexthe666.iceandfire.item.IafStackStatsItem;
import dev.vladeezyy.iceandfire.platform.Platform;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.WitherSkull;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Ice and Fire on Fabric: Fabric API where it has an equivalent of the NeoForge API, vanilla behaviour for NeoForge's
 * hooks. Entries are queued and registered by {@link #registerAll()} in vanilla's registry order.
 */
public final class FabricPlatform implements Platform {
    private record Pending(Identifier id, Supplier<?> factory) {}

    private static final Map<ResourceKey<?>, List<Pending>> PENDING = new LinkedHashMap<>();
    /** Sends a packet to the server; set by the client entry point (the client networking class is client-only) */
    static Consumer<CustomPacketPayload> clientSender = payload -> {
        throw new IllegalStateException("Packets to the server can only be sent from the client");
    };
    @Nullable
    static MinecraftServer server;

    /** The original's capability "iceandfire:entity_data", saved with the entity */
    static final AttachmentType<EntityData> ENTITY_DATA = AttachmentRegistry.create(Identifier.fromNamespaceAndPath(IceAndFire.MODID, "entity_data"),
        builder -> builder.initializer(EntityData::new).persistent(CompoundTag.CODEC.xmap(tag -> {
            EntityData data = new EntityData();
            data.deserialize(tag);
            return data;
        }, EntityData::serialize)));

    /** Registers the queued entries: attributes, data components, armor materials, then vanilla order, then modded registries */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void registerAll() {
        Set<Identifier> order = new LinkedHashSet<>();
        order.add(Registries.ATTRIBUTE.location());
        order.add(Registries.DATA_COMPONENT_TYPE.location());
        order.add(Registries.ARMOR_MATERIAL.location());
        List<Identifier> modded = new ArrayList<>();
        for (Registry<?> each : BuiltInRegistries.REGISTRY) {
            Identifier id = each.key().location();
            if (id.getNamespace().equals("minecraft")) {
                order.add(id);
            } else {
                modded.add(id);
            }
        }
        modded.sort(java.util.Comparator.comparing(Identifier::getNamespace).thenComparing(Identifier::getPath));
        order.addAll(modded);
        for (Identifier registryId : order) {
            List<Pending> entries = PENDING.remove(ResourceKey.createRegistryKey(registryId));
            if (entries == null) {
                continue;
            }
            Registry registry = BuiltInRegistries.REGISTRY.get(registryId);
            for (Pending entry : entries) {
                Registry.register(registry, entry.id(), entry.factory().get());
            }
        }
        if (!PENDING.isEmpty()) {
            throw new IllegalStateException("Ice and Fire entries for registries that do not exist: " + PENDING.keySet());
        }
    }

    @Override
    public String name() {
        return "fabric";
    }

    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    @Nullable
    public MinecraftServer currentServer() {
        return server;
    }

    @Override
    public String modVersion() {
        return FabricLoader.getInstance().getModContainer(IceAndFire.MODID).map(mod -> mod.getMetadata().getVersion().getFriendlyString()).orElse("UNKNOWN");
    }

    // --- registration ---

    @Override
    public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, Identifier id, Supplier<? extends T> factory) {
        PENDING.computeIfAbsent(registry, key -> new ArrayList<>()).add(new Pending(id, factory));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        return new SpawnEggItem((EntityType<? extends Mob>) type.get(), background, highlight, properties);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    // --- network ---

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        clientSender.accept(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    @Override
    public void sendToAll(CustomPacketPayload payload) {
        if (server != null) {
            for (ServerPlayer player : PlayerLookup.all(server)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            ServerPlayNetworking.send(player, payload);
        }
        if (entity instanceof ServerPlayer self) {
            ServerPlayNetworking.send(self, payload);
        }
    }

    @Override
    public void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(level, pos)) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    // --- game hooks: what NeoForge does without listeners ---

    @Override
    public boolean canEntityGrief(Level level, @Nullable Entity entity) {
        return level.getGameRules().get(GameRules.MOB_GRIEFING);
    }

    @Override
    public void onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hit) {
        return false;
    }

    @Override
    public int experienceDrop(LivingEntity entity, @Nullable Player attacker, int experience) {
        return experience;
    }

    @Override
    public boolean canShieldBlock(ItemStack stack) {
        return stack.getUseAnimation() == ItemUseAnimation.BLOCK;
    }

    @Override
    public EntityData entityData(LivingEntity entity) {
        return entity.getAttachedOrCreate(ENTITY_DATA);
    }

    @Override
    public Optional<EntityData> existingEntityData(LivingEntity entity) {
        return Optional.ofNullable(entity.getAttached(ENTITY_DATA));
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof EnderDragon) {
            return !state.is(BlockTags.DRAGON_IMMUNE);
        }
        if (entity instanceof WitherBoss) {
            return state.isAir() || WitherBoss.canDestroy(state);
        }
        return true;
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.is(BlockTags.CLIMBABLE);
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.FIRE) || state.is(Blocks.LAVA);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        state.getBlock().wasExploded(level, pos, explosion);
    }

    @Override
    public boolean canRiderInteract(Entity entity) {
        return false;
    }

    @Override
    public boolean shouldRiderSit(Entity vehicle) {
        // the mod's NeoForge overrides (Entity#shouldRiderSit), vanilla riders always sit
        if (vehicle instanceof EntityDragonBase dragon) {
            return dragon.shouldRiderSit();
        }
        if (vehicle instanceof EntityCyclops cyclops) {
            return cyclops.shouldRiderSit();
        }
        if (vehicle instanceof EntityMyrmexSentinel sentinel) {
            return sentinel.shouldRiderSit();
        }
        return true;
    }

    @Override
    public float digSpeed(Player player, BlockState state, BlockPos pos) {
        return player.getDestroySpeed(state);
    }

    @Override
    public ItemAttributeModifiers attributeModifiers(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        if (modifiers.modifiers().isEmpty()) {
            modifiers = stack.getItem() instanceof IafStackStatsItem item ? item.getDefaultAttributeModifiers(stack) : stack.getItem().getDefaultAttributeModifiers();
        }
        return modifiers;
    }

    @Override
    public void onLivingJump(LivingEntity entity) {
    }
}
