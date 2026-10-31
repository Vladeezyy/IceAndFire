package com.github.alexthe666.iceandfire.selftest;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexQueen;
import com.github.alexthe666.iceandfire.entity.IafEntityRegistry;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import com.github.alexthe666.iceandfire.world.gen.WorldGenMyrmexHive;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.phys.AABB;

/** A myrmex hive with its queen for the tests of #5197 (the queen left her hive). */
public class TestHive {
    public final MyrmexHive hive;
    public final EntityMyrmexQueen queen;
    public final int hivesBefore;

    private TestHive(MyrmexHive hive, EntityMyrmexQueen queen, int hivesBefore) {
        this.hive = hive;
        this.queen = queen;
        this.hivesBefore = hivesBefore;
    }

    public static void forceArea(ServerLevel level, ChunkPos center, boolean load) {
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                level.setChunkForced(center.x + x, center.z + z, load);
            }
        }
    }

    /**
     * Builds the hive in a finished world as a queen digs it (small: the surface heightmaps of the world, not of
     * worldgen) 25 blocks below the surface, and places the queen as the worldgen does.
     */
    public static TestHive place(ServerLevel level, ChunkPos chunk) {
        BlockPos top = chunk.getMiddleBlockPosition(0);
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, top.getX(), top.getZ());
        BlockPos center = new BlockPos(top.getX(), Math.min(surface, level.getSeaLevel()) - 25, top.getZ());
        WorldGenMyrmexHive gen = new WorldGenMyrmexHive(true, false, NoneFeatureConfiguration.CODEC);
        gen.placeSmallGen(level, level.random, center);
        EntityMyrmexQueen queen = IafEntityRegistry.MYRMEX_QUEEN.get().create(level);
        BlockPos ground = MyrmexHive.getGroundedPos(level, center);
        queen.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.CHUNK_GENERATION, null);
        queen.setHive(gen.hive);
        queen.setPersistenceRequired();
        queen.moveTo(ground.getX() + 0.5D, ground.getY() + 1D, ground.getZ() + 0.5D, 0, 0);
        level.addFreshEntity(queen);
        return new TestHive(gen.hive, queen, MyrmexWorldData.get(level).getHivelist().size());
    }

    /** Workers, soldiers and a sentinel of the hive, as the world generation of a big hive adds them. */
    public int populate(ServerLevel level) {
        BlockPos ground = MyrmexHive.getGroundedPos(level, hive.getCenter());
        int added = 0;
        for (int i = 0; i < 9; i++) {
            var type = i < 5 ? IafEntityRegistry.MYRMEX_WORKER.get() : i < 8 ? IafEntityRegistry.MYRMEX_SOLDIER.get() : IafEntityRegistry.MYRMEX_SENTINEL.get();
            com.github.alexthe666.iceandfire.entity.EntityMyrmexBase myrmex = type.create(level);
            myrmex.finalizeSpawn(level, level.getCurrentDifficultyAt(ground), EntitySpawnReason.CHUNK_GENERATION, null);
            myrmex.setHive(hive);
            myrmex.setJungleVariant(queen.isJungle());
            myrmex.setPersistenceRequired();
            myrmex.moveTo(ground.getX() + 0.5D, ground.getY() + 1D, ground.getZ() + 0.5D, 0, 0);
            if (level.addFreshEntity(myrmex)) {
                added++;
            }
        }
        return added;
    }

    public double distance() {
        return Math.sqrt(queen.distanceToSqr(hive.getCenter().getCenter()));
    }

    public boolean queenAtHome(ServerLevel level) {
        return queen.isAlive() && queen.getHive() == hive && queen.isInHive() && MyrmexWorldData.get(level).getHivelist().size() == hivesBefore;
    }

    public String state(ServerLevel level) {
        long myrmexes = level.getEntitiesOfClass(com.github.alexthe666.iceandfire.entity.EntityMyrmexBase.class, new AABB(hive.getCenter()).inflate(96),
            myrmex -> myrmex.getHive() == hive).size();
        return "alive " + queen.isAlive() + ", own hive " + (queen.getHive() == hive) + ", in hive " + queen.isInHive() + ", "
            + String.format("%.1f", distance()) + " blocks from the center, hives " + hivesBefore + " -> "
            + MyrmexWorldData.get(level).getHivelist().size() + ", sees the sky " + queen.canSeeSky() + ", " + myrmexes + " myrmexes of the hive";
    }
}
