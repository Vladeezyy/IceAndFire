package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Registers the entries of one registry for a mod (the common replacement of NeoForge's {@code DeferredRegister}, with
 * the same calls). The loader decides when the factories run: NeoForge on its register event, Fabric right away.
 */
public class Registrar<R> {
    private final ResourceKey<? extends Registry<R>> registryKey;
    private final String modId;
    private final List<RegistryEntry<R, ? extends R>> entries = new ArrayList<>();

    protected Registrar(ResourceKey<? extends Registry<R>> registryKey, String modId) {
        this.registryKey = registryKey;
        this.modId = modId;
    }

    public static <R> Registrar<R> create(ResourceKey<? extends Registry<R>> registryKey, String modId) {
        return new Registrar<>(registryKey, modId);
    }

    public static <R> Registrar<R> create(Registry<R> registry, String modId) {
        return new Registrar<>(registry.key(), modId);
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Supplier<? extends T> factory) {
        return this.add(name, factory, new RegistryEntry<>(this.registryKey, ResourceLocation.fromNamespaceAndPath(this.modId, name)));
    }

    /** Queues {@code entry} (made for {@code name}) with the loader */
    protected <T extends R, E extends RegistryEntry<R, T>> E add(String name, Supplier<? extends T> factory, E entry) {
        this.entries.add(entry);
        Platform.INSTANCE.register(this.registryKey, entry.getId(), factory);
        return entry;
    }

    public static Items createItems(String modId) {
        return new Items(modId);
    }

    public static Blocks createBlocks(String modId) {
        return new Blocks(modId);
    }

    /** NeoForge's {@code DeferredRegister.Items}: its entries are {@link ItemEntry} (an {@code ItemLike}) */
    public static class Items extends Registrar<net.minecraft.world.item.Item> {
        protected Items(String modId) {
            super(net.minecraft.core.registries.Registries.ITEM, modId);
        }

        @Override
        public <T extends net.minecraft.world.item.Item> ItemEntry<T> register(String name, Supplier<? extends T> factory) {
            return this.add(name, factory, new ItemEntry<>(ResourceLocation.fromNamespaceAndPath(this.modId(), name)));
        }

        public <T extends net.minecraft.world.item.Item> ItemEntry<T> registerItem(String name, Function<net.minecraft.world.item.Item.Properties, ? extends T> factory, net.minecraft.world.item.Item.Properties properties) {
            return this.register(name, () -> factory.apply(properties));
        }

        public <T extends net.minecraft.world.item.Item> ItemEntry<T> registerItem(String name, Function<net.minecraft.world.item.Item.Properties, ? extends T> factory) {
            return this.registerItem(name, factory, new net.minecraft.world.item.Item.Properties());
        }

        public ItemEntry<net.minecraft.world.item.BlockItem> registerSimpleBlockItem(String name, Supplier<? extends net.minecraft.world.level.block.Block> block) {
            return this.register(name, () -> new net.minecraft.world.item.BlockItem(block.get(), new net.minecraft.world.item.Item.Properties()));
        }
    }

    /** NeoForge's {@code DeferredRegister.Blocks}: its entries are {@link BlockEntry} (an {@code ItemLike}) */
    public static class Blocks extends Registrar<net.minecraft.world.level.block.Block> {
        protected Blocks(String modId) {
            super(net.minecraft.core.registries.Registries.BLOCK, modId);
        }

        @Override
        public <T extends net.minecraft.world.level.block.Block> BlockEntry<T> register(String name, Supplier<? extends T> factory) {
            return this.add(name, factory, new BlockEntry<>(ResourceLocation.fromNamespaceAndPath(this.modId(), name)));
        }
    }

    public <T extends R> RegistryEntry<R, T> register(String name, Function<ResourceLocation, ? extends T> factory) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(this.modId, name);
        return this.register(name, () -> factory.apply(id));
    }

    public Collection<RegistryEntry<R, ? extends R>> getEntries() {
        return Collections.unmodifiableList(this.entries);
    }

    protected String modId() {
        return this.modId;
    }

    public ResourceKey<? extends Registry<R>> getRegistryKey() {
        return this.registryKey;
    }
}
