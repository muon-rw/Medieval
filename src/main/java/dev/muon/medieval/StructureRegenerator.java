package dev.muon.medieval;

import dev.ftb.mods.ftbchunks.api.ClaimedChunk;
import dev.ftb.mods.ftbchunks.api.ClaimedChunkManager;
import dev.ftb.mods.ftbchunks.api.FTBChunksAPI;
import dev.ftb.mods.ftblibrary.math.ChunkDimPos;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class StructureRegenerator {
    private static final Logger LOGGER = LogManager.getLogger();
    public static int SEARCH_RADIUS = 6;
    private static final Set<String> WHITELISTED_NAMESPACES = Set.of(
            "eeeabsmobs",
            "cataclysm",
            "dungeons_arise",
            "dungeons_arise_seven_seas",
            "mowziesmobs"
    );

    private static final List<ResourceLocation> ADDITIONAL_VALID_STRUCTURES = List.of(
            new ResourceLocation("irons_spellbooks:catacombs")
    );


    public static RegenerationResult regenerateStructure(ServerLevel level, BlockPos pos, ResourceLocation structureId) {
        if (!isValidStructure(structureId)) {
            return new RegenerationResult(false, "Invalid structure");
        }

        StructureStart structureStart = findNearestStructure(level, pos, structureId);
        if (structureStart == null || !structureStart.isValid()) {
            return new RegenerationResult(false, "Structure not found or invalid");
        }

        LOGGER.info("Found structure {} at {}", structureId, structureStart.getBoundingBox().getCenter());

        BlockPos claimedChunkCenter = isAnyClaimed(level, structureStart.getBoundingBox());
        if (claimedChunkCenter != null) {
            String claimResult = String.format("Chunk at (%d, ~, %d) is claimed", claimedChunkCenter.getX(), claimedChunkCenter.getZ());
            LOGGER.info("Structure {} contains claimed chunks. Regeneration cancelled. {}", structureId, claimResult);
            return new RegenerationResult(false, claimResult);
        }

        long startTime = System.currentTimeMillis();

        BoundingBox boundingBox = structureStart.getBoundingBox();
        ChunkPos chunkPosMin = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.minX()), SectionPos.blockToSectionCoord(boundingBox.minZ()));
        ChunkPos chunkPosMax = new ChunkPos(SectionPos.blockToSectionCoord(boundingBox.maxX()), SectionPos.blockToSectionCoord(boundingBox.maxZ()));

        removeExistingEntities(level, boundingBox);
        forceResetLootContainers(level, boundingBox);

        ChunkPos.rangeClosed(chunkPosMin, chunkPosMax).forEach(chunkPos -> {
            structureStart.placeInChunk(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                    new BoundingBox(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                            chunkPos.getMaxBlockX(), level.getMaxBuildHeight(), chunkPos.getMaxBlockZ()),
                    chunkPos);
            level.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true);
        });

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        LOGGER.info("Regenerated structure {} in {} ms", structureId, duration);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.5F);

        return new RegenerationResult(true, duration + " ms");
    }



    public static BlockPos isAnyClaimed(ServerLevel level, BoundingBox boundingBox) {
        if (!FTBChunksAPI.api().isManagerLoaded()) {
            LOGGER.warn("FTB Chunks manager is not loaded. This shouldn't happen! Assuming no chunks are claimed.");
            return null;
        }

        ClaimedChunkManager chunkManager = FTBChunksAPI.api().getManager();
        ResourceKey<Level> dimension = level.dimension();

        int minChunkX = boundingBox.minX() >> 4;
        int minChunkZ = boundingBox.minZ() >> 4;
        int maxChunkX = boundingBox.maxX() >> 4;
        int maxChunkZ = boundingBox.maxZ() >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                ChunkDimPos chunkDimPos = new ChunkDimPos(dimension, chunkX, chunkZ);
                ClaimedChunk claimedChunk = chunkManager.getChunk(chunkDimPos);
                if (claimedChunk != null) {
                    return new BlockPos((chunkX << 4) + 8, 0, (chunkZ << 4) + 8);
                }
            }
        }

        // No chunks are claimed
        return null;
    }


    public static boolean isValidStructure(ResourceLocation structureId) {
        return WHITELISTED_NAMESPACES.contains(structureId.getNamespace()) || ADDITIONAL_VALID_STRUCTURES.contains(structureId);
    }

    private static void removeExistingEntities(ServerLevel level, BoundingBox boundingBox) {
        List<net.minecraft.world.entity.Entity> entitiesToRemove = level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class,
                new net.minecraft.world.phys.AABB(
                        boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1
                ),
                entity -> entity instanceof net.minecraft.world.entity.Mob
                        && !(entity instanceof net.minecraft.world.entity.TamableAnimal)
        );
        entitiesToRemove.forEach(net.minecraft.world.entity.Entity::discard);
    }

    private static void forceResetLootContainers(ServerLevel level, BoundingBox boundingBox) {
        BlockPos.betweenClosed(boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ())
                .forEach(pos -> {
                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof RandomizableContainerBlockEntity container) {
                        container.unpackLootTable(null);
                        container.clearContent();
                        container.setChanged();
                        level.removeBlock(pos, false);
                    }
                });
    }

    public static StructureStart findNearestStructure(ServerLevel level, BlockPos pos, ResourceLocation structureId) {
        Map<Structure, StructureStart> structures = new HashMap<>();
        for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
            for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                ChunkPos chunkPos = new ChunkPos((pos.getX() >> 4) + x, (pos.getZ() >> 4) + z);
                Map<Structure, LongSet> structuresInChunk = level.structureManager().getAllStructuresAt(chunkPos.getWorldPosition());

                for (Map.Entry<Structure, LongSet> entry : structuresInChunk.entrySet()) {
                    if (level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(entry.getKey()).equals(structureId)) {
                        StructureStart start = level.structureManager().getStartForStructure(
                                SectionPos.of(chunkPos, 0),
                                entry.getKey(),
                                level.getChunk(chunkPos.x, chunkPos.z)
                        );
                        if (start != null && start.isValid()) {
                            structures.put(entry.getKey(), start);
                        }
                    }
                }
            }
        }

        if (structures.isEmpty()) {
            return null;
        }

        return structures.values().stream()
                .min((s1, s2) -> Double.compare(s1.getBoundingBox().getCenter().distSqr(pos), s2.getBoundingBox().getCenter().distSqr(pos)))
                .orElse(null);
    }
    public static class RegenerationResult {
        public final boolean success;
        public final String message;

        public RegenerationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}