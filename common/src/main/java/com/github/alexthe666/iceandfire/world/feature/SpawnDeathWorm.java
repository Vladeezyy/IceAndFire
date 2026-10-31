package com.github.alexthe666.iceandfire.world.feature;
import com.github.alexthe666.iceandfire.world.gen.IafFeature;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnDeathWorm extends IafFeature {

    public SpawnDeathWorm() {
    }

    @Override
    public boolean place(IafFeature.Context context) {
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();
        position = worldIn.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, position.offset(8, 0, 8));

        if (IafWorldRegistry.isFarEnoughFromSpawn(worldIn, position)) {
            if (rand.nextInt(IafConfig.deathWormSpawnRate + 1) == 0) {
                EntityDeathWorm deathWorm = IafEntityRegistry.DEATH_WORM.get().create(worldIn.getLevel(), EntitySpawnReason.STRUCTURE);
                deathWorm.setPos(position.getX() + 0.5F, position.getY() + 1, position.getZ() + 0.5F);
                deathWorm.finalizeSpawn(worldIn, worldIn.getCurrentDifficultyAt(position), EntitySpawnReason.CHUNK_GENERATION, null);
                worldIn.addFreshEntity(deathWorm);
            }
        }

        return true;
    }
}
