package dev.muon.medieval.quest;

import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import dev.muon.medieval.Medieval;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

public class TaskTypes {
    public static final TaskType TEMPERATURE = register(Medieval.loc("temperature"), TemperatureTask::new,
            () -> Icon.getIcon("minecraft:item/blaze_powder"));

    private static TaskType register(ResourceLocation name, TaskType.Provider provider, Supplier<Icon> iconSupplier) {
        return dev.ftb.mods.ftbquests.quest.task.TaskTypes.register(name, provider, iconSupplier);
    }

    public static void init() {
    }
}