package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import dev.vladeezyy.iceandfire.bus.SubscribeEvent;
import dev.vladeezyy.iceandfire.bus.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Item handlers of the block entities (hoppers and pipes), formerly getCapability overrides.
 */
@EventBusSubscriber(modid = IceAndFire.MODID, bus = EventBusSubscriber.Bus.MOD)
public class IafCapabilities {

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_CORE.get(), TileEntityDragonforge::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_BRICK.get(), TileEntityDragonforgeBrick::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_INPUT.get(), TileEntityDragonforgeInput::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.PODIUM.get(), TileEntityPodium::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.IAF_LECTERN.get(), TileEntityLectern::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.PIXIE_JAR.get(), TileEntityJar::getItemHandler);
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.FIRE_DRAGON.get(), (dragon, context) -> dragon.getItemHandler());
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.ICE_DRAGON.get(), (dragon, context) -> dragon.getItemHandler());
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.LIGHTNING_DRAGON.get(), (dragon, context) -> dragon.getItemHandler());
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.HIPPOCAMPUS.get(), (hippocampus, context) -> hippocampus.getItemHandler());
    }
}
