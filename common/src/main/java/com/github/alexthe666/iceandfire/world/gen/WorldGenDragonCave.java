package com.github.alexthe666.iceandfire.world.gen;
import net.minecraft.world.entity.EntitySpawnReason;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import com.github.alexthe666.iceandfire.IafConfig;
import com.github.alexthe666.iceandfire.block.BlockGoldPile;
import com.github.alexthe666.iceandfire.datagen.tags.IafBlockTags;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.util.HomePosition;
import com.github.alexthe666.iceandfire.util.ShapeBuilder;
import com.github.alexthe666.iceandfire.world.IafWorldData;
import com.github.alexthe666.iceandfire.world.IafWorldRegistry;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.core.registries.BuiltInRegistries;

import net.minecraft.server.level.WorldGenRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class WorldGenDragonCave extends Feature<NoneFeatureConfiguration> implements TypedFeature {
    public ResourceKey<LootTable> DRAGON_CHEST;
    public ResourceKey<LootTable> DRAGON_MALE_CHEST;
    public WorldGenCaveStalactites CEILING_DECO;
    public BlockState PALETTE_BLOCK1;
    public BlockState PALETTE_BLOCK2;
    public TagKey<Block> dragonTypeOreTag;
    public BlockState TREASURE_PILE;
    private static final Direction[] HORIZONTALS = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    protected WorldGenDragonCave(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel worldIn = context.level();
        RandomSource rand = context.random();
        BlockPos position = context.origin();
        if (rand.nextInt(IafConfig.generateDragonDenChance) != 0 || !IafWorldRegistry.isFarEnoughFromSpawn(worldIn, position) || !IafWorldRegistry.isFarEnoughFromDangerousGen(worldIn, position, getId(), getFeatureType())) {
            return false;
        }
        // Not a field: the feature is one object for all the worldgen threads
        boolean isMale = rand.nextBoolean();
        ChunkPos chunkPos = worldIn.getChunk(position).getPos();


        int j = 40;
        // Update the position so it doesn't go above the ocean floor
        for(int k = 0; k < 20; ++k) {
            for(int l = 0; l < 20; ++l) {
                j = Math.min(j, worldIn.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, position.getX() + k, position.getZ() + l));
            }
        }

        // Offset the position randomly
        j -= 20;
        j -= rand.nextInt(30);

        // If the cave generation point is too low
        if (j < worldIn.getMinY() + 20) {
            return false;
        }
        // Center the position at the "middle" of the chunk
        position = new BlockPos((chunkPos.x << 4) + 8, j, (chunkPos.z << 4) + 8);
        int dragonAge = 75 + rand.nextInt(50);
        int radius = (int) (dragonAge * 0.2F) + rand.nextInt(4);
        generateCave(worldIn, radius, 3, position, rand, isMale);
        EntityDragonBase dragon = createDragon(worldIn, rand, position, dragonAge, isMale);
        worldIn.addFreshEntity(dragon);
        return true;
    }

    public void generateCave(LevelAccessor worldIn, int radius, int amount, BlockPos center, RandomSource rand, boolean isMale) {
        List<SphereInfo> sphereList = new ArrayList<>();
        sphereList.add(new SphereInfo(radius, center.immutable()));
        for (int i = 0; i < amount + rand.nextInt(2); i++) {
            Direction direction = HORIZONTALS[rand.nextInt(HORIZONTALS.length - 1)];
            int r = 2 * (int) (radius / 3F) + rand.nextInt(8);
            BlockPos centerOffset = center.relative(direction, radius - 2);
            sphereList.add(new SphereInfo(r, centerOffset));
        }
        // A bit per block instead of HashSets of ~150,000 BlockPos. The shapes are walked in the original's order (all the
        // shells, then the hollows, first sphere first), so the random hollows take the same random numbers
        CaveMask shell = CaveMask.around(worldIn, sphereList);
        CaveMask hollow = shell.emptyCopy();
        for (int i = 0; i < sphereList.size(); i++) {
            SphereInfo sphere = sphereList.get(i);
            // the main sphere is cut off at half its radius
            shell.addAll(ShapeBuilder.start().getAllInCutOffSphereMutable(sphere.radius, i == 0 ? sphere.radius / 2 : sphere.radius, sphere.pos).toIterable());
        }
        for (SphereInfo sphere : sphereList) {
            int r = sphere.radius - 2;
            hollow.addAll(ShapeBuilder.start().getAllInRandomlyDistributedRangeYCutOffSphereMutable(r, (int) (r * 0.75), r / 2, rand, sphere.pos).toIterable());
        }
        shell.removeAll(hollow);

        //setBlocks
        createShell(worldIn, rand, shell);
        //removeBlocks
        hollowOut(worldIn, hollow);
        //decorate
        decorateCave(worldIn, rand, hollow, sphereList, center, isMale);
    }

    private void createShell(LevelAccessor worldIn, RandomSource rand, CaveMask positions) {
        Registry<Block> tagManager = BuiltInRegistries.BLOCK;

        List<Block> rareOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_RARE_ORES);
        List<Block> uncommonOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_UNCOMMON_ORES);
        List<Block> commonOres = getBlockList(tagManager, IafBlockTags.DRAGON_CAVE_COMMON_ORES);
        List<Block> dragonTypeOres = getBlockList(tagManager, dragonTypeOreTag);

        positions.forEach(blockPos -> {
            BlockState state = worldIn.getBlockState(blockPos);
            if (!(state.getBlock() instanceof BaseEntityBlock) && state.getDestroySpeed(worldIn, blockPos) >= 0) {
                boolean doOres = rand.nextInt(IafConfig.oreToStoneRatioForDragonCaves + 1) == 0;

                if (doOres) {
                    Block toPlace = null;

                    if (rand.nextBoolean()) {
                        toPlace = !dragonTypeOres.isEmpty() ? dragonTypeOres.get(rand.nextInt(dragonTypeOres.size())) : null;
                    } else {
                        double chance = rand.nextDouble();

                        if (!rareOres.isEmpty() && chance <= 0.15) {
                            toPlace = rareOres.get(rand.nextInt(rareOres.size()));
                        } else if (!uncommonOres.isEmpty() && chance <= 0.45) {
                            toPlace = uncommonOres.get(rand.nextInt(uncommonOres.size()));
                        } else if (!commonOres.isEmpty()) {
                            toPlace = commonOres.get(rand.nextInt(commonOres.size()));
                        }
                    }

                    if (toPlace != null) {
                        worldIn.setBlock(blockPos, toPlace.defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else {
                        worldIn.setBlock(blockPos, rand.nextBoolean() ? PALETTE_BLOCK1 : PALETTE_BLOCK2, Block.UPDATE_CLIENTS);
                    }
                } else {
                    worldIn.setBlock(blockPos, rand.nextBoolean() ? PALETTE_BLOCK1 : PALETTE_BLOCK2, Block.UPDATE_CLIENTS);
                }
            }
        });
    }

    private List<Block> getBlockList(final Registry<Block> tagManager, final TagKey<Block> tagKey) {
        if (tagManager == null) {
            return List.of();
        }

        return tagManager.get(tagKey).map(set -> set.stream().map(Holder::value).toList()).orElse(List.of());
    }

    private void hollowOut(LevelAccessor worldIn, CaveMask positions) {
        BlockState air = Blocks.AIR.defaultBlockState();
        positions.forEach(blockPos -> {
            if (!(worldIn.getBlockState(blockPos).getBlock() instanceof BaseEntityBlock)) {
                worldIn.setBlock(blockPos, air, Block.UPDATE_ALL);
            }
        });
    }

    private void decorateCave(LevelAccessor worldIn, RandomSource rand, CaveMask positions, List<SphereInfo> spheres, BlockPos center, boolean isMale) {
        for (SphereInfo sphere : spheres) {
            BlockPos pos = sphere.pos;
            int radius = sphere.radius;

            for (int i = 0; i < 15 + rand.nextInt(10); i++) {
                CEILING_DECO.generate(worldIn, rand, pos.above(radius / 2 - 1).offset(rand.nextInt(radius) - radius / 2, 0, rand.nextInt(radius) - radius / 2));
            }
        }

        BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();
        positions.forEach(blockPos -> {
            if (blockPos.getY() < center.getY()) {
                BlockState stateBelow = worldIn.getBlockState(below.setWithOffset(blockPos, Direction.DOWN));

                if ((stateBelow.is(BlockTags.BASE_STONE_OVERWORLD) || stateBelow.is(IafBlockTags.DRAGON_ENVIRONMENT_BLOCKS)) && worldIn.getBlockState(blockPos).isAir()) {
                    setGoldPile(worldIn, blockPos.immutable(), rand, isMale);
                }
            }
        });
    }

    public void setGoldPile(LevelAccessor world, BlockPos pos, RandomSource rand, boolean isMale) {
        if (!(world.getBlockState(pos).getBlock() instanceof BaseEntityBlock)) {
            int chance = rand.nextInt(99) + 1;
            if (chance < 60) {
                int goldRand = Math.max(1, IafConfig.dragonDenGoldAmount) * (isMale ? 1 : 2);
                boolean generateGold = rand.nextInt(goldRand) == 0;
                world.setBlock(pos, generateGold ? TREASURE_PILE.setValue(BlockGoldPile.LAYERS, 1 + rand.nextInt(7)) : Blocks.AIR.defaultBlockState(), 3);
            } else if (chance == 61) {
                world.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, HORIZONTALS[rand.nextInt(3)]), Block.UPDATE_CLIENTS);

                if (world.getBlockState(pos).getBlock() instanceof ChestBlock) {
                    BlockEntity blockEntity = world.getBlockEntity(pos);

                    if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
                        chestBlockEntity.setLootTable(isMale ? DRAGON_MALE_CHEST : DRAGON_CHEST, rand.nextLong());
                    }
                }
            }
        }
    }

    private EntityDragonBase createDragon(final WorldGenLevel worldGen, final RandomSource random, final BlockPos position, int dragonAge, boolean isMale) {
        EntityDragonBase dragon = getDragonType().create(worldGen.getLevel(), EntitySpawnReason.STRUCTURE);
        dragon.setGender(isMale);
        dragon.growDragon(dragonAge);
        dragon.setAgingDisabled(true);
        dragon.setHealth(dragon.getMaxHealth());
        dragon.setVariant(random.nextInt(4));
        dragon.absSnapTo(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5, random.nextFloat() * 360, 0);
        dragon.setInSittingPose(true);
        dragon.homePos = new HomePosition(position, worldGen.getLevel());
        dragon.setHunger(50);
        return dragon;
    }

    public abstract EntityType<? extends EntityDragonBase> getDragonType();

    private static class SphereInfo {
        int radius;
        BlockPos pos;

        private SphereInfo(int radius, BlockPos pos) {
            this.radius = radius;
            this.pos = pos;
        }
    }

    /** Blocks of one cave: a bit per block of a box around its spheres, local to one cave (not shared by threads). */
    private static final class CaveMask {
        private final int minX, minY, minZ, sizeX, sizeY, sizeZ;
        private final long[] bits;

        private CaveMask(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.sizeX = Math.max(0, sizeX);
            this.sizeY = Math.max(0, sizeY);
            this.sizeZ = Math.max(0, sizeZ);
            this.bits = new long[(this.sizeX * this.sizeY * this.sizeZ + 63) >>> 6];
        }

        static CaveMask around(LevelAccessor level, List<SphereInfo> spheres) {
            int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (SphereInfo sphere : spheres) {
                minX = Math.min(minX, sphere.pos.getX() - sphere.radius);
                minY = Math.min(minY, sphere.pos.getY() - sphere.radius);
                minZ = Math.min(minZ, sphere.pos.getZ() - sphere.radius);
                maxX = Math.max(maxX, sphere.pos.getX() + sphere.radius);
                maxY = Math.max(maxY, sphere.pos.getY() + sphere.radius);
                maxZ = Math.max(maxZ, sphere.pos.getZ() + sphere.radius);
            }
            if (level instanceof WorldGenRegion region) {
                // A feature may write only its chunk and the 8 around it (blockStateWriteRadius 1 of the features step):
                // the region refused the rest of the cave one block at a time, building a log line for each
                ChunkPos chunk = region.getCenter();
                minX = Math.max(minX, (chunk.x - 1) << 4);
                minZ = Math.max(minZ, (chunk.z - 1) << 4);
                maxX = Math.min(maxX, ((chunk.x + 1) << 4) + 15);
                maxZ = Math.min(maxZ, ((chunk.z + 1) << 4) + 15);
            }
            return new CaveMask(minX, minY, minZ, maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
        }

        CaveMask emptyCopy() {
            return new CaveMask(minX, minY, minZ, sizeX, sizeY, sizeZ);
        }

        void addAll(Iterable<BlockPos> positions) {
            for (BlockPos pos : positions) {
                int x = pos.getX() - minX, y = pos.getY() - minY, z = pos.getZ() - minZ;
                if (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ) {
                    int index = (y * sizeX + x) * sizeZ + z;
                    bits[index >>> 6] |= 1L << index;
                }
            }
        }

        /** Same box (emptyCopy) */
        void removeAll(CaveMask other) {
            for (int i = 0; i < bits.length; i++) {
                bits[i] &= ~other.bits[i];
            }
        }

        /** The position is reused: immutable() to keep it */
        void forEach(Consumer<BlockPos> action) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            int index = 0;
            for (int y = 0; y < sizeY; y++) {
                for (int x = 0; x < sizeX; x++) {
                    for (int z = 0; z < sizeZ; z++, index++) {
                        if ((bits[index >>> 6] & (1L << index)) != 0) {
                            action.accept(pos.set(minX + x, minY + y, minZ + z));
                        }
                    }
                }
            }
        }
    }

    @Override
    public IafWorldData.FeatureType getFeatureType() {
        return IafWorldData.FeatureType.UNDERGROUND;
    }

    @Override
    public String getId() {
        return "dragon_cave";
    }
}
