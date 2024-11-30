package dev.muon.medieval.regenerate;


import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.MedievalConfig;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class AutomaticStructureRegenerator {
    private static final int CACHE_DURATION_TICKS = 100;
    private int tickCounter = 0;

    private record CachedStructure(StructureStart start, long expirationTime) {}

    // Cache: Player UUID -> (Structure ID -> (Structure Start, Expiration Time))
    private final Map<UUID, Map<ResourceLocation, CachedStructure>> structureCache = new HashMap<>();

    private static final Set<ResourceLocation> BLACKLISTED_STRUCTURES = new HashSet<>();

    public static void addToBlacklist(ResourceLocation structure) {
        BLACKLISTED_STRUCTURES.add(structure);
    }

    public static boolean isBlacklisted(ResourceLocation structure) {
        return BLACKLISTED_STRUCTURES.contains(structure)
                || MedievalConfig.get().autoRegenBlacklistedNamespaces.contains(structure.getNamespace())
                || MedievalConfig.get().autoRegenBlacklistedStructures.contains(structure.toString());
    }

    public static boolean isValidForAutoRegen(ResourceLocation structure) {
        if (StructureRegenerator.isValidStructure(structure)) {
            Medieval.LOGGER.debug("Structure {} is valid for Challenge Orb, skipping auto-regen", structure);
            return false;
        }
        if (isBlacklisted(structure)) {
            Medieval.LOGGER.debug("Structure {} is blacklisted, skipping auto-regen", structure);
            return false;
        }
        if (!structure.getNamespace().equals("minecraft")) {
            Medieval.LOGGER.debug("Structure {} is not in minecraft namespace, skipping auto-regen", structure);
            return false;
        }
        Medieval.LOGGER.debug("Structure {} is valid for auto-regen", structure);
        return true;
    }

    private void sendActionBar(ServerPlayer player, String translationKey, ChatFormatting formatting, Object... args) {
        player.displayClientMessage(
                Component.translatable(translationKey, args).withStyle(formatting),
                true
        );
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private StructureStart findNearbyStructure(ServerLevel level, ServerPlayer player, ResourceLocation structureId) {
        UUID playerId = player.getUUID();
        long currentTime = level.getGameTime();

        Map<ResourceLocation, CachedStructure> playerCache = structureCache.computeIfAbsent(playerId, k -> new HashMap<>());
        CachedStructure cached = playerCache.get(structureId);
        if (cached != null) {
            if (currentTime < cached.expirationTime) {
                return cached.start;
            } else {
                playerCache.remove(structureId);
            }
        }

        ChunkPos playerChunk = new ChunkPos(player.blockPosition());

        // Check player's chunk and adjacent chunks only
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos checkChunk = new ChunkPos(playerChunk.x + dx, playerChunk.z + dz);
                Map<Structure, LongSet> structuresInChunk = level.structureManager()
                        .getAllStructuresAt(checkChunk.getWorldPosition());

                for (Map.Entry<Structure, LongSet> entry : structuresInChunk.entrySet()) {
                    if (level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                            .getKey(entry.getKey()).equals(structureId)) {
                        StructureStart start = level.structureManager().getStartForStructure(
                                SectionPos.of(checkChunk, 0),
                                entry.getKey(),
                                level.getChunk(checkChunk.x, checkChunk.z)
                        );
                        if (start != null && start.isValid()) {
                            playerCache.put(structureId,
                                    new CachedStructure(start, currentTime + CACHE_DURATION_TICKS));
                            return start;
                        }
                    }
                }
            }
        }

        playerCache.put(structureId, new CachedStructure(null, currentTime + CACHE_DURATION_TICKS));
        return null;
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!MedievalConfig.get().enableAutoRegeneration) {
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }
        if (++tickCounter % MedievalConfig.get().autoRegenCheckInterval != 0) {
            return;
        }

        ServerLevel level = player.serverLevel();
        BlockPos playerPos = player.blockPosition();
        StructureRegenerationData data = StructureRegenerationData.get(level);

        if (tickCounter % (20 * 60) == 0) {
            cleanupCache(level.getGameTime());
        }

        for (var structure : level.registryAccess().registryOrThrow(Registries.STRUCTURE)) {
            ResourceLocation structureId = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE).getKey(structure);

            if (structureId == null || !isValidForAutoRegen(structureId)) {
                continue;
            }

            StructureStart start = findNearbyStructure(level, player, structureId);
            if (start == null || !start.isValid()) {
                continue;
            }

            if (data.hasPlayerRegeneratedStructure(player.getUUID(), structureId)) {
                continue;
            }

            long currentTime = level.getGameTime();
            if (data.isStructureOnCooldown(structureId, currentTime)) {
                if (start.getBoundingBox().getCenter().closerThan(playerPos, MedievalConfig.get().autoRegenRadius)) {
                    long remaining = data.getRemainingCooldown(structureId, currentTime);
                    sendActionBar(player,
                            "message.medieval.structure.cooldown",
                            ChatFormatting.RED,
                            structureId.getPath(), formatTime(remaining));
                }
                continue;
            }

            if (StructureRegenerator.isAnyClaimed(level, start.getBoundingBox()) != null) {
                if (start.getBoundingBox().getCenter().closerThan(playerPos, MedievalConfig.get().autoRegenRadius)) {
                    sendActionBar(player,
                            "message.medieval.structure.claimed",
                            ChatFormatting.RED,
                            structureId.getPath());
                }
                continue;
            }

            sendActionBar(player,
                    "message.medieval.structure.regenerating",
                    ChatFormatting.YELLOW,
                    structureId.getPath());

            StructureRegenerator.RegenerationResult result =
                    StructureRegenerator.regenerateStructure(level, playerPos, structureId);

            if (result.success) {
                data.markStructureRegenerated(player.getUUID(), structureId);
                data.setStructureCooldown(structureId, currentTime);

                sendActionBar(player,
                        "message.medieval.structure.success",
                        ChatFormatting.GREEN,
                        structureId.getPath());
            } else {
                sendActionBar(player,
                        "message.medieval.structure.fail",
                        ChatFormatting.RED,
                        structureId.getPath(), result.message);
            }
        }
    }

    private void cleanupCache(long currentTime) {
        structureCache.values().forEach(playerCache ->
                playerCache.values().removeIf(cached -> currentTime >= cached.expirationTime));
        structureCache.values().removeIf(Map::isEmpty);
    }
}