package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.props.EntityData;
import dev.vladeezyy.iceandfire.platform.Platform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/** Ice and Fire on NeoForge: the NeoForge APIs and hooks the original code called */
public final class NeoForgePlatform implements Platform {
    /** Deferred registers per registry, filled by the common code before {@link #registerAll} */
    private static final Map<ResourceKey<?>, DeferredRegister<?>> REGISTERS = new LinkedHashMap<>();

    static void registerAll(IEventBus modBus) {
        REGISTERS.values().forEach(register -> register.register(modBus));
    }

    @Override
    public String name() {
        return "neoforge";
    }

    @Override
    public Path configDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isClient() {
        return FMLEnvironment.dist.isClient();
    }

    @Override
    @Nullable
    public MinecraftServer currentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public String modVersion() {
        return ModList.get().getModContainerById(IceAndFire.MODID).map(mod -> mod.getModInfo().getVersion().toString()).orElse("UNKNOWN");
    }

    // --- registration ---

    @SuppressWarnings("unchecked")
    private static <R> DeferredRegister<R> registerFor(ResourceKey<? extends Registry<R>> registry) {
        return (DeferredRegister<R>) REGISTERS.computeIfAbsent(registry, key -> DeferredRegister.create((ResourceKey<? extends Registry<R>>) key, IceAndFire.MODID));
    }

    @Override
    public <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, Supplier<? extends T> factory) {
        registerFor(registry).register(id.getPath(), factory);
    }

    @Override
    public Item spawnEgg(Supplier<? extends EntityType<? extends Mob>> type, int background, int highlight, Item.Properties properties) {
        return new DeferredSpawnEggItem(type, background, highlight, properties);
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder();
    }

    // --- network ---

    @Override
    public void sendToServer(CustomPacketPayload payload) {
        PacketDistributor.sendToServer(payload);
    }

    @Override
    public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayer(player, payload);
    }

    @Override
    public void sendToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, new ChunkPos(pos), payload);
    }

    // --- game hooks ---

    @Override
    public boolean canEntityGrief(Level level, @Nullable Entity entity) {
        return EventHooks.canEntityGrief(level, entity);
    }

    @Override
    public boolean canPlayerAttack(Player player, Entity target) {
        // The event alone: CommonHooks.onPlayerAttackTarget also runs the held weapon's onLeftClickEntity, a hit's side
        // effect (and a refusal from a weapon that is only recharging)
        return !net.neoforged.neoforge.common.NeoForge.EVENT_BUS.post(new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(player, target)).isCanceled();
    }

    @Override
    public void onPlayerDestroyItem(Player player, ItemStack stack, @Nullable InteractionHand hand) {
        EventHooks.onPlayerDestroyItem(player, stack, hand);
    }

    @Override
    public boolean onProjectileImpact(Projectile projectile, HitResult hit) {
        return EventHooks.onProjectileImpact(projectile, hit);
    }

    @Override
    public int experienceDrop(LivingEntity entity, @Nullable Player attacker, int experience) {
        return EventHooks.getExperienceDrop(entity, attacker, experience);
    }

    @Override
    public boolean canShieldBlock(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHIELD_BLOCK);
    }

    @Override
    public EntityData entityData(LivingEntity entity) {
        return entity.getData(IafAttachments.ENTITY_DATA);
    }

    @Override
    public Optional<EntityData> existingEntityData(LivingEntity entity) {
        return entity.getExistingData(IafAttachments.ENTITY_DATA);
    }

    @Override
    public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, LivingEntity entity) {
        return state.canEntityDestroy(level, pos, entity);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.isLadder(level, pos, entity);
    }

    @Override
    public boolean isBurning(BlockState state, BlockGetter level, BlockPos pos) {
        return state.isBurning(level, pos);
    }

    @Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        state.onBlockExploded(level, pos, explosion);
    }

    @Override
    public boolean canRiderInteract(Entity entity) {
        return entity.canRiderInteract();
    }

    @Override
    public boolean shouldRiderSit(Entity vehicle) {
        return vehicle.shouldRiderSit();
    }

    @Override
    public float digSpeed(Player player, BlockState state, BlockPos pos) {
        return player.getDigSpeed(state, pos);
    }

    @Override
    public ItemAttributeModifiers attributeModifiers(ItemStack stack) {
        return stack.getAttributeModifiers();
    }

    @Override
    public void onLivingJump(LivingEntity entity) {
        CommonHooks.onLivingJump(entity);
    }
}
