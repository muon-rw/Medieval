package dev.muon.medieval;

import dev.muon.medieval.compat.travelersbackpack.TravelersBackpackCompat;
import dev.muon.medieval.config.MedievalConfig;
import dev.muon.medieval.leveling.EnhancedEntityLevelingSettingsReloader;
import dev.muon.medieval.network.SyncPlayerLevelPacket;
import dev.muon.medieval.network.TownPortalScrollPacket;
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
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.muon.medieval.item.ItemRegistry;


@Mod(Medieval.MODID)
public class Medieval {
    public static final String MODID = "medieval";
    public static final Logger LOGGER = LogManager.getLogger();

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

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
        event.enqueueWork(() -> {
            int id = 0;
            NETWORK.registerMessage(id++, SyncPlayerLevelPacket.class,
                    SyncPlayerLevelPacket::encode,
                    SyncPlayerLevelPacket::decode,
                    SyncPlayerLevelPacket::handle);
            NETWORK.registerMessage(id++, TownPortalScrollPacket.class,
                    TownPortalScrollPacket::encode,
                    TownPortalScrollPacket::decode,
                    TownPortalScrollPacket::handle);
        });
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