package com.github.alexthe666.iceandfire.world.feature;
import com.github.alexthe666.iceandfire.world.gen.IafFeature;
import net.minecraft.world.entity.EntitySpawnReason;

import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;

public class SpawnDragonSkeleton extends IafFeature {

    protected EntityType<? extends EntityDragonBase> dragonType;

    public SpawnDragonSkeleton(EntityType<? extends EntityDragonBase> dt) {
        dragonType = dt;
    }

    @Override
    public boolean place(IafFeature.Context context) {
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();

        position = worldIn.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, position.offset(8, 0, 8));

        if (IafConfig.generateDragonSkeletons) {
            if (rand.nextInt(IafConfig.generateDragonSkeletonChance + 1) == 0) {
                EntityDragonBase dragon = dragonType.create(worldIn.getLevel(), EntitySpawnReason.STRUCTURE);
                dragon.setPos(position.getX() + 0.5F, position.getY() + 1, position.getZ() + 0.5F);
                int dragonage = 10 + rand.nextInt(100);
                dragon.growDragon(dragonage);
                dragon.modelDeadProgress = 20;
                dragon.setModelDead(true);
                dragon.setDeathStage((dragonage / 5) / 2);
                dragon.setYRot(rand.nextInt(360));
                worldIn.addFreshEntity(dragon);
            }
        }

        return true;
    }
}
