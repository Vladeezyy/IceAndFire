package com.github.alexthe666.iceandfire.mixin.gen;

import com.github.alexthe666.iceandfire.datagen.IafStructures;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.feature.LakeFeature;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

// Based on code from TelepathicGrunts RepurposedStructures. A mixin: lakes are vanilla's, no event before they place.
@Mixin(LakeFeature.class)
public class NoLakesInStructuresMixin {
    @Inject(
            method = "place(Lnet/minecraft/world/level/WorldGenLevel;Lnet/minecraft/world/level/chunk/ChunkGenerator;Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Z",
            at = @At(value = "HEAD"),
            cancellable = true
    )
    private void iaf_noLakesInMausoleum(net.minecraft.world.level.WorldGenLevel level, net.minecraft.world.level.chunk.ChunkGenerator generator, net.minecraft.util.RandomSource random, BlockPos origin, CallbackInfoReturnable<Boolean> cir) {
        if (!(level instanceof WorldGenRegion region)) {
            return;
        }
        // StructureManager.getStructureAt on the region's own chunks, as vanilla's features do. The server level's manager
        // (the original) took the chunks through the server thread: a blocking wait of a worldgen thread per structure.
        int chunkX = SectionPos.blockToSectionCoord(origin.getX());
        int chunkZ = SectionPos.blockToSectionCoord(origin.getZ());
        // the 3x3 chunks of a feature have their structure references; vanilla lakes start in the center chunk
        if (region.getCenter().getChessboardDistance(chunkX, chunkZ) > 1) {
            return;
        }
        Map<Structure, LongSet> references = region.getChunk(chunkX, chunkZ, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
        // most chunks: no structure at all
        if (references.isEmpty()) {
            return;
        }
        Registry<Structure> registry = region.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        if (iaf$inside(region, registry, references, IafStructures.MAUSOLEUM, origin)
                || iaf$inside(region, registry, references, IafStructures.GRAVEYARD, origin)
                || iaf$inside(region, registry, references, IafStructures.GORGON_TEMPLE, origin)) {
            cir.setReturnValue(false);
        }
    }

    @Unique
    private static boolean iaf$inside(WorldGenRegion region, Registry<Structure> registry, Map<Structure, LongSet> references, ResourceKey<Structure> key, BlockPos origin) {
        Structure structure = registry.getValue(key);
        LongSet starts = structure == null ? null : references.get(structure);
        if (starts == null) {
            return false;
        }
        for (LongIterator it = starts.iterator(); it.hasNext(); ) {
            long start = it.nextLong();
            int startX = ChunkPos.getX(start);
            int startZ = ChunkPos.getZ(start);
            // the region holds the structure starts 8 chunks around its center
            if (!region.hasChunk(startX, startZ)) {
                continue;
            }
            StructureStart found = region.getChunk(startX, startZ, ChunkStatus.STRUCTURE_STARTS).getStartForStructure(structure);
            if (found != null && found.isValid() && found.getBoundingBox().isInside(origin)) {
                return true;
            }
        }
        return false;
    }
}
