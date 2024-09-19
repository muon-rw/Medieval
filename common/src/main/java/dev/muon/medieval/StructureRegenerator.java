package dev.muon.medieval;


import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.platform.Services;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class StructureRegenerator {
    private static final Logger LOGGER = LogManager.getLogger();

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
        //forceResetLootContainers(level, boundingBox);

        List<ChunkPos> affectedChunks = new ArrayList<>();

        ChunkPos.rangeClosed(chunkPosMin, chunkPosMax).forEach(chunkPos -> {
            structureStart.placeInChunk(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(),
                    new BoundingBox(chunkPos.getMinBlockX(), level.getMinBuildHeight(), chunkPos.getMinBlockZ(),
                            chunkPos.getMaxBlockX(), level.getMaxBuildHeight(), chunkPos.getMaxBlockZ()),
                    chunkPos);
            level.getChunk(chunkPos.x, chunkPos.z).setUnsaved(true);
            affectedChunks.add(chunkPos);
        });

        // surely one of these works, should probably figure out which
        for (ChunkPos chunkPos : affectedChunks) {
                level.getChunkSource().blockChanged(new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()));
        }
        resendChunksToNearbyPlayers(level, affectedChunks);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        LOGGER.info("Regenerated structure {} in {} ms", structureId, duration);
        level.playSound(null, pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 2.0F, 0.5F);

        return new RegenerationResult(true, duration + " ms");
    }

    private static void resendChunksToNearbyPlayers(ServerLevel level, List<ChunkPos> chunks) {
        Map<ServerPlayer, List<LevelChunk>> playerChunks = new HashMap<>();

        for (ChunkPos chunkPos : chunks) {
            LevelChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
            for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(chunkPos, false)) {
                playerChunks.computeIfAbsent(player, k -> new ArrayList<>()).add(chunk);
                ClientboundForgetLevelChunkPacket forgetPacket = new ClientboundForgetLevelChunkPacket(chunkPos);
                player.connection.send(forgetPacket);
            }
        }
    }

    public static BlockPos isAnyClaimed(ServerLevel level, BoundingBox boundingBox) {
        return Services.PLATFORM.getFTBHelper().isAnyClaimed(level, boundingBox);
    }

    public static boolean isValidStructure(ResourceLocation structureId) {
        return true;
        //return MedievalConfig.INSTANCE.allowedStructureNamespaces.get().contains(structureId.getNamespace()) ||
        //        MedievalConfig.INSTANCE.additionalValidStructures.get().contains(structureId.toString());
    }

    private static void removeExistingEntities(ServerLevel level, BoundingBox boundingBox) {
        List<net.minecraft.world.entity.Entity> entitiesToRemove = level.getEntitiesOfClass(
                net.minecraft.world.entity.Entity.class,
                new net.minecraft.world.phys.AABB(
                        boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                        boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1
                ),
                StructureRegenerator::shouldRemoveEntity
        );
        entitiesToRemove.forEach(net.minecraft.world.entity.Entity::discard);
    }

    private static boolean shouldRemoveEntity(net.minecraft.world.entity.Entity entity) {

        if (entity instanceof net.minecraft.world.entity.Mob) {
            return !(entity instanceof OwnableEntity) || ((OwnableEntity) entity).getOwner() == null;
        }
        return entity instanceof net.minecraft.world.entity.decoration.Painting ||
                entity instanceof net.minecraft.world.entity.decoration.ItemFrame ||
                entity instanceof net.minecraft.world.entity.decoration.ArmorStand ||
                entity instanceof net.minecraft.world.entity.vehicle.AbstractMinecart ||
                entity instanceof net.minecraft.world.entity.vehicle.Boat ||
                entity instanceof net.minecraft.world.entity.decoration.LeashFenceKnotEntity ||
                entity instanceof net.minecraft.world.entity.projectile.Arrow ||
                entity instanceof net.minecraft.world.entity.projectile.SpectralArrow;
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
        // SEARCH RADIUS IN CHUNKS
        int searchRadius = 4;//MedievalConfig.INSTANCE.structureSearchRadius.get();
        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int z = -searchRadius; z <= searchRadius; z++) {
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