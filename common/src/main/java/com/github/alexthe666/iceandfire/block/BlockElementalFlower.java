package com.github.alexthe666.iceandfire.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockElementalFlower extends net.minecraft.world.level.block.VegetationBlock {
    public static final MapCodec<BlockElementalFlower> CODEC = simpleCodec(properties -> new BlockElementalFlower());

    @Override
    protected @NotNull MapCodec<? extends BlockElementalFlower> codec() {
        return CODEC;
    }

    public Item itemBlock;
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public BlockElementalFlower() {
        super(
            dev.vladeezyy.iceandfire.platform.Registrar.blockProps()
                .mapColor(MapColor.PLANT)
                .replaceable()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY)
                .noOcclusion()
                .noCollision()
                .dynamicShape()
                .randomTicks()
                .sound(SoundType.GRASS)
        );
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos) {
        Block block = state.getBlock();
        return block == Blocks.GRASS_BLOCK || block == Blocks.DIRT || block == Blocks.COARSE_DIRT || block == Blocks.PODZOL || block == Blocks.FARMLAND || state.is(BlockTags.SAND);
    }

    public boolean canStay(Level worldIn, BlockPos pos) {
        BlockState soil = worldIn.getBlockState(pos.below());
        if (this == IafBlockRegistry.FIRE_LILY.get()) {
            return soil.is(BlockTags.SAND) || soil.is(Blocks.NETHERRACK);
        } else if (this == IafBlockRegistry.LIGHTNING_LILY.get()) {
            return soil.is(BlockTags.DIRT) || soil.is(Blocks.SHORT_GRASS);
        } else {
            return soil.is(BlockTags.ICE) || soil.is(BlockTags.SNOW) || soil.is(BlockTags.SUPPORT_OVERRIDE_SNOW_LAYER);
        }
    }

    public void updateTick(Level worldIn, BlockPos pos, BlockState state, Random rand) {
        this.checkFall(worldIn, pos);
    }

    private boolean checkFall(Level worldIn, BlockPos pos) {
        if (!this.canStay(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
            return false;
        } else {
            return true;
        }
    }

    protected boolean canSustainBush(BlockState state) {
        return true;
    }

}
