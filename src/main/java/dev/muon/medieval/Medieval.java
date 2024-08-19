package dev.muon.medieval;

import dev.muon.medieval.config.MedievalConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import dev.muon.medieval.item.ItemRegistry;


@Mod(Medieval.MODID)
public class Medieval
{
    public static final String MODID = "medieval";
    private static final Logger LOGGER = LogManager.getLogger();
    public Medieval()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        Medieval.LOGGER.info("Medieval MC Forge Tweaks loading");

        MedievalConfig.register();
        ItemRegistry.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
    private void commonSetup(final FMLCommonSetupEvent event)
    {}

    public static ResourceLocation loc(String id) {
        return new ResourceLocation(Medieval.MODID, id);
    }
}