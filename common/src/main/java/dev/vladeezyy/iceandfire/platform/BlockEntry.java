package dev.vladeezyy.iceandfire.platform;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

/** NeoForge's {@code DeferredBlock}: a registered block that is also an {@link ItemLike} (its block item) */
public class BlockEntry<T extends Block> extends RegistryEntry<Block, T> implements ItemLike {
    protected BlockEntry(Identifier id) {
        super(Registries.BLOCK, id);
    }

    @Override
    public Item asItem() {
        return this.get().asItem();
    }
}
