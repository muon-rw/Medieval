package dev.muon.medieval.config;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class MedievalConfig {
/*
        public static final ModConfigSpec SPEC;
        public static final MedievalConfig INSTANCE;

        static {
                final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
                INSTANCE = new MedievalConfig(builder);
                SPEC = builder.build();
        }

        public final ModConfigSpec.DoubleValue levelingSearchRadius;
        public final ModConfigSpec.DoubleValue levelsPerPoint;
        public final ModConfigSpec.BooleanValue cancelLevelsForPassives;
        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedStructureNamespaces;
        public final ModConfigSpec.ConfigValue<List<? extends String>> additionalValidStructures;
        public final ModConfigSpec.BooleanValue applyPlayerBasedLeveling;
        public final ModConfigSpec.IntValue structureSearchRadius;
        public final ModConfigSpec.BooleanValue enableStructureLevelBonus;
        public final ModConfigSpec.ConfigValue<Map<String, Integer>> structureLevelBonuses;

        public MedievalConfig(ModConfigSpec.Builder builder) {
                builder.comment("Medieval Mod Configuration");

                levelingSearchRadius = builder
                        .comment("The radius to search for structures when leveling (min: 1, max: 256)")
                        .worldRestart()
                        .defineInRange("levelingSearchRadius", 128.0, 1.0, 256.0);

                levelsPerPoint = builder
                        .comment("The number of levels per point (min: 0, max: 10)")
                        .defineInRange("levelsPerPoint", 0.2, 0.0, 10.0);

                cancelLevelsForPassives = builder
                        .comment("Whether to cancel levels for passives")
                        .define("cancelLevelsForPassives", true);

                allowedStructureNamespaces = builder
                        .comment("List of allowed structure namespaces")
                        .defineList("allowedStructureNamespaces", ArrayList::new, s -> s instanceof String);

                additionalValidStructures = builder
                        .comment("List of additional valid structures")
                        .defineList("additionalValidStructures", ArrayList::new, s -> s instanceof String);

                applyPlayerBasedLeveling = builder
                        .comment("Whether to apply player-based leveling")
                        .define("applyPlayerBasedLeveling", false);

                structureSearchRadius = builder
                        .comment("The radius to search for structures (min: 1, max: 32)")
                        .defineInRange("structureSearchRadius", 6, 1, 32);

                enableStructureLevelBonus = builder
                        .comment("Whether to enable structure level bonus")
                        .define("enableStructureLevelBonus", true);

                structureLevelBonuses = builder
                        .comment("Map of structure level bonuses")
                        .define("structureLevelBonuses", new HashMap<>());
        }

        public int getStructureLevelBonus(ResourceLocation structureId) {
                return structureLevelBonuses.get().getOrDefault(structureId.toString(), 0);
        }

 */
}