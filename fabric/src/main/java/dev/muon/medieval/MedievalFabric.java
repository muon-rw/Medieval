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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
            "the_bumblezone"
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
            Set<ResourceLocation> structureIds = structureRegistry.keySet();

            Medieval.LOG.info("--- Dumping Structure IDs ---");
            structureIds.stream()
                    .filter(id -> TARGET_NAMESPACES.contains(id.getNamespace()))
                    .sorted(ResourceLocation::compareTo)
                    .forEach(id -> Medieval.LOG.info(id.toString()));
            Medieval.LOG.info("--- Structure ID Dump Complete ---");
            source.sendSystemMessage(Component.literal("Structure dump complete."));

        } catch (Exception e) {
            Medieval.LOG.error("Error occurred during structure dump command:", e);
            source.sendFailure(Component.literal("Error during structure dump. See logs."));
        }
        return 1; // Command success
    }
}