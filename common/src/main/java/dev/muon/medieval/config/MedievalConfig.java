package dev.muon.medieval.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

// Config definition using FCAP in the common module.
public class MedievalConfig {

    // Common Config (for server/gameplay settings)
    public static final ModConfigSpec COMMON_SPEC;
    public static final Common COMMON;

    // Client Config (for client-side/rendering settings)
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Client CLIENT;

    static {
        // Common Builder
        ModConfigSpec.Builder commonBuilder = new ModConfigSpec.Builder();
        COMMON = new Common(commonBuilder);
        COMMON_SPEC = commonBuilder.build();

        // Client Builder
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();
    }

    // Registration logic is platform-specific

    public static class Common {
        public final ModConfigSpec.ConfigValue<List<? extends String>> structureNamespaces;
        public final ModConfigSpec.ConfigValue<List<? extends String>> additionalStructures;
        public final ModConfigSpec.ConfigValue<List<? extends String>> excludedStructurePaths;

        Common(ModConfigSpec.Builder builder) {
            builder.push("Structure Regeneration");

            structureNamespaces = builder
                    .comment("List of namespaces to allow regeneration for. Structures from these namespaces can be regenerated.")
                    .defineListAllowEmpty("structure_namespaces",
                            Arrays.asList(
                                    "dungeons_arise",
                                    "dungeons_arise_seven_seas",
                                    "cataclysm",
                                    "bosses_of_mass_destruction",
                                    "formidulus",
                                    "irons_spellbooks",
                                    "illager_invasion",
                                    "adventuredungeons",
                                    "aether",
                                    "betterdungeons",
                                    "betterdeserttemples",
                                    "betterjungletemples",
                                    "betterfortresses",
                                    "betteroceanmonuments",
                                    "nova_structures"
                            ),
                            () -> "",
                            obj -> obj instanceof String);

            excludedStructurePaths = builder
                    .comment("List of structure path sections to EXCLUDE regeneration for. If a structure ID's path (the part after the colon) contains any string in this list, it will be excluded, even if its namespace is allowed.",
                             "Useful for excluding specific types like 'village' across allowed namespaces.")
                    .defineListAllowEmpty("excluded_structure_patterns",
                            Arrays.asList(
                                    "village",
                                    "tavern",
                                    "inn",
                                    "well"
                            ),
                            () -> "",
                            obj -> obj instanceof String && !((String)obj).contains(":"));

            additionalStructures = builder
                    .comment("List of specific structures (namespace:path) to allow regeneration for, IN ADDITION to the namespaces above. Bypasses the exclude patterns above.")
                    .defineListAllowEmpty("additional_structures",
                            Arrays.asList(
                                    "minecraft:ancient_city",
                                    "minecraft:end_city",
                                    "eternal_starlight:golem_forge",
                                    "eternal_starlight:cursed_garden",
                                    "deep_aether:brass_dungeon"
                            ),
                            () -> "",
                            obj -> obj instanceof String && ((String) obj).contains(":"));

            builder.pop();
        }
    }

    public static class Client {
        // Custom HUD
        public final ModConfigSpec.BooleanValue enableCustomResourceBars;

        Client(ModConfigSpec.Builder builder) {
            builder.push("Custom HUD");
            enableCustomResourceBars = builder
                    .comment("Enable custom rendering for Health, Stamina (Hunger), and Mana bars.",
                             "Set to false to use vanilla/other mods' rendering.")
                    .define("enable_custom_resource_bars", true);
            builder.pop();
        }
    }
} 