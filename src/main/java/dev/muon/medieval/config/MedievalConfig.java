package dev.muon.medieval.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

@Config(name = "medieval")
public class MedievalConfig implements ConfigData {

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        public double levelingSearchRadius = 128.0;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        public double levelsPerPoint = 0.2;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean cancelLevelsForPassives = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> allowedStructureNamespaces = new ArrayList<>();

        @ConfigEntry.Gui.Tooltip(count = 2)
        public List<String> additionalValidStructures = new ArrayList<>();

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean applyPlayerBasedLeveling = false;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int structureSearchRadius = 6;

        /*
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean scaleWithEnchantPower = false;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 0, max = 1)
        public double levelsPerEnchantmentLevel = 0.1;
        */

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean enableStructureLevelBonus = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public Map<String, Integer> structureLevelBonuses = new HashMap<>();

        public static MedievalConfig get() {
                return AutoConfig.getConfigHolder(MedievalConfig.class).getConfig();
        }

        public static void register() {
                AutoConfig.register(MedievalConfig.class, JanksonConfigSerializer::new);
        }

        public boolean isValidStructure(ResourceLocation structureId) {
                return allowedStructureNamespaces.contains(structureId.getNamespace()) ||
                        additionalValidStructures.contains(structureId.toString());
        }
        public int getStructureLevelBonus(ResourceLocation structureId) {
                return structureLevelBonuses.getOrDefault(structureId.toString(), 0);
        }
}