package com.github.alexthe666.iceandfire.client.render;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.item.ItemDragonHorn;
import com.github.alexthe666.iceandfire.item.ItemSummoningCrystal;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * The mod's item model properties (26.x item definitions in assets/iceandfire/items; 1.21.1: ItemProperties). The
 * dragon bow's pull and the tide trident's throwing are vanilla's using_item / use_duration.
 */
public final class IafItemModels {
    public static final Identifier ICE_OR_FIRE = Identifier.fromNamespaceAndPath(IceAndFire.MODID, "iceorfire");
    public static final Identifier HAS_DRAGON = Identifier.fromNamespaceAndPath(IceAndFire.MODID, "has_dragon");

    private IafItemModels() {
    }

    /** The dragon in a dragon horn: its type * 0.25 (0 none, 0.25 fire, 0.5 ice, 0.75 lightning) */
    public record IceOrFire() implements RangeSelectItemModelProperty {
        public static final MapCodec<IceOrFire> MAP_CODEC = MapCodec.unit(new IceOrFire());

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
            return ItemDragonHorn.getDragonType(stack) * 0.25F;
        }

        @Override
        public MapCodec<IceOrFire> type() {
            return MAP_CODEC;
        }
    }

    /** A summoning crystal bound to a dragon */
    public record HasDragon() implements ConditionalItemModelProperty {
        public static final MapCodec<HasDragon> MAP_CODEC = MapCodec.unit(new HasDragon());

        @Override
        public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext display) {
            return ItemSummoningCrystal.hasDragon(stack);
        }

        @Override
        public MapCodec<HasDragon> type() {
            return MAP_CODEC;
        }
    }
}
