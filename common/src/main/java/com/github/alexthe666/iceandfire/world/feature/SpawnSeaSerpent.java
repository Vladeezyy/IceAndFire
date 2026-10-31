package com.github.alexthe666.iceandfire.world.feature;
import com.github.alexthe666.iceandfire.world.gen.IafFeature;
import net.minecraft.world.entity.EntitySpawnReason;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;

public class SpawnSeaSerpent extends IafFeature {

    public SpawnSeaSerpent() {
    }

    @Override
    public boolean place(IafFeature.Context context) {
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();

        position = worldIn.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, position.offset(8, 0, 8));
        BlockPos oceanPos = worldIn.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, position.offset(8, 0, 8));

        if (IafWorldRegistry.isFarEnoughFromSpawn(worldIn, position) && rand.nextInt(IafConfig.seaSerpentSpawnChance + 1) == 0) {
            BlockPos pos = oceanPos.offset(rand.nextInt(10) - 5, rand.nextInt(30), rand.nextInt(10) - 5);
            if (worldIn.getFluidState(pos).getType() == Fluids.WATER) {
                EntitySeaSerpent serpent = IafEntityRegistry.SEA_SERPENT.get().create(worldIn.getLevel(), EntitySpawnReason.STRUCTURE);
                serpent.onWorldSpawn(rand);
                serpent.snapTo(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0, 0);
                worldIn.addFreshEntity(serpent);
            }
        }

        return true;
    }
}
