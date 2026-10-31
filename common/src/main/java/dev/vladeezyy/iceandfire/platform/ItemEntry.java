package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/** NeoForge's {@code DeferredItem}: a registered item that is also an {@link ItemLike} */
public class ItemEntry<T extends Item> extends RegistryEntry<Item, T> implements ItemLike {
    protected ItemEntry(Identifier id) {
        super(Registries.ITEM, id);
    }

    @Override
    public Item asItem() {
        return this.get();
    }

    public ItemStack toStack() {
        return new ItemStack(this.get());
    }

    public ItemStack toStack(int count) {
        return new ItemStack(this.get(), count);
    }
}
