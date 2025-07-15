package dev.muon.medieval.world.structure;

import dev.muon.medieval.Medieval;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.world.saved_data.StructureRegenerationState;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

@Mod.EventBusSubscriber(modid = Medieval.MODID)
public class AutomaticStructureRegenerator {
    private static int tickCounter = 0;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!MedievalConfig.get().enableAutoRegeneration) {
            return;
        }

        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!(event.player instanceof ServerPlayer player)) {
            return;
        }

        // Only check on interval
        if (++tickCounter % MedievalConfig.get().autoRegenCheckInterval != 0) {
            return;
        }

        ServerLevel level = player.serverLevel();
        StructureRegenerationState data = StructureRegenerationState.get(level);

        // Check structures within autoRegenRadius
        BlockPos playerPos = player.blockPosition();
        ChunkPos playerChunk = new ChunkPos(playerPos);

        // Check structures in nearby chunks
        int chunkRadius = (int) Math.ceil(MedievalConfig.get().autoRegenRadius / 16.0);
        for (int dx = -chunkRadius; dx <= chunkRadius; dx++) {
            for (int dz = -chunkRadius; dz <= chunkRadius; dz++) {
                ChunkPos checkChunk = new ChunkPos(playerChunk.x + dx, playerChunk.z + dz);
                checkStructuresInChunk(level, player, checkChunk, playerPos, data);
            }
        }
    }

    private static void checkStructuresInChunk(ServerLevel level, ServerPlayer player, ChunkPos chunkPos,
                                               BlockPos playerPos, StructureRegenerationState data) {
        Map<Structure, LongSet> structuresInChunk = level.structureManager()
                .getAllStructuresAt(chunkPos.getWorldPosition());

        for (Map.Entry<Structure, LongSet> entry : structuresInChunk.entrySet()) {
            ResourceLocation structureId = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE).getKey(entry.getKey());

            if (structureId == null || !MedievalConfig.get().isStructureWhitelisted(structureId)) {
                continue;
            }

            StructureStart start = level.structureManager().getStartForStructure(
                    SectionPos.of(chunkPos, 0),
                    entry.getKey(),
                    level.getChunk(chunkPos.x, chunkPos.z)
            );

            if (start == null || !start.isValid()) {
                continue;
            }

            BoundingBox boundingBox = start.getBoundingBox();

            // Check if player is within the structure's bounding box
            if (!isPlayerInStructure(playerPos, boundingBox)) {
                continue;
            }

            // Get structure center for instance tracking
            BlockPos structureCenter = boundingBox.getCenter();

            // Check if player has already regenerated this specific structure instance
            if (data.hasPlayerRegeneratedStructure(player.getUUID(), structureId, structureCenter)) {
                continue;
            }

            // Check all conditions for auto-regeneration
            if (!canAutoRegenerate(level, player, structureId, boundingBox, data)) {
                continue;
            }

            // Check if structure is claimed
            if (StructureRegenerator.isAnyClaimed(level, boundingBox) != null) {
                sendActionBar(player, "message.medieval.structure.claimed",
                        ChatFormatting.RED, structureId);
                continue;
            }

            // Regenerate the structure
            sendActionBar(player, "message.medieval.structure.regenerating",
                    ChatFormatting.YELLOW, structureId);

            StructureRegenerator.RegenerationResult result =
                    StructureRegenerator.regenerateStructure(level, playerPos, structureId);

            if (result.success) {
                data.markStructureRegenerated(player.getUUID(), structureId, structureCenter);
                data.clearStructureInteraction(structureId);

                sendActionBar(player, "message.medieval.structure.success",
                        ChatFormatting.GREEN, structureId);
            } else {
                sendActionBar(player, "message.medieval.structure.fail",
                        ChatFormatting.RED, structureId, result.message);
            }
        }
    }

    private static boolean isPlayerInStructure(BlockPos playerPos, BoundingBox boundingBox) {
        return playerPos.getX() >= boundingBox.minX() && playerPos.getX() <= boundingBox.maxX() &&
                playerPos.getY() >= boundingBox.minY() && playerPos.getY() <= boundingBox.maxY() &&
                playerPos.getZ() >= boundingBox.minZ() && playerPos.getZ() <= boundingBox.maxZ();
    }

    private static boolean canAutoRegenerate(ServerLevel level, ServerPlayer player, ResourceLocation structureId,
                                             BoundingBox boundingBox, StructureRegenerationState data) {
        long currentTime = level.getGameTime();

        // Check for recent interactions
        if (data.hasRecentInteraction(structureId, currentTime,
                MedievalConfig.get().structureInactivityTimeoutSeconds)) {
            StructureRegenerationState.StructureInteraction interaction = data.getLastInteraction(structureId);
            if (interaction != null) {
                long timeSince = (currentTime - interaction.timestamp) / 20;
                sendActionBar(player, "message.medieval.structure.recent_activity",
                        ChatFormatting.YELLOW, structureId,
                        interaction.type.name().toLowerCase(), timeSince);
                return false;
            }
        }

        // Check if other players are in the structure by scanning the bounding box
        if (hasOtherPlayersInStructure(level, player, boundingBox)) {
            sendActionBar(player, "message.medieval.structure.players_present",
                    ChatFormatting.YELLOW, structureId);
            return false;
        }

        return true;
    }

    private static boolean hasOtherPlayersInStructure(ServerLevel level, ServerPlayer excludePlayer, BoundingBox boundingBox) {
        AABB aabb = new AABB(
                boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(),
                boundingBox.maxX() + 1, boundingBox.maxY() + 1, boundingBox.maxZ() + 1
        );

        List<Player> playersInStructure = level.getEntitiesOfClass(Player.class, aabb);

        // Check if any player other than the current one is in the structure
        return playersInStructure.stream()
                .anyMatch(p -> p != excludePlayer && p instanceof ServerPlayer);
    }

    private static void sendActionBar(ServerPlayer player, String translationKey,
                                      ChatFormatting formatting, Object... args) {
        player.displayClientMessage(
                Component.translatable(translationKey, args).withStyle(formatting),
                true
        );
    }
}