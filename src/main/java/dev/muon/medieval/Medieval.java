package dev.muon.medieval;

import com.tiviacz.travelersbackpack.fluids.EffectFluidRegistry;
import dev.muon.medieval.compat.travelersbackpack.TravelersBackpackCompat;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.compat.travelersbackpack.PurifiedWaterEffect;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettingsReloader;
import dev.muon.medieval.leveling.LevelSyncHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.muon.medieval.item.ItemRegistry;


@Mod(Medieval.MODID)
public class Medieval {
    public static final String MODID = "medieval";
    public static final Logger LOGGER = LogManager.getLogger();

    public Medieval() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        Medieval.LOGGER.info("Medieval MC Forge Tweaks loading");

        MedievalConfig.register();
        ItemRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(Medieval.class);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LevelSyncHandler.init(event);
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        if (ModList.get().isLoaded("autoleveling")) {
            event.addListener(new EnhancedEntityLevelingSettingsReloader());
        }
    }


    // TODO: Remove if Nyf's Compat updates to be compatible with latest dependencies
    @SubscribeEvent
    public static void onInterMod(InterModEnqueueEvent event) {
        event.enqueueWork(() -> {
            TravelersBackpackCompat.init();
        });
    }

    public static ResourceLocation loc(String id) {
        return new ResourceLocation(Medieval.MODID, id);
    }
}