package dev.muon.medieval.quest;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class TemperatureTask extends AbstractBooleanTask {
    private int targetTemperature;
    private TemperatureComparison comparison;

    public enum TemperatureComparison {
        ABOVE,
        BELOW,
        EQUAL;
        public static final NameMap<TemperatureComparison> NAME_MAP = NameMap.of(ABOVE, values()).create();
    }
    public static void checkTemperatureQuests(ServerPlayer player) {
        ServerQuestFile file = ServerQuestFile.INSTANCE;
        if (file == null) return;

        TeamData data = file.getOrCreateTeamData(player);
        if (data.isLocked()) return;

        int playerTemperature = player.thermoo$getTemperature();

        file.withPlayerContext(player, () -> {
            for (TemperatureTask task : file.collect(TemperatureTask.class)) {
                if (!data.isCompleted(task) && data.canStartTasks(task.getQuest())) {
                    if (task.checkTemperature(playerTemperature)) {
                        task.submitTask(data, player, ItemStack.EMPTY);
                    }
                }
            }
        });
    }

    @Override
    public int autoSubmitOnPlayerTick() {
        return comparison == TemperatureComparison.EQUAL ? 1 : 20; // Check every tick for EQUAL, every second for ABOVE/BELOW
    }



    public TemperatureTask(long id, Quest quest) {
        super(id, quest);
        targetTemperature = 0;
        comparison = TemperatureComparison.ABOVE;
    }

    @Override
    public TaskType getType() {
        return TaskTypes.TEMPERATURE;
    }

    @Override
    public void writeData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeData(nbt, provider);
        nbt.putInt("target_temperature", targetTemperature);
        nbt.putInt("comparison", comparison.ordinal());
    }

    @Override
    public void readData(CompoundTag nbt, HolderLookup.Provider provider) {
        super.readData(nbt, provider);
        targetTemperature = nbt.getInt("target_temperature");
        comparison = TemperatureComparison.values()[nbt.getInt("comparison")];
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeInt(targetTemperature);
        buffer.writeEnum(comparison);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        targetTemperature = buffer.readInt();
        comparison = buffer.readEnum(TemperatureComparison.class);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        config.addInt("target_temperature", targetTemperature, v -> targetTemperature = v, 0, -14000, 14000);
        config.addEnum("comparison", comparison, v -> comparison = v, TemperatureComparison.NAME_MAP);
    }

    @Override
    public Component getAltTitle() {
        return Component.translatable("ftbquests.task.medieval.temperature").append(": ")
                .append(Component.literal(String.format("%d", targetTemperature)).withStyle(ChatFormatting.DARK_GREEN))
                .append(" ")
                .append(Component.translatable("ftbquests.task.medieval.temperature." + comparison.name().toLowerCase()));
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        int playerTemperature = player.thermoo$getTemperature();
        return checkTemperature(playerTemperature);
    }

    public boolean checkTemperature(int playerTemperature) {
        return switch (comparison) {
            case ABOVE -> playerTemperature > targetTemperature;
            case BELOW -> playerTemperature < targetTemperature;
            case EQUAL -> playerTemperature == targetTemperature;
        };
    }

}