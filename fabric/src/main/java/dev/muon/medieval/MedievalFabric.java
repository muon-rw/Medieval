package dev.muon.medieval;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import dev.muon.medieval.compat.OverflowingBarsCompat;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.item.ItemRegistry;
import dev.muon.medieval.item.ItemRegistryFabric;
import dev.muon.medieval.platform.MedievalPlatformHelperFabric;
import dev.muon.medieval.platform.Services;
import dev.muon.medieval.quest.TaskTypes;
import fuzs.puzzleslib.api.client.event.v1.gui.RenderGuiLayerEvents;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeModConfigEvents;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.fml.config.ModConfig;
import net.minecraft.tags.TagKey;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

public class MedievalFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Medieval.LOG.info("Hello Fabric world!");
        Medieval.init();
        Medieval.setHelper(new MedievalPlatformHelperFabric());
        Services.setup(new MedievalPlatformHelperFabric());

        TaskTypes.init();
        ItemRegistryFabric.init();
        registerCreativeTabs();

        NeoForgeConfigRegistry.INSTANCE.register(Medieval.MOD_ID, ModConfig.Type.COMMON, MedievalConfig.COMMON_SPEC);
        NeoForgeConfigRegistry.INSTANCE.register(Medieval.MOD_ID, ModConfig.Type.CLIENT, MedievalConfig.CLIENT_SPEC);

        initializeCompat();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> registerCommand(dispatcher));

        // Register config load/reload events if needed late
        // NeoForgeModConfigEvents.loading(Medieval.MOD_ID).register(MedievalConfig::onLoad);
        // NeoForgeModConfigEvents.reloading(Medieval.MOD_ID).register(MedievalConfig::onReload);
    }

    public void initializeCompat() {
        if (FabricLoader.getInstance().isModLoaded("overflowingbars")) {
            RenderGuiLayerEvents.before(RenderGuiLayerEvents.PLAYER_HEALTH)
                    .register(OverflowingBarsCompat::onRenderPlayerHealth);
        }
    }

    private void registerCreativeTabs() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, Medieval.loc("medieval_tab"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(ItemRegistry.CHALLENGE_ORB))
                        .title(Component.translatable("itemGroup.medieval"))
                        .displayItems((parameters, output) -> {
                            output.accept(ItemRegistry.CHALLENGE_ORB);
                            output.accept(ItemRegistry.TOWN_PORTAL_SCROLL);
                        })
                        .build());
    }

    private static final List<String> TARGET_NAMESPACES = Arrays.asList(
            "dungeons_arise",
            "aether",
            "adventuredungeons",
            "dungeons_arise_seven_seas",
            "mes",
            "nova_structures",
            "the_bumblezone",
            "ati_structures"
    );

    private static final List<TagKey<Structure>> DIFFICULTY_TAGS = Arrays.asList(
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_1")),
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_2")),
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_3")),
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_4")),
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_5")),
            TagKey.create(Registries.STRUCTURE, ResourceLocation.fromNamespaceAndPath("dungeon_difficulty", "level_6"))
    );

    private static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dumpstructures")
                .requires(source -> source.hasPermission(2))
                .executes(MedievalFabric::runDumpCommand));
    }

    private static int runDumpCommand(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();

        source.sendSystemMessage(Component.literal("Starting structure ID dump... Check server logs."));
        Medieval.LOG.info("Structure dump initiated by command from: " + source.getTextName());
        try {
            Registry<Structure> structureRegistry = server.registryAccess().registryOrThrow(Registries.STRUCTURE);
            
            Set<ResourceLocation> allStructureIdsInTargetNamespaces = structureRegistry.keySet().stream()
                    .filter(id -> TARGET_NAMESPACES.contains(id.getNamespace()))
                    .collect(Collectors.toSet());

            Set<ResourceLocation> categorizedStructureIds = new HashSet<>();

            Medieval.LOG.info("--- Dumping Structure IDs by Difficulty Tag ---");

            for (TagKey<Structure> tagKey : DIFFICULTY_TAGS) {
                Medieval.LOG.info("--- Structures in Tag: " + tagKey.location() + " ---");
                List<ResourceLocation> structuresInThisTag = new ArrayList<>();

                structureRegistry.getTagOrEmpty(tagKey).forEach(holder -> {
                    ResourceLocation id = holder.unwrapKey().get().location(); 
                    if (allStructureIdsInTargetNamespaces.contains(id)) {
                        structuresInThisTag.add(id);
                    }
                });

                structuresInThisTag.stream()
                        .sorted(ResourceLocation::compareTo)
                        .forEach(id -> {
                            Medieval.LOG.info(id.toString());
                            categorizedStructureIds.add(id);
                        });
                if (structuresInThisTag.isEmpty()) {
                    Medieval.LOG.info("No structures found in this tag and target namespaces.");
                }
            }

            Medieval.LOG.info("--- Uncategorized Structures (from target namespaces) ---");
            List<ResourceLocation> uncategorizedStructures = allStructureIdsInTargetNamespaces.stream()
                    .filter(id -> !categorizedStructureIds.contains(id))
                    .sorted(ResourceLocation::compareTo)
                    .collect(Collectors.toList());

            if (uncategorizedStructures.isEmpty()) {
                Medieval.LOG.info("No uncategorized structures found in target namespaces.");
            } else {
                uncategorizedStructures.forEach(id -> Medieval.LOG.info(id.toString()));
            }
            
            Medieval.LOG.info("--- Structure ID Dump Complete ---");
            source.sendSystemMessage(Component.literal("Structure dump complete."));

        } catch (Exception e) {
            Medieval.LOG.error("Error occurred during structure dump command:", e);
            source.sendFailure(Component.literal("Error during structure dump. See logs."));
        }
        return 1; // Command success
    }
}