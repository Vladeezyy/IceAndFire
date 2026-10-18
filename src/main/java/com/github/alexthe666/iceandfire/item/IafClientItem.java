package com.github.alexthe666.iceandfire.item;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Items with client extensions (armor models, custom renderers). NeoForge 1.21 removed Item#initializeClient, the
 * extensions are collected in {@code RegisterClientExtensionsEvent}.
 */
public interface IafClientItem {
    void initializeClient(Consumer<IClientItemExtensions> consumer);
}
