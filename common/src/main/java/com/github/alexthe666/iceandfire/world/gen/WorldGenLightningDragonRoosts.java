package com.github.alexthe666.iceandfire.world.gen;

import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import com.github.alexthe666.iceandfire.block.IafBlockRegistry;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import dev.vladeezyy.iceandfire.platform.Tags;
import org.jetbrains.annotations.NotNull;

public class WorldGenLightningDragonRoosts extends WorldGenDragonRoosts {
    private static final ResourceKey<LootTable> DRAGON_CHEST = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("iceandfire", "chest/lightning_dragon_roost"));

    public WorldGenLightningDragonRoosts() {
        super(IafBlockRegistry.COPPER_PILE.get());
    }

    @Override
    protected EntityType<? extends EntityDragonBase> getDragonType() {
        return IafEntityRegistry.LIGHTNING_DRAGON.get();
    }

    @Override
    protected ResourceKey<LootTable> getRoostLootTable() {
        return DRAGON_CHEST;
    }

    @Override
    protected BlockState transform(final BlockState state) {
        Block block = null;

        if (state.is(Blocks.GRASS_BLOCK)) {
            block = IafBlockRegistry.CRACKLED_GRASS.get();
        } else if (state.is(Blocks.DIRT_PATH)) {
            block = IafBlockRegistry.CRACKLED_DIRT_PATH.get();
        } else if (state.is(Tags.Blocks.GRAVELS)) {
            block = IafBlockRegistry.CRACKLED_GRAVEL.get();
        } else if (state.is(BlockTags.DIRT)) {
            block = IafBlockRegistry.CRACKLED_DIRT.get();
        } else if (state.is(Tags.Blocks.STONES)) {
            block = IafBlockRegistry.CRACKLED_STONE.get();
        } else if (state.is(Tags.Blocks.COBBLESTONES)) {
            block = IafBlockRegistry.CRACKLED_COBBLESTONE.get();
        } else if (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS)) {
            block = IafBlockRegistry.ASH.get();
        } else if (state.is(Blocks.SHORT_GRASS) || state.is(BlockTags.LEAVES) || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)) {
            block = Blocks.AIR;
        }

        if (block != null) {
            return block.defaultBlockState();
        }

        return state;
    }

    @Override
    protected void handleCustomGeneration(@NotNull final IafFeature.Context context, final BlockPos position, double distance) {
        if (distance > 0.05D && context.random().nextInt(800) == 0) {
            // FIXME
            new WorldGenRoostSpire().generate(context.level(), context.random(), getSurfacePosition(context.level(), position));
        }

        if (distance > 0.05D && context.random().nextInt(1000) == 0) {
            // FIXME
            new WorldGenRoostSpike(HORIZONTALS[context.random().nextInt(3)]).generate(context.level(), context.random(), getSurfacePosition(context.level(), position));
        }
    }
}
