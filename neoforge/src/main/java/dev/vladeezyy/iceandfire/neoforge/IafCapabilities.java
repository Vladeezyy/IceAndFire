package dev.vladeezyy.iceandfire.neoforge;

import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.tile.*;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Item handlers of the block entities and mounts (hoppers and pipes), formerly getCapability overrides */
final class IafCapabilities {
    private IafCapabilities() {
    }

    static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_CORE.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_BRICK.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.DRAGONFORGE_INPUT.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.PODIUM.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.IAF_LECTERN.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, IafTileEntityRegistry.PIXIE_JAR.get(), (be, side) -> NeoForgeTransfer.toNeo(be.getItemHandler(side)));
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.FIRE_DRAGON.get(), (dragon, context) -> NeoForgeTransfer.toNeo(dragon.getItemHandler()));
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.ICE_DRAGON.get(), (dragon, context) -> NeoForgeTransfer.toNeo(dragon.getItemHandler()));
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.LIGHTNING_DRAGON.get(), (dragon, context) -> NeoForgeTransfer.toNeo(dragon.getItemHandler()));
        event.registerEntity(Capabilities.ItemHandler.ENTITY, IafEntityRegistry.HIPPOCAMPUS.get(), (hippocampus, context) -> NeoForgeTransfer.toNeo(hippocampus.getItemHandler()));
    }
}
