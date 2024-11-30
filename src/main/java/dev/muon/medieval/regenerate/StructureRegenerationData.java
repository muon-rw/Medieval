package dev.muon.medieval.regenerate;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class StructureRegenerationData extends SavedData {
    private static final String DATA_NAME = "medieval_structure_regeneration";
    private final Map<UUID, Set<ResourceLocation>> playerRegenerations = new HashMap<>();
    private final Map<ResourceLocation, Long> structureCooldowns = new HashMap<>();

    private static final long COOLDOWN_DURATION = 20 * 60 * 30; // 30 minutes in ticks

    public static StructureRegenerationData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage()
                .computeIfAbsent(StructureRegenerationData::load, StructureRegenerationData::new, DATA_NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        // Save player regenerations
        CompoundTag playerData = new CompoundTag();
        playerRegenerations.forEach((uuid, structures) -> {
            ListTag list = new ListTag();
            structures.forEach(structure -> list.add(StringTag.valueOf(structure.toString())));
            playerData.put(uuid.toString(), list);
        });
        tag.put("PlayerData", playerData);

        // Save cooldowns
        CompoundTag cooldownData = new CompoundTag();
        structureCooldowns.forEach((structure, time) ->
                cooldownData.putLong(structure.toString(), time));
        tag.put("Cooldowns", cooldownData);

        return tag;
    }

    private static StructureRegenerationData load(CompoundTag tag) {
        StructureRegenerationData data = new StructureRegenerationData();

        CompoundTag playerData = tag.getCompound("PlayerData");
        for (String uuidStr : playerData.getAllKeys()) {
            UUID uuid = UUID.fromString(uuidStr);
            Set<ResourceLocation> structures = new HashSet<>();
            ListTag list = playerData.getList(uuidStr, 8);

            for (int i = 0; i < list.size(); i++) {
                structures.add(new ResourceLocation(list.getString(i)));
            }
            data.playerRegenerations.put(uuid, structures);
        }

        CompoundTag cooldownData = tag.getCompound("Cooldowns");
        for (String structureId : cooldownData.getAllKeys()) {
            data.structureCooldowns.put(
                    new ResourceLocation(structureId),
                    cooldownData.getLong(structureId)
            );
        }

        return data;
    }

    public boolean hasPlayerRegeneratedStructure(UUID playerId, ResourceLocation structure) {
        return playerRegenerations.getOrDefault(playerId, Collections.emptySet())
                .contains(structure);
    }

    public void markStructureRegenerated(UUID playerId, ResourceLocation structure) {
        playerRegenerations.computeIfAbsent(playerId, k -> new HashSet<>()).add(structure);
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
}