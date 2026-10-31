package com.github.alexthe666.iceandfire.entity.tile;
import net.minecraft.world.level.block.Block;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import dev.vladeezyy.iceandfire.platform.Registrar;
import net.minecraft.core.registries.BuiltInRegistries;
import dev.vladeezyy.iceandfire.platform.RegistryEntry;

import java.util.function.Supplier;

public class IafTileEntityRegistry {

    public static final Registrar<BlockEntityType<?>> TYPES = Registrar.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, IceAndFire.MODID);

    //@formatter:off
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityLectern>> IAF_LECTERN = registerTileEntity(TileEntityLectern::new, "lectern", () -> new Block[]{IafBlockRegistry.LECTERN.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityPodium>> PODIUM = registerTileEntity(TileEntityPodium::new, "podium", () -> new Block[]{IafBlockRegistry.PODIUM_OAK.get(), IafBlockRegistry.PODIUM_BIRCH.get(), IafBlockRegistry.PODIUM_SPRUCE.get(), IafBlockRegistry.PODIUM_JUNGLE.get(), IafBlockRegistry.PODIUM_DARK_OAK.get(), IafBlockRegistry.PODIUM_ACACIA.get(), IafBlockRegistry.PODIUM_CRIMSON.get(), IafBlockRegistry.PODIUM_WARPED.get(), IafBlockRegistry.PODIUM_MANGROVE.get(), IafBlockRegistry.PODIUM_CHERRY.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityEggInIce>> EGG_IN_ICE = registerTileEntity(TileEntityEggInIce::new, "egginice", () -> new Block[]{IafBlockRegistry.EGG_IN_ICE.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityPixieHouse>> PIXIE_HOUSE = registerTileEntity(TileEntityPixieHouse::new, "pixie_house", () -> new Block[]{IafBlockRegistry.PIXIE_HOUSE_MUSHROOM_RED.get(), IafBlockRegistry.PIXIE_HOUSE_MUSHROOM_BROWN.get(), IafBlockRegistry.PIXIE_HOUSE_OAK.get(), IafBlockRegistry.PIXIE_HOUSE_BIRCH.get(), IafBlockRegistry.PIXIE_HOUSE_BIRCH.get(), IafBlockRegistry.PIXIE_HOUSE_SPRUCE.get(), IafBlockRegistry.PIXIE_HOUSE_DARK_OAK.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityJar>> PIXIE_JAR = registerTileEntity(TileEntityJar::new, "pixie_jar", () -> new Block[]{IafBlockRegistry.JAR_EMPTY.get(), IafBlockRegistry.JAR_PIXIE_0.get(), IafBlockRegistry.JAR_PIXIE_1.get(), IafBlockRegistry.JAR_PIXIE_2.get(), IafBlockRegistry.JAR_PIXIE_3.get(), IafBlockRegistry.JAR_PIXIE_4.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityMyrmexCocoon>> MYRMEX_COCOON = registerTileEntity(TileEntityMyrmexCocoon::new, "myrmex_cocoon", () -> new Block[]{IafBlockRegistry.DESERT_MYRMEX_COCOON.get(), IafBlockRegistry.JUNGLE_MYRMEX_COCOON.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityDragonforge>> DRAGONFORGE_CORE = registerTileEntity(TileEntityDragonforge::new, "dragonforge_core", () -> new Block[]{IafBlockRegistry.DRAGONFORGE_FIRE_CORE.get(), IafBlockRegistry.DRAGONFORGE_ICE_CORE.get(), IafBlockRegistry.DRAGONFORGE_FIRE_CORE_DISABLED.get(), IafBlockRegistry.DRAGONFORGE_ICE_CORE_DISABLED.get(), IafBlockRegistry.DRAGONFORGE_LIGHTNING_CORE.get(), IafBlockRegistry.DRAGONFORGE_LIGHTNING_CORE_DISABLED.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityDragonforgeBrick>> DRAGONFORGE_BRICK = registerTileEntity(TileEntityDragonforgeBrick::new, "dragonforge_brick", () -> new Block[]{IafBlockRegistry.DRAGONFORGE_FIRE_BRICK.get(), IafBlockRegistry.DRAGONFORGE_ICE_BRICK.get(), IafBlockRegistry.DRAGONFORGE_LIGHTNING_BRICK.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityDragonforgeInput>> DRAGONFORGE_INPUT = registerTileEntity(TileEntityDragonforgeInput::new, "dragonforge_input", () -> new Block[]{IafBlockRegistry.DRAGONFORGE_FIRE_INPUT.get(), IafBlockRegistry.DRAGONFORGE_ICE_INPUT.get(), IafBlockRegistry.DRAGONFORGE_LIGHTNING_INPUT.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityDreadPortal>> DREAD_PORTAL = registerTileEntity(TileEntityDreadPortal::new, "dread_portal", () -> new Block[]{IafBlockRegistry.DREAD_PORTAL.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityDreadSpawner>> DREAD_SPAWNER = registerTileEntity(TileEntityDreadSpawner::new, "dread_spawner", () -> new Block[]{IafBlockRegistry.DREAD_SPAWNER.get()});
    public static final RegistryEntry<BlockEntityType<?>, BlockEntityType<TileEntityGhostChest>> GHOST_CHEST = registerTileEntity(TileEntityGhostChest::new, "ghost_chest", () -> new Block[]{IafBlockRegistry.GHOST_CHEST.get()});


    // 26.x: no BlockEntityType.Builder, the type takes its factory and blocks (copyOf: the pixie house lists birch twice)
    public static <T extends BlockEntity> RegistryEntry<BlockEntityType<?>, BlockEntityType<T>> registerTileEntity(
            BlockEntityType.BlockEntitySupplier<T> factory, String entityName, Supplier<Block[]> blocks) {
        return TYPES.register(entityName, () -> new BlockEntityType<>(factory, java.util.Set.copyOf(java.util.Arrays.asList(blocks.get()))));
    }
}
