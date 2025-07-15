package dev.muon.medieval.world.saved_data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class StructureRegenerationState extends SavedData {
    private static final String DATA_NAME = "medieval_structure_regeneration";
    // Changed to track specific structure instances: player -> set of "structureId:x:z" strings
    private final Map<UUID, Set<String>> playerRegenerations = new HashMap<>();
    private final Map<ResourceLocation, Long> structureCooldowns = new HashMap<>();
    private final Map<ResourceLocation, StructureInteraction> structureInteractions = new HashMap<>();

    private static final long COOLDOWN_DURATION = 20 * 60 * 30; // 30 minutes in ticks

    public static class StructureInteraction {
        public final UUID playerId;
        public final long timestamp;
        public final InteractionType type;

        public StructureInteraction(UUID playerId, long timestamp, InteractionType type) {
            this.playerId = playerId;
            this.timestamp = timestamp;
            this.type = type;
        }
    }

    public enum InteractionType {
        CHEST_OPENED,
        ENTITY_INTERACTED,
        ENTITY_DAMAGED
    }

    public static StructureRegenerationState get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(StructureRegenerationState::load, StructureRegenerationState::new, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save player regenerations
        CompoundTag playerData = new CompoundTag();
        playerRegenerations.forEach((uuid, structures) -> {
            ListTag list = new ListTag();
            structures.forEach(structure -> list.add(StringTag.valueOf(structure)));
            playerData.put(uuid.toString(), list);
        });
        tag.put("PlayerData", playerData);

        // Save cooldowns
        CompoundTag cooldownData = new CompoundTag();
        structureCooldowns.forEach((structure, time) ->
                cooldownData.putLong(structure.toString(), time));
        tag.put("Cooldowns", cooldownData);

        // Save structure interactions
        CompoundTag interactionData = new CompoundTag();
        structureInteractions.forEach((structure, interaction) -> {
            CompoundTag interactionTag = new CompoundTag();
            interactionTag.putUUID("PlayerId", interaction.playerId);
            interactionTag.putLong("Timestamp", interaction.timestamp);
            interactionTag.putString("Type", interaction.type.name());
            interactionData.put(structure.toString(), interactionTag);
        });
        tag.put("Interactions", interactionData);

        return tag;
    }

    private static StructureRegenerationState load(CompoundTag tag) {
        StructureRegenerationState data = new StructureRegenerationState();

        // Load player regenerations
        CompoundTag playerData = tag.getCompound("PlayerData");
        for (String uuidStr : playerData.getAllKeys()) {
            UUID uuid = UUID.fromString(uuidStr);
            Set<String> structures = new HashSet<>();
            ListTag list = playerData.getList(uuidStr, 8);

            for (int i = 0; i < list.size(); i++) {
                structures.add(list.getString(i));
            }
            data.playerRegenerations.put(uuid, structures);
        }

        // Load cooldowns
        CompoundTag cooldownData = tag.getCompound("Cooldowns");
        for (String structureId : cooldownData.getAllKeys()) {
            data.structureCooldowns.put(
                    new ResourceLocation(structureId),
                    cooldownData.getLong(structureId)
            );
        }

        // Load interactions
        CompoundTag interactionData = tag.getCompound("Interactions");
        for (String structureId : interactionData.getAllKeys()) {
            CompoundTag interactionTag = interactionData.getCompound(structureId);
            UUID playerId = interactionTag.getUUID("PlayerId");
            long timestamp = interactionTag.getLong("Timestamp");
            InteractionType type = InteractionType.valueOf(interactionTag.getString("Type"));
            data.structureInteractions.put(
                    new ResourceLocation(structureId),
                    new StructureInteraction(playerId, timestamp, type)
            );
        }

        return data;
    }

    /**
     * Create a unique identifier for a structure instance based on its type and location
     */
    private static String getStructureInstanceId(ResourceLocation structureId, BlockPos centerPos) {
        return structureId.toString() + ":" + centerPos.getX() + ":" + centerPos.getZ();
    }

    public boolean hasPlayerRegeneratedStructure(UUID playerId, ResourceLocation structure, BlockPos centerPos) {
        String instanceId = getStructureInstanceId(structure, centerPos);
        return playerRegenerations.getOrDefault(playerId, Collections.emptySet())
                .contains(instanceId);
    }

    public void markStructureRegenerated(UUID playerId, ResourceLocation structure, BlockPos centerPos) {
        String instanceId = getStructureInstanceId(structure, centerPos);
        playerRegenerations.computeIfAbsent(playerId, k -> new HashSet<>()).add(instanceId);
        setDirty();
    }

    public boolean isStructureOnCooldown(ResourceLocation structure, long currentTime) {
        return structureCooldowns.containsKey(structure) &&
                (currentTime - structureCooldowns.get(structure)) < COOLDOWN_DURATION;
    }

    public void setStructureCooldown(ResourceLocation structure, long time) {
        structureCooldowns.put(structure, time);
        setDirty();
    }

    public long getRemainingCooldown(ResourceLocation structure, long currentTime) {
        if (!structureCooldowns.containsKey(structure)) return 0;
        long remaining = COOLDOWN_DURATION - (currentTime - structureCooldowns.get(structure));
        return Math.max(0, remaining);
    }

    public void recordStructureInteraction(ResourceLocation structure, UUID playerId, long timestamp, InteractionType type) {
        structureInteractions.put(structure, new StructureInteraction(playerId, timestamp, type));
        setDirty();
    }

    public boolean hasRecentInteraction(ResourceLocation structure, long currentTime, int timeoutSeconds) {
        StructureInteraction interaction = structureInteractions.get(structure);
        if (interaction == null) return false;

        long timeoutTicks = timeoutSeconds * 20L;
        return (currentTime - interaction.timestamp) < timeoutTicks;
    }

    public StructureInteraction getLastInteraction(ResourceLocation structure) {
        return structureInteractions.get(structure);
    }

    public void clearStructureInteraction(ResourceLocation structure) {
        structureInteractions.remove(structure);
        setDirty();
    }

    /**
     * Removes interaction records older than the specified timeout.
     * Can be called periodically to prevent unbounded growth.
     */
    public void cleanupOldInteractions(long currentTime, int timeoutSeconds) {
        long timeoutTicks = timeoutSeconds * 20L;
        structureInteractions.entrySet().removeIf(entry -> {
            StructureInteraction interaction = entry.getValue();
            return (currentTime - interaction.timestamp) > timeoutTicks;
        });
    }
}