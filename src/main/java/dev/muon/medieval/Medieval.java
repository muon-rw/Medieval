package dev.muon.medieval;

import dev.muon.medieval.compat.travelersbackpack.TravelersBackpackCompat;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.config.ConfigConstants;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettingsReloader;
import dev.muon.medieval.leveling.LevelSyncHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.LoadingModList;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.muon.medieval.item.ItemRegistry;


@Mod(Medieval.MODID)
public class Medieval {
    public static final String MODID = "medieval";
    public static final Logger LOGGER = LogManager.getLogger();

    public Medieval(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::interMod);

        Medieval.LOGGER.info("Medieval MC Forge Tweaks loading");

        MedievalConfig.register();
        ItemRegistry.register(modEventBus);

        //TravelersBackpackCompat.init();
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Medieval.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LevelSyncHandler.init(event);
    }

    private void interMod(final InterModEnqueueEvent event) {
        event.enqueueWork(() -> {
            TravelersBackpackCompat.init();
        });
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        if (ModList.get().isLoaded("autoleveling")) {
            event.addListener(new EnhancedEntityLevelingSettingsReloader());
        }
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(Medieval.MODID, path);
    }
}