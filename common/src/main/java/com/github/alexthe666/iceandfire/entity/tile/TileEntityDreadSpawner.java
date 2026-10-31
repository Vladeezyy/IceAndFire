package com.github.alexthe666.iceandfire.entity.tile;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * SpawnerBlockEntity with the dread spawner type: in 1.21 the constructor of BlockEntity checks the block, extending
 * SpawnerBlockEntity (type minecraft:mob_spawner) crashed the generation of the dread mausoleum.
 */
public class TileEntityDreadSpawner extends BlockEntity implements Spawner {
    private final DreadSpawnerBaseLogic spawner = new DreadSpawnerBaseLogic() {
        @Override
        public void broadcastEvent(Level p_155767_, @NotNull BlockPos p_155768_, int p_155769_) {
            p_155767_.blockEvent(p_155768_, Blocks.SPAWNER, p_155769_, 0);
        }

        @Override
        public void setNextSpawnData(@Nullable Level p_155771_, @NotNull BlockPos p_155772_, @NotNull SpawnData p_155773_) {
            super.setNextSpawnData(p_155771_, p_155772_, p_155773_);
            if (p_155771_ != null) {
                BlockState blockstate = p_155771_.getBlockState(p_155772_);
                p_155771_.sendBlockUpdated(p_155772_, blockstate, blockstate, 4);
            }

        }

        // NeoForge extension method (no vanilla override)
        @javax.annotation.Nullable
        public com.mojang.datafixers.util.Either<net.minecraft.world.level.block.entity.BlockEntity, net.minecraft.world.entity.Entity> getOwner() {
            return com.mojang.datafixers.util.Either.left(TileEntityDreadSpawner.this);
        }
    };

    public TileEntityDreadSpawner(BlockPos pos, BlockState state) {
        super(IafTileEntityRegistry.DREAD_SPAWNER.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput p_155760_) {
        super.loadAdditional(p_155760_);
        this.spawner.load(this.level, this.worldPosition, p_155760_);
    }

    @Override
    protected void saveAdditional(ValueOutput p_59795_) {
        super.saveAdditional(p_59795_);
        this.spawner.save(p_59795_);
    }

    public static void clientTick(Level p_155755_, BlockPos p_155756_, BlockState p_155757_, TileEntityDreadSpawner p_155758_) {
        p_155758_.spawner.clientTick(p_155755_, p_155756_);
    }

    public static void serverTick(Level p_155762_, BlockPos p_155763_, BlockState p_155764_, TileEntityDreadSpawner p_155765_) {
        p_155765_.spawner.serverTick((ServerLevel) p_155762_, p_155763_);
    }

    @Override
    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag compoundtag = this.saveCustomOnly(registries);
        compoundtag.remove("SpawnPotentials");
        return compoundtag;
    }

    @Override
    public boolean triggerEvent(int p_59797_, int p_59798_) {
        return this.spawner.onEventTriggered(this.level, p_59797_) || super.triggerEvent(p_59797_, p_59798_);
    }

    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void setEntityId(@NotNull EntityType<?> type, @NotNull RandomSource random) {
        this.spawner.setEntityId(type, this.level, random, this.worldPosition);
        this.setChanged();
    }

    public @NotNull BaseSpawner getSpawner() {
        return this.spawner;
    }


    @Override
    public void setEntityData(net.minecraft.world.item.component.TypedEntityData<net.minecraft.world.entity.EntityType<?>> data, net.minecraft.util.RandomSource random) {
        this.spawner.setEntityData(data, this.level, random, this.worldPosition);
        this.setChanged();
    }
}
