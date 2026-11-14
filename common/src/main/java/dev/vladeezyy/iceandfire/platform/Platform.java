package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/** What the common code of Ice and Fire needs from the mod loader (NeoForge or Fabric), found by {@link ServiceLoader} */
public interface Platform {
    Platform INSTANCE = ServiceLoader.load(Platform.class, Platform.class.getClassLoader()).findFirst()
        .orElseThrow(() -> new IllegalStateException("No Ice and Fire platform for this mod loader"));

    /** "neoforge" or "fabric" */
    String name();

    Path configDir();

    boolean isModLoaded(String modId);

    /** True in a client (also while it runs the integrated server) */
    boolean isClient();

    /** The running server (the integrated one in single player), or null */
    @javax.annotation.Nullable
    net.minecraft.server.MinecraftServer currentServer();

    /** The mod's version as the loader read it from the jar */
    String modVersion();

    // --- registration ---

    /** Registers {@code factory}'s value under {@code id} (NeoForge: on its register event, Fabric: in registry order at init) */
    <R, T extends R> void register(ResourceKey<? extends Registry<R>> registry, ResourceLocation id, Supplier<? extends T> factory);

    // --- network ---

    void sendToServer(CustomPacketPayload payload);

    void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

    void sendToAll(CustomPacketPayload payload);

    void sendToTrackingAndSelf(Entity entity, CustomPacketPayload payload);

    void sendToTrackingChunk(ServerLevel level, BlockPos pos, CustomPacketPayload payload);

    // --- game hooks (NeoForge fires its events here, Fabric does what vanilla does) ---

    /** Whether a mob may change blocks (the mobGriefing rule, NeoForge: EntityMobGriefingEvent) */
    boolean canEntityGrief(net.minecraft.world.level.Level level, @javax.annotation.Nullable Entity entity);

    /**
     * Whether a player may attack the target, asked through the loader's attack event (NeoForge: AttackEntityEvent, Fabric:
     * AttackEntityCallback) so claim and PvP mods can refuse the gorgon head and the cockatrice scepter
     */
    boolean canPlayerAttack(net.minecraft.world.entity.player.Player player, Entity target);

    /** A player's item broke while used */
    void onPlayerDestroyItem(net.minecraft.world.entity.player.Player player, net.minecraft.world.item.ItemStack stack, @javax.annotation.Nullable net.minecraft.world.InteractionHand hand);

    /** True if the impact of a projectile was canceled */
    boolean onProjectileImpact(net.minecraft.world.entity.projectile.Projectile projectile, net.minecraft.world.phys.HitResult hit);

    /** The experience a mob drops */
    int experienceDrop(net.minecraft.world.entity.LivingEntity entity, @javax.annotation.Nullable net.minecraft.world.entity.player.Player attacker, int experience);

    /** Whether the item blocks attacks like a shield */
    boolean canShieldBlock(net.minecraft.world.item.ItemStack stack);

    // --- entity data (the original's capability "iceandfire:entity_data") ---

    /** The data of a living entity, created when missing */
    com.github.alexthe666.iceandfire.entity.props.EntityData entityData(net.minecraft.world.entity.LivingEntity entity);

    /** The data of a living entity if it has any */
    java.util.Optional<com.github.alexthe666.iceandfire.entity.props.EntityData> existingEntityData(net.minecraft.world.entity.LivingEntity entity);

    // --- block and entity hooks of NeoForge's extension interfaces (Fabric: what vanilla does) ---

    /** A spawn egg (NeoForge: DeferredSpawnEggItem, which takes the entity type lazily) */
    net.minecraft.world.item.Item spawnEgg(Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> type, int background, int highlight, net.minecraft.world.item.Item.Properties properties);

    /** Whether a mob may break this block (vanilla: the dragon and wither immune tags) */
    boolean canEntityDestroy(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos, net.minecraft.world.entity.LivingEntity entity);

    /** Whether a mob can climb this block (vanilla: the climbable tag) */
    boolean isLadder(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, net.minecraft.world.entity.LivingEntity entity);

    /** Whether the block burns entities in it (vanilla: fire and lava) */
    boolean isBurning(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos);

    /** An explosion removes the block */
    void onBlockExploded(net.minecraft.world.level.block.state.BlockState state, net.minecraft.world.level.Level level, BlockPos pos, net.minecraft.world.level.Explosion explosion);

    /** Whether a rider may interact with entities around (NeoForge: Entity#canRiderInteract, false by default) */
    boolean canRiderInteract(Entity entity);

    /** Whether the passengers of a vehicle sit (NeoForge: Entity#shouldRiderSit) */
    boolean shouldRiderSit(Entity vehicle);

    /** How fast a player mines a block (NeoForge fires its break speed event) */
    float digSpeed(net.minecraft.world.entity.player.Player player, net.minecraft.world.level.block.state.BlockState state, BlockPos pos);

    /** The attribute modifiers of a stack (NeoForge: with ItemAttributeModifierEvent) */
    net.minecraft.world.item.component.ItemAttributeModifiers attributeModifiers(net.minecraft.world.item.ItemStack stack);

    /** A mob jumped by itself (NeoForge: LivingJumpEvent) */
    void onLivingJump(net.minecraft.world.entity.LivingEntity entity);

    /** A creative tab builder (NeoForge's builder() puts the tab after the vanilla ones) */
    net.minecraft.world.item.CreativeModeTab.Builder creativeTabBuilder();
}
