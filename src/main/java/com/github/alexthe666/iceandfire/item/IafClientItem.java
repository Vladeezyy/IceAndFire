package com.github.alexthe666.iceandfire.item;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Items with client extensions (armor models, custom renderers), collected in {@code RegisterClientExtensionsEvent}.
 * Not named initializeClient: NeoForge 21.1 still calls the deprecated Item#initializeClient, which would register
 * every extension twice and crash.
 */
public interface IafClientItem {
    void registerClientExtensions(Consumer<IClientItemExtensions> consumer);
}
