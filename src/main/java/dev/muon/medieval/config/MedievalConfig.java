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


        @ConfigEntry.Gui.Tooltip(count = 1)
        public boolean enableCustomResourceBars = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean cancelLevelsForPassives = true;

        // Structure Based Leveling
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 32)
        public int structureSearchRadius = 6;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean enableStructureLevelBonus = false;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public Map<String, Integer> structureLevelBonuses = new HashMap<>();

        // Player based leveling
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 256)
        public double levelingSearchRadius = 128.0;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
        public double levelsPerPoint = 0.2;

        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean applyPlayerBasedLeveling = false;

        // Regeneration settings
        @ConfigEntry.Gui.Tooltip(count = 2)
        public boolean enableAutoRegeneration = true;

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 20, max = 200)
        public int autoRegenCheckInterval = 40; // Ticks between checks

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 60, max = 3600)
        public int structureInactivityTimeoutSeconds = 900; // 15 minutes default

        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 8, max = 128)
        public double autoRegenRadius = 32.0; // Detection radius for autoregeneration

        // Shared structure whitelist for Challenge Orbs, Exploration Orbs, and Autoregenerate
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.Gui.CollapsibleObject
        public StructureWhitelist structureWhitelist = new StructureWhitelist();

        public static class StructureWhitelist {
                @ConfigEntry.Gui.Tooltip(count = 2)
                public List<String> allowedNamespaces = new ArrayList<>();

                @ConfigEntry.Gui.Tooltip(count = 2)
                public List<String> additionalStructures = new ArrayList<>();

                @ConfigEntry.Gui.Tooltip(count = 2)
                public List<String> blacklistedStructures = new ArrayList<>();
        }

        // Stack limit
        @ConfigEntry.Gui.Tooltip(count = 2)
        @ConfigEntry.BoundedDiscrete(min = 1, max = 1024)
        public int maxStacksPerEntity = 128;


        public static MedievalConfig get() {
                return AutoConfig.getConfigHolder(MedievalConfig.class).getConfig();
        }

        public static void register() {
                AutoConfig.register(MedievalConfig.class, JanksonConfigSerializer::new);
        }

        public int getStructureLevelBonus(ResourceLocation structureId) {
                return structureLevelBonuses.getOrDefault(structureId.toString(), 0);
        }


        public boolean isStructureWhitelisted(ResourceLocation structureId) {
                if (structureWhitelist.blacklistedStructures.contains(structureId.toString())) {
                        return false;
                }
                return structureWhitelist.allowedNamespaces.contains(structureId.getNamespace()) ||
                        structureWhitelist.additionalStructures.contains(structureId.toString());
        }
}